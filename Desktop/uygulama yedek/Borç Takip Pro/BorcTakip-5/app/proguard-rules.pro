# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase Firestore Serialization Rules - CRITICAL for deserialization
-keep class com.burhan2855.borctakip.data.** { *; }

# Keepclass members for Firebase Firestore deserialization
# Contact class - must have no-arg constructor
-keepclassmembers class com.burhan2855.borctakip.data.Contact {
    public <init>();
    public <init>(long, java.lang.String, java.lang.String);
    public <fields>;
    public <methods>;
}

# Transaction class - must have no-arg constructor
-keepclassmembers class com.burhan2855.borctakip.data.Transaction {
    public <init>();
    public <init>(long, java.lang.String, double, java.lang.String, long, long, boolean, java.lang.Long, java.lang.String, java.lang.String, java.lang.String);
    public <fields>;
    public <methods>;
}

# PartialPayment class - must have no-arg constructor
-keepclassmembers class com.burhan2855.borctakip.data.PartialPayment {
    public <init>();
    public <init>(long, long, double, long);
    public <fields>;
    public <methods>;
}

# Keep all Firebase Firestore classes
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** {
    public <init>();
    public <fields>;
    public <methods>;
}

# Keep Google Generative AI (Gemini) SDK classes
-keep class com.google.ai.client.generativeai.** { *; }
-keepclassmembers class com.google.ai.client.generativeai.** {
    public <init>();
    public <fields>;
    public <methods>;
}

# Keep data classes for Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * {
    public <init>();
    public <fields>;
    public <methods>;
}

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Google AI Client Library - Gemini SDK ProGuard rules
-keep class com.google.ai.client.** { *; }
-keep class com.google.ai.generativeai.** { *; }
-keepclassmembers class com.google.ai.client.** { *; }
-keepclassmembers class com.google.ai.generativeai.** { *; }

# Kotlin specific rules
-keepattributes *Annotation*
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

