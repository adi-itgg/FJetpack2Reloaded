package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Message {

    private @NonNull String prefix;
    private @NonNull String turnOn;
    private @NonNull String turnOff;
    private @NonNull String noPermission;
    private @NonNull String detached;
    private @NonNull String outOfFuel;
    private @NonNull String blockedWorlds;
    private @NonNull String noFuel;
    private @NonNull String warnRunOutBelow;
    private @NonNull String onEmptyFuelDropped;
    private @NonNull String onEmptyFuelRemoved;
    private @NonNull String onDeathDropped;
    private @NonNull String onDeathRemoved;
    private @NonNull String griefPreventionOutsideClaim;
    private @NonNull String griefPreventionTurnedOffOutsideClaim;
    private @NonNull String griefPreventionOutsideOwnClaim;
    private @NonNull String cmdGetSelf;
    private @NonNull String cmdGiveReceived;
    private @NonNull String cmdGiveSuccess;
    private @NonNull String cmdSet;
    private @NonNull String cmdReload;
    private @NonNull String cmdFuelSet;
    private @NonNull String cmdFuelGet;
    private @NonNull String cmdFuelGiveSuccess;
    private @NonNull String cmdFuelGiveReceived;
    private @NonNull String noItemInMainHand;
    private @NonNull String cmdPlayerOnly;
    private @NonNull String inventoryFull;
    private @NonNull String notJetpackItem;
    private @NonNull String invalidNumber;
    private @NonNull String noUpdate;
    private @NonNull String usingDevVersion;
    private @NonNull String foundUpdate;
    private @NonNull String superiorSkyblock2NoFlag;
    private @NonNull String superiorSkyblock2NoPermission;
    private @NonNull String superiorSkyblock2OutsideIsland;

}
