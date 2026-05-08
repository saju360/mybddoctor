# Retrofit + OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# App models — keep all DTOs
-keep class com.lifeplus.healthcare.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Security Crypto
-keep class androidx.security.crypto.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel

# Shimmer / Compose
-keep class androidx.compose.** { *; }

# Coil
-keep class coil.** { *; }

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
