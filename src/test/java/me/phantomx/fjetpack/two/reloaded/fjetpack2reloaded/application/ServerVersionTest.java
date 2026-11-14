package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Version strings only - no Bukkit server is needed.
 */
class ServerVersionTest {

    @Test
    void legacyVersionsKeepTheMinorNumber() {
        assertEquals(8, ServerVersion.parseServerVersion("1.8.8-R0.1-SNAPSHOT"), "1.8.8");
        assertEquals(19, ServerVersion.parseServerVersion("git-Paper-196 (MC: 1.19.3)"), "1.19.3");
        assertEquals(20, ServerVersion.parseServerVersion("git-Spigot-abc (MC: 1.20.4)"), "1.20.4");
    }

    @Test
    void newSchemeComparesAboveEveryLegacyGate() {
        assertEquals(2603, ServerVersion.parseServerVersion("26.3-R0.1-SNAPSHOT"), "bukkit version 26.3");
        assertEquals(2603, ServerVersion.parseServerVersion("26.3-249-1a995d6 (MC: 26.3)"), "server version 26.3");
    }

    @Test
    void unknownVersionIsZero() {
        assertEquals(0, ServerVersion.parseServerVersion("unknown"));
        assertEquals(0, ServerVersion.parseServerVersion("test"));
    }

    @Test
    void craftBukkitPackageSegmentOnlyExistsOnVersionedPackages() {
        assertEquals("v1_19_R3", ServerVersion.parseNmsApiVersion("org.bukkit.craftbukkit.v1_19_R3"));
        assertEquals("", ServerVersion.parseNmsApiVersion("org.bukkit.craftbukkit"));
    }
}
