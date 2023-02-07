package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack2Reloaded;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemUtil;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;


public class FJ2RCommandExecutor {


    private static final Log log = new Log(FJ2RCommandExecutor.class);

    @Getter(lazy = true)
    private static final String helpText = initializeHelp();

    @SneakyThrows
    private static @NotNull String initializeHelp() {
        val is = FJetpack2Reloaded.getPlugin().getResource("help.txt");
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
                FJetpack2Reloaded.getPlugin().getDescription().getVersion()
        );
    }

    private static String getHelpLine(int line) {
        return getHelpText().split("\n")[line - 1];
    }

    public static @NotNull String cmd(@NotNull FJ2RCommand cmd) {
        return cmd.name().replace("_", "").toLowerCase();
    }

    @SuppressWarnings("deprecation")
    public static boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        log.debug("onCommand: Sender=%s, Command=%s, label=%s, args=%s", sender.getName(), command.getName(), label, args);

        var isContainsCommand = false;
        if (args.length > 0)
            for (FJ2RCommand cmd : FJ2RCommand.values()) {
                isContainsCommand = cmd(cmd).equalsIgnoreCase(args[0]);
                if (isContainsCommand) break;
            }

        // cmd help
        if (!isContainsCommand || args[0].equalsIgnoreCase(cmd(FJ2RCommand.HELP))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.HELP)))
                NoPermissionLvlException.send();
            Messages.sendMessage(false, sender, getHelpText());
            return true;
        }

        // cmd reload
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.RELOAD))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.RELOAD)))
                NoPermissionLvlException.send();
            Configs.reloadConfig(sender);
            return true;
        }

        // cmd check update
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.CHECK_UPDATE))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.CHECK_UPDATE)))
                NoPermissionLvlException.send();
            Version.checkUpdate(sender);
            return true;
        }

        // cmd set
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.SET))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.SET)))
                NoPermissionLvlException.send();

            // if args is empty should send how to use this cmd
            if (args.length == 1) {
                Messages.sendMessage(sender, getHelpLine(7));
                return true;
            }

            if (!(sender instanceof Player player)) {
                Messages.sendMessage(Configs.getMessage().getCmdPlayerOnly());
                return true;
            }

            val item = PlayerUtil.getItemInHand(player);
            if (item == null) {
                Messages.sendMessage(Configs.getMessage().getNoItemInMainHand());
                return true;
            }

            ItemUtil.setItemAsJetpack(sender, item, args[1], args.length == 3 ? Long.parseLong(args[2]) : 0)
               .onSuccess(result -> {
                   if (Version.getServerVersion() > 11)
                       player.getInventory().setItemInMainHand(result);
                   else
                       //noinspection deprecation
                       player.setItemInHand(result);
                   Messages.sendMessage(sender, Configs.getMessage().getCmdSet()
                           .replace(Placeholder.JETPACK, args[1])
                           .replace(Placeholder.FUEL_VALUE, String.valueOf(args.length == 3 ? args[2] : 0))
                   );
               }).throwIfError();

            return true;
        }

        // cmd get/give
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.GET)) || args[0].equalsIgnoreCase(cmd(FJ2RCommand.GIVE))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.GET)) && !Permissions.hasPermission(sender, cmd(FJ2RCommand.GIVE)))
                NoPermissionLvlException.send();

            if (args.length == 1) {
                Messages.sendMessage(sender, getHelpLine(5));
                return true;
            }

            val jetpackId = Arrays.stream(args)
                    .filter(arg -> Configs.getJetpacksLoaded().get(arg) != null)
                    .findFirst()
                    .orElse(null);

            if (jetpackId == null) {
                Messages.sendMessage(sender, "&cJetpack didn't exist");
                return true;
            }

            val targetPlayer = Arrays.stream(args)
                    .map(Bukkit::getPlayerExact)
                    .filter(Objects::nonNull)
                    .findFirst().orElseGet(() -> {
                        if (!(sender instanceof Player)) {
                            Messages.sendMessage(Configs.getMessage().getCmdPlayerOnly());
                            return null;
                        }
                        return (Player) sender;
                    });

            if (targetPlayer == null) return true;

            val fuelAmount = Arrays.stream(args)
                    .filter(NumberUtils::isDigits)
                    .map(NumberUtils::toInt)
                    .findFirst().orElse(0);

            val result = ItemUtil.createJetpackItem(sender, jetpackId, fuelAmount);
            result.onSuccess(item -> {
                addItemToInventory(targetPlayer, item);

                if (sender == targetPlayer) {
                    Messages.sendMessage(sender,Configs.getMessage().getCmdGetSelf()
                            .replace(Placeholder.JETPACK, jetpackId)
                            .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount)));
                    return;
                }

                Messages.sendMessage(sender, Configs.getMessage().getCmdGiveSuccess()
                        .replace(Placeholder.JETPACK, jetpackId)
                        .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount))
                        .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                );
                Messages.sendMessage(targetPlayer, Configs.getMessage().getCmdGiveReceived()
                        .replace(Placeholder.JETPACK, jetpackId)
                        .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount))
                        .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                        .replace(Placeholder.SENDER, sender.getName())
                );
            });
            result.throwIfError();
        }

        // cmd setfuel
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.SET_FUEL))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.SET_FUEL)))
                NoPermissionLvlException.send();

            // if args is empty should send how to use this cmd
            if (args.length == 1) {
                Messages.sendMessage(sender, getHelpLine(8));
                return true;
            }

            if (!(sender instanceof Player player)) {
                Messages.sendMessage(Configs.getMessage().getCmdPlayerOnly());
                return true;
            }

            val item = PlayerUtil.getItemInHand(player);
            if (item == null) {
                Messages.sendMessage(Configs.getMessage().getNoItemInMainHand());
                return true;
            }

            val fuelAmount = NumberUtils.toLong(args[1], 0);

            val jetpackId = ItemMetaData.getJetpackID(item, "");
            if (jetpackId.isEmpty()) {
                Messages.sendMessage(sender, Configs.getMessage().getNotJetpackItem());
                return true;
            }

            ItemUtil.setItemAsJetpack(sender, item, jetpackId, fuelAmount)
            .onSuccess(result -> {
                if (Version.getServerVersion() > 11)
                    player.getInventory().setItemInMainHand(result);
                else
                    player.setItemInHand(result);
                Messages.sendMessage(sender, Configs.getMessage().getCmdFuelSet()
                        .replace(Placeholder.AMOUNT, String.valueOf(fuelAmount))
                );
            }).throwIfError();

            return true;
        }

        // cmd get/give fuel
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.GET_FUEL)) || args[0].equalsIgnoreCase(cmd(FJ2RCommand.GIVE_FUEL))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.GET_FUEL)) && !Permissions.hasPermission(sender, cmd(FJ2RCommand.GIVE_FUEL)))
                NoPermissionLvlException.send();

            if (args.length == 1) {
                Messages.sendMessage(sender, getHelpLine(6));
                return true;
            }

            val customFuelId = Arrays.stream(args)
                    .filter(arg -> Configs.getCustomFuelLoaded().get(arg) != null)
                    .findFirst()
                    .orElse(null);

            if (customFuelId == null) {
                Messages.sendMessage(sender, "&cInvalid custom fuel id!");
                return true;
            }

            val targetPlayer = Arrays.stream(args)
                    .map(Bukkit::getPlayerExact)
                    .filter(Objects::nonNull)
                    .findFirst().orElseGet(() -> {
                        if (!(sender instanceof Player)) {
                            Messages.sendMessage(Configs.getMessage().getCmdPlayerOnly());
                            return null;
                        }
                        return (Player) sender;
                    });

            if (targetPlayer == null) return true;

            val amount = Arrays.stream(args)
                    .filter(NumberUtils::isDigits)
                    .map(NumberUtils::toInt)
                    .findFirst().orElse(1);

            val result = ItemUtil.createCustomFuelItem(sender, customFuelId, amount);
            result.onSuccess(item -> {
                addItemToInventory(targetPlayer, item);

                if (sender == targetPlayer) {
                    Messages.sendMessage(sender, Configs.getMessage().getCmdFuelGiveSuccess()
                            .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                            .replace(Placeholder.AMOUNT, String.valueOf(amount))
                            .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                            .replace(Placeholder.SENDER, sender.getName())
                    );
                    return;
                }

                Messages.sendMessage(sender, Configs.getMessage().getCmdFuelGiveSuccess()
                        .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                        .replace(Placeholder.AMOUNT, String.valueOf(amount))
                        .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                );
                Messages.sendMessage(targetPlayer, Configs.getMessage().getCmdFuelGiveReceived()
                        .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                        .replace(Placeholder.AMOUNT, String.valueOf(amount))
                        .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                        .replace(Placeholder.SENDER, sender.getName())
                );
            });
            result.throwIfError();

            return true;
        }

        return true;
    }

    private static void addItemToInventory(@NotNull Player targetPlayer, @NotNull ItemStack item) {
        val items = targetPlayer.getInventory().addItem(item);
        // drop if inventory is full!
        items.forEach((index, itemStack) -> {
            val location = targetPlayer.getLocation();
            targetPlayer.getWorld().dropItemNaturally(location, itemStack);
            Messages.sendMessage(targetPlayer, Configs.getMessage().getInventoryFull()
                    .replace(Placeholder.X, String.valueOf(location.getX()))
                    .replace(Placeholder.Y, String.valueOf(location.getY()))
                    .replace(Placeholder.Z, String.valueOf(location.getZ()))
            );
        });
        targetPlayer.updateInventory();
    }

}
