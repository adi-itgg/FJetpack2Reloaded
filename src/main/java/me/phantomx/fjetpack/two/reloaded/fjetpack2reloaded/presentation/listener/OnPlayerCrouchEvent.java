package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnPlayerCrouchEvent {

    private final PlayerRegistry players;
    private final ItemFactory items;

    public OnPlayerCrouchEvent(@NotNull PlayerRegistry players, @NotNull ItemFactory items) {
        this.players = players;
        this.items = items;
    }

    public void onToggleSneak(@NotNull PlayerToggleSneakEvent e) {
        val player = e.getPlayer();
        if (e.isSneaking() || !((LivingEntity) player).isOnGround()) return;

        val ac = players.of(player);
        if (ac.isActive()) {
            ac.turnOff(true);
            return;
        }

        val offHandItem = player.getInventory().getItemInOffHand();
        val jetpack = items.jetpackOf(offHandItem);
        if (jetpack != null && jetpack.isRunInOffHandOnly()) {
            val check = ac.isJetpack(offHandItem, false, true);
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.throwNow();
            if (check == FlightCheck.ALLOWED) ac.turnOn();
            return;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        for (@Nullable ItemStack armor : equipment.getArmorContents()) {
            val check = ac.isJetpack(armor, false, true);
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.throwNow();
            if (check == FlightCheck.BLOCKED) return;
            if (check != FlightCheck.ALLOWED) continue;
            if (ac.getJetpack() == null) continue;
            if (ac.getJetpack().isRunInOffHandOnly()) return;
            ac.turnOn();
            return;
        }

    }

}
