package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/** Loads help.txt from the jar, with the version placeholder replaced. */
@Singleton
public class HelpText {

    private final JavaPlugin plugin;

    @Getter(lazy = true)
    private final String text = load();

    public HelpText(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** One 1-based line of the help text. */
    public @NotNull String line(int line) {
        return getText().split("\n")[line - 1];
    }

    @SneakyThrows
    private @NotNull String load() {
        val is = plugin.getResource("help.txt");
        if (is == null) return "";
        val bufferReader = new BufferedReader(new InputStreamReader(is));
        val result = new StringBuilder();
        var str = "";
        while ((str = bufferReader.readLine()) != null) {
            if (!result.isEmpty()) result.append("\n");
            result.append(str);
        }
        return result.toString().replace(
                Placeholder.VERSION,
                plugin.getDescription().getVersion()
        );
    }

}
