package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.ItemDataKey;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemDataProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Component
@RequiredArgsConstructor
public class SetFuelFJCommandExecutor implements FJCommandExecutor {

    private final FJConfig config;
    private final FJVersion version;
    private final ItemDataProvider itemDataProvider;
    private final JetpackItemFactory jetpackItemFactory;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.SET_FUEL;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 1) {
            Messages.sendMessage(sender, config.helpText().get(7));
            return;
        }

        if (!(sender instanceof Player player)) {
            Messages.sendMessage(sender, config.message().getCmdPlayerOnly());
            return;
        }

        var item = getItemInHand(version, player);
        if (item == null) {
            Messages.sendMessage(sender, config.message().getNoItemInMainHand());
            return;
        }

        var fuelAmount = NumberUtils.toLong(args[1], 0L);
        var jetpackId = itemDataProvider.getString(item, ItemDataKey.JETPACK_ID, "");
        if (jetpackId.isEmpty()) {
            Messages.sendMessage(sender, config.message().getNotJetpackItem());
            return;
        }

        var jetpackItem = jetpackItemFactory.setJetpackItem(sender, item, jetpackId, fuelAmount);
        if (version.getServerVersion() > 11) {
            player.getInventory().setItemInMainHand(jetpackItem);
        } else {
            //noinspection deprecation
            player.getInventory().setItemInHand(jetpackItem);
        }
        var msg = config.message().getCmdFuelSet()
                .replace(Placeholder.AMOUNT, String.valueOf(fuelAmount));
        Messages.sendMessage(sender, msg);
    }

}
