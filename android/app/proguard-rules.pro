# ─────────────────────────────────────────────────────────────────────────────
# ProGuard / R8 Rules — Living Trust App
# ─────────────────────────────────────────────────────────────────────────────
# These rules prevent R8 (the code shrinker) from removing or renaming classes
# that are accessed via reflection, dynamic loading, or JNI — things the
# shrinker cannot detect statically.
#
# Only rules that are actually required are included here. Avoid blanket -keep
# rules like "-keep class **" as they defeat the purpose of code shrinking.
# ─────────────────────────────────────────────────────────────────────────────


# ── Stripe Android SDK ────────────────────────────────────────────────────────
# Stripe uses reflection internally to load payment method handlers and to
# serialize/deserialize card data. The SDK ships its own consumer-rules.pro
# (which Gradle picks up automatically via AGP), but we add these as a safety
# net in case the embedded rules are incomplete.
-keep class com.stripe.android.** { *; }
-keep class com.stripe.android.model.** { *; }
-keep class com.stripe.android.paymentsheet.** { *; }
-dontwarn com.stripe.android.**

# ── OkHttp / Retrofit ────────────────────────────────────────────────────────
# OkHttp uses reflection for platform-specific SSL and HTTP/2 support.
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.internal.platform.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

# ── Gson ─────────────────────────────────────────────────────────────────────
# Gson serializes/deserializes Kotlin data classes using reflection.
# Without this, field names may be renamed by R8 and Gson cannot find them.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all data/DTO classes used in network responses — R8 must not rename fields
# that map to JSON keys received from the server.
-keep class com.livingtrust.app.data.remote.dto.** { *; }
-keep class com.livingtrust.app.domain.model.** { *; }

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
# Hilt generates code at compile time, but its internal annotations and generated
# factories need to survive code shrinking.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ── Room ──────────────────────────────────────────────────────────────────────
# Room generates SQL queries and DAO implementations at compile time. The
# generated classes reference the annotated entity classes by name — keep them.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── Kotlin Serialization ─────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Compose ───────────────────────────────────────────────────────────────────
# Compose uses reflection-like mechanisms for @Composable tracking. The Compose
# Gradle plugin handles most of this, but we keep runtime classes as a safeguard.
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ── Security: Remove logging in release builds ────────────────────────────────
# Strip all android.util.Log calls in release to prevent leaking debug info
# (tokens, API responses) into device logs accessible by other apps.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ── General Android ───────────────────────────────────────────────────────────
-keepclassmembers class * extends android.os.Parcelable {
    static ** CREATOR;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep application entry point
-keep class com.livingtrust.app.LivingTrustApplication { *; }
-keep class com.livingtrust.app.MainActivity { *; }
