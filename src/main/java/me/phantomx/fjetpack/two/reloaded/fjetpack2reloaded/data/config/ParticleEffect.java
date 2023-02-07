package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath;

@Data
@NoArgsConstructor
@SectionPath("Particle")
public class ParticleEffect {

    private boolean enable;
    private @NonNull String effect;
    private int amount;
    private long delay;

}
