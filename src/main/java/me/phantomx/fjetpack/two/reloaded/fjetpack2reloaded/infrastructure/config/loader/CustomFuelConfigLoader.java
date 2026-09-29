package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.ColorText;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** custom_fuel.yml - {@code id -> CustomFuel} */
@Singleton
public class CustomFuelConfigLoader implements ConfigLoader.Entries<CustomFuel> {

    private final ConfigValueMappers mappers;
    private final Log log;

    public CustomFuelConfigLoader(@NotNull ConfigValueMappers mappers, @NotNull MessageSink messages) {
        this.mappers = mappers;
        this.log = new Log(getClass(), messages);
    }

    @Override
    public @NotNull String file() {
        return "custom_fuel.yml";
    }

    @Override
    public @NotNull Map<String, CustomFuel> load(@NotNull ConfigurationSection root, @NotNull LoadSupport support) {
        val loadedCustomFuels = new HashMap<String, CustomFuel>();

        for (String id : root.getKeys(false)) {
            Catcher.create(() -> {
                val customFuel = mappers.mapSection(root.getConfigurationSection(id), CustomFuel.class, support.customFuels(), support.messages());
                assert customFuel != null;
                customFuel.setId(id);
                customFuel.setDisplayName(ColorText.translate(customFuel.getDisplayName()));
                customFuel.setLore(customFuel.getLore().stream().map(ColorText::translate).toList());
                return customFuel;
            }).onSuccess(customFuel -> {
                loadedCustomFuels.put(customFuel.getId(), customFuel);
                log.log("&aLoaded custom fuel: &l%s", customFuel);
                support.messages().send(support.sender(), "&aLoaded custom fuel: &l%s", id);
            }).onFailure(error -> log.log("&cFailed load custom fuel: %s - details: %s", id, error));
        }

        return loadedCustomFuels;
    }

}
