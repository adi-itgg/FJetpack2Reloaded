package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.ColorText;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** jetpack.yml - {@code id -> Jetpack} */
@Singleton
public class JetpackConfigLoader implements ConfigLoader.Entries<Jetpack> {

    private final ConfigValueMappers mappers;
    private final Log log;

    public JetpackConfigLoader(@NotNull ConfigValueMappers mappers, @NotNull MessageSink messages) {
        this.mappers = mappers;
        this.log = new Log(getClass(), messages);
    }

    @Override
    public @NotNull String file() {
        return "jetpack.yml";
    }

    @Override
    public @NotNull Map<String, Jetpack> load(@NotNull ConfigurationSection root, @NotNull LoadSupport support) {
        val jetpackList = new HashMap<String, Jetpack>();

        for (String id : root.getKeys(false)) {
            Catcher.create(() -> {
                val jetpack = mappers.mapSection(root.getConfigurationSection(id), Jetpack.class, support.customFuels(), support.messages());
                assert jetpack != null;
                jetpack.setId(id);
                return jetpack;
            }).onSuccess(jetpack -> {
                jetpack.setDisplayName(ColorText.translate(jetpack.getDisplayName()));
                jetpack.setLore(jetpack.getLore().stream().map(ColorText::translate).toList());
                jetpackList.put(jetpack.getId(), jetpack);
                log.log("&aLoaded Jetpack:&r %s", jetpack);
                support.messages().send(support.sender(), "&aLoaded jetpack: &l%s", id);
            }).onFailure(error -> {
                log.log("&cFailure load jetpack: %s - error: %s", id, error);
                support.messages().send(support.sender(), "&cFailure load jetpack: %s", id);
            });
        }

        return jetpackList;
    }

}
