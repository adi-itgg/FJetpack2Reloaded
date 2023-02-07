package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath;

@Data
@NoArgsConstructor
@SectionPath("GriefPrevention")
public class GriefPrevention {

    private boolean enable;
    private boolean onlyAllowInsideOwnClaim;
    private boolean allowBypassClaim;
    private boolean allowInsideAllClaim;

}
