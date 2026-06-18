# RepoFlow ProGuard Rules

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class com.repoflow.core.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Gson
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
