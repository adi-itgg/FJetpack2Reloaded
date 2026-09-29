package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.SectionPath;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.ActionHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Reflection mapper from a YAML section to a config DTO.
 * <p>
 * Field names of the DTOs are the YAML keys (first letter may be upper or lower case).
 */
public class ConfigsLoader {

    @SneakyThrows
    private static <T> void iterateFields(@NotNull Class<T> clazz, @NotNull ActionHandler<Field> actionHandler) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            actionHandler.handle(declaredField);
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> @Nullable T appendConfigValue(@Nullable ConfigurationSection section,
                                                    @NotNull Class<T> clazz,
                                                    @NotNull Map<String, CustomFuel> customFuels,
                                                    @NotNull MessageSink messages) {
        if (section == null) return null;
        val instance = (T) clazz.getDeclaredConstructors()[0].newInstance();
        iterateFields(clazz, field -> {
            var value = section.get(field.getName());
            if (value == null) {
                var key = field.getName();
                key = key.substring(0, 1).toUpperCase() + key.substring(1);
                value = section.get(key);
            }
            if (value == null) return;
            val type = field.getType();
            if (type.isAssignableFrom(Material.class)) {
                field.set(instance, Material.valueOf(((String) value).toUpperCase()));
                return;
            }
            if (type.isAssignableFrom(CustomFuel.class) && value instanceof String val) {
                if (!val.equalsIgnoreCase("none")) {
                    val customFuel = customFuels.get(val);
                    if (customFuel == null)
                        messages.send(Bukkit.getConsoleSender(), "&cInvalid custom fuel id %s", val);
                    field.set(instance, customFuel);
                    return;
                }
            }
            val annotation = type.getDeclaredAnnotation(SectionPath.class);
            if (annotation != null) {
                field.set(instance, appendConfigValue(section.getConfigurationSection(annotation.value()), type, customFuels, messages));
                return;
            }
            if (type.isEnum()) {
                field.set(instance, Enum.valueOf((Class<? extends Enum>) type, ((String) value).toUpperCase()));
                return;
            }
            if (type.isAssignableFrom(CustomFuel.class) && value instanceof String) {
                field.set(instance, customFuels.get(value));
                return;
            }
            if (type.isAssignableFrom(String.class) && value instanceof String text) {
                if (text.contains("&")) {
                    field.set(instance, Messages.translateColorCodes(text + "&r"));
                    return;
                }
            }
            if (type.isAssignableFrom(List.class)) {
                //noinspection rawtypes
                val list = (List) value;
                for (int i = 0; i < list.size(); i++) {
                    val v = list.get(i);
                    if (!(v instanceof String)) continue;
                    list.set(i, Messages.translateColorCodes(v + (((String) v).contains("&") ? "&r" : "")));
                }
            }
            if (type.isAssignableFrom(Float.class)) {
                field.set(instance, ((Double) value).floatValue());
                return;
            }
            if (type.isAssignableFrom(Permission.class)) {
                val permString = ((String) value).replace("#id", section.getName()).toLowerCase();
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
                return;
            }
            field.set(instance, value);
        });
        return instance;
    }

}
