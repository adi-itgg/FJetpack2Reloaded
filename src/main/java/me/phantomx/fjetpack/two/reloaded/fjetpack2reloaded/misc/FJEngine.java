package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.AccessDeniedLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.FJPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

@AssistFactory(FJEngine.Factory.class)
public class FJEngine implements Consumer<BukkitTask> {

    private final Factory.Params params;
    private final JetpackItemFactory itemFactory;

    public FJEngine(@Assisted Factory.Params params, JetpackItemFactory itemFactory) {
        this.params = params;
        this.itemFactory = itemFactory;
    }

    public interface Factory {

        FJEngine create(Params params);

        @Data
        @Accessors(fluent = true)
        class Params {

            private FJPlayer fjPlayer;
            private Jetpack jetpack;
            private ItemStack itemStack;
            private EquipmentSlot equipmentSlot;

        }

    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        var fuelBurn = params.fjPlayer.fuelBurn();
        if (!fuelBurn.active()) {
            var task = fuelBurn.task();
            if (task != null) {
                task.cancel();
                fuelBurn.task(null);
            }
            return;
        }
        var player = params.fjPlayer.player().get();
        if (player == null) { // maybe disconnected?
            // TODO cleanup like non active
            return;
        }

        // check player permission
        if (!Permissions.hasRawPermission(player, params.jetpack.getPermission())) {
            throw new AccessDeniedLevelException(params.jetpack.getDisplayName());
        }

        if (((LivingEntity) player).isOnGround() && !player.isFlying()) {
            // maybe add counting to prevent abuse (optional)
            return; // player is not flying so don't burn fuel
        }

        var jpItem = player.getInventory().getItem(params.equipmentSlot);
        if (jpItem == null) {
            // turn off and send msg detached
            //  TODO turn off
            return;
        }
        var jetpack = itemFactory.getJetpack(jpItem);
        if (jetpack == null) {
            // turn off and send msg detached
            //  TODO turn off
            return;
        }
        if (jetpack.getId().equals(params.jetpack.getId())) {
            // turn off and send msg detached
            //  TODO turn off
            return;
        }

        // check world is blocked
        var currentPlayerWorld = player.getWorld().getName();
        for (String blockedWorld : jetpack.getBlockedWorlds()) {
            if (currentPlayerWorld.equals(blockedWorld)) {
                // turn off and send msg blocked world
                // TODO turn off
                return;
            }
        }

        // TODO handle more

    }

}
