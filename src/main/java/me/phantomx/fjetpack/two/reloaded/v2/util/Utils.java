package me.phantomx.fjetpack.two.reloaded.v2.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Utils {

    public static int generateRandomId() {
        return (int) (Math.random() * 1000000);
    }

    public static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

}
