/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.lanrhyme.shardlauncher.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File

/**
 * Native invoker for bridging between Java/LWJGL and Android
 * This class is called from native code via JNI
 */
@Keep
object SLNativeInvoker {
    private const val TAG = "SLNativeInvoker"
    
    /**
     * Global context reference - should be set from Application or Activity
     */
    @JvmStatic
    var context: Context? = null
    
    /**
     * Current launcher instance for exit handling
     */
    @JvmStatic
    var staticLauncher: LauncherCallback? = null

    /**
     * Initialize the invoker with context
     */
    @JvmStatic
    fun init(context: Context) {
        this.context = context.applicationContext
        Logger.i(TAG, "SLNativeInvoker initialized")
    }

    /**
     * Open a link or share a file
     * Called from native code when game wants to open a URL or share a file
     */
    @Keep
    @JvmStatic
    fun openLink(link: String) {
        Logger.i(TAG, "openLink: $link")
        
        val ctx = context ?: run {
            Logger.e(TAG, "Context is null, cannot open link")
            return
        }
        
        try {
            // Check if this is a file:// URI for sharing
            val prefix = "file:"
            if (link.startsWith(prefix)) {
                // Handle file sharing
                val filePath = when {
                    link.startsWith("file://") -> link.removePrefix("file://")
                    else -> link.removePrefix(prefix)
                }
                
                Logger.i(TAG, "Sharing file: $filePath")
                val file = File(filePath)
                
                if (file.exists()) {
                    val uri = Uri.fromFile(file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    val chooserIntent = Intent.createChooser(shareIntent, "Share file")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(chooserIntent)
                } else {
                    Logger.e(TAG, "File does not exist: $filePath")
                }
            } else {
                // Handle URL opening
                val uri = Uri.parse(link)
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open link: $link", e)
        }
    }

    /**
     * Query system clipboard content
     * Called from native code when game wants to read clipboard
     */
    @Keep
    @JvmStatic
    fun querySystemClipboard() {
        Logger.d(TAG, "querySystemClipboard")
        
        val ctx = context ?: run {
            Logger.e(TAG, "Context is null, cannot query clipboard")
            SLBridge.clipboardReceived(null, null)
            return
        }
        
        try {
            val clipboardManager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            
            if (clipboardManager?.hasPrimaryClip() == true) {
                val clipData = clipboardManager.primaryClip
                val clipItem = clipData?.getItemAt(0)
                val clipText = clipItem?.text?.toString()
                
                if (clipText != null) {
                    val mimeType = when (clipData?.description?.mimeTypeCount ?: 0) {
                        0 -> "text/plain"
                        else -> clipData?.description?.getMimeType(0) ?: "text/plain"
                    }
                    Logger.d(TAG, "Clipboard content received: ${clipText.take(20)}...")
                    SLBridge.clipboardReceived(clipText, mimeType)
                } else {
                    SLBridge.clipboardReceived(null, null)
                }
            } else {
                SLBridge.clipboardReceived(null, null)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to query clipboard", e)
            SLBridge.clipboardReceived(null, null)
        }
    }

    /**
     * Put data to system clipboard
     * Called from native code when game wants to write to clipboard
     */
    @Keep
    @JvmStatic
    fun putClipboardData(data: String, mimeType: String) {
        Logger.d(TAG, "putClipboardData: ${data.take(20)}... ($mimeType)")
        
        val ctx = context ?: run {
            Logger.e(TAG, "Context is null, cannot put clipboard data")
            return
        }
        
        try {
            val clipboardManager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            
            val clipData = when (mimeType) {
                "text/html" -> ClipData.newHtmlText("ShardLauncher", data, data)
                else -> ClipData.newPlainText("ShardLauncher", data)
            }
            
            clipboardManager?.setPrimaryClip(clipData)
            Logger.d(TAG, "Clipboard data set successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to put clipboard data", e)
        }
    }

    /**
     * Update FPS value from native code
     * Called from native code to report current FPS
     */
    @Keep
    @JvmStatic
    fun putFpsValue(fps: Int) {
        SLBridgeStates.currentFPS = fps
    }

    /**
     * Handle JVM exit
     * Called from native code when JVM terminates
     */
    @Keep
    @JvmStatic
    fun jvmExit(exitCode: Int, isSignal: Boolean) {
        Logger.i(TAG, "JVM Exit: code=$exitCode, isSignal=$isSignal")
        
        try {
            // Call cleanup method
            staticLauncher?.onCleanup()
            
            // Call exit callback
            staticLauncher?.onJvmExit(exitCode, isSignal)
            
            // Clear static reference
            staticLauncher = null
            
            // Kill any remaining processes
            killProcess()
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error during JVM exit handling", e)
        }
    }

    /**
     * Kill the game process
     */
    private fun killProcess() {
        try {
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to kill process", e)
        }
    }
}