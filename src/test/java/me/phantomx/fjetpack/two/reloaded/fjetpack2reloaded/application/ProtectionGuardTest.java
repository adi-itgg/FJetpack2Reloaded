package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.GriefPrevention;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.SuperiorSkyblock2;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Claim;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ClaimLookup;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Island;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.IslandLookup;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.TestPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The protection checks are the clearest seam of the port split: every third-party type sits behind
 * {@link ClaimLookup} / {@link IslandLookup}, so the decisions can be exercised without the plugins
 * that own them.
 */
@DisplayName("ProtectionGuard")
class ProtectionGuardTest {

    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-0000000000bb");

    private final Sink sink = new Sink();
    private final FakeClaims claims = new FakeClaims();
    private final FakeIslands islands = new FakeIslands();
    private final Player owner = player(OWNER);

    private ProtectionGuard guard;

    @BeforeAll
    static void installServer() {
        TestPlugin.installServer();
    }

    @BeforeEach
    void setUp() {
        guard = new ProtectionGuard(new FakeConfig(), sink, claims, islands);
    }

    @Test
    @DisplayName("outside every claim the player is told and refused")
    void outsideEveryClaimRefuses() {
        claims.claim = null;

        assertFalse(guard.allowsGriefPrevention(owner, jetpackWithGriefPrevention()));

        assertEquals(List.of("OUTSIDE_CLAIM"), sink.messages);
    }

    @Test
    @DisplayName("inside someone else's claim the player is refused")
    void someoneElsesClaimRefuses() {
        claims.claim = () -> OTHER;

        assertFalse(guard.allowsGriefPrevention(owner, jetpackWithGriefPrevention()));

        assertEquals(List.of("NO_PERM"), sink.messages);
    }

    @Test
    @DisplayName("inside the player's own claim flight is allowed")
    void ownClaimAllows() {
        claims.claim = () -> OWNER;

        assertTrue(guard.allowsGriefPrevention(owner, jetpackWithGriefPrevention()));

        assertTrue(sink.messages.isEmpty(), sink.messages.toString());
    }

    @Test
    @DisplayName("AllowInsideAllClaim allows any claim, owned or not")
    void allowInsideAllClaimAllows() {
        claims.claim = () -> OTHER;
        var jetpack = jetpackWithGriefPrevention();
        jetpack.getGriefPrevention().setAllowInsideAllClaim(true);

        assertTrue(guard.allowsGriefPrevention(owner, jetpack));

        assertTrue(sink.messages.isEmpty(), sink.messages.toString());
    }

    @Test
    @DisplayName("the bypass permission wins over someone else's claim")
    void bypassPermissionAllows() {
        claims.claim = () -> OTHER;
        var jetpack = jetpackWithGriefPrevention();
        jetpack.getGriefPrevention().setAllowBypassClaim(true);
        var privileged = player(OWNER, Set.of("fjetpack2reloaded.vip.bypass.gp.claim"));

        assertTrue(guard.allowsGriefPrevention(privileged, jetpack));
    }

    @Test
    @DisplayName("a disabled protection plugin short-circuits before any lookup")
    void inactiveClaimPluginAllows() {
        claims.enabled = false;
        claims.claim = () -> OTHER;

        assertTrue(guard.allowsGriefPrevention(owner, jetpackWithGriefPrevention()));

        assertEquals(0, claims.lookups, "no lookup is started when the plugin is absent");
    }

    @Test
    @DisplayName("outside every island the player is warned but allowed")
    void outsideEveryIslandAllows() {
        islands.inside = false;

        assertTrue(guard.allowsSuperiorSkyblock(owner, jetpackWithSuperiorSkyblock()));

        assertEquals(List.of("OUTSIDE_ISLAND"), sink.messages);
    }

    @Test
    @DisplayName("an island without the flag refuses")
    void islandWithoutFlagRefuses() {
        islands.flagEnabled = false;

        assertFalse(guard.allowsSuperiorSkyblock(owner, jetpackWithSuperiorSkyblock()));

        assertEquals(List.of("NO_FLAG"), sink.messages);
    }

    @Test
    @DisplayName("an island without the privilege refuses")
    void islandWithoutPrivilegeRefuses() {
        islands.privilege = false;

        assertFalse(guard.allowsSuperiorSkyblock(owner, jetpackWithSuperiorSkyblock()));

        assertEquals(List.of("SS_NO_PERM"), sink.messages);
    }

    @Test
    @DisplayName("an island with flag and privilege allows")
    void islandWithEverythingAllows() {
        assertTrue(guard.allowsSuperiorSkyblock(owner, jetpackWithSuperiorSkyblock()));

        assertTrue(sink.messages.isEmpty(), sink.messages.toString());
        assertEquals("FJETPACK2_RELOADED", islands.askedPrivilege, "the guard asks about its own flag");
    }

    private static Jetpack jetpackWithGriefPrevention() {
        var jetpack = new Jetpack();
        jetpack.setPermission(new Permission("fjetpack2reloaded.vip"));
        var griefPrevention = new GriefPrevention();
        griefPrevention.setEnable(true);
        griefPrevention.setOnlyAllowInsideOwnClaim(true);
        jetpack.setGriefPrevention(griefPrevention);
        return jetpack;
    }

    private static Jetpack jetpackWithSuperiorSkyblock() {
        var jetpack = new Jetpack();
        jetpack.setPermission(new Permission("fjetpack2reloaded.vip"));
        var superiorSkyblock = new SuperiorSkyblock2();
        superiorSkyblock.setEnable(true);
        jetpack.setSuperiorSkyblock2(superiorSkyblock);
        return jetpack;
    }

    /** A Player that only implements what Permissions and the guard use. */
    private static Player player(UUID id, Set<String> granted) {
        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPermission(anyString()))
                .thenAnswer(invocation -> granted.contains(invocation.getArgument(0)));
        when(player.isOp()).thenReturn(false);
        when(player.getName()).thenReturn("tester");
        return player;
    }

    private static Player player(UUID id) {
        return player(id, Set.of());
    }

    private static final class FakeClaims implements ClaimLookup {

        private boolean enabled = true;
        private Claim claim;
        private int lookups;

        @Override
        public boolean active() {
            return enabled;
        }

        @Override
        public Claim claimAt(Player player) {
            lookups++;
            return claim;
        }

    }

    private static final class FakeIslands implements IslandLookup {

        private boolean enabled = true;
        private boolean inside = true;
        private boolean flagEnabled = true;
        private boolean privilege = true;
        private String askedPrivilege;

        private final Island island = new Island() {

            @Override
            public UUID id() {
                return OWNER;
            }

            @Override
            public boolean flagEnabled(String flag) {
                return flagEnabled;
            }

            @Override
            public boolean hasPrivilege(Player player, String privilege) {
                askedPrivilege = privilege;
                return FakeIslands.this.privilege;
            }

        };

        @Override
        public boolean active() {
            return enabled;
        }

        @Override
        public String privilegeName() {
            return "FJETPACK2_RELOADED";
        }

        @Override
        public Island islandAt(Player player) {
            return inside ? island : null;
        }

    }

    /** Only the messages this test asserts on; the rest stay at the empty default. */
    private static final class FakeConfig implements ConfigRepository {

        private final Message message = new Message();

        private FakeConfig() {
            message.setNoPermission("NO_PERM");
            message.setGriefPreventionOutsideClaim("OUTSIDE_CLAIM");
            message.setSuperiorSkyblock2OutsideIsland("OUTSIDE_ISLAND");
            message.setSuperiorSkyblock2NoFlag("NO_FLAG");
            message.setSuperiorSkyblock2NoPermission("SS_NO_PERM");
        }

        @Override
        public Config config() {
            return new Config();
        }

        @Override
        public Message message() {
            return message;
        }

        @Override
        public Map<String, Jetpack> jetpacks() {
            return Map.of();
        }

        @Override
        public Map<String, CustomFuel> customFuels() {
            return Map.of();
        }

    }

    /** Records the text the guard decided to send. */
    private static final class Sink implements MessageSink {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void send(CommandSender target, String message, Object... args) {
            messages.add(message);
        }

        @Override
        public void usePrefix(String prefix) {
            // not used by the guard
        }

    }

}
