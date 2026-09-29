package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** message.yml */
@Singleton
public class MessageConfigLoader implements ConfigLoader.Single<Message> {

    private final ConfigValueMappers mappers;
    private final Log log;

    public MessageConfigLoader(@NotNull ConfigValueMappers mappers, @NotNull MessageSink messages) {
        this.mappers = mappers;
        this.log = new Log(getClass(), messages);
    }

    @Override
    public @NotNull String file() {
        return "message.yml";
    }

    @Override
    public @Nullable Message load(@NotNull ConfigurationSection root, @NotNull LoadSupport support) throws Exception {
        try {
            val loaded = mappers.mapSection(root, Message.class, support.customFuels(), support.messages());
            log.log("&aLoaded config %s - %s", file(), loaded);
            return loaded;
        } catch (Exception error) {
            log.log("&cLoad %s error: %s", file(), error);
            throw error;
        }
    }

}
