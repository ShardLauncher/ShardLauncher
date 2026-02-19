#include <jni.h>
#include <assert.h>
#include <dlfcn.h>
#include <android/log.h>

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

#include <EGL/egl.h>
#include <GL/osmesa.h>
#include "ctxbridges/egl_loader.h"
#include "ctxbridges/osmesa_loader.h"
#include "ctxbridges/renderer_config.h"
#include "ctxbridges/virgl_bridge.h"
#include "driver_helper/nsbypass.h"

#ifdef GLES_TEST
#include <GLES2/gl2.h>
#endif

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/rect.h>
#include <string.h>
#include <environ/environ.h>
#include <android/dlext.h>
#include <time.h>
#include "utils.h"
#include "ctxbridges/bridge_tbl.h"
#include "ctxbridges/osm_bridge.h"

#define GLFW_CLIENT_API 0x22001
/* Consider GLFW_NO_API as Vulkan API */
#define GLFW_NO_API 0
#define GLFW_OPENGL_API 0x30001

// This means that the function is an external API and that it will be used
#define EXTERNAL_API __attribute__((used))
// This means that you are forced to have this function/variable for ABI compatibility
#define ABI_COMPAT __attribute__((unused))

EGLConfig config;
struct PotatoBridge potatoBridge;

void* loadTurnipVulkan();
void calculateFPS();

EXTERNAL_API void pojavTerminate() {
    printf("EGLBridge: Terminating\n");

    switch (pojav_environ->config_renderer) {
        case RENDERER_GL4ES: {
            eglMakeCurrent_p(potatoBridge.eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            eglDestroySurface_p(potatoBridge.eglDisplay, potatoBridge.eglSurface);
            eglDestroyContext_p(potatoBridge.eglDisplay, potatoBridge.eglContext);
            eglTerminate_p(potatoBridge.eglDisplay);
            eglReleaseThread_p();

            potatoBridge.eglContext = EGL_NO_CONTEXT;
            potatoBridge.eglDisplay = EGL_NO_DISPLAY;
            potatoBridge.eglSurface = EGL_NO_SURFACE;
        } break;

            //case RENDERER_VIRGL:
        case RENDERER_VK_ZINK: {
            // Nothing to do here
        } break;
    }
}

JNIEXPORT void JNICALL Java_com_lanrhyme_shardlauncher_bridge_SLBridge_setupBridgeWindow(JNIEnv* env, ABI_COMPAT jclass clazz, jobject surface) {
    __android_log_print(ANDROID_LOG_INFO, "EGLBridge", "setupBridgeWindow() called");

    if (!pojav_environ) {
        __android_log_print(ANDROID_LOG_ERROR, "EGLBridge", "ERROR: pojav_environ is NULL in setupBridgeWindow!");
        return;
    }

    pojav_environ->pojavWindow = ANativeWindow_fromSurface(env, surface);
    __android_log_print(ANDROID_LOG_INFO, "EGLBridge", "pojavWindow set to %p", (void*)pojav_environ->pojavWindow);

    // NOTE: pojavInitOpenGL() is NOT called here anymore because
    // POJAV_RENDERER environment variable is not set yet at this point.
    // It will be called from dlopenEngine() after environment variables are set.

    if (br_setup_window) br_setup_window();
}

JNIEXPORT void JNICALL
Java_com_lanrhyme_shardlauncher_bridge_SLBridge_releaseBridgeWindow(ABI_COMPAT JNIEnv *env, ABI_COMPAT jclass clazz) {
    ANativeWindow_release(pojav_environ->pojavWindow);
}

EXTERNAL_API void* pojavGetCurrentContext() {
    if (pojav_environ->config_renderer == RENDERER_VIRGL)
        return virglGetCurrentContext();

    return br_get_current();
}

static void set_vulkan_ptr(void* ptr) {
    char envval[64];
    sprintf(envval, "%"PRIxPTR, (uintptr_t)ptr);
    setenv("VULKAN_PTR", envval, 1);
}

void load_vulkan() {
    const char* zinkPreferSystemDriver = getenv("POJAV_ZINK_PREFER_SYSTEM_DRIVER");
    int deviceApiLevel = android_get_device_api_level();
    if (zinkPreferSystemDriver == NULL && deviceApiLevel >= 28) {
#ifdef ADRENO_POSSIBLE
        void* result = loadTurnipVulkan();
        if (result != NULL)
        {
            printf("AdrenoSupp: Loaded Turnip, loader address: %p\n", result);
            set_vulkan_ptr(result);
            return;
        }
#endif
    }

    printf("OSMDroid: Loading Vulkan regularly...\n");
    void* vulkanPtr = dlopen("libvulkan.so", RTLD_LAZY | RTLD_LOCAL);
    printf("OSMDroid: Loaded Vulkan, ptr=%p\n", vulkanPtr);
    set_vulkan_ptr(vulkanPtr);
}

int pojavInitOpenGL() {
    const char *renderer = getenv("POJAV_RENDERER");

    __android_log_print(ANDROID_LOG_INFO, "EGLBridge", "pojavInitOpenGL() called, POJAV_RENDERER=%s", renderer ? renderer : "(null)");

    if (!renderer) {
        __android_log_print(ANDROID_LOG_ERROR, "EGLBridge", "ERROR: POJAV_RENDERER environment variable is not set!");
        return -1;
    }

    if (!strncmp("opengles", renderer, 8))
    {
        __android_log_print(ANDROID_LOG_INFO, "EGLBridge", "Setting renderer to GL4ES");
        if (!pojav_environ) {
            __android_log_print(ANDROID_LOG_ERROR, "EGLBridge", "ERROR: pojav_environ is NULL!");
            return -1;
        }
        pojav_environ->config_renderer = RENDERER_GL4ES;
        set_gl_bridge_tbl();
    }

    if (!strcmp(renderer, "custom_gallium"))
    {
        pojav_environ->config_renderer = RENDERER_VK_ZINK;
        load_vulkan();
        set_osm_bridge_tbl();
    }

    if (!strcmp(renderer, "vulkan_zink"))
    {
        pojav_environ->config_renderer = RENDERER_VK_ZINK;
        load_vulkan();
        setenv("GALLIUM_DRIVER", "zink", 1);
        set_osm_bridge_tbl();
    }

    if (!strcmp(renderer, "gallium_freedreno"))
    {
        pojav_environ->config_renderer = RENDERER_VK_ZINK;
        setenv("MESA_LOADER_DRIVER_OVERRIDE", "kgsl", 1);
        setenv("GALLIUM_DRIVER", "freedreno", 1);
        set_osm_bridge_tbl();
    }

    if (!strcmp(renderer, "gallium_panfrost"))
    {
        pojav_environ->config_renderer = RENDERER_VK_ZINK;
        setenv("GALLIUM_DRIVER", "panfrost", 1);
        setenv("MESA_DISK_CACHE_SINGLE_FILE", "1", 1);
        set_osm_bridge_tbl();
    }

    if (!strcmp(renderer, "gallium_virgl"))
    {
        pojav_environ->config_renderer = RENDERER_VIRGL;
        setenv("GALLIUM_DRIVER", "virpipe", 1);
        setenv("OSMESA_NO_FLUSH_FRONTBUFFER", "1", false);
        setenv("MESA_GL_VERSION_OVERRIDE", "4.3", 1);
        setenv("MESA_GLSL_VERSION_OVERRIDE", "430", 1);
        if (!strcmp(getenv("OSMESA_NO_FLUSH_FRONTBUFFER"), "1"))
            printf("VirGL: OSMesa buffer flush is DISABLED!\n");
        loadSymbolsVirGL();
        virglInit();
        return 0;
    }

    if (!strcmp(renderer, "opengles3_ng_gl4es"))
    {
        pojav_environ->config_renderer = RENDERER_GL4ES;
        setenv("LIBGL_FB", "2", 1);
        setenv("LIBGL_NOMIPMAP", "1", 1);
        setenv("LIBGL_WORKAROUND_NULLTEX", "1", 1);
        setenv("LIBGL_NOERROR", "1", 1);
        setenv("LIBGL_RECYCLEFBO", "1", 1);
        setenv("LIBGL_VERTEX_CLAMP", "1", 1);
        setenv("LIBGL_ALWAYS_16_BITS", "1", 1);
        setenv("LIBGL_ALLOW_UNOFFICIAL_ES3", "1", 1);
        set_gl_bridge_tbl();
    }

    if (!strcmp(renderer, "opengles2_ng_gl4es"))
    {
        pojav_environ->config_renderer = RENDERER_GL4ES;
        setenv("LIBGL_FB", "2", 1);
        setenv("LIBGL_NOMIPMAP", "1", 1);
        setenv("LIBGL_WORKAROUND_NULLTEX", "1", 1);
        setenv("LIBGL_NOERROR", "1", 1);
        setenv("LIBGL_RECYCLEFBO", "1", 1);
        setenv("LIBGL_VERTEX_CLAMP", "1", 1);
        set_gl_bridge_tbl();
    }

    if (!strcmp(renderer, "opengles1_ng_gl4es"))
    {
        pojav_environ->config_renderer = RENDERER_GL4ES;
        setenv("LIBGL_FB", "2", 1);
        setenv("LIBGL_NOMIPMAP", "1", 1);
        setenv("LIBGL_WORKAROUND_NULLTEX", "1", 1);
        setenv("LIBGL_NOERROR", "1", 1);
        setenv("LIBGL_RECYCLEFBO", "1", 1);
        setenv("LIBGL_VERTEX_CLAMP", "1", 1);
        set_gl_bridge_tbl();
    }

    if (!strcmp(renderer, "osmesa_ng_gl4es"))
    {
        pojav_environ->config_renderer = RENDERER_GL4ES;
        setenv("LIBGL_FB", "2", 1);
        setenv("LIBGL_NOMIPMAP", "1", 1);
        setenv("LIBGL_WORKAROUND_NULLTEX", "1", 1);
        setenv("LIBGL_NOERROR", "1", 1);
        setenv("LIBGL_RECYCLEFBO", "1", 1);
        setenv("LIBGL_VERTEX_CLAMP", "1", 1);
        setenv("LIBGL_ALWAYS_16_BITS", "1", 1);
        setenv("LIBGL_ALLOW_UNOFFICIAL_ES3", "1", 1);
        setenv("MESA_GL_VERSION_OVERRIDE", "3.2", 1);
        setenv("MESA_GLSL_VERSION_OVERRIDE", "150", 1);
        set_gl_bridge_tbl();
    }

    // 在所有渲染器配置完成后，初始化 bridge（仅适用于 GL4ES 和 Zink） 
    if (pojav_environ->config_renderer == RENDERER_GL4ES || pojav_environ->config_renderer == RENDERER_VK_ZINK)
    {
        if (br_init()) br_setup_window();
    }

    return 0;
}

EXTERNAL_API void pojavSetWindowHint(int hint, int value) {
    if (hint != GLFW_CLIENT_API) return;
    switch (value) {
        case GLFW_NO_API:
            pojav_environ->config_renderer = RENDERER_VULKAN;
            /* Nothing to do: initialization is handled in Java-side */
            // pojavInitVulkan();
            break;
        case GLFW_OPENGL_API:
            /* Nothing to do: initialization is called in pojavCreateContext */
            // pojavInitOpenGL();
            break;
        default:
            printf("GLFW: Unimplemented API 0x%x\n", value);
            abort();
    }
}

EXTERNAL_API void pojavSwapBuffers() {
    calculateFPS();

    if (pojav_environ->config_renderer == RENDERER_VK_ZINK
     || pojav_environ->config_renderer == RENDERER_GL4ES)
    {
        br_swap_buffers();
    }

    if (pojav_environ->config_renderer == RENDERER_VIRGL)
    {
        virglSwapBuffers();
    }

}

EXTERNAL_API void pojavMakeCurrent(void* window) {
    if (pojav_environ->config_renderer == RENDERER_VK_ZINK
     || pojav_environ->config_renderer == RENDERER_GL4ES)
    {
        br_make_current((basic_render_window_t*)window);
    }

    if (pojav_environ->config_renderer == RENDERER_VIRGL)
    {
        virglMakeCurrent(window);
    }

}

int pojavCreateContext() {
    int result = 0;

    if (pojav_environ->config_renderer == RENDERER_VK_ZINK
     || pojav_environ->config_renderer == RENDERER_GL4ES)
    {
        printf("EGLBridge: Creating context...\n");
        if (br_init()) {
            printf("EGLBridge: Context created successfully\n");
            result = 1;
        } else {
            printf("EGLBridge: Failed to create context\n");
        }
    }

    if (pojav_environ->config_renderer == RENDERER_VIRGL)
    {
        printf("VirGL: Creating context...\n");
        void* ctx_result = virglCreateContext(NULL);
        printf("VirGL: Context created, pointer: %p\n", ctx_result);
        result = ctx_result != NULL; // Convert to boolean (int) result
    }

    return result;
}

void calculateFPS() {
    static uint64_t lastTime = 0;
    static int frameCount = 0;

    struct timespec currentTime;
    clock_gettime(CLOCK_MONOTONIC, &currentTime);
    uint64_t currentTimeMs = currentTime.tv_sec * 1000 + currentTime.tv_nsec / 1000000;

    if (lastTime == 0) {
        lastTime = currentTimeMs;
    }

    frameCount++;

    if (currentTimeMs - lastTime >= 1000) {
        int fps = (int) (frameCount * 1000.0 / (currentTimeMs - lastTime));
        
        // Call FPS update method
        JNIEnv *dalvikEnv;
        (*pojav_environ->dalvikJavaVMPtr)->AttachCurrentThread(pojav_environ->dalvikJavaVMPtr,&dalvikEnv,NULL);
        (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv,pojav_environ->class_ZLInvoker,pojav_environ->method_PutFpsValue,(jint) frameCount);
        (*pojav_environ->dalvikJavaVMPtr)->DetachCurrentThread(pojav_environ->dalvikJavaVMPtr);

        frameCount = 0;
        lastTime = currentTimeMs;
    }
}

// 添加 maybe_load_vulkan 函数定义
void* maybe_load_vulkan() {
    // We use the env var because
    // 1. it's easier to do that
    // 2. it won't break if something will try to load vulkan and osmesa simultaneously
    if(getenv("VULKAN_PTR") == NULL) load_vulkan();
    return (void*) strtoul(getenv("VULKAN_PTR"), NULL, 0x10);
}

EXTERNAL_API JNIEXPORT jlong JNICALL
Java_org_lwjgl_vulkan_VK_getVulkanDriverHandle(ABI_COMPAT JNIEnv *env, ABI_COMPAT jclass thiz) {
    printf("EGLBridge: LWJGL-side Vulkan loader requested the Vulkan handle\n");
    return (jlong) maybe_load_vulkan();
}

EXTERNAL_API int pojavInit() {
    ANativeWindow_acquire(pojav_environ->pojavWindow);
    pojav_environ->savedWidth = ANativeWindow_getWidth(pojav_environ->pojavWindow);
    pojav_environ->savedHeight = ANativeWindow_getHeight(pojav_environ->pojavWindow);
    ANativeWindow_setBuffersGeometry(pojav_environ->pojavWindow,pojav_environ->savedWidth,pojav_environ->savedHeight,AHARDWAREBUFFER_FORMAT_R8G8B8X8_UNORM);
    pojavInitOpenGL();
    return 1;
}

// Expose pojavInitOpenGL for Java to call before loading renderer libraries
EXTERNAL_API int pojavInitOpenGLExternal() {
    return pojavInitOpenGL();
}

EXTERNAL_API void pojavSwapInterval(int interval) {
    if (pojav_environ->config_renderer == RENDERER_VK_ZINK
     || pojav_environ->config_renderer == RENDERER_GL4ES)
    {
        br_swap_interval(interval);
    }

    if (pojav_environ->config_renderer == RENDERER_VIRGL)
    {
        virglSwapInterval(interval);
    }

}

// 新增：刷新桥接窗口设置，用于渲染器库加载后重新初始化窗口
JNIEXPORT void JNICALL
Java_com_lanrhyme_shardlauncher_bridge_SLBridge_refreshBridgeWindow(JNIEnv* env, ABI_COMPAT jclass clazz) {
    printf("EGLBridge: refreshBridgeWindow called, br_setup_window=%p\n", (void*)br_setup_window);
    
    // 如果 br_setup_window 还没有被初始化，先初始化桥接表
    // 这是必须的，因为 libopenal.so 可能在桥接表初始化之前加载
    if (!br_setup_window) {
        printf("EGLBridge: Bridge table not initialized, calling pojavInitOpenGL to set up bridge\n");
        pojavInitOpenGL();
        printf("EGLBridge: After pojavInitOpenGL, br_setup_window=%p\n", (void*)br_setup_window);
    }
    
    if (br_setup_window) {
        printf("EGLBridge: Refreshing bridge window settings\n");
        br_setup_window();
        
        // 在刷新窗口后，如果需要重新初始化 OpenGL，则执行
        // 确保在渲染器库加载后，OpenGL 环境被正确设置
        if (pojav_environ && pojav_environ->config_renderer != 0) {
            printf("EGLBridge: Renderer already configured (renderer=%d), attempting to init bridge\n", pojav_environ->config_renderer);
            if (pojav_environ->config_renderer == RENDERER_GL4ES || pojav_environ->config_renderer == RENDERER_VK_ZINK) {
                if (br_init()) {
                    printf("EGLBridge: Bridge re-initialized successfully after renderer load\n");
                } else {
                    printf("EGLBridge: Failed to re-initialize bridge after renderer load\n");
                }
            }
        }
    } else {
        printf("EGLBridge: Cannot refresh bridge window, br_setup_window is still not initialized\n");
    }
}