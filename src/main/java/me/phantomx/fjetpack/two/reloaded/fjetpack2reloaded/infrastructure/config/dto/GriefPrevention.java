package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.SectionPath;

@Data
@NoArgsConstructor
@SectionPath("GriefPrevention")
public class GriefPrevention {

    private boolean enable;
    private boolean onlyAllowInsideOwnClaim;
    private boolean allowBypassClaim;
    private boolean allowInsideAllClaim;

}
