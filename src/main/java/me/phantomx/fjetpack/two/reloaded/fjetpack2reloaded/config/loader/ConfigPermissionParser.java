package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;

public class ConfigPermissionParser implements ConfigParser<String, Permission> {

    @Override
    public boolean support(Class<?> type) {
        return type.isAssignableFrom(Permission.class);
    }

    @Override
    public Permission parse(Class<?> type, String value, ConfigurationSection section) {
        var perms = value.replace("#id", section.getName().toLowerCase());
        return new Permission(perms);
    }

}
