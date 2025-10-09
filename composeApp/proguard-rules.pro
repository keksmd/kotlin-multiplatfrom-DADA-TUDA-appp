# ProGuard rules for DADATUDA

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keepattributes Signature

# Keep annotation attributes
-keepattributes *Annotation*, InnerClasses

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class ru.dada.tuda.**$$serializer { *; }
-keepclassmembers class ru.dada.tuda.** {
    *** Companion;
}
-keepclasseswithmembers class ru.dada.tuda.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable annotated classes
-keep @kotlinx.serialization.Serializable class ru.dada.tuda.** { *; }

# Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# Ktor Client
-keep class io.ktor.** { *; }
-keep class io.ktor.client.** { *; }
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.utils.io.** { *; }
-dontwarn io.ktor.**

-keepclassmembers class io.ktor.** { 
    volatile <fields>; 
}

# Ktor Serialization
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.serialization.kotlinx.** { *; }

# OkHttp (если используется CIO или OkHttp engine)
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.** {
    *;
}

# Compose Resources
-keep class org.jetbrains.compose.resources.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Navigation
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# Koin DI
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}

# Coil Image Loading
-keep class coil3.** { *; }
-keep interface coil3.** { *; }
-dontwarn coil3.**

# Kotlinx DateTime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# Security Crypto (для Android)
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Application classes - Keep all model classes
-keep class ru.dada.tuda.domain.models.** { *; }
-keep class ru.dada.tuda.domain.http.models.** { *; }
-keep class ru.dada.tuda.data.dto.** { *; }

# Google ErrorProne и JSR-305 аннотации (используются только для статического анализа)
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**

# Google Crypto Tink (если используется)
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Platform specific implementations
-keep class ru.dada.tuda.Platform { *; }
-keep class ru.dada.tuda.Platform.* { *; }

# Keep utility classes that might be accessed via reflection
-keep class ru.dada.tuda.domain.util.** { *; }

# R8 Optimization flags
-optimizationpasses 5
-allowaccessmodification

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-assumenosideeffects class kotlin.io.ConsoleKt {
    public static *** println(...);
    public static *** print(...);
}

# Remove KmpLog debug/verbose in release (если такие методы есть)
-assumenosideeffects class ru.dada.tuda.domain.util.KmpLog {
    public static *** d(...);
    public static *** v(...);
}

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Для отладки ProGuard (удалить после тестирования)
# -printconfiguration ./build/outputs/mapping/configuration.txt
# -printmapping ./build/outputs/mapping/mapping.txt
