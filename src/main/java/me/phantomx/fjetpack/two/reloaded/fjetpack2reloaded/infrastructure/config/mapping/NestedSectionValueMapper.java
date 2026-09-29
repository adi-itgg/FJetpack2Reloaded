package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.SectionPath;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * {@code @SectionPath("Fuel")} - a nested section mapped onto a nested DTO, by recursion.
 * <p>
 * Note that the nested section becomes the current section of the recursion, so a {@code #id}
 * placeholder inside it would use the nested name.
 */
public final class NestedSectionValueMapper implements ConfigValueMapper {

    @Override
    public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
        val type = field.getType();
        val annotation = type.getDeclaredAnnotation(SectionPath.class);
        if (annotation == null) return false;
        field.set(instance, context.mappers().mapSection(
                context.section().getConfigurationSection(annotation.value()),
                type,
                context.customFuels(),
                context.messages()
        ));
        return true;
    }

}
