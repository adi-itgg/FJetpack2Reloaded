package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@SectionPath("Fuel")
public class Fuel {

    private @Nullable CustomFuel customFuel;
    private @NonNull Material item;
    private int warnRunOutBelow = 5;
    private int cost = 1;
    private int sprintCost = 1;
    private int burnRate = 5;
    private int customModelData = -1;
    private boolean allowBypassCost;
    private boolean allowBypassSprintCost;


}
