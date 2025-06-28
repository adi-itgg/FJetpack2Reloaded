package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.List;

@RequiredArgsConstructor
public class PermissionConfigProcessor implements ConfigProcessor<String> {

    private final PluginManager pluginManager;

    private final List<String> subPermissions = List.of(
            Permissions.PERMISSION_REFILL_FUEL_SUFFIX,
            Permissions.PERMISSION_BYPASS_FUEL_SUFFIX,
            Permissions.PERMISSION_BYPASS_FUEL_SPRINT_SUFFIX,
            Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG,
            Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE,
            Permissions.PERMISSION_BYPASS_GRIEF_PREVENTION_CLAIM,
            Permissions.PERMISSION_KEEP_ON_DEATH_SUFFIX,
            Permissions.PERMISSION_KEEP_ON_EMPTY_SUFFIX
    );

    @Override
    public boolean support(Class<?> clazz, String value) {
        return clazz.isAssignableFrom(Permission.class) && value != null;
    }

    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, String value) {
        val permString = value.replace("#id", section.getName()).toLowerCase();
        val permission = new Permission(permString);

        // register permission
        if (pluginManager.getPermission(permString) == null) {
            pluginManager.addPermission(permission);
        }

        // register sub permissions
        subPermissions.stream().map(subPerm -> permString + subPerm).forEach(subPerm -> {
            if (pluginManager.getPermission(subPerm) != null) {
                return;
            }
            pluginManager.addPermission(new Permission(subPerm));
        });

        return permission;
    }

}
