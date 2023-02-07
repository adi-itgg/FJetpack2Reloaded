-ignorewarnings

-dump build/class_files.txt
-printseeds build/seeds.txt
-printusage build/unused.txt
#-printmapping build/mapping.txt

# We only want minification, not obfuscation.
-dontobfuscate
-verbose
# Keep annotations
#-keepattributes *Annotation*
#-keepattributes Signature

# Entry point to the app.
-keep class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack2Reloaded { *; }
#-keep class me.ryanhamshire.GriefPrevention.** { *; }
#-keep class com.bgsoftware.superiorskyblock.** { *; }
-keepclassmembers,allowobfuscation class * {
  @org.bukkit.event.EventHandler <methods>;
}
-keep,allowobfuscation @me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath class *
# Keep data
-keepclassmembernames class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.** { private *; }
# Keep enums
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Remove logs
-assumenosideeffects class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log {
    public static *** log(...);
    public *** debug(...);
}