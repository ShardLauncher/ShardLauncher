#include <jni.h>
#include <sys/types.h>
#include <stdbool.h>
#include <unistd.h>
#include <pthread.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <android/api-level.h>
#include <dlfcn.h>
#include <environ/environ.h>

#include "stdio_is.h"

// fdsan error levels
#define ANDROID_FDSAN_ERROR_LEVEL_DISABLED 0
#define ANDROID_FDSAN_ERROR_LEVEL_WARN_ONCE 1
#define ANDROID_FDSAN_ERROR_LEVEL_WARN_ALWAYS 2
#define ANDROID_FDSAN_ERROR_LEVEL_FATAL 3

// Typedef for fdsan function pointer
typedef void (*android_fdsan_set_error_level_func)(int new_level);

// Helper function to disable fdsan at runtime
static void disable_fdsan(void) {
    // Only attempt on Android 10 (API 29) or higher
    if (android_get_device_api_level() >= 29) {
        // Use dlsym to find the function (it's in libc.so)
        void *libc_handle = dlopen("libc.so", RTLD_NOW);
        if (libc_handle) {
            android_fdsan_set_error_level_func set_error_level = 
                (android_fdsan_set_error_level_func)dlsym(libc_handle, "android_fdsan_set_error_level");
            if (set_error_level) {
                set_error_level(ANDROID_FDSAN_ERROR_LEVEL_WARN_ONCE);
            }
            dlclose(libc_handle);
        }
    }
}

//
// Created by maks on 17.02.21.
//

static volatile jobject exitTrap_ctx;
static volatile jclass exitTrap_exitClass;
static volatile jmethodID exitTrap_exitMethod;
static JavaVM *exitTrap_jvm;

static int pfd[2];
static pthread_t logger;
static jmethodID logger_onEventLogged;
static volatile jobject logListener = NULL;
static int latestlog_fd = -1;

static bool recordBuffer(char* buf, ssize_t len) {
    if (strstr(buf, "Session ID is")) return false;
    if (latestlog_fd != -1)
    {
        write(latestlog_fd, buf, len);
        fdatasync(latestlog_fd);
    }
    return true;
}

static void *logger_thread() {
    JNIEnv *env;
    jstring writeString;

    JavaVM* dvm = pojav_environ->dalvikJavaVMPtr;
    (*dvm)->AttachCurrentThread(dvm, &env, NULL);

    ssize_t  rsize;
    char buf[2050];

    while ((rsize = read(pfd[0], buf, sizeof(buf)-1)) > 0)
    {
        bool shouldRecordString = recordBuffer(buf, rsize); //record with newline int latestlog
        if (buf[rsize-1]=='\n')
        {
            rsize=rsize-1; //truncate
        }
        buf[rsize]=0x00;
        if (shouldRecordString && logListener != NULL)
        {
            writeString = (*env)->NewStringUTF(env, buf); //send to app without newline
            (*env)->CallVoidMethod(env, logListener, logger_onEventLogged, writeString);
            (*env)->DeleteLocalRef(env, writeString);
        }
    }
    (*dvm)->DetachCurrentThread(dvm);
    return NULL;
}

JNIEXPORT void JNICALL
Java_com_lanrhyme_shardlauncher_bridge_LoggerBridge_start(JNIEnv *env, __attribute((unused)) jclass clazz, jstring logPath) {
    // Disable fdsan on Android 10+ to prevent crashes from JVM/legacy code
    // that may incorrectly close file descriptors owned by FILE* streams
    disable_fdsan();

    if (latestlog_fd != -1)
    {
        int localfd = latestlog_fd;
        latestlog_fd = -1;
        close(localfd);
    }

    if (logger_onEventLogged == NULL)
    {
        jclass eventLogListener = (*env)->FindClass(env, "com/lanrhyme/shardlauncher/bridge/LoggerBridge$EventLogListener");
        logger_onEventLogged = (*env)->GetMethodID(env, eventLogListener, "onEventLogged", "(Ljava/lang/String;)V");
    }

    jclass ioeClass = (*env)->FindClass(env, "java/io/IOException");


    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    
    // Fix fdsan ownership issue on Android 10+:
    // The FILE* streams (stdout/stderr) own their file descriptors on Android 10+.
    // When we use dup2() to replace them, the FILE* still thinks it owns the fd,
    // causing fdsan to abort when the FILE* is closed at exit.
    // Solution: Close the FILE* with fclose(), then recreate with fdopen().
    
    // Save the original fd numbers (should be 1 and 2)
    int stdout_fd = STDOUT_FILENO;
    int stderr_fd = STDERR_FILENO;
    
    // Flush before closing
    fflush(stdout);
    fflush(stderr);
    
    // Close the FILE* streams - this also closes the underlying fds
    // and releases fdsan ownership
    fclose(stdout);
    fclose(stderr);
    
    // Now open new fds on the same numbers using dup2
    dup2(pfd[1], stdout_fd);
    dup2(pfd[1], stderr_fd);
    
    // Recreate FILE* streams on the new fds
    stdout = fdopen(stdout_fd, "w");
    stderr = fdopen(stderr_fd, "w");
    
    // Set buffering modes
    setvbuf(stdout, 0, _IOLBF, 0); // make stdout line-buffered
    setvbuf(stderr, 0, _IONBF, 0); // make stderr unbuffered
    
    // Close the write end of the pipe that we duplicated (no longer needed)
    close(pfd[1]);

    /* open latestlog.txt for writing */
    const char* logFilePath = (*env)->GetStringUTFChars(env, logPath, NULL);
    latestlog_fd = open(logFilePath, O_WRONLY | O_CREAT | O_TRUNC, 0644);

    if (latestlog_fd == -1)
    {
        latestlog_fd = 0;
        (*env)->ThrowNew(env, ioeClass, strerror(errno));
        return;
    }
    (*env)->ReleaseStringUTFChars(env, logPath, logFilePath);

    /* spawn the logging thread */
    int result = pthread_create(&logger, 0, logger_thread, 0);

    if (result != 0)
    {
        close(latestlog_fd);
        (*env)->ThrowNew(env, ioeClass, strerror(result));
    }
    pthread_detach(logger);
}

_Noreturn void nominal_exit(int code, bool is_signal) {
    JNIEnv *env;
    jint errorCode = (*exitTrap_jvm)->GetEnv(exitTrap_jvm, (void**)&env, JNI_VERSION_1_6);

    if (errorCode == JNI_EDETACHED)
        errorCode = (*exitTrap_jvm)->AttachCurrentThread(exitTrap_jvm, &env, NULL);

    if (errorCode != JNI_OK)
        killpg(getpgrp(), SIGTERM);

    (*env)->CallStaticVoidMethod(env, exitTrap_exitClass, exitTrap_exitMethod, code, is_signal);

    // Delete the reference, not gonna need 'em later anyway
    (*env)->DeleteGlobalRef(env, exitTrap_ctx);
    (*env)->DeleteGlobalRef(env, exitTrap_exitClass);

    // A hat trick, if you will
    // Call the Android System.exit() to perform Android's shutdown hooks and do a
    // fully clean exit.
    // After doing this, either of these will happen:
    // 1. Runtime calls exit() for real and it will be handled by ByteHook's recurse handler
    // and redirected back to the OS
    // 2. Zygote sends SIGTERM (no handling necessary, the process perishes)
    // 3. A different thread calls exit() and the hook will go through the exit_tripped path
    jclass systemClass = (*env)->FindClass(env,"java/lang/System");
    jmethodID exitMethod = (*env)->GetStaticMethodID(env, systemClass, "exit", "(I)V");
    (*env)->CallStaticVoidMethod(env, systemClass, exitMethod, 0);
    // System.exit() should not ever return, but the compiler doesn't know about that
    // so put a while loop here
    while(1) {}
}

JNIEXPORT void JNICALL Java_com_lanrhyme_shardlauncher_bridge_LoggerBridge_append(JNIEnv *env, __attribute((unused)) jclass clazz, jstring text) {
    jsize appendStringLength = (*env)->GetStringUTFLength(env, text);
    char newChars[appendStringLength+2];
    (*env)->GetStringUTFRegion(env, text, 0, (*env)->GetStringLength(env, text), newChars);
    newChars[appendStringLength] = '\n';
    newChars[appendStringLength+1] = 0;
    if (recordBuffer(newChars, appendStringLength+1) && logListener != NULL)
        (*env)->CallVoidMethod(env, logListener, logger_onEventLogged, text);
}

JNIEXPORT void JNICALL
Java_com_lanrhyme_shardlauncher_bridge_LoggerBridge_setListener(JNIEnv *env, __attribute((unused)) jclass clazz, jobject log_listener) {
    jobject logListenerLocal = logListener;

    if (log_listener == NULL) logListener = NULL;
    else logListener = (*env)->NewGlobalRef(env, log_listener);

    if (logListenerLocal != NULL && logListenerLocal != logListener)
        (*env)->DeleteGlobalRef(env, logListenerLocal);
}

JNIEXPORT void JNICALL
Java_com_lanrhyme_shardlauncher_bridge_SLBridge_setupExitMethod(JNIEnv *env, jclass clazz,
                                                        jobject context) {
    exitTrap_ctx = (*env)->NewGlobalRef(env,context);
    (*env)->GetJavaVM(env,&exitTrap_jvm);
    exitTrap_exitClass = (*env)->NewGlobalRef(env,(*env)->FindClass(env,"com/lanrhyme/shardlauncher/bridge/SLNativeInvoker"));
    exitTrap_exitMethod = (*env)->GetStaticMethodID(env, exitTrap_exitClass, "jvmExit", "(IZ)V");
}

JNIEXPORT void JNICALL
Java_com_lanrhyme_shardlauncher_bridge_SLBridge_disableFdsan(JNIEnv *env, __attribute((unused)) jclass clazz) {
    disable_fdsan();
}