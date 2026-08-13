# TDLib is reached through JNI. Keep its bridge classes intact in release builds.
-keep class org.drinkless.tdlib.** { *; }
-keep class kotlinx.serialization.** { *; }

# Do not preserve verbose logging metadata in release artifacts.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
