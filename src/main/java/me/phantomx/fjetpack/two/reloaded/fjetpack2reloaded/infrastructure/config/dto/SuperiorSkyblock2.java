package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.SectionPath;

@Data
@NoArgsConstructor
@SectionPath("SuperiorSkyblock2")
public class SuperiorSkyblock2 {

    private boolean enable;
    private boolean allowBypassFlag;
    private boolean allowBypassPrivilege;

}
