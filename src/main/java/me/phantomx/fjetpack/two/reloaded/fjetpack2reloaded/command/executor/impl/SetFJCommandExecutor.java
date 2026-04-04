package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Component
@RequiredArgsConstructor
public class SetFJCommandExecutor implements FJCommandExecutor {

    private final FJConfig config;
    private final FJVersion version;
    private final JetpackItemFactory jetpackItemFactory;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.SET;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 1) {
            Messages.sendMessage(sender, config.helpText().get(6));
            return;
        }
        if (!(sender instanceof Player player)) {
            Messages.sendMessage(config.message().getCmdPlayerOnly());
            return;
        }

        var item = version.getServerVersion() > 8 ? player.getInventory().getItemInMainHand() : player.getItemInHand();

        if (item.getItemMeta() == null || item.getType() == Material.AIR) {
            Messages.sendMessage(config.message().getNoItemInMainHand());
            return;
        }

        var jetpackItem = jetpackItemFactory.createJetpackItem(sender, args[1], args.length == 3 ? Long.parseLong(args[2]) : 0);
        if (version.getServerVersion() > 11) {
            player.getInventory().setItemInMainHand(jetpackItem);
        } else {
            player.setItemInHand(jetpackItem);
        }

        Messages.sendMessage(sender, config.message().getCmdSet()
                .replace(Placeholder.JETPACK, args[1])
                .replace(Placeholder.FUEL_VALUE, String.valueOf(args.length == 3 ? args[2] : 0))
        );
    }

}
