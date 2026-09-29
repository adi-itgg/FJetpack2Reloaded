package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@SectionPath("Particle")
public class ParticleEffect {

    private boolean enable;
    private @NonNull String effect;
    private int amount;
    private long delay;

}
