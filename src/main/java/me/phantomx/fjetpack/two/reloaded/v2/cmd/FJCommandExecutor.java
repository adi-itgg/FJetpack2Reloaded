package me.phantomx.fjetpack.two.reloaded.v2.cmd;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.v2.config.ConfigManager;
import me.phantomx.fjetpack.two.reloaded.v2.exception.UnauthorizedException;
import me.phantomx.fjetpack.two.reloaded.v2.item.ItemMeta;
import me.phantomx.fjetpack.two.reloaded.v2.util.MessageUtil;
import me.phantomx.fjetpack.two.reloaded.v2.util.Utils;
import me.phantomx.fjetpack.two.reloaded.v2.util.VersionManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;

public class FJCommandExecutor {

    private final ConfigManager configManager;
    private final VersionManager versionManager;
    private final ItemMeta itemMeta;
    private final Logger logger;

    private final String help;

    @SneakyThrows
    public FJCommandExecutor(Plugin plugin, ConfigManager configManager, VersionManager versionManager, ItemMeta itemMeta, Logger logger) {
        this.configManager = configManager;
        this.versionManager = versionManager;
        this.itemMeta = itemMeta;
        this.logger = logger;

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

            val jetpackId = args[1];
            val fuelValue = args.length == 3 ? Utils.toLong(args[2], 0) : 0;
            val jetpackItemStack = asJetpackItem(sender, item, jetpackId, fuelValue);

            if (versionManager.serverVersion() > 11) {
                player.getInventory().setItemInMainHand(jetpackItemStack);
            } else {
                //noinspection deprecation
                player.setItemInHand(jetpackItemStack);
            }

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

    private ItemStack asJetpackItem(CommandSender sender, @NotNull ItemStack item, String jetpackId, long fuelValue) {
        val jetpack = configManager.jetpacks().get(jetpackId);
        if (jetpack == null) {
            MessageUtil.sendMessage(sender, "&cThere is no such jetpack: " + jetpackId);
            return null;
        }
        val customFuel = jetpack.getFuel().getCustomFuel();
        var fuelDisplay = jetpack.getFuel().getItem().name().replace("_", " ");
        if (customFuel != null) {
            fuelDisplay = customFuel.getCustomDisplay().isEmpty() ? customFuel.getDisplayName() : customFuel.getCustomDisplay();
        } else {
            fuelDisplay = Utils.capitalize(fuelDisplay.toLowerCase());
        }
        val meta = item.getItemMeta();
        if (meta == null) {
            MessageUtil.sendMessage(sender, "&cInvalid item");
            return null;
        }
        if (jetpack.getCustomModelData() != -1) {
            meta.setCustomModelData(jetpack.getCustomModelData());
        }
        String finalFuelDisplay = fuelDisplay;

        // display name
        meta.setDisplayName(jetpack.getDisplayName());
        // lore
        meta.setLore(jetpack.getLore().stream()
                .map(v -> v.replace(Placeholder.FUEL, finalFuelDisplay).replace(Placeholder.FUEL_VALUE, String.valueOf(fuelValue))).toList());
        // flags
        for (String flag : jetpack.getFlags()) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase().trim()));
            } catch (Throwable e) {
                logger.warning("Invalid flag: " + flag);
                MessageUtil.sendMessage(sender, "&cInvalid flag " + flag);
            }
        }
        // unbreakable
        if (versionManager.serverVersion() > 16) {
            meta.setUnbreakable(jetpack.isUnbreakable());
        }

        // update item meta
        item.setItemMeta(meta);

        var jetpackItemStack = itemMeta.setJetpack(item, jetpackId);
        jetpackItemStack = itemMeta.setFuelValue(item, fuelValue);

        // enchantments
        for (String enchant : jetpack.getEnchantments()) {
            try {
                val enchantName = enchant.split(":")[0];
                val enchantLvl = Integer.parseInt(enchant.split(":")[1]);
                @SuppressWarnings("deprecation")
                val enchantment = versionManager.serverVersion() > 16 ?
                        Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase())) :
                        Enchantment.getByName(enchantName.toUpperCase());
                // enchantment not found
                if (enchantment == null) {
                    logger.warning("Invalid enchantment: " + enchant);
                    MessageUtil.sendMessage(sender, "&cInvalid enchantment " + enchant);
                    continue;
                }
                jetpackItemStack.addUnsafeEnchantment(enchantment, enchantLvl);
            } catch (Throwable e) {
                logger.warning("Invalid enchantment: " + enchant);
                MessageUtil.sendMessage(sender, "&cInvalid enchantment " + enchant);
            }
        }

        return jetpackItemStack;
    }

}
