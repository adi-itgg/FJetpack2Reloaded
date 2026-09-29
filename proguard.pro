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
-keep,allowobfuscation @me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.SectionPath class *
# Keep config record field names: the loader maps YAML keys to fields by reflection.
-keepclassmembernames class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.** { private *; }
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

# Avaje Inject: the generated module is discovered at runtime through META-INF/services, so the
# provider file must follow renamed classes and the module class must keep its name.
-adaptresourcefilecontents META-INF/services/**
-keepnames class * implements io.avaje.inject.spi.InjectExtension
-keep class io.avaje.inject.** { *; }
