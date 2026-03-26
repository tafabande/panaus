# ProGuard rules to prevent lock verification issues
-dontwarn com.google.android.gms.internal.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# Preserve Phenotype classes
-keep class com.google.android.gms.phenotype.** { *; }

# For Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherLoader {}
-keepnames class kotlinx.coroutines.android.HandlerContext {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# Preserve Firestore Model Classes
-keep class com.ourspace.app.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
