package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public class Permissions {

    public static final String PERMISSION_PREFIX = "fjetpack2reloaded.";

    public static final String PERMISSION_KEEP_ON_EMPTY_SUFFIX = ".keep.on.empty";
    public static final String PERMISSION_KEEP_ON_DEATH_SUFFIX = ".keep.on.death";
    public static final String PERMISSION_REFILL_FUEL_SUFFIX = ".fuel.refill";
    public static final String PERMISSION_BYPASS_FUEL_SUFFIX = ".bypass.fuel";
    public static final String PERMISSION_BYPASS_FUEL_SPRINT_SUFFIX = ".bypass.fuel.sprint";

    public static final String PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG = ".bypass.ss2.flag";
    public static final String PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE = ".bypass.ss2.privilege";
    public static final String PERMISSION_BYPASS_GRIEF_PREVENTION_CLAIM = ".bypass.gp.claim";

    public static boolean isAdminOrOp(@NotNull CommandSender sender) {
        return sender.hasPermission(PERMISSION_PREFIX + "*") || sender.isOp();
    }

    public static boolean hasPermission(@NotNull CommandSender sender, String perm) {
        return hasRawPermission(sender, PERMISSION_PREFIX + perm);
    }

    public static boolean hasRawPermission(@NotNull CommandSender sender, @NotNull Permission permission) {
        return hasRawPermission(sender, permission.getName());
    }
    public static boolean hasRawPermission(@NotNull CommandSender sender, String permission) {
        return isAdminOrOp(sender) || !((sender instanceof Player)) || sender.hasPermission(permission);
    }

}
