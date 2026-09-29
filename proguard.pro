-ignorewarnings

-dump build/class_files.txt
-printseeds build/seeds.txt
-printusage build/unused.txt
-printmapping build/mapping.txt

-keepattributes SourceFile,LineNumberTable

# We only want minification, not obfuscation.
#-dontobfuscate
-verbose
# Keep annotations
#-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
#-keepattributes Signature

# Entry point to the app.
-keep class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack2Reloaded { *; }
#-keep class me.ryanhamshire.GriefPrevention.** { *; }
#-keep class com.bgsoftware.superiorskyblock.** { *; }
-keepclassmembers,allowobfuscation class * {
  @org.bukkit.event.EventHandler <methods>;
}
-keep,allowobfuscation @me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.SectionPath class *
# Keep config DTO field names: the loader maps YAML keys to fields by reflection.
-keepclassmembernames class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.** { private *; }
# Keep enums: config values (NONE/DROP/REMOVE) are resolved by name.
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Remove logs
-assumenosideeffects class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log {
    public *** log(...);
    public *** debug(...);
}
# keep listeners & hooks
-keep,allowobfuscation class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener.** { *; }
-keep,allowobfuscation class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.** { *; }
