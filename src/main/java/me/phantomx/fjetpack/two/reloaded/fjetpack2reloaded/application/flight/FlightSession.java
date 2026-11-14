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
    private final Log log;
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
        this.log = new Log(getClass(), messages);
        log.debug("session created: player=%s serverVersion=%d api=%s",
                player.getName(), composition.version().getServerVersion(), composition.version().getNmsApiVersion());

        this.admission = new JetpackAdmission(configs, messages, itemTags, protection);
        this.fuelCharge = new FuelCharge();
        this.wornJetpack = new WornJetpack(itemTags, composition.version(), messages);
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
        updateJetpackArmorEquipment(null, action);
    }

    /**
     * Rewrites the equipped jetpack; {@code target} lets an activation replace the untagged stack it
     * just read (legacy NMS writes return a new ItemStack).
     */
    @SneakyThrows
    void updateJetpackArmorEquipment(@Nullable ItemStack target, @NotNull ActionResultReturnHandler<@NotNull ItemStack, @Nullable ItemStack> action) {
        // the handler may throw Throwable; @SneakyThrows keeps that out of every caller's signature,
        // exactly as before the split
        wornJetpack.update(getPlayer(), state.getJetpack(), state.getJetpackUniqueId(), target, item -> {
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
        log.debug("isJetpack: player=%s stack=%s consumeFuel=%s updateCurrent=%s current=%s",
                getPlayer().getName(), Log.item(stack), consumeFuel, updateCurrentJetpack,
                state.getJetpack() == null ? "null" : state.getJetpack().getId());
        val verdict = admission.judge(getPlayer(), stack, state.getJetpack());
        log.debug("isJetpack: admission=%s jetpack=%s", verdict.check(),
                verdict.jetpack() == null ? "null" : verdict.jetpack().getId());
        if (verdict.check() != FlightCheck.ALLOWED) return verdict.check();
        val jetpack = verdict.jetpack();
        assert jetpack != null;

        var fuel = itemTags.getFuelValue(stack);
        // item without a fuel tag: not a usable jetpack, never destroy it
        if (fuel == null) {
            log.debug("isJetpack: item=%s has no fuel tag -> NOT_A_JETPACK", Log.item(stack));
            return FlightCheck.NOT_A_JETPACK;
        }
        log.debug("isJetpack: player=%s fuelBefore=%d cost=%d sprintCost=%d sprinting=%s",
                getPlayer().getName(), fuel, jetpack.getFuel().getCost(), jetpack.getFuel().getSprintCost(),
                getPlayer().isSprinting());

        val settlement = fuelCharge.settle(getPlayer(), fuel, jetpack, getPlayer().isSprinting());
        log.debug("isJetpack: settlement stop=%s fuel=%d warn=%s", settlement.stop(), settlement.fuel(), settlement.warn());

        if (settlement.stop() != FuelStop.NONE) {
            log.debug("isJetpack: out of fuel -> BLOCKED");
            items.updateDisplayItem(jetpack, stack, settlement.fuel());
            onOutOfFuel();
            return FlightCheck.BLOCKED;
        }
        fuel = settlement.fuel();

        if (updateCurrentJetpack) {
            setJetpack(jetpack);
            log.debug("isJetpack: adopted jetpack=%s", jetpack.getId());
        }

        if (!consumeFuel) {
            state.setJetpackUniqueId(itemTags.newItemId());
            log.debug("isJetpack: activation uniqueId=%d runIdWrite", state.getJetpackUniqueId());
            val original = stack;
            stack = itemTags.setActiveJetpack(stack, state.getJetpackUniqueId());
            @NotNull ItemStack finalArmor = stack;
            updateJetpackArmorEquipment(original, item -> finalArmor);
            log.debug("isJetpack: activation wrote back original=%s result=%s", Log.item(original), Log.item(stack));
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
        log.debug("isJetpack: tick done player=%s fuelAfter=%d item=%s", getPlayer().getName(), fuel, Log.item(stack));
        return FlightCheck.ALLOWED;
    }

    /** Every branch of this also tells the player why; see {@link ItemDispositionApplier}. */
    private void onOutOfFuel() {
        log.debug("onOutOfFuel: player=%s jetpack=%s", getPlayer().getName(),
                state.getJetpack() == null ? "null" : state.getJetpack().getId());
        messages.send(getPlayer(), configs.message().getOutOfFuel());
        val jetpack = state.getJetpack();
        if (jetpack == null || jetpack.getOnEmptyFuel() == ItemDisposition.NONE) {
            log.debug("onOutOfFuel: nothing to apply (jetpack=%s disposition=%s)",
                    jetpack == null ? "null" : jetpack.getId(),
                    jetpack == null ? "null" : jetpack.getOnEmptyFuel());
            return;
        }
        if (Permissions.hasRawPermission(getPlayer(), jetpack.getPermission(Permissions.PERMISSION_KEEP_ON_EMPTY_SUFFIX))) {
            log.debug("onOutOfFuel: keep-on-empty permission bypass, no disposition");
            return;
        }
        log.debug("onOutOfFuel: applying disposition=%s", jetpack.getOnEmptyFuel());
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
        val burnTask = state.getBurnTask();
        log.debug("turnOn: player=%s starting=%s active=%s jetpack=%s task=%s",
                getPlayer().getName(), state.isStarting(), state.isActive(),
                state.getJetpack() == null ? "null" : state.getJetpack().getId(),
                burnTask == null ? "null" : burnTask.getTaskId() + (burnTask.isCancelled() ? "/cancelled" : "/live"));
        if (state.isStarting()) {
            if (burnTask != null && !burnTask.isCancelled()) {
                log.debug("turnOn: already starting with a live task, ignored");
                return;
            }
            // A previous start never reached the ticker (scheduler failure, cancelled task, reload):
            // without this reset the session could never be started again.
            log.warn("turnOn: stale starting flag without a live task (task=%s) - resetting",
                    burnTask == null ? "null" : "cancelled");
            state.setStarting(false);
        }
        state.setStarting(true);

        assert state.getJetpack() != null;

        try {
            ticker.start(this, this::turnOff, () -> turnOff(true));
            log.debug("turnOn: ticker requested player=%s", getPlayer().getName());
        } catch (RuntimeException error) {
            state.setStarting(false);
            log.warn("turnOn FAILED: player=%s, starting flag reset: %s", getPlayer().getName(), error);
            throw error;
        }
    }

    public void turnOffDetached() {
        log.debug("turnOffDetached: player=%s flying=%s", getPlayer().getName(), getPlayer().isFlying());
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
        log.debug("turnOff: player=%s sendMsg=%s active=%s jetpack=%s",
                getPlayer().getName(), sendMsg, state.isActive(),
                state.getJetpack() == null ? "null" : state.getJetpack().getId());
        if (state.getJetpack() == null || !state.isActive()) {
            log.debug("turnOff: ignored (no active jetpack)");
            return;
        }
        releaseFlight();

        if (sendMsg)
            messages.send(getPlayer(), configs.message().getTurnOff());
        state.setJetpack(null);
        log.debug("turnOff: done player=%s", getPlayer().getName());
    }

    /** Plugin unload: the jetpack stays equipped on the player so it survives the restart. */
    public void turnOffUnloaded() {
        log.debug("turnOffUnloaded: player=%s active=%s", getPlayer().getName(), state.isActive());
        if (state.getJetpack() == null || !state.isActive()) return;
        releaseFlight();
        messages.send(getPlayer(), "&cThis plugin has been unloaded!");
    }

    private void releaseFlight() {
        log.debug("releaseFlight: player=%s starting=%s active=%s task=%s",
                getPlayer().getName(), state.isStarting(), state.isActive(), state.getBurnTask() == null ? "null" : "running");
        state.setStarting(false);
        state.setActive(false);
        getPlayer().setAllowFlight(false);

        ticker.stop(state.getBurnTask());
        state.setBurnTask(null);
        registry.remove(getPlayer());
        log.debug("releaseFlight: done player=%s allowFlight=false", getPlayer().getName());
    }

}
