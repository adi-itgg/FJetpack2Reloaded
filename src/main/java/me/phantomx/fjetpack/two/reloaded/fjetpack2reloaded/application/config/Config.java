package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Config {

    private @NonNull Integer version;
    private boolean updateNotification;

}
