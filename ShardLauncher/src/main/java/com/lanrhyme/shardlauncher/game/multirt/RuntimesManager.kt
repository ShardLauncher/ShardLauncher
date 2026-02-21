/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.multirt

import android.system.Os
import com.lanrhyme.shardlauncher.components.jre.Jre
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.utils.file.child
import com.lanrhyme.shardlauncher.utils.file.ensureDirectory
import com.lanrhyme.shardlauncher.utils.file.ensureParentDirectory
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.math.findNearestPositive
import com.lanrhyme.shardlauncher.utils.string.compareVersions
import com.lanrhyme.shardlauncher.utils.string.extractUntilCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

object RuntimesManager {
    private val cache = ConcurrentHashMap<String, Runtime>()

    private val RUNTIME_FOLDER = PathManager.DIR_MULTIRT
    private const val JAVA_VERSION_STR: String = "JAVA_VERSION=\""
    private const val OS_ARCH_STR: String = "OS_ARCH=\""

    fun getRuntimes(forceLoad: Boolean = false): List<Runtime> {
        if (!RUNTIME_FOLDER.exists()) {
            Logger.w("RuntimesManager", "Runtime directory not found: ${RUNTIME_FOLDER.absolutePath}")
            return emptyList()
        }

        return RUNTIME_FOLDER.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { loadRuntime(it.name, forceLoad = forceLoad) }
            ?.sortedWith { o1, o2 ->
                val thisVer = o1.versionString ?: o1.name
                -thisVer.compareVersions(o2.versionString ?: o2.name)
            }
            ?: throw IllegalStateException("Failed to access runtime directory")
    }

    fun getExactJreName(majorVersion: Int): String? {
        return getRuntimes().firstOrNull { it.javaVersion == majorVersion }?.name
    }

    fun getNearestJreName(majorVersion: Int): String? {
        return findNearestPositive(majorVersion, getRuntimes()) { it.javaVersion }?.value?.name
    }

    fun forceReload(name: String): Runtime {
        cache.remove(name)
        return loadRuntime(name)
    }

    fun loadRuntime(name: String, forceLoad: Boolean = false): Runtime {
        return cache[name]?.takeIf { !forceLoad } ?: run {
            val runtimeDir = RUNTIME_FOLDER.child(name)
            val releaseFile = runtimeDir.child("release")

            if (!releaseFile.exists()) return Runtime(name).also { cache[name] = it }

            runCatching {
                val content = releaseFile.readText()
                val javaVersion = content.extractUntilCharacter(JAVA_VERSION_STR, '"')
                val osArch = content.extractUntilCharacter(OS_ARCH_STR, '"')

                if (javaVersion != null && osArch != null) {
                    val versionParts = javaVersion.split('.')
                    val majorVersion = if (versionParts.first() == "1") {
                        versionParts.getOrNull(1)?.toIntOrNull() ?: 0
                    } else {
                        versionParts.first().toIntOrNull() ?: 0
                    }

                    Runtime(
                        name = name,
                        versionString = javaVersion,
                        arch = osArch,
                        javaVersion = majorVersion,
                        isProvidedByLauncher = Jre.entries.any { it.jreName == name },
                        isJDK8 = isJDK8(runtimeDir.absolutePath)
                    )
                } else {
                    Runtime(name)
                }
            }.onFailure { e ->
                Logger.e("RuntimesManager", "Failed to load runtime $name", e)
            }.getOrElse {
                Runtime(name)
            }.also { cache[name] = it }
        }
    }

    @Throws(IOException::class)
    suspend fun installRuntime(
        inputStream: InputStream,
        name: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            uncompressTarXZ(inputStream, dest, updateProgress)
            unpack200(PathManager.DIR_NATIVE_LIB, dest.absolutePath)
            loadRuntime(name).also { runtime ->
                postPrepare(runtime)
            }
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    @Throws(IOException::class)
    suspend fun postPrepare(name: String) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists()) return@withContext
        val runtime = loadRuntime(name)
        postPrepare(runtime)
    }

    @Throws(IOException::class)
    suspend fun postPrepare(runtime: Runtime) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(runtime.name)
        if (!dest.exists()) return@withContext
        var libFolder = "lib"

        val arch = runtime.arch
        if (arch != null && dest.child(libFolder, arch).exists()) {
            libFolder += "/$arch"
        }

        val isJDK8 = isJDK8(dest.absolutePath)
        if (isJDK8) {
            libFolder = "/jre$libFolder"
        }

        val ftIn = dest.child(libFolder, "libfreetype.so.6")
        val ftOut = dest.child(libFolder, "libfreetype.so")
        if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) {
            if (!ftIn.renameTo(ftOut)) throw IOException("Failed to rename freetype")
        }

        val ft2In = dest.child(libFolder, "libfreetype.so")
        if (isJDK8 && ft2In.exists()) {
            ft2In.renameTo(ftOut)
        }

        val localXawtLib = File(PathManager.DIR_NATIVE_LIB, "libawt_xawt.so")
        val targetXawtLib = dest.child(libFolder, "libawt_xawt.so")
        if (targetXawtLib.exists()) targetXawtLib.delete()
        FileUtils.copyFile(localXawtLib, targetXawtLib)
    }

    @Throws(IOException::class)
    suspend fun installRuntimeBinPack(
        universalFileInputStream: InputStream,
        platformBinsInputStream: InputStream,
        name: String,
        binPackVersion: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            installRuntimeNoRemove(universalFileInputStream, dest, updateProgress)
            installRuntimeNoRemove(platformBinsInputStream, dest, updateProgress)

            unpack200(PathManager.DIR_NATIVE_LIB, dest.absolutePath)

            val versionFile = dest.child("version")
            versionFile.writeText(binPackVersion)

            forceReload(name)
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    fun loadInternalRuntimeVersion(name: String): String? {
        val versionFile = RUNTIME_FOLDER.child(name, "version")
        try {
            return if (versionFile.exists()) {
                versionFile.readText()
            } else {
                null
            }
        } catch (_: IOException) {
            return null
        }
    }

    @Throws(IOException::class)
    fun removeRuntime(name: String) {
        val dest: File = RUNTIME_FOLDER.child(name).takeIf { it.exists() } ?: return
        FileUtils.deleteDirectory(dest)
        cache.remove(name)
    }

    fun getRuntimeHome(name: String): File {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists() || forceReload(name).versionString == null) {
            throw RuntimeException("Selected runtime is broken!")
        }

        return dest
    }

    private suspend fun unpack200(
        nativeLibraryDir: String,
        runtimePath: String
    ) = withContext(Dispatchers.Default) {
            val basePath = File(runtimePath)
            val files: Collection<File> = FileUtils.listFiles(basePath, arrayOf("pack"), true)

            val workDir = File(nativeLibraryDir)
            val processBuilder = ProcessBuilder().directory(workDir)

            files.forEach { jarFile ->
                ensureActive()
                runCatching {
                    val destPath = jarFile.absolutePath.replace(".pack", "")
                    processBuilder.command(
                        "./libunpack200.so",
                        "-r",
                        jarFile.absolutePath,
                        destPath
                    ).start().apply {
                        waitFor()
                    }
                }.onFailure { e ->
                    if (e is IOException) {
                        Logger.e("RuntimesManager", "Failed to unpack the runtime!", e)
                    } else throw e
                }
            }
        }

    @Throws(IOException::class)
    private suspend fun installRuntimeNoRemove(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        uncompressTarXZ(inputStream, dest, updateProgress)
        inputStream.close()
    }

    @Throws(IOException::class)
    private suspend fun uncompressTarXZ(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        dest.ensureDirectory()
        val buffer = ByteArray(8192)

        TarArchiveInputStream(XZCompressorInputStream(inputStream)).use { tarIn ->
            generateSequence { tarIn.nextEntry }.forEach { tarEntry ->
                ensureActive()
                val tarEntryName = tarEntry.name
                updateProgress(0, arrayOf(tarEntryName))

                val destPath = File(dest, tarEntryName).ensureParentDirectory()

                when {
                    tarEntry.isSymbolicLink -> try {
                        Os.symlink(tarEntry.linkName, tarEntryName)
                    } catch (e: Throwable) {
                        Logger.e("RuntimesManager", "Exception occurred while creating symbolic link", e)
                    }

                    tarEntry.isDirectory -> destPath.ensureDirectory()
                    !destPath.exists() || destPath.length() != tarEntry.size ->
                        FileOutputStream(destPath).use { os ->
                            IOUtils.copyLarge(tarIn, os, buffer)
                        }
                }
            }
        }
    }

    fun isJDK8(runtimeDir: String): Boolean {
        return File(runtimeDir, "jre").exists() && File(runtimeDir, "bin/javac").exists()
    }
}
