package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackGrant;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.UpdateChecker;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Version;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

/**
 * Translates /fj2r arguments into use case calls. Argument scanning is order-insensitive on purpose:
 * {@code /fj2r give vip 64 Player} and {@code /fj2r give 64 Player vip} are both valid.
 */
public class FJ2RCommandExecutor {

    private final JavaPlugin plugin;
    private final YamlConfigRepository configs;
    private final Messages messages;
    private final ItemFactory items;
    private final JetpackGrant grant;
    private final UpdateChecker updateChecker;
    private final Log log;

    @Getter(lazy = true)
    private final String helpText = initializeHelp();

    public FJ2RCommandExecutor(@NotNull JavaPlugin plugin,
                               @NotNull YamlConfigRepository configs,
                               @NotNull Messages messages,
                               @NotNull ItemFactory items,
                               @NotNull JetpackGrant grant,
                               @NotNull UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.items = items;
        this.grant = grant;
        this.updateChecker = updateChecker;
        this.log = new Log(getClass(), messages);
    }

    @SneakyThrows
    private @NotNull String initializeHelp() {
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

    private String getHelpLine(int line) {
        return getHelpText().split("\n")[line - 1];
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        log.debug("onCommand: Sender=%s, Command=%s, label=%s, args=%s", sender.getName(), command.getName(), label, args);

        var isContainsCommand = false;
        if (args.length > 0)
            for (FJ2RCommand cmd : FJ2RCommand.values()) {
                isContainsCommand = cmd.literal().equalsIgnoreCase(args[0]);
                if (isContainsCommand) break;
            }

        // cmd help
        if (!isContainsCommand || args[0].equalsIgnoreCase(FJ2RCommand.HELP.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.HELP.literal()))
                NoPermissionLvlException.send();
            messages.sendMessage(false, sender, getHelpText());
            return true;
        }

        // cmd reload
        if (args[0].equalsIgnoreCase(FJ2RCommand.RELOAD.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.RELOAD.literal()))
                NoPermissionLvlException.send();
            configs.reload(sender);
            return true;
        }

        // cmd check update
        if (args[0].equalsIgnoreCase(FJ2RCommand.CHECK_UPDATE.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.CHECK_UPDATE.literal()))
                NoPermissionLvlException.send();
            updateChecker.check(sender);
            return true;
        }

        // cmd set
        if (args[0].equalsIgnoreCase(FJ2RCommand.SET.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.SET.literal()))
                NoPermissionLvlException.send();

            // if args is empty should send how to use this cmd
            if (args.length == 1) {
                messages.sendMessage(sender, getHelpLine(7));
                return true;
            }

            if (!(sender instanceof Player player)) {
                messages.sendMessage(configs.message().getCmdPlayerOnly());
                return true;
            }

            val item = items.itemInHand(player);
            if (item == null) {
                messages.sendMessage(configs.message().getNoItemInMainHand());
                return true;
            }

            items.setItemAsJetpack(sender, item, args[1], args.length == 3 ? Long.parseLong(args[2]) : 0)
               .onSuccess(result -> {
                   if (Version.getServerVersion() > 11)
                       player.getInventory().setItemInMainHand(result);
                   else
                       //noinspection deprecation
                       player.setItemInHand(result);
                   messages.sendMessage(sender, configs.message().getCmdSet()
                           .replace(Placeholder.JETPACK, args[1])
                           .replace(Placeholder.FUEL_VALUE, String.valueOf(args.length == 3 ? args[2] : 0))
                   );
               }).throwIfError();

            return true;
        }

        // cmd get/give
        if (args[0].equalsIgnoreCase(FJ2RCommand.GET.literal()) || args[0].equalsIgnoreCase(FJ2RCommand.GIVE.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.GET.literal()) && !Permissions.hasPermission(sender, FJ2RCommand.GIVE.literal()))
                NoPermissionLvlException.send();

            if (args.length == 1) {
                messages.sendMessage(sender, getHelpLine(5));
                return true;
            }

            val jetpackId = Arrays.stream(args)
                    .filter(arg -> configs.jetpacks().get(arg) != null)
                    .findFirst()
                    .orElse(null);

            if (jetpackId == null) {
                messages.sendMessage(sender, "&cJetpack didn't exist");
                return true;
            }

            val targetPlayer = targetOf(sender, args);
            if (targetPlayer == null) return true;

            val fuelAmount = Arrays.stream(args)
                    .filter(NumberUtils::isDigits)
                    .map(NumberUtils::toInt)
                    .findFirst().orElse(0);

            grant.giveJetpack(sender, targetPlayer, jetpackId, fuelAmount);
            return true;
        }

        // cmd setfuel
        if (args[0].equalsIgnoreCase(FJ2RCommand.SET_FUEL.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.SET_FUEL.literal()))
                NoPermissionLvlException.send();

            // if args is empty should send how to use this cmd
            if (args.length == 1) {
                messages.sendMessage(sender, getHelpLine(8));
                return true;
            }

            if (!(sender instanceof Player player)) {
                messages.sendMessage(configs.message().getCmdPlayerOnly());
                return true;
            }

            val item = items.itemInHand(player);
            if (item == null) {
                messages.sendMessage(configs.message().getNoItemInMainHand());
                return true;
            }

            val fuelAmount = NumberUtils.toLong(args[1], 0);

            val jetpackId = items.jetpackIdOf(item);
            if (jetpackId.isEmpty()) {
                messages.sendMessage(sender, configs.message().getNotJetpackItem());
                return true;
            }

            items.setItemAsJetpack(sender, item, jetpackId, fuelAmount)
            .onSuccess(result -> {
                if (Version.getServerVersion() > 11)
                    player.getInventory().setItemInMainHand(result);
                else
                    player.setItemInHand(result);
                messages.sendMessage(sender, configs.message().getCmdFuelSet()
                        .replace(Placeholder.AMOUNT, String.valueOf(fuelAmount))
                );
            }).throwIfError();

            return true;
        }

        // cmd get/give fuel
        if (args[0].equalsIgnoreCase(FJ2RCommand.GET_FUEL.literal()) || args[0].equalsIgnoreCase(FJ2RCommand.GIVE_FUEL.literal())) {
            if (!Permissions.hasPermission(sender, FJ2RCommand.GET_FUEL.literal()) && !Permissions.hasPermission(sender, FJ2RCommand.GIVE_FUEL.literal()))
                NoPermissionLvlException.send();

            if (args.length == 1) {
                messages.sendMessage(sender, getHelpLine(6));
                return true;
            }

            val customFuelId = Arrays.stream(args)
                    .filter(arg -> configs.customFuels().get(arg) != null)
                    .findFirst()
                    .orElse(null);

            if (customFuelId == null) {
                messages.sendMessage(sender, "&cInvalid custom fuel id!");
                return true;
            }

            val targetPlayer = targetOf(sender, args);
            if (targetPlayer == null) return true;

            val amount = Arrays.stream(args)
                    .filter(NumberUtils::isDigits)
                    .map(NumberUtils::toInt)
                    .findFirst().orElse(1);

            grant.giveCustomFuel(sender, targetPlayer, customFuelId, amount);
            return true;
        }

        return true;
    }

    /** First online player named in the arguments, falling back to the sender when it is a player. */
    private @Nullable Player targetOf(@NotNull CommandSender sender, @NotNull String[] args) {
        return Arrays.stream(args)
                .map(Bukkit::getPlayerExact)
                .filter(Objects::nonNull)
                .findFirst().orElseGet(() -> {
                    if (!(sender instanceof Player)) {
                        messages.sendMessage(configs.message().getCmdPlayerOnly());
                        return null;
                    }
                    return (Player) sender;
                });
    }

}
