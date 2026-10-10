# ProGuard / R8 Rules for com.flatcode apps

# -------------------------------------------------------------------------
# Keep AndroidX Navigation & Fragment classes and constructors
# -------------------------------------------------------------------------
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>();
}
-keepnames class * extends androidx.fragment.app.Fragment

-keep public class * extends androidx.activity.ComponentActivity {
    public <init>();
}

# -------------------------------------------------------------------------
# Keep ViewBinding classes
# -------------------------------------------------------------------------
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}

# -------------------------------------------------------------------------
# Keep UI package classes & custom UI libraries
# -------------------------------------------------------------------------
-keep class com.flatcode.**.ui.** { *; }
-keep class io.selimdawa.bubblebottom.** { *; }
-keep class com.smarteist.autoimageslider.** { *; }

# -------------------------------------------------------------------------
# Keep Data Models & POJOs for Firebase Realtime Database, Firestore, and Room
# -------------------------------------------------------------------------
-keep class com.flatcode.**.model.** { *; }
-keepclassmembers class com.flatcode.**.model.** {
    <fields>;
    <methods>;
    public <init>();
}
-keep class com.flatcode.**.db.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep interface * extends androidx.room.Dao

# -------------------------------------------------------------------------
# Keep Hilt / Dagger generated classes & ViewModels
# -------------------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# -------------------------------------------------------------------------
# Preserve annotations & source attributes for stacktraces and reflection
# -------------------------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable
