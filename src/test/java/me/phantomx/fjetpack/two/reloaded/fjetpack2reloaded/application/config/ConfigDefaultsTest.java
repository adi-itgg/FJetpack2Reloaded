package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A config record is read before its file is loaded, and a file may be missing a key. Neither may
 * hand a null to a caller: an unconfigured message has to read as the empty string, because several
 * call sites do {@code message.getCmdSet().replace(...)} on it.
 */
@DisplayName("Config defaults")
class ConfigDefaultsTest {

    @Test
    @DisplayName("a fresh Message has no null field")
    void freshMessageIsFullyPopulated() {
        var message = new Message();

        for (Field field : Message.class.getDeclaredFields()) {
            if (field.getType() != String.class || Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            try {
                assertNotNull(field.get(message), field.getName() + " must default to a value");
            } catch (IllegalAccessException error) {
                throw new AssertionError(error);
            }
        }
    }

    @Test
    @DisplayName("a fresh Config has a version")
    void freshConfigHasAVersion() {
        assertNotNull(new Config().getVersion());
    }

}
