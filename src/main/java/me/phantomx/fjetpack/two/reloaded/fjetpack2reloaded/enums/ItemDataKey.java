package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum ItemDataKey {

    JETPACK_ID("FJetpack2Reloaded"),
    FUEL_VALUE_ID("FJ2RFuel"),
    CUSTOM_FUEL_ID("FJ2RCF"),
    PARTICLE_ID("FJ2RParticle"),
    ACTIVE_JETPACK_ID("FJ2RActive"),
    JETPACK_RUN_PLUGIN_ID("FJ2RRunPlugin"),

    ENCHANTMENT("ench")
    ;

    private final String key;

    ItemDataKey(String key) {
        this.key = key;
    }

}
