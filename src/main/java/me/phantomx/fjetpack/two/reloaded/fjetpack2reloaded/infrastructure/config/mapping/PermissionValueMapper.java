package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 * {@code Permission: 'fjetpack2reloaded.#id'} - registers the node (and its sub nodes) on the
 * server and stores it on the DTO. {@code #id} is replaced with the name of the current section.
 */
public final class PermissionValueMapper implements ConfigValueMapper {

    @Override
    public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
        if (!field.getType().isAssignableFrom(Permission.class)) return false;

        val permString = ((String) value).replace("#id", context.section().getName()).toLowerCase();
        val perm = new Permission(permString);
        field.set(instance, new Permission(permString));

        val pluginManager = Bukkit.getPluginManager();
        if (pluginManager.getPermission(permString) == null)
            pluginManager.addPermission(perm);

        // register sub perms
        val subPermissions = List.of(
                Permissions.PERMISSION_REFILL_FUEL_SUFFIX,
                Permissions.PERMISSION_BYPASS_FUEL_SUFFIX,
                Permissions.PERMISSION_BYPASS_FUEL_SPRINT_SUFFIX,
                Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG,
                Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE,
                Permissions.PERMISSION_BYPASS_GRIEF_PREVENTION_CLAIM,
                Permissions.PERMISSION_KEEP_ON_DEATH_SUFFIX,
                Permissions.PERMISSION_KEEP_ON_EMPTY_SUFFIX
        );
        subPermissions.stream().map(subPerm -> permString + subPerm).forEach(subPerm -> {
            if (pluginManager.getPermission(subPerm) != null) return;
            pluginManager.addPermission(new Permission(subPerm));
        });
        return true;
    }

}
