package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath;

@Data
@NoArgsConstructor
@SectionPath("ItemColor")
public class ItemColor {

    private int r;
    private int g;
    private int b;

}
