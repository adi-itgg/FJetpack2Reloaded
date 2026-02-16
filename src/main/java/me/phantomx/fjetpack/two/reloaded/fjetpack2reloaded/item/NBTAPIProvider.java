package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item;

import de.tr7zw.nbtapi.NBT;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.ItemDataKey;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class NBTAPIProvider implements ItemDataProvider {

    @Override
    public boolean isSupported() {
        return Try.of(() -> Class.forName("de.tr7zw.nbtapi.NBT")).isSuccess();
    }

    @Override
    public void setString(ItemStack item, ItemDataKey key, String value) {
        NBT.modify(item, nbt -> { nbt.setString(key.key(), value); });
    }

    @Override
    public String getString(ItemStack item, ItemDataKey key, String defaultValue) {
        return NBT.get(item, nbt -> nbt.hasTag(key.key()) ? nbt.getString(key.key()) : defaultValue);
    }

    @Override
    public void setLong(ItemStack item, ItemDataKey key, Long value) {
        NBT.modify(item, nbt -> { nbt.setLong(key.key(), value); });
    }

    @Override
    public Long getLong(ItemStack item, ItemDataKey key, Long defaultValue) {
        return NBT.get(item, nbt -> nbt.hasTag(key.key()) ? nbt.getLong(key.key()) : defaultValue);
    }

}
