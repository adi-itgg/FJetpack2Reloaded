package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class OnPlayerCrouchEvent {

    private final PlayerRegistry players;
    private final ItemFactory items;
    private final Log log;

    public OnPlayerCrouchEvent(@NotNull PlayerRegistry players, @NotNull ItemFactory items,
                               @NotNull MessageSink messages) {
        this.players = players;
        this.items = items;
        this.log = new Log(getClass(), messages);
    }

    public void onToggleSneak(@NotNull PlayerToggleSneakEvent e) {
        val player = e.getPlayer();
        log.debug("crouch: player=%s sneaking=%s onGround=%s cancelled=%s",
                player.getName(), e.isSneaking(), ((LivingEntity) player).isOnGround(), e.isCancelled());
        if (e.isSneaking() || !((LivingEntity) player).isOnGround()) {
            log.debug("crouch: ignored - sneak must end while standing on ground");
            return;
        }

        val ac = players.of(player);
        log.debug("crouch: player=%s session active=%s jetpack=%s uniqueId=%d",
                player.getName(), ac.isActive(), ac.getJetpack() == null ? "null" : ac.getJetpack().getId(), ac.getJetpackUniqueId());
        if (ac.isActive()) {
            log.debug("crouch: player=%s already active -> turnOff", player.getName());
            ac.turnOff(true);
            return;
        }

        val offHandItem = player.getInventory().getItemInOffHand();
        val jetpack = items.jetpackOf(offHandItem);
        log.debug("crouch: player=%s offhand=%s jetpack=%s offhandOnly=%s",
                player.getName(), Log.item(offHandItem), jetpack == null ? "null" : jetpack.getId(),
                jetpack != null && jetpack.isRunInOffHandOnly());
        if (jetpack != null && jetpack.isRunInOffHandOnly()) {
            val check = ac.isJetpack(offHandItem, false, true);
            log.debug("crouch: offhand check=%s", check);
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.throwNow(
                    jetpack.getPermission().getName(),
                    "turning the offhand jetpack on");
            if (check == FlightCheck.ALLOWED) ac.turnOn();
            return;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        val armor = equipment.getArmorContents();
        log.debug("crouch: player=%s scanning %d armor slots", player.getName(), armor.length);
        for (int i = 0; i < armor.length; i++) {
            ItemStack slot = armor[i];
            val check = ac.isJetpack(slot, false, true);
            log.debug("crouch: player=%s slot=%d item=%s check=%s selected=%s",
                    player.getName(), i, Log.item(slot), check, ac.getJetpack() == null ? "null" : ac.getJetpack().getId());
            if (check == FlightCheck.NO_PERMISSION) {
                val refused = items.jetpackOf(slot);
                NoPermissionLvlException.throwNow(
                        refused == null ? null : refused.getPermission().getName(),
                        "turning the jetpack in armor slot " + i + " on");
            }
            if (check == FlightCheck.BLOCKED) return;
            if (check != FlightCheck.ALLOWED) continue;
            if (ac.getJetpack() == null) continue;
            if (ac.getJetpack().isRunInOffHandOnly()) return;
            ac.turnOn();
            return;
        }
        log.debug("crouch: player=%s no jetpack accepted in armor", player.getName());
    }

}
