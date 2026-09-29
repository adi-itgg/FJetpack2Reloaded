package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Hands jetpack / custom fuel items to a player and reports the result to both sides.
 */
public class JetpackGrant {

    private final YamlConfigRepository configs;
    private final Messages messages;
    private final ItemFactory items;

    public JetpackGrant(@NotNull YamlConfigRepository configs, @NotNull Messages messages, @NotNull ItemFactory items) {
        this.configs = configs;
        this.messages = messages;
        this.items = items;
    }

    public void giveJetpack(@NotNull CommandSender sender, @NotNull Player targetPlayer, @NotNull String jetpackId, long fuelAmount) {
        val result = items.createJetpackItem(sender, jetpackId, fuelAmount);
        result.onSuccess(item -> {
            addItemToInventory(targetPlayer, item);

            if (sender == targetPlayer) {
                messages.sendMessage(sender, configs.message().getCmdGetSelf()
                        .replace(Placeholder.JETPACK, jetpackId)
                        .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount)));
                return;
            }

            messages.sendMessage(sender, configs.message().getCmdGiveSuccess()
                    .replace(Placeholder.JETPACK, jetpackId)
                    .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount))
                    .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
            );
            messages.sendMessage(targetPlayer, configs.message().getCmdGiveReceived()
                    .replace(Placeholder.JETPACK, jetpackId)
                    .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount))
                    .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                    .replace(Placeholder.SENDER, sender.getName())
            );
        });
        result.throwIfError();
    }

    public void giveCustomFuel(@NotNull CommandSender sender, @NotNull Player targetPlayer, @NotNull String customFuelId, int amount) {
        val result = items.createCustomFuelItem(sender, customFuelId, amount);
        result.onSuccess(item -> {
            addItemToInventory(targetPlayer, item);

            if (sender == targetPlayer) {
                messages.sendMessage(sender, configs.message().getCmdFuelGiveSuccess()
                        .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                        .replace(Placeholder.AMOUNT, String.valueOf(amount))
                        .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                        .replace(Placeholder.SENDER, sender.getName())
                );
                return;
            }

            messages.sendMessage(sender, configs.message().getCmdFuelGiveSuccess()
                    .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                    .replace(Placeholder.AMOUNT, String.valueOf(amount))
                    .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
            );
            messages.sendMessage(targetPlayer, configs.message().getCmdFuelGiveReceived()
                    .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                    .replace(Placeholder.AMOUNT, String.valueOf(amount))
                    .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                    .replace(Placeholder.SENDER, sender.getName())
            );
        });
        result.throwIfError();
    }

    private void addItemToInventory(@NotNull Player targetPlayer, @NotNull ItemStack item) {
        val leftovers = targetPlayer.getInventory().addItem(item);
        // drop if inventory is full!
        leftovers.forEach((index, itemStack) -> {
            val location = targetPlayer.getLocation();
            targetPlayer.getWorld().dropItemNaturally(location, itemStack);
            messages.sendMessage(targetPlayer, configs.message().getInventoryFull()
                    .replace(Placeholder.X, String.valueOf(location.getX()))
                    .replace(Placeholder.Y, String.valueOf(location.getY()))
                    .replace(Placeholder.Z, String.valueOf(location.getZ()))
            );
        });
        targetPlayer.updateInventory();
    }

}
