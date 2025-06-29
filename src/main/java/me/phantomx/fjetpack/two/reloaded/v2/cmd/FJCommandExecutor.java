package me.phantomx.fjetpack.two.reloaded.v2.cmd;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemUtil;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import me.phantomx.fjetpack.two.reloaded.v2.config.ConfigManager;
import me.phantomx.fjetpack.two.reloaded.v2.exception.UnauthorizedException;
import me.phantomx.fjetpack.two.reloaded.v2.util.MessageUtil;
import me.phantomx.fjetpack.two.reloaded.v2.util.Utils;
import me.phantomx.fjetpack.two.reloaded.v2.util.VersionManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class FJCommandExecutor {

    private final ConfigManager configManager;
    private final VersionManager versionManager;

    private final String help;

    @SneakyThrows
    public FJCommandExecutor(Plugin plugin, ConfigManager configManager, VersionManager versionManager) {
        this.configManager = configManager;
        this.versionManager = versionManager;

        val is = plugin.getResource("help.txt");
        if (is == null) {
            throw new RuntimeException("Failed to load help.txt");
        }
        val bufferReader = new BufferedReader(new InputStreamReader(is));
        val result = new StringBuilder();
        var str = "";
        while ((str = bufferReader.readLine()) != null) {
            if (!result.isEmpty()) {
                result.append("\n");
            }
            result.append(str);
        }
        this.help = result.toString().replace(
                Placeholder.VERSION,
                plugin.getDescription().getVersion()
        );
    }

    private String getHelpLine(int line) {
        return help.split("\n")[line - 1];
    }

    public @NotNull String cmd(@NotNull FJ2RCommand cmd) {
        return cmd.name().replace("_", "").toLowerCase();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        var isContainsCommand = args.length == 0 || Arrays.stream(FJ2RCommand.VALUES)
                .anyMatch(cmd -> cmd(cmd).equalsIgnoreCase(args[0]));

        // cmd help
        if (!isContainsCommand || args[0].equalsIgnoreCase(cmd(FJ2RCommand.HELP))) {
            if (!Permissions.hasPermission(sender, cmd(FJ2RCommand.HELP))) {
                throw new UnauthorizedException();
            }
            MessageUtil.sendMessage(sender, help);
            return true;
        }

        // cmd reload
        if (isCmd(args, sender, FJ2RCommand.RELOAD)) {
            configManager.reloadConfig(sender);
            return true;
        }

        // cmd check update
        if (isCmd(args, sender, FJ2RCommand.CHECK_UPDATE)) {
            versionManager.checkUpdate(sender);
            return true;
        }


        // cmd set
        if (isCmd(args, sender, FJ2RCommand.SET)) {

            // if args is empty should send how to use this cmd
            if (args.length == 1) {
                MessageUtil.sendMessage(sender, getHelpLine(7));
                return true;
            }

            if (!(sender instanceof Player player)) {
                MessageUtil.sendMessage(sender, configManager.message().getCmdPlayerOnly());
                return true;
            }

            val item = getItemInHand(player);
            if (item == null) {
                MessageUtil.sendMessage(sender, configManager.message().getNoItemInMainHand());
                return true;
            }

            // TODO implement new rewrite
            ItemUtil.setItemAsJetpack(sender, item, args[1], args.length == 3 ? Long.parseLong(args[2]) : 0)
                    .onSuccess(result -> {
                        if (Version.getServerVersion() > 11)
                            player.getInventory().setItemInMainHand(result);
                        else
                            //noinspection deprecation
                            player.setItemInHand(result);
                        MessageUtil.sendMessage(sender, configManager.message().getCmdSet()
                                .replace(Placeholder.JETPACK, args[1])
                                .replace(Placeholder.FUEL_VALUE, String.valueOf(args.length == 3 ? args[2] : 0))
                        );
                    }).throwIfError();

            // new implement
            val jetpackId = args[1];
            val fuelValue = args.length == 3 ? Utils.toLong(args[2], 0) : 0;

            return true;
        }


        return true;
    }

    private boolean isCmd(String[] args, @NotNull CommandSender sender, @NotNull FJ2RCommand cmd) {
        val command = cmd(cmd);
        if (args[0].equalsIgnoreCase(command)) {
            if (!Permissions.hasPermission(sender, command)) {
                throw new UnauthorizedException();
            }
            return true;
        }
        return false;
    }


    @SuppressWarnings("deprecation")
    private @Nullable ItemStack getItemInHand(@NotNull Player player) {
        val item = versionManager.serverVersion() > 8 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        val meta = item.getItemMeta();
        if (meta == null || item.getType() == Material.AIR) {
            MessageUtil.sendMessage(player, "&cYou not holding any item in hand.");
            return null;
        }
        if (ItemMetaData.isNotItemArmor(item)) {
            MessageUtil.sendMessage(player, "&cThis item is not armor item!");
            return null;
        }
        return item;
    }

}
