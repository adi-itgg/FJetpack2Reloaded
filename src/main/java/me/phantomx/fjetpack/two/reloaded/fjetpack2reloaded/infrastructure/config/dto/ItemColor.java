package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.SectionPath;

@Data
@NoArgsConstructor
@SectionPath("ItemColor")
public class ItemColor {

    private int r;
    private int g;
    private int b;

}
