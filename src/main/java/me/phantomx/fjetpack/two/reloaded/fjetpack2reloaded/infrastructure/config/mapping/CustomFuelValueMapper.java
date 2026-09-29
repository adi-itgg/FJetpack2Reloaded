package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * {@code CustomFuel: CVIP} - a reference to an already loaded custom fuel.
 * <p>
 * The value {@code none} (any case) disables the field; an unknown id warns and leaves it null.
 */
public final class CustomFuelValueMapper implements ConfigValueMapper {

    @Override
    public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
        if (!field.getType().isAssignableFrom(me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel.class))
            return false;
        if (!(value instanceof String id)) return false;

        if (id.equalsIgnoreCase("none")) {
            field.set(instance, null);
            return true;
        }

        val customFuel = context.customFuels().get(id);
        if (customFuel == null)
            context.messages().sendToConsole("&cInvalid custom fuel id %s", id);
        field.set(instance, customFuel);
        return true;
    }

}
