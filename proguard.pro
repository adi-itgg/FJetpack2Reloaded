-ignorewarnings

-dump build/class_files.txt
-printseeds build/seeds.txt
-printusage build/unused.txt
-printmapping build/mapping.txt

-keepattributes SourceFile,LineNumberTable

# We only want minification, not obfuscation.
#-dontobfuscate
-verbose
# Avaje-inject generated code and reflective config access need annotations, generic signatures and
# inner-class metadata at runtime.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,MethodParameters

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
-assumenosideeffects class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log {
    public *** log(...);
    public *** debug(...);
}
# keep listeners & hooks
-keep,allowobfuscation class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener.** { *; }
-keep,allowobfuscation class me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.** { *; }

# Avaje Inject: ServiceLoader looks the generated module up by name from
# META-INF/services/io.avaje.inject.spi.InjectExtension. That name is a string the shrinker cannot
# see, so the provider must be kept outright - -keepnames only stopped renaming and let the class be
# removed as unused.
-adaptresourcefilecontents META-INF/services/**
-adaptclassstrings
-keep class * implements io.avaje.inject.spi.InjectExtension { *; }
-keep class io.avaje.inject.** { *; }
