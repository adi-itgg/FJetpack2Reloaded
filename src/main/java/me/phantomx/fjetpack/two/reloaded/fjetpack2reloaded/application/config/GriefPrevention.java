package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@SectionPath("GriefPrevention")
public class GriefPrevention {

    private boolean enable;
    private boolean onlyAllowInsideOwnClaim;
    private boolean allowBypassClaim;
    private boolean allowInsideAllClaim;

}
