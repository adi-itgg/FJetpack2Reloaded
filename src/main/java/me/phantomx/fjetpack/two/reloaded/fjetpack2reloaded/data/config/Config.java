package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Config {

    private @NonNull Integer version;
    private boolean updateNotification;

}
