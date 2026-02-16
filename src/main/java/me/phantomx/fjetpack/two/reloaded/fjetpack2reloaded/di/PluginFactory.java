package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanScope;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemDataProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.NBTAPIProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.PDCProvider;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.logging.Level;

@Factory
public class PluginFactory {

    @RequiredArgsConstructor
    public static class Initiator {
        private final @Getter FJetpack plugin;
        private final Supplier<BeanScope> beanSupplier;
    }

    @RequiredArgsConstructor
    public static class Provider {
        private final Supplier<BeanScope> beanSupplier;

        public <T> T provide(Class<T> type) {
            return this.beanSupplier.get().get(type);
        }

        public <T> List<T> provideList(Class<T> type) {
            return this.beanSupplier.get().list(type);
        }
    }

    private final Initiator initiator;

    @Inject
    PluginFactory(@External Initiator initiator) {
        this.initiator = initiator;
    }

    @Bean
    Provider provideProvider() {
        return new Provider(this.initiator.beanSupplier);
    }

    @Bean
    FJetpack providePlugin() {
        return initiator.getPlugin();
    }

    @Bean
    Server provideServer() {
        return initiator.getPlugin().getServer();
    }

    @Bean
    PluginLogger providePluginLogger(Server server) {
        var logger = (PluginLogger) initiator.getPlugin().getLogger();
        if (!server.getMotd().contains("[DEV-FJ2R-DEBUG]")) {
            logger.setLevel(Level.INFO);
        } else {
            logger.info("[DEV-FJ2R-DEBUG] Enabled");
            logger.setLevel(Level.ALL);
        }
        return logger;
    }

    @Bean
    Logger provideLogger() {
        return LoggerFactory.getLogger(FJetpack.class);
    }

    @Bean
    Integer provideUniqueId() {
        return ThreadLocalRandom.current().nextInt();
    }

    @Bean
    YamlConfiguration provideYamlConfiguration() {
        return new YamlConfiguration();
    }

    @Bean
    public ItemDataProvider itemDataProvider(Plugin plugin) {
        boolean isNewVersion = Try.of(() -> Class.forName("org.bukkit.persistence.PersistentDataContainer")).isSuccess();
        if (isNewVersion) {
            return new PDCProvider(plugin);
        }
        return new NBTAPIProvider();
    }

}
