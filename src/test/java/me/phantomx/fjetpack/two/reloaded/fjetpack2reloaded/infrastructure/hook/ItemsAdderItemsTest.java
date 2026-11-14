package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.TestPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ItemsAdder is a {@code compileOnly} dependency, so its classes are absent from the test runtime -
 * exactly what a server without the plugin looks like.
 */
@DisplayName("ItemsAdder hook")
class ItemsAdderItemsTest {

    @BeforeAll
    static void installStubServer() {
        TestPlugin.installServer();
    }

    @Test
    @DisplayName("the adapter loads and reports nothing when ItemsAdder is absent")
    void absentItemsAdderIsNotAnError() {
        var items = new ItemsAdderItems();

        assertFalse(items.isAvailable(), "ItemsAdder is not installed");
        assertNull(items.get("fancypacks:jetpack_vip"), "no source, so the caller falls back to the material");
    }

}
