package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Startup self-check: the server must be able to carry the plugin tags. When it cannot, a jetpack is
 * indistinguishable from any other item and every player would be told their gear is not a jetpack.
 * <p>
 * Separate from {@link ServerVersion} because the check needs the tag codec and the tag codec already
 * needs the version - keeping it here is what stops the two from forming a cycle.
 */
@Singleton
public class ServerSupport {

    private final ServerVersion version;
    private final ItemTags itemTags;
    private final MessageSink messages;

    public ServerSupport(@NotNull ServerVersion version,
                         @NotNull ItemTags itemTags,
                         @NotNull MessageSink messages) {
        this.version = version;
        this.itemTags = itemTags;
        this.messages = messages;
    }

    /** @return false when this server cannot run the plugin */
    public boolean check() {
        messages.sendToConsole("&6Checking Server Version...");
        if (version.getServerVersion() == 0) {
            messages.sendToConsole("&cUnknown Server Version! - " + Bukkit.getVersion());
            return false;
        }
        if (version.getServerVersion() > 17) {
            val armor = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            val storedValue = itemTags.getJetpackID(itemTags.setJetpack(armor, version.getNmsApiVersion()), "");
            if (!storedValue.equals(version.getNmsApiVersion())) {
                messages.sendToConsole("&cUnsupported server api version!");
                return false;
            }
        }
        messages.sendToConsole("&6Detected Server: &a%s v%d - API %s", Bukkit.getName(), version.getServerVersion(), version.getNmsApiVersion());
        return true;
    }

}
