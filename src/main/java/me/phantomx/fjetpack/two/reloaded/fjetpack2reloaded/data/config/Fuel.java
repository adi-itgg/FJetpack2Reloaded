package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@SectionPath("Fuel")
public class Fuel {

    private @Nullable CustomFuel customFuel;
    private @NonNull Material item;
    private int warnRunOutBelow;
    private int cost;
    private int sprintCost;
    private int burnRate;
    private int customModelData;
    private boolean allowBypassCost;
    private boolean allowBypassSprintCost;


}
