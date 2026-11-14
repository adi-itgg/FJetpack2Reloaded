# FJetpack2Reloaded

Jetpack Plugin - Lightweight - Fuel System - Multi Jetpack with customizable configuration

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

### Next generation from FJetpackReloaded

FJetpack2Reloaded adds jetpacks to a Minecraft server. A jetpack is an item a player wears and flies with; every use burns **fuel**, and both the jetpacks and the fuel items are defined in configuration files, so one server can ship any number of jetpack types.

This guide takes a new user from installation to a working jetpack: requirements, installation, configuration, commands, and the in-game flow.

## Table of contents

1. [Requirements](#requirements)
2. [Installation](#installation)
3. [Concepts](#concepts)
4. [Quick start](#quick-start)
5. [Commands](#commands)
6. [Flying a jetpack](#flying-a-jetpack)
7. [Refuelling](#refuelling)
8. [Permissions](#permissions)
9. [Configuration](#configuration)
10. [Optional integrations](#optional-integrations)
11. [Update checking](#update-checking)
12. [Troubleshooting](#troubleshooting)
13. [bStats](#bstats)
14. [License](#license)
15. [Support](#support)

## Requirements

- **Minecraft server:** Bukkit/Spigot, **1.8 through 26.3**.
- **Java:** **17** or newer.
- **Optional integrations:** GriefPrevention (claims), SuperiorSkyblock2 (island flag and privilege) and ItemsAdder (jetpack items that come from ItemsAdder). All three are optional: the plugin runs without them, the GriefPrevention and SuperiorSkyblock2 checks are disabled by default in `jetpack.yml`, and ItemsAdder is only used when a jetpack sets an id.
- **Access:** write access to the server's `plugins` directory, and an operator account (or a group holding the plugin permissions) to run the management commands.

## Installation

1. Stop the server.
2. Copy the plugin's `.jar` file into the server's `plugins` directory.
3. Start (or restart) the server.
4. Check the console: the plugin reports the server version it detected and each configuration file it loaded, then one `Loaded jetpack: <id>` line per jetpack.
5. Run `/fj2r help` and check that the command list appears.
6. Grant the permissions the players need - see [Permissions](#permissions).

On the first start the plugin creates its data folder, `plugins/FJetpack2Reloaded/`, and fills it with the default configuration files: `config.yml`, `jetpack.yml`, `custom_fuel.yml` and `message.yml`. Files are only created when they are missing, so your edits are never overwritten.

## Concepts

| Term | Meaning |
| --- | --- |
| **Jetpack** | An item defined by a section in `jetpack.yml`. Worn in an armour slot (the plugin looks for the jetpack in every armour slot) or held in the off hand, it lets the player fly and spends fuel. |
| **Fuel** | A number stored on the jetpack item itself. Every burn tick subtracts the configured cost; the current value is shown through the `#{fuel_value}` placeholder in the item lore. |
| **Custom fuel** | An item defined by a section in `custom_fuel.yml`. When a jetpack sets `Fuel.CustomFuel`, that item - not the plain material in `Fuel.Item` - is used to refuel it. |
| **Id** | The YAML section name (`VIP`, `Member`, `CVIP`, ...). Command arguments must match it exactly, and it is used in permission nodes. |

Two consequences worth knowing before configuring anything:

- Every jetpack needs its own permission node (`fjetpack2reloaded.<id>`); adding a jetpack means adding a permission as well.
- A jetpack id that no longer exists in `jetpack.yml` is not a jetpack any more: the item is left in the inventory but cannot be flown.

## Quick start

### As a server admin

1. Install the plugin - see [Installation](#installation).
2. Open `plugins/FJetpack2Reloaded/jetpack.yml` and check the jetpack sections. The default file defines two: `VIP` and `Member`.
3. Give players the permissions they need. At minimum:
   - `fjetpack2reloaded.<jetpack>` to use a jetpack (for the default jetpacks: `fjetpack2reloaded.vip`, `fjetpack2reloaded.member`);
   - `fjetpack2reloaded.get` or `fjetpack2reloaded.give` if players may take jetpack items themselves;
   - `fjetpack2reloaded.<jetpack>.fuel.refill` if players may refuel.
4. Hand out a jetpack, with the starting fuel:

   ```text
   /fj2r give <player> VIP <fuel>
   ```

   `<player>` must be online; `VIP` must be a section name from `jetpack.yml`.
5. If the jetpack uses a custom fuel, hand that out too:

   ```text
   /fj2r getfuel <player> CVIP <amount>
   ```

6. After every configuration change, run `/fj2r reload`.

### As a player

1. Equip the jetpack in an armour slot - the default jetpacks are chestplates, so that is the chestplate slot - or hold it in the off hand when the jetpack sets `RunInOffHandOnly: true`.
2. Make sure you have the jetpack permission.
3. Press and release sneak while standing on the ground. You should see the `ON` message.
4. Double-jump to start flying.
5. Watch the fuel value in the item lore; refuel before it reaches zero - see [Refuelling](#refuelling).
6. Press and release sneak again to turn the jetpack off.

## Commands

The command is `/fjetpack2reloaded`, with the alias `/fj2r`. Every listed subcommand works with either form.

| Command | Description |
|  :---  |     :-      |
| `/fj2r help` | Show the available commands. |
| `/fj2r get` or `/fj2r give` (Player) [Jetpack] {Fuel} | Give a jetpack item, with fuel. |
| `/fj2r getfuel` or `/fj2r givefuel` (Player) [CustomFuel] {Fuel} | Give a custom fuel item. |
| `/fj2r set` [Jetpack] {Fuel} | Turn the item in hand into the named jetpack, with fuel. |
| `/fj2r setfuel` <Amount> | Set the fuel of the jetpack in hand. |
| `/fj2r reload` | Reload every configuration file. |
| `/fj2r checkupdate` | Check whether a newer version is available. |

The in-game help text uses this notation:

| Notation | Meaning |
| --- | --- |
| `[Jetpack]` / `[CustomFuel]` | Section name from `jetpack.yml` / `custom_fuel.yml`. |
| `(Player)` | Name of an online player. |
| `<Fuel>` / `<Amount>` | A whole number. |

Notes that apply to the item commands:

- For `get`, `give`, `getfuel` and `givefuel`, arguments may be given **in any order**: the command picks the argument that matches a known jetpack or custom fuel id, the argument that matches an online player, and the first numeric argument. `set` and `setfuel` take positional arguments.
- `(Player)` may be omitted when the sender is a player; the sender is then the target. From the console the player name is required.
- When no number is given, `get` and `give` use `0` fuel, and `getfuel` and `givefuel` use an amount of `1`.
- `get` and `give` behave identically, and so do `getfuel` and `givefuel`; each pair is two spellings of one behaviour.
- Tab completion offers the subcommands the sender may use, then the loaded jetpack or custom fuel ids and online player names, then a list of amounts where a number is expected.

### /fj2r help

Prints the help text. Permission: `fjetpack2reloaded.help` (default: granted to everyone). An unknown subcommand also prints this text.

### /fj2r get and /fj2r give

Creates a jetpack item and puts it in the target's inventory. If the inventory is full, the item is dropped at the player's location and the player is told so. Permission: `fjetpack2reloaded.get` or `fjetpack2reloaded.give`.

```text
/fj2r give Alex VIP 250
```

### /fj2r getfuel and /fj2r givefuel

Creates a custom fuel item and puts it in the target's inventory. Permission: `fjetpack2reloaded.getfuel` or `fjetpack2reloaded.givefuel`. The custom fuel id must exist in `custom_fuel.yml`.

```text
/fj2r getfuel Alex CVIP 8
```

### /fj2r set

Turns the armour item in the main hand into the named jetpack. The item must be armour, otherwise the command refuses it. Permission: `fjetpack2reloaded.set`. Player-only.

```text
/fj2r set VIP 100
```

### /fj2r setfuel

Sets the fuel value of the jetpack in the main hand to the given number. The held item must carry a jetpack id, otherwise the command reports that it is not a jetpack item. Permission: `fjetpack2reloaded.setfuel`. Player-only.

```text
/fj2r setfuel 500
```

### /fj2r reload

Re-reads `config.yml`, `message.yml`, `custom_fuel.yml` and `jetpack.yml`, reports every file it loaded, and warns when the result contains no jetpack at all. A file that fails to load keeps the values that were already in use, so a broken edit never empties a running server. Permission: `fjetpack2reloaded.reload`.

### /fj2r checkupdate

Asks SpigotMC whether a newer version exists and replies in chat. Permission: `fjetpack2reloaded.checkupdate`.

## Flying a jetpack

### Turning it on and off

The toggle is a sneak press: press and release sneak **while standing on the ground**. Toggling on requires the jetpack to be worn and the player to hold the jetpack permission; the player then sees the `ON` message and the plugin allows flight and applies `Speed`.

Once flight is allowed, double-jump to fly (the vanilla "toggle flight" input). The `ON` and `OFF` messages come from `message.yml`.

The jetpack turns itself off when:

- the player presses sneak again on the ground;
- the worn (or off-hand) jetpack is removed or swapped out - the player sees the "took off your jetpack" message;
- the player loses the permission, or enters a world listed in `BlockedWorlds`, or a protection check starts refusing the flight;
- the player leaves the server or the plugin is unloaded.

### Fuel

Fuel is charged on a repeating tick whose length is `Fuel.BurnRate` seconds (default `5`). Each tick:

- `Fuel.Cost` is subtracted (default `1`);
- while the player is sprinting, `Fuel.SprintCost` is subtracted as well (default `1`);
- ticks while the player stands on the ground and is not flying do not charge fuel.

When the remaining fuel reaches `Fuel.WarnRunOutBelow` (default `5`, `-1` disables the warning), the player is warned with the `WarnRunOutBelow` message.

`AllowBypassCost` and `AllowBypassSprintCost` appear in `jetpack.yml` but currently have no effect: the bypass is decided by the permissions `fjetpack2reloaded.<jetpack>.bypass.fuel` and `fjetpack2reloaded.<jetpack>.bypass.fuel.sprint` alone.

### Running out of fuel

When a tick cannot pay the cost, the player is told "out of fuel" and the jetpack's `OnEmptyFuel` setting decides what happens to the item:

- `NONE` - the item stays equipped and the flight stops;
- `DROP` - the item falls to the ground;
- `REMOVE` - the item is deleted.

Players holding `fjetpack2reloaded.<jetpack>.keep.on.empty` keep the item in every case.

### Death

If the player dies while a jetpack is active, `OnDeath` is applied the same way (`NONE`, `DROP` or `REMOVE`), unless the player holds `fjetpack2reloaded.<jetpack>.keep.on.death`.

### Worlds, claims and islands

- `BlockedWorlds` lists worlds where the jetpack cannot be turned on.
- With GriefPrevention support enabled for the jetpack, flight is restricted to claims according to `OnlyAllowInsideOwnClaim` and `AllowInsideAllClaim`; `AllowBypassClaim` plus the `bypass.gp.claim` permission lifts the restriction.
- With SuperiorSkyblock2 support enabled, flight requires the island flag `FJETPACK2_RELOADED` (and the matching island privilege) on the player's island; `AllowBypassFlag` and `AllowBypassPrivilege` plus their permissions lift the checks.

### Off-hand mode

A jetpack with `RunInOffHandOnly: true` only works from the off hand; the armour slots are ignored for it, and the toggle checks the off-hand item instead.

## Refuelling

Refuelling is done in the inventory, by clicking a fuel item onto the jetpack:

1. Hold the fuel item on the cursor. This is the material in `Fuel.Item`, or, when the jetpack sets `Fuel.CustomFuel`, the item created from that custom fuel section.
2. Left-click the jetpack in the inventory to add the **whole held stack** to the fuel value, or right-click to add **one item**.
3. The jetpack's fuel value grows by that amount, its lore is refreshed, and the fuel item is consumed.

Requirements:

- `fjetpack2reloaded.<jetpack>.fuel.refill` for the player;
- for a custom fuel, also the `Permission` node defined in its section, for the default `CVIP` that is `fjetpack2reloaded.fuel.cvip`;
- a fuel item that matches the jetpack: same material and, for a custom fuel, the same custom fuel id.

Refuelling is a survival action: clicks made in the creative inventory are ignored by the plugin.

## Permissions

| Permission | Description | Default |
|   :---    |     :-      |   :-    |
| `fjetpack2reloaded.*` | Allow access all commands | op |
| `fjetpack2reloaded.help` | Allow player use command /fj2r help | true |
| `fjetpack2reloaded.get` or `fjetpack2reloaded.give` | Allow player to use command /fj2r get/give <name> (the original table writes this as `fjetpack2reloaded.get or give`) | false |
| `fjetpack2reloaded.getfuel` or `fjetpack2reloaded.givefuel` | Allow player to use command /fj2r getfuel/givefuel <name> (original table: `fjetpack2reloaded.getfuel or givefuel`) | false |
| `fjetpack2reloaded.set` | Allow player to use command /fj2r set <jetpack> | false |
| `fjetpack2reloaded.setfuel` | Allow player to use command /fj2r setfuel <amount> | false |
| `fjetpack2reloaded.reload` | Allow player to use command /fj2r reload | false |
| `fjetpack2reloaded.checkupdate` | Allow player to check for plugin updates | false |
| `fjetpack2reloaded.{jetpack}` | Allow player to use Jetpack (from config) | false |
| `fjetpack2reloaded.{jetpack}.fuel.refill` | Allow player to refuel Jetpack | false |
| `fjetpack2reloaded.fuel.{customfuel}` | Allow player to refill Jetpack using a custom fuel item (from config) | false |
| `fjetpack2reloaded.{jetpack}.keep.on.empty` | Allow player to bypass on empty fuel | false |
| `fjetpack2reloaded.{jetpack}.keep.on.death` | Allow player to bypass on death fuel | false |
| `fjetpack2reloaded.{jetpack}.bypass.fuel` | Allow player to bypass on fuel cost | false |
| `fjetpack2reloaded.{jetpack}.bypass.fuel.sprint` | Allow player to bypass on sprint fuel cost | false |
| `fjetpack2reloaded.{jetpack}.bypass.ss2.flag` | Allow player to bypass SuperiorSkyblock2 flag | false |
| `fjetpack2reloaded.{jetpack}.bypass.ss2.privilege` | Allow player to bypass SuperiorSkyblock2 privilege | false |
| `fjetpack2reloaded.{jetpack}.bypass.gp.claim` | Allow player to bypass GriefPrevention claim | false |

Notes:

- `{jetpack}` and `{customfuel}` are placeholders. The permission nodes of a jetpack are built from its `Permission` value in `jetpack.yml` (`#id` is replaced with the section name, lowercased), so the default `VIP` section produces `fjetpack2reloaded.vip` and its sub-nodes such as `fjetpack2reloaded.vip.fuel.refill`.
- The defaults above come with the plugin. Per-jetpack and per-custom-fuel nodes are granted only when you give them; their default is `false`.
- Operators and holders of `fjetpack2reloaded.*` bypass the plugin's own permission checks, and console senders are never blocked.

## Configuration

All files live in `plugins/FJetpack2Reloaded/` and are reloaded with `/fj2r reload`.

| File | Purpose |
| --- | --- |
| `config.yml` | Plugin-wide options. |
| `jetpack.yml` | One section per jetpack: item, fuel, behaviour, hooks. |
| `custom_fuel.yml` | One section per custom fuel item. |
| `message.yml` | Every message the plugin sends, plus the prefix. |

### config.yml

| Key | Default | Meaning |
| --- | --- | --- |
| `version` | `1` | Config version marker. |
| `UpdateNotification` | `true` | When true and a player who holds `fjetpack2reloaded.checkupdate` joins, the plugin checks for an update in the background and reports the result. |

### jetpack.yml

Each top-level section is one jetpack. Default examples: `VIP` (chainmail chestplate) and `Member` (leather chestplate, higher sprint cost).

| Key | Meaning |
| --- | --- |
| `DisplayName` | Item display name. Colour codes (`&`) are supported. |
| `Lore` | Item lore lines. `#{fuel}` is the fuel source name, `#{fuel_value}` the current fuel. |
| `Permission` | Base permission node; `#id` is replaced with the section name, lowercased. |
| `Item` | Bukkit material of the jetpack item; also the fallback when `ItemsAdderID` is set but unusable. |
| `ItemsAdderID` | Optional `namespace:id` of an ItemsAdder item. Empty means "use `Item`". |
| `ItemColor` | `R`/`G`/`B` colour, leather armour only. |
| `Unbreakable` | Makes the item unbreakable (1.17+; on older servers the item is kept at zero damage). |
| `OnEmptyFuel` | `NONE`, `DROP` or `REMOVE`, applied when the fuel runs out. |
| `OnDeath` | `NONE`, `DROP` or `REMOVE`, applied when the player dies while the jetpack is active. |
| `CustomModelData` | Custom model data, `-1` disables. |
| `Speed` | Fly speed. The plugin sets the player's fly speed to `Speed / 10`, so `1.1` means `0.11`. |
| `Flags` | Item flags, e.g. `HIDE_ATTRIBUTES`, `HIDE_ENCHANTS`. `[]` for none. |
| `Enchantments` | `name:level` entries; use `DURABILITY` on servers below 1.17 and `UNBREAKING` from 1.17. |
| `BlockedWorlds` | World names where the jetpack cannot be turned on. |
| `RunInOffHandOnly` | When true the jetpack only works from the off hand. |
| `Fuel.CustomFuel` | Custom fuel id from `custom_fuel.yml`; `NONE` disables custom fuel. Case sensitive. |
| `Fuel.Item` | Fuel material used for refuelling when no custom fuel is configured. |
| `Fuel.Cost` | Fuel charged per burn tick. |
| `Fuel.SprintCost` | Extra fuel charged per burn tick while sprinting. |
| `Fuel.BurnRate` | Seconds between two burn ticks. |
| `Fuel.WarnRunOutBelow` | Warn the player at or below this fuel value; `-1` disables the warning. |
| `Particle.Enable` | Turns the flight trail on or off. |
| `Particle.Effect` | Particle (or, on 1.8, effect) name. |
| `Particle.Amount` | Particle count per spawn. |
| `Particle.Delay` | Delay between spawns, in ticks. |
| `GriefPrevention.Enable` | Enables the GriefPrevention check for this jetpack. |
| `GriefPrevention.OnlyAllowInsideOwnClaim` | Only the player's own claim counts. |
| `GriefPrevention.AllowInsideAllClaim` | Any claim counts (overrides the previous key). |
| `GriefPrevention.AllowBypassClaim` | Lets the `bypass.gp.claim` permission skip the check. |
| `SuperiorSkyblock2.Enable` | Enables the SuperiorSkyblock2 check for this jetpack. |
| `SuperiorSkyblock2.AllowBypassFlag` | Lets the `bypass.ss2.flag` permission skip the flag check. |
| `SuperiorSkyblock2.AllowBypassPrivilege` | Lets the `bypass.ss2.privilege` permission skip the privilege check. |

Three keys in the default `jetpack.yml` currently have no effect: `Fuel.CustomModelData` (only the jetpack's own `CustomModelData` and the custom fuel's own `CustomModelData` are used), `Fuel.AllowBypassCost` and `Fuel.AllowBypassSprintCost` (the bypass is decided by the permissions alone).

### custom_fuel.yml

Each top-level section is one custom fuel; default example: `CVIP`.

| Key | Meaning |
| --- | --- |
| `CustomDisplay` | Name used for the `#{fuel}` placeholder. Empty falls back to `DisplayName`. |
| `DisplayName` | Item display name. |
| `Lore` | Item lore lines. |
| `Item` | Bukkit material of the fuel item. |
| `Permission` | Permission node needed to refill with this fuel; `#id` is replaced with the section name, lowercased. |
| `Glowing` | Adds the enchantment glow. |
| `CustomModelData` | Custom model data, `-1` disables. |

### message.yml

`Prefix` is prepended to every message. Any message left empty is not sent. The file covers, by group:

| Group | Keys |
| --- | --- |
| Prefix and state | `Prefix`, `TurnOn`, `TurnOff` |
| Errors | `NoPermission`, `OutOfFuel`, `BlockedWorlds`, `NoFuel`, `Detached`, `CmdPlayerOnly`, `NoItemInMainHand`, `InventoryFull`, `NotJetpackItem`, `InvalidNumber` |
| Fuel and death events | `WarnRunOutBelow`, `OnEmptyFuelDropped`, `OnEmptyFuelRemoved`, `OnDeathDropped`, `OnDeathRemoved` |
| Protection | `GriefPreventionOutsideClaim`, `GriefPreventionTurnedOffOutsideClaim`, `GriefPreventionOutsideOwnClaim`, `SuperiorSkyblock2NoFlag`, `SuperiorSkyblock2NoPermission`, `SuperiorSkyblock2OutsideIsland` |
| Command feedback | `CmdGetSelf`, `CmdGiveSuccess`, `CmdGiveReceived`, `CmdSet`, `CmdReload`, `CmdFuelSet`, `CmdFuelGetSelf`, `CmdFuelGiveSuccess`, `CmdFuelGiveReceived` |
| Update feedback | `NoUpdate`, `UsingDevVersion`, `FoundUpdate` |

Messages and the jetpack lore accept these placeholders: `#{jetpack}`, `#{custom_fuel}`, `#{fuel}`, `#{fuel_value}`, `#{amount}`, `#{player}`, `#{sender}`, `#{x}`, `#{y}`, `#{z}`.

### Reloading

Run `/fj2r reload` after saving any of the four files. The command prints one `Loaded ...` line per file and warns when no jetpack is loaded. A file that cannot be read is reported, and the previously loaded values stay active.

## Optional integrations

- **GriefPrevention** - when a jetpack enables `GriefPrevention.Enable`, the claim at the player's position is consulted. `OnlyAllowInsideOwnClaim` (default `true`) restricts flight to the player's own claim, `AllowInsideAllClaim` accepts any claim, and `AllowBypassClaim` together with `fjetpack2reloaded.<jetpack>.bypass.gp.claim` lets a player skip the restriction. The integration is off by default.
- **SuperiorSkyblock2** - the plugin adds the island flag and privilege `FJETPACK2_RELOADED` when SuperiorSkyblock2 starts. With `SuperiorSkyblock2.Enable`, an island must have that flag enabled for its members to fly, and the player needs the matching island privilege; `AllowBypassFlag` and `AllowBypassPrivilege` plus their permissions skip the two checks. The integration is off by default.
- **ItemsAdder** - when a jetpack sets `ItemsAdderID`, the item is taken from ItemsAdder instead of the configured material. If ItemsAdder is missing, the id is unknown, or its data is not loaded yet, the plugin falls back to `Item` and says so in the console.

All three are optional: without them the plugin works, with their hooks disabled.

## Update checking

- `/fj2r checkupdate` asks SpigotMC, in the background, whether a newer version of the plugin is available, and replies with that version and its SpigotMC page or with "no new update".
- With `UpdateNotification: true` in `config.yml`, the same check runs for a player on join, but only for players holding `fjetpack2reloaded.checkupdate`.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| The plugin does not start, or disables itself | The console prints `Unknown Server Version!` when the server version cannot be read, or `Unsupported server api version!` when this server cannot store the jetpack data. Run a supported server (1.8 - 26.3) and the matching Java version. |
| `No jetpack could be loaded ...` on startup | `jetpack.yml` (and the other files) could not be read. Fix the YAML, then run `/fj2r reload`; the message names the data folder and the failed files. |
| "You don't have permission!" | The denial message includes the missing node in parentheses. Grant it, or `fjetpack2reloaded.*`; per-jetpack nodes are built from the jetpack's `Permission` value. |
| A command is refused although the player is an operator | Operators bypass the plugin's permission checks and the claim/island checks too. If an operator still cannot fly, check `BlockedWorlds`, the fuel value, and whether the item is the right jetpack; the message in chat names the reason. |
| The jetpack is not recognised | The item must be one the plugin created for that jetpack, or an armour item converted with `/fj2r set`; a plain item of the same material is not enough. |
| `/fj2r set` says the item is not armour | `set` requires an armour item in the main hand. |
| Refuelling does nothing | The held item must match `Fuel.Item` (or the configured custom fuel), the player needs `fjetpack2reloaded.<jetpack>.fuel.refill` (and the custom fuel permission), and refuelling does not happen in the creative inventory. |
| Nothing happens on sneak | Sneak must start and end while the player stands on the ground; the jetpack must be worn (or in the off hand when `RunInOffHandOnly` is set); the world must not be listed in `BlockedWorlds`. |
| Configuration changes are ignored | Save the file, then run `/fj2r reload`. |
| Need more detail in the console | Add `[DEV-FJ2R-DEBUG]` to the server MOTD to enable verbose logging. |

## bStats

![bStats](https://bstats.org/signatures/bukkit/FJetpack2Reloaded.svg)

## License

Released under the **MIT** license - see the [LICENSE](LICENSE) file.

## Support

- SpigotMC resource page: https://www.spigotmc.org/resources/fjetpackreloaded.100816/
- Author: `PhantomXCraft`.