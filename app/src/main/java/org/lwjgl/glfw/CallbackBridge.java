package org.lwjgl.glfw;

import static com.lanrhyme.shardlauncher.bridge.ZLBridgeStatesKt.CURSOR_DISABLED;
import static com.lanrhyme.shardlauncher.bridge.ZLBridgeStatesKt.CURSOR_ENABLED;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Choreographer;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.lanrhyme.shardlauncher.bridge.CursorShape;
import com.lanrhyme.shardlauncher.bridge.ZLBridgeStates;
import com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker;
import com.lanrhyme.shardlauncher.game.keycodes.LwjglGlfwKeycode;

import java.util.function.Consumer;

import dalvik.annotation.optimization.CriticalNative;

@Keep
public class CallbackBridge {
    private static Choreographer sChoreographer;
    private static boolean isGrabbing = false;
    private static final Consumer<Boolean> grabListener = isGrabbing -> ZLBridgeStates
            .setCursorMode(isGrabbing ? CURSOR_DISABLED : CURSOR_ENABLED);

    public static Choreographer getChoreographer() {
        if (sChoreographer == null) {
            // Only initialize Choreographer on the main thread
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                sChoreographer = Choreographer.getInstance();
            } else {
                // If not on main thread, return null and handle gracefully
                return null;
            }
        }
        return sChoreographer;
    }

    private static int cursorShape = 0x36001;
    private static final Consumer<CursorShape> cursorShapeListener = ZLBridgeStates::setCursorShape;

    public static final int CLIPBOARD_COPY = 2000;
    public static final int CLIPBOARD_PASTE = 2001;
    public static final int CLIPBOARD_OPEN = 2002;

    public static volatile int windowWidth, windowHeight;
    public static volatile int physicalWidth, physicalHeight;
    public static float mouseX, mouseY;
    public volatile static boolean holdingAlt, holdingCapslock, holdingCtrl,
            holdingNumlock, holdingShift;

    public static void putMouseEventWithCoords(int button, float x, float y) {
        sendCursorPos(x, y);
        putMouseEvent(button);
    }

    public static void putMouseEvent(int button) {
        putMouseEvent(button, true);
        Choreographer choreographer = getChoreographer();
        if (choreographer != null) {
            choreographer.postFrameCallbackDelayed(l -> putMouseEvent(button, false), 33);
        } else {
            // Fallback: use a simple delay mechanism
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                () -> putMouseEvent(button, false), 33);
        }
    }

    public static void putMouseEvent(int button, boolean isDown) {
        sendMouseKeycode(button, CallbackBridge.getCurrentMods(), isDown);
    }

    public static void sendCursorPos(float x, float y) {
        mouseX = x;
        mouseY = y;
        nativeSendCursorPos(mouseX, mouseY);
    }

    public static void sendCursorDelta(float x, float y) {
        sendCursorPos(mouseX + x, mouseY + y);
    }

    public static void sendKeycode(int keycode, char keychar, int scancode, int modifiers, boolean isDown) {
        // TODO CHECK: This may cause input issue, not receive input!
        if (keycode != 0)
            nativeSendKey(keycode, scancode, isDown ? 1 : 0, modifiers);
        if (isDown && keychar != '\u0000') {
            nativeSendCharMods(keychar, modifiers);
            nativeSendChar(keychar);
        }
    }

    public static void sendChar(char keychar, int modifiers) {
        nativeSendCharMods(keychar, modifiers);
        nativeSendChar(keychar);
    }

    public static void sendKeyPress(int keyCode, int modifiers, boolean status) {
        sendKeyPress(keyCode, 0, modifiers, status);
    }

    public static void sendKeyPress(int keyCode, int scancode, int modifiers, boolean status) {
        sendKeyPress(keyCode, '\u0000', scancode, modifiers, status);
    }

    public static void sendKeyPress(int keyCode, char keyChar, int scancode, int modifiers, boolean status) {
        CallbackBridge.sendKeycode(keyCode, keyChar, scancode, modifiers, status);
    }

    public static void sendKeyPress(int keyCode) {
        sendKeyPress(keyCode, CallbackBridge.getCurrentMods(), true);
        sendKeyPress(keyCode, CallbackBridge.getCurrentMods(), false);
    }

    public static void sendMouseButton(int button, boolean status) {
        CallbackBridge.sendMouseKeycode(button, CallbackBridge.getCurrentMods(), status);
    }

    public static void sendMouseKeycode(int button, int modifiers, boolean isDown) {
        // if (isGrabbing()) DEBUG_STRING.append("MouseGrabStrace: " +
        // android.util.Log.getStackTraceString(new Throwable()) + "\n");
        nativeSendMouseButton(button, isDown ? 1 : 0, modifiers);
    }

    public static void sendMouseKeycode(int keycode) {
        sendMouseKeycode(keycode, CallbackBridge.getCurrentMods(), true);
        sendMouseKeycode(keycode, CallbackBridge.getCurrentMods(), false);
    }

    public static void sendScroll(double xoffset, double yoffset) {
        nativeSendScroll(xoffset, yoffset);
    }

    public static void sendUpdateWindowSize(int w, int h) {
        nativeSendScreenSize(w, h);
    }

    public static boolean isGrabbing() {
        // Avoid going through the JNI each time.
        return isGrabbing;
    }

    public static Context globalContext = null;

    // Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    public static @Nullable String accessAndroidClipboard(int type, String copy) {
        if (globalContext == null)
            return null;
        ClipboardManager clipboard = (ClipboardManager) globalContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        switch (type) {
            case CLIPBOARD_COPY:
                ClipData clip = ClipData.newPlainText("ShardLauncher", copy);
                clipboard.setPrimaryClip(clip);
                return null;
            case CLIPBOARD_PASTE:
                if (clipboard.hasPrimaryClip()
                        && clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                } else {
                    return "";
                }
            case CLIPBOARD_OPEN:
                ZLNativeInvoker.openLink(copy);
            default:
                return null;
        }
    }

    public static int getCurrentMods() {
        int currMods = 0;
        if (holdingAlt) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_ALT;
        }
        if (holdingCapslock) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_CAPS_LOCK;
        }
        if (holdingCtrl) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_CONTROL;
        }
        if (holdingNumlock) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_NUM_LOCK;
        }
        if (holdingShift) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_SHIFT;
        }
        return currMods;
    }

    public static void setModifiers(int keyCode, boolean isDown) {
        switch (keyCode) {
            case LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT:
                CallbackBridge.holdingShift = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL:
                CallbackBridge.holdingCtrl = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT:
                CallbackBridge.holdingAlt = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK:
                CallbackBridge.holdingCapslock = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK:
                CallbackBridge.holdingNumlock = isDown;
        }
    }

    // Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    private static void onGrabStateChanged(final boolean grabbing) {
        isGrabbing = grabbing;
        getChoreographer().postFrameCallbackDelayed((time) -> {
            // If the grab re-changed, skip notify process
            if (isGrabbing != grabbing)
                return;

            System.out.println("Grab changed : " + grabbing);
            synchronized (grabListener) {
                grabListener.accept(isGrabbing);
            }

        }, 16);
    }

    // Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    private static void onCursorShapeChanged(final int shape) {
        cursorShape = shape;
        getChoreographer().postFrameCallbackDelayed((time) -> {
            if (cursorShape != shape)
                return;

            synchronized (cursorShapeListener) {
                CursorShape shape1;
                switch (cursorShape) {
                    // IBeam
                    case 0x36002: // GLFW_IBEAM_CURSOR
                        shape1 = CursorShape.IBeam;
                        break;
                    // Hand
                    case 0x36004: // GLFW_HAND_CURSOR
                        shape1 = CursorShape.Hand;
                        break;
                    // Cross Hair
                    case 0x36003: // GLFW_CROSSHAIR_CURSOR
                        shape1 = CursorShape.CrossHair;
                        break;
                    // Resize NS
                    case 0x36006: // GLFW_RESIZE_NS_CURSOR
                        shape1 = CursorShape.ResizeNS;
                        break;
                    // Resize EW
                    case 0x36005: // GLFW_RESIZE_EW_CURSOR
                        shape1 = CursorShape.ResizeEW;
                        break;
                    // Resize All
                    case 0x36009: // GLFW_RESIZE_ALL_CURSOR
                        shape1 = CursorShape.ResizeAll;
                        break;
                    // Not Allowed
                    case 0x3600A: // GLFW_NOT_ALLOWED_CURSOR
                        shape1 = CursorShape.NotAllowed;
                        break;
                    // Arrow
                    case 0x36001: // GLFW_ARROW_CURSOR
                    default:
                        shape1 = CursorShape.Arrow;
                }

                cursorShapeListener.accept(shape1);
            }
        }, 16);
    }

    @Keep
    @CriticalNative
    public static native void nativeSetUseInputStackQueue(boolean useInputStackQueue);

    @Keep
    @CriticalNative
    private static native boolean nativeSendChar(char codepoint);

    // GLFW: GLFWCharModsCallback deprecated, but is Minecraft still use?
    @Keep
    @CriticalNative
    private static native boolean nativeSendCharMods(char codepoint, int mods);

    @Keep
    @CriticalNative
    private static native void nativeSendKey(int key, int scancode, int action, int mods);

    // private static native void nativeSendCursorEnter(int entered);
    @Keep
    @CriticalNative
    private static native void nativeSendCursorPos(float x, float y);

    @Keep
    @CriticalNative
    private static native void nativeSendMouseButton(int button, int action, int mods);

    @Keep
    @CriticalNative
    private static native void nativeSendScroll(double xoffset, double yoffset);

    @Keep
    @CriticalNative
    private static native void nativeSendScreenSize(int width, int height);

    @Keep
    public static native void nativeSetWindowAttrib(int attrib, int value);

    static {
        System.loadLibrary("pojavexec");
    }
}
