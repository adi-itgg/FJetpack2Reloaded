package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
public class Pair<F, S> {

    @Setter(AccessLevel.NONE)
    private F first;
    @Setter(AccessLevel.NONE)
    private S second;

}
