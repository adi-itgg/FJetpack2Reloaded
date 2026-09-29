package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.ColorText;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Maps a YAML section onto a config DTO by reflection.
 * <p>
 * Field names are the YAML keys (first letter may be upper or lower case), so the field names of the
 * DTOs are frozen. The mapping itself is a chain of {@link ConfigValueMapper}s, tried in the order
 * given by {@link #defaults()}.
 */
public final class ConfigValueMappers implements ConfigValueMapper {

    private final List<ConfigValueMapper> mappers;

    private ConfigValueMappers(@NotNull List<ConfigValueMapper> mappers) {
        this.mappers = mappers;
    }

    /** The chain, in the order the value kinds are tried. */
    public static @NotNull ConfigValueMappers defaults() {
        return new ConfigValueMappers(List.of(
                new MaterialValueMapper(),
                new CustomFuelValueMapper(),
                new NestedSectionValueMapper(),
                new EnumValueMapper(),
                new ColorStringValueMapper(),
                new StringListValueMapper(),
                new FloatValueMapper(),
                new PermissionValueMapper(),
                new FallbackValueMapper()
        ));
    }

    @Override
    public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
        for (ConfigValueMapper mapper : mappers)
            if (mapper.map(instance, field, value, context)) return true;
        return false;
    }

    /** Fills a new instance of {@code clazz} from the section. Returns null for a null section. */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T mapSection(@Nullable ConfigurationSection section,
                               @NotNull Class<T> clazz,
                               @NotNull Map<String, CustomFuel> customFuels,
                               @NotNull MessageSink messages) throws Exception {
        if (section == null) return null;

        val instance = (T) clazz.getDeclaredConstructors()[0].newInstance();
        val context = new MappingContext(section, customFuels, messages, this);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            var value = section.get(field.getName());
            if (value == null) {
                var key = field.getName();
                key = key.substring(0, 1).toUpperCase() + key.substring(1);
                value = section.get(key);
            }
            if (value == null) continue;

            map(instance, field, value, context);
        }
        return instance;
    }

    /** {@code Item: CHAINMAIL_CHESTPLATE} */
    static final class MaterialValueMapper implements ConfigValueMapper {

        @Override
        public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
            if (!field.getType().isAssignableFrom(Material.class)) return false;
            field.set(instance, Material.valueOf(((String) value).toUpperCase()));
            return true;
        }

    }

    /** {@code OnDeath: DROP} */
    static final class EnumValueMapper implements ConfigValueMapper {

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
            val type = field.getType();
            if (!type.isEnum()) return false;
            field.set(instance, Enum.valueOf((Class<? extends Enum>) type, ((String) value).toUpperCase()));
            return true;
        }

    }

    /** Text with {@code &} codes gets the codes translated; plain text falls through to the default. */
    static final class ColorStringValueMapper implements ConfigValueMapper {

        @Override
        public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
            if (!field.getType().isAssignableFrom(String.class) || !(value instanceof String text)) return false;
            if (!text.contains("&")) return false;
            field.set(instance, ColorText.translate(text + "&r"));
            return true;
        }

    }

    /** A list of strings has its color codes translated in place. */
    static final class StringListValueMapper implements ConfigValueMapper {

        @SuppressWarnings("rawtypes")
        @Override
        public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
            if (!field.getType().isAssignableFrom(List.class)) return false;
            val list = (List) value;
            for (int i = 0; i < list.size(); i++) {
                val entry = list.get(i);
                if (!(entry instanceof String text)) continue;
                list.set(i, ColorText.translate(text + (text.contains("&") ? "&r" : "")));
            }
            field.set(instance, list);
            return true;
        }

    }

    /** {@code Speed: 1.1} - a whole number in the YAML would fail here, as it always did. */
    static final class FloatValueMapper implements ConfigValueMapper {

        @Override
        public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
            if (!field.getType().isAssignableFrom(Float.class)) return false;
            field.set(instance, ((Double) value).floatValue());
            return true;
        }

    }

    /** Anything else is assigned as is. */
    static final class FallbackValueMapper implements ConfigValueMapper {

        @Override
        public boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception {
            field.set(instance, value);
            return true;
        }

    }

}
