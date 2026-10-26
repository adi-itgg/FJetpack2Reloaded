package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.TestPlugin;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.CustomFuelConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.JetpackConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.MessageConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.PluginConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.testkit.RecordingSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The repository over real files, without a running server.
 * <p>
 * What matters here is the failure path: a file the plugin cannot read has to be reported rather
 * than swallowed, and it must not replace the state that is already in use.
 */
@DisplayName("YamlConfigRepository")
class FailingFileLoadersTest {

    private final RecordingSink sink = new RecordingSink();

    @BeforeEach
    void installStubServer() {
        TestPlugin.installServer();
    }

    @Test
    @DisplayName("a broken file is reported, and the loaded state survives")
    void brokenFileIsReportedAndDoesNotReplaceLoadedState() throws IOException {
        var folder = Files.createTempDirectory("fj2r-broken");
        Files.writeString(folder.resolve("config.yml"), "version: not-a-number\n");

        var repository = repositoryFor(TestPlugin.withDataFolder(folder));
        repository.reload(sink.console());

        assertTrue(repository.failedFiles().contains("config.yml"), repository.failedFiles().toString());
        assertTrue(sink.saw("config.yml"), sink.sent.toString());

        // the repository still answers, with the built in default
        assertNotNull(repository.config());
        assertNotNull(repository.message());
    }

    @Test
    @DisplayName("the shipped files load as they are")
    void shippedFilesLoad() throws IOException {
        var folder = Files.createTempDirectory("fj2r-fresh");

        var repository = repositoryFor(TestPlugin.withDataFolder(folder));
        repository.reload(sink.console());

        assertTrue(repository.failedFiles().isEmpty(), repository.failedFiles().toString() + " / " + sink.sent);
        assertTrue(Files.exists(folder.resolve("config.yml")), "config.yml is written out");
        assertTrue(Files.exists(folder.resolve("jetpack.yml")), "jetpack.yml is written out");
        assertEquals(1, repository.config().getVersion(), "the shipped config.yml says version: 1");
        assertFalse(repository.jetpacks().isEmpty(), "a loaded jetpack is what makes the plugin usable");
    }

    private YamlConfigRepository repositoryFor(TestPlugin plugin) {
        var mappers = ConfigValueMappers.defaults();
        return new YamlConfigRepository(
                plugin,
                sink,
                new PluginConfigLoader(mappers),
                new MessageConfigLoader(mappers, sink),
                new CustomFuelConfigLoader(mappers, sink),
                new JetpackConfigLoader(mappers, sink)
        );
    }

}
