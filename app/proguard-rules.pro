# SafeOrbit ProGuard & R8 Optimization Rules

# 1. Keep data models used with Firestore serialization
-keepclassmembers class com.ogken.safeorbit.model.** { *; }
-keep class com.ogken.safeorbit.model.** { *; }

# 2. MapLibre Native SDK
-keep class org.maplibre.android.** { *; }
-keepclassmembers class org.maplibre.android.** {
    native <methods>;
}
-dontwarn org.maplibre.android.**

# 3. OkHttp
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# 4. Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# 5. Firebase & Play Services Location
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
