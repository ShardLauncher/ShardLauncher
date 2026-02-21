# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in Android SDK tools.
# For more details, see
#   https://developer.android.com/build/shrink-code

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep ZalithLauncher core classes
-keep class com.movtery.zalithlauncher.** { *; }
-keep class org.lwjgl.** { *; }

# Keep bridge classes
-keep class com.movtery.zalithlauncher.bridge.** { *; }

# Keep annotation classes
-keep class androidx.annotation.** { *; }
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
