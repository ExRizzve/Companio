<div align="center">

[Русский](README.md) · [English](README_EN.md)

# Companio

A flying companion wearing any Minecraft player's head.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3_·_26.2_·_26.1.2_·_26.1.1_·_26.1_·_1.21.11_·_1.21.10_·_1.21.9_·_1.21.8-62B47A?style=flat-square)](#installation)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-F16436?style=flat-square)](https://neoforged.net/)
[![Environment](https://img.shields.io/badge/Environment-Client-4C8BF5?style=flat-square)](#installation)
[![License](https://img.shields.io/badge/License-All_Rights_Reserved-lightgrey?style=flat-square)](LICENSE.txt)

</div>

Companio adds a small visual companion that flies beside you, follows you around, and occasionally gets distracted by its own activities. It can wear a friend's skin, display a custom name, and perform several animations.

The mod is entirely client-side. Nothing needs to be installed on the server or by other players, but only the owner of the mod can see the companion.

> [!IMPORTANT]
> This is an early version of Companio. Some mechanics still need work, and the companion's behavior may change in future updates. The project is actively being developed.

## Features

- loads player skins by username through Mojang services;
- uses the default Steve head when no username is provided;
- smooth free flight with fast owner catch-up;
- teleports to the owner when stuck, unable to reach them, or too far away;
- follows the owner into other dimensions;
- occasional spins and flights around the player;
- custom name displayed through a `TextDisplay`;
- tab completion for player names and event types;
- English and Russian localization;
- validated configuration with safe limits.

## Commands

| Command | Action |
|---|---|
| `/companio create` | Create a companion with the default Steve head |
| `/companio create <username>` | Create a companion using the specified player's skin |
| `/companio name <name>` | Set a name up to 32 characters long |
| `/companio name clear` | Remove the name |
| `/companio event` | Start a random event |
| `/companio event spin` | Perform a full 360° spin |
| `/companio event twirl` | Perform an extended spin |
| `/companio event orbit` | Fly around the owner |
| `/companio reload` | Reload the configuration |
| `/companio remove` | Remove the companion |

## Installation

### Fabric

- Minecraft Java Edition `26.3`, `26.2`, `26.1.2`, `26.1.1`, `26.1`, `1.21.11`, `1.21.10`, `1.21.9` or `1.21.8`;
- Fabric Loader `0.19.3` or newer;
- Fabric API for your Minecraft version.

Files are named like `companio-1.3+1.21.10.jar`.

### NeoForge

- the same Minecraft versions as Fabric;
- NeoForge for your Minecraft version.

Files are named like `companio-1.3+26.2-neoforge.jar`. Fabric API is not needed.

Download the JAR matching your version and loader from [Releases](../../releases) and place it in `.minecraft/mods`. Companio does not need to be installed on the server.

## Configuration

The following file is created after the first launch:

```text
.minecraft/config/companio.json
```

It controls flight height, wandering radius, return distance, speed, and acceleration. Values outside the supported range are corrected automatically when the configuration is loaded.

## Building from source

Development requires JDK 25. Versions `1.21.8` to `1.21.11` are compiled for Java 21, but JDK 25 builds all of them.

The target version is selected with `-Pmc` and defaults to `26.2`. Per-version settings live in `versions/<version>.properties`.

```bash
./gradlew build -Pmc=1.21.10
```

The NeoForge build lives in its own `neoforge` module:

```bash
./gradlew -p neoforge build -Pmc=26.2
```

> [!NOTE]
> The first NeoForge build takes 20-40 minutes per Minecraft version, because its toolchain decompiles and recompiles the client. After that the result is cached and builds take seconds. For `1.21.x` targets Gradle downloads JDK 21 automatically if needed.

## Project layout

| Folder | Contents |
|---|---|
| `common` | Shared code: flight, display, configuration, skin loading |
| `src` | Fabric: entrypoint, commands, config loading |
| `neoforge` | NeoForge: entrypoint, commands, config loading |
| `versions` | Minecraft, Fabric API and NeoForge versions for each build |

## Notes

Companio is a visual client-side mod. The companion does not exist on the server, has no hitbox, cannot interact with the world, and provides no gameplay advantages.

This project is not affiliated with Mojang Studios or Microsoft.
