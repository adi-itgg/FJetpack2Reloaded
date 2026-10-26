package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackParticles;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.ItemDisposition;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelStop;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.ActionResultReturnHandler;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One player's flight: the state it is in, and the lifecycle calls the listeners make.
 * <p>
 * The class is a thin shell around {@link FlightState}. It delegates every decision to a collaborator
 * in this package - {@link JetpackAdmission} for "may this stack fly", {@link FuelCharge} for the cost
 * of a tick, {@link WornJetpack} for touching the armour, {@link FlightTicker} for the repeating
 * charge, {@link ItemDispositionApplier} for out-of-fuel handling - so reading it tells you the order
 * of the steps, not the arithmetic behind them.
 * <p>
 * Not a bean: a session is created per player by {@link PlayerRegistry}.
 */
public class FlightSession {

    private final FlightState state;
    private final PlayerRegistry registry;
    private final ConfigRepository configs;
    private final MessageSink messages;
    private final ProtectionGuard protection;
    private final ItemFactory items;

    private final ItemTags itemTags;
    private final JetpackAdmission admission;
    private final FuelCharge fuelCharge;
    private final WornJetpack wornJetpack;
    private final FlightTicker ticker;
    private final ItemDispositionApplier dispositions;

    public FlightSession(@NotNull Player player,
                         @NotNull PlayerRegistry registry,
                         @NotNull FlightComposition composition) {
        this.state = new FlightState(player);
        this.registry = registry;
        this.configs = composition.configs();
        this.messages = composition.messages();
        this.protection = composition.protection();
        this.items = composition.items();
        this.itemTags = composition.itemTags();

        this.admission = new JetpackAdmission(configs, messages, itemTags, protection);
        this.fuelCharge = new FuelCharge();
        this.wornJetpack = new WornJetpack(itemTags, composition.version());
        this.ticker = new FlightTicker(
                composition.plugin(),
                configs,
                messages,
                new JetpackParticles(composition.plugin(), composition.version(), messages),
                new Log(getClass(), messages)
        );
        this.dispositions = new ItemDispositionApplier(configs, messages);
    }

    // ---- state -------------------------------------------------------------------------------

    public @NotNull Player getPlayer() {
        return state.getPlayer();
    }

    public void setPlayer(@NotNull Player player) {
        state.setPlayer(player);
    }

    public boolean isActive() {
        return state.isActive();
    }

    public void setActive(boolean active) {
        state.setActive(active);
    }

    public @Nullable Jetpack getJetpack() {
        return state.getJetpack();
    }

    public void setJetpack(@Nullable Jetpack jetpack) {
        state.setJetpack(jetpack);
    }

    public @Nullable BukkitTask getBurnTask() {
        return state.getBurnTask();
    }

    public void setBurnTask(@Nullable BukkitTask task) {
        state.setBurnTask(task);
    }

    public boolean isStarting() {
        return state.isStarting();
    }

    public void setStarting(boolean starting) {
        state.setStarting(starting);
    }

    public int getJetpackUniqueId() {
        return state.getJetpackUniqueId();
    }

    // ---- protection --------------------------------------------------------------------------

    public boolean isAllowedGriefPrevention() {
        if (state.getJetpack() == null) return true;
        return protection.allowsGriefPrevention(getPlayer(), state.getJetpack());
    }

    public boolean isAllowedSuperiorSkyblock() {
        if (state.getJetpack() == null) return true;
        return protection.allowsSuperiorSkyblock(getPlayer(), state.getJetpack());
    }

    // ---- worn armour -------------------------------------------------------------------------

    @SneakyThrows
    public void updateActiveJetpackArmorEquipment(@NotNull ActionResultReturnHandler<@NotNull ItemStack, @Nullable ItemStack> action) {
        // the handler may throw Throwable; @SneakyThrows keeps that out of every caller's signature,
        // exactly as before the split
        wornJetpack.update(getPlayer(), state.getJetpack(), state.getJetpackUniqueId(), item -> {
            try {
                return action.action(item);
            } catch (Throwable error) {
                throw new IllegalStateException(error);
            }
        });
    }

    // ---- admission and fuel ------------------------------------------------------------------

    public @NotNull FlightCheck isJetpack(@Nullable ItemStack stack, boolean consumeFuel) {
        return isJetpack(stack, consumeFuel, false);
    }

    /**
     * Can the player fly with this item, and charge the fuel of one tick?
     *
     * @return the refusal reason, or {@link FlightCheck#ALLOWED}; side effects (fuel, item tags,
     * messages, worn-armor rewrite) only happen when allowed
     */
    public @NotNull FlightCheck isJetpack(@Nullable ItemStack stack, boolean consumeFuel, boolean updateCurrentJetpack) {
        val verdict = admission.judge(getPlayer(), stack, state.getJetpack());
        if (verdict.check() != FlightCheck.ALLOWED) return verdict.check();
        val jetpack = verdict.jetpack();
        assert jetpack != null;

        var fuel = itemTags.getFuelValue(stack);
        // item without a fuel tag: not a usable jetpack, never destroy it
        if (fuel == null) return FlightCheck.NOT_A_JETPACK;

        val settlement = fuelCharge.settle(getPlayer(), fuel, jetpack, getPlayer().isSprinting());

        if (settlement.stop() != FuelStop.NONE) {
            items.updateDisplayItem(jetpack, stack, settlement.fuel());
            onOutOfFuel();
            return FlightCheck.BLOCKED;
        }
        fuel = settlement.fuel();

        if (updateCurrentJetpack)
            setJetpack(jetpack);

        if (!consumeFuel) {
            state.setJetpackUniqueId(itemTags.newItemId());
            stack = itemTags.setActiveJetpack(stack, state.getJetpackUniqueId());
            @NotNull ItemStack finalArmor = stack;
            updateActiveJetpackArmorEquipment(item -> finalArmor);
            return FlightCheck.ALLOWED;
        }

        if (settlement.warn())
            messages.send(getPlayer(),
                    configs.message().getWarnRunOutBelow().replace(Placeholder.AMOUNT, String.valueOf(fuel))
            );

        items.updateDisplayItem(jetpack, stack, fuel);
        stack = itemTags.setFuelValue(stack, fuel);
        @NotNull ItemStack finalArmor = stack;
        updateActiveJetpackArmorEquipment(item -> finalArmor);
        return FlightCheck.ALLOWED;
    }

    /** Every branch of this also tells the player why; see {@link ItemDispositionApplier}. */
    private void onOutOfFuel() {
        messages.send(getPlayer(), configs.message().getOutOfFuel());
        val jetpack = state.getJetpack();
        if (jetpack == null || jetpack.getOnEmptyFuel() == ItemDisposition.NONE) return;
        if (Permissions.hasRawPermission(getPlayer(), jetpack.getPermission(Permissions.PERMISSION_KEEP_ON_EMPTY_SUFFIX)))
            return;
        // out-of-fuel disposition, per jetpack.yml OnEmptyFuel (was: OnDeath - fixed)
        updateActiveJetpackArmorEquipment(item -> dispositions.apply(
                getPlayer(),
                item,
                jetpack.getOnEmptyFuel(),
                dispositions.outOfFuelRemoved(),
                dispositions.outOfFuelDropped()
        ));
    }

    // ---- lifecycle ---------------------------------------------------------------------------

    /** require {@link #setJetpack(Jetpack)} first! */
    public void turnOn() {
        if (state.isStarting()) return;
        state.setStarting(true);

        assert state.getJetpack() != null;

        ticker.start(this, this::turnOff, () -> turnOff(true));
    }

    public void turnOffDetached() {
        messages.send(getPlayer(), getPlayer().isFlying()
                        ? configs.message().getDetached()
                        : configs.message().getTurnOff()
        );
        turnOff();
    }

    public void turnOff() {
        turnOff(false);
    }

    /**
     * @param sendMsg report the turn off to the player
     */
    public void turnOff(boolean sendMsg) {
        if (state.getJetpack() == null || !state.isActive()) return;
        releaseFlight();

        if (sendMsg)
            messages.send(getPlayer(), configs.message().getTurnOff());
        state.setJetpack(null);
    }

    /** Plugin unload: the jetpack stays equipped on the player so it survives the restart. */
    public void turnOffUnloaded() {
        if (state.getJetpack() == null || !state.isActive()) return;
        releaseFlight();
        messages.send(getPlayer(), "&cThis plugin has been unloaded!");
    }

    private void releaseFlight() {
        state.setStarting(false);
        state.setActive(false);
        getPlayer().setAllowFlight(false);

        ticker.stop(state.getBurnTask());
        state.setBurnTask(null);
        registry.remove(getPlayer());
    }

}
