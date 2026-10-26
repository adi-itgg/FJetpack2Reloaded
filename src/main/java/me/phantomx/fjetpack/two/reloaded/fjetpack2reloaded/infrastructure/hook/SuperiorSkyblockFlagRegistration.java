package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.jetbrains.annotations.NotNull;

/**
 * Registers this plugin's island flag and privilege with SuperiorSkyblock2, once.
 * <p>
 * Called from the {@code PluginInitializeEvent} listener: the flag can only be added while
 * SuperiorSkyblock2 is still initialising, which is why it is a separate step from the hook.
 */
@Singleton
public class SuperiorSkyblockFlagRegistration {

    private final MessageSink messages;
    private final SuperiorSkyblockHookState state;

    public SuperiorSkyblockFlagRegistration(@NotNull MessageSink messages,
                                            @NotNull SuperiorSkyblockHookState state) {
        this.messages = messages;
        this.state = state;
    }

    public void register() {
        IslandFlag.register(SuperiorSkyblock.FLAG_PRIVILEGE);
        messages.sendToConsole("&aRegistered flag &6%s", SuperiorSkyblock.FLAG_PRIVILEGE);
        IslandPrivilege.register(SuperiorSkyblock.FLAG_PRIVILEGE);
        messages.sendToConsole("&aRegistered privilege &6%s", SuperiorSkyblock.FLAG_PRIVILEGE);
        state.markRegistered();
    }

}
