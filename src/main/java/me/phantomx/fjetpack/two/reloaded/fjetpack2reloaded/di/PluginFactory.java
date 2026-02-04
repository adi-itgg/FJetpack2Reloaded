package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpackImpl;
import org.bukkit.Server;
import org.bukkit.plugin.PluginLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

@Factory
public class PluginFactory {

    @RequiredArgsConstructor
    public static class Provider {
        private final @Getter FJetpackImpl plugin;
    }

    private final Provider provider;

    @Inject
    public PluginFactory(@External Provider provider) {
        this.provider = provider;
    }

    @Bean
    public FJetpackImpl providePlugin() {
        return provider.getPlugin();
    }

    @Bean
    public Server provideServer() {
        return provider.getPlugin().getServer();
    }

    @Bean
    public PluginLogger providePluginLogger(Server server) {
        var logger = (PluginLogger) provider.getPlugin().getLogger();
        if (!server.getMotd().contains("[DEV-FJ2R-DEBUG]")) {
            logger.setLevel(Level.INFO);
        } else {
            logger.info("[DEV-FJ2R-DEBUG] Enabled");
            logger.setLevel(Level.ALL);
        }
        return logger;
    }

    @Bean
    public Logger provideLogger() {
        return LoggerFactory.getLogger(FJetpack.class);
    }

    @Bean
    Integer provideUniqueId() {
        return ThreadLocalRandom.current().nextInt();
    }
}
