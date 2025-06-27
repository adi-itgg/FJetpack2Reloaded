package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.permissions.Permission;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomFuel {

    private @NonNull String id;
    private @NonNull String customDisplay;
    private @NonNull String displayName;
    private @NonNull List<String> lore;
    private @NonNull Material item;
    private @NonNull Permission permission;
    private boolean glowing;
    private int customModelData;

    @Override
    public String toString() {
        return "CustomFuel{" +
                "id='" + id + '\'' +
                ", customDisplay='" + customDisplay + '\'' +
                ", displayName='" + displayName + '\'' +
                ", lore=" + lore +
                ", item=" + item +
                ", permission=" + permission.getName() +
                ", glowing=" + glowing +
                ", customModelData=" + customModelData +
                '}';
    }
}
