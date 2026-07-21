# Companio

**A small client-side companion that flies alongside you using any Minecraft player's skin.**

Companio adds a floating player head that follows its owner, wanders nearby, and occasionally performs its own animations. Use a friend's username, give the companion a custom name, or keep the default Steve appearance.

> Companio is currently in early development. Movement, animations, and configuration may change as the mod is improved.

## Features

- Load any licensed Minecraft player's skin by username
- Create a default Steve companion without entering a username
- Smooth wandering with fast catch-up when the owner moves away
- Random spins, twirls, and flights around the owner
- Custom name displayed above the companion
- Tab completion for online player names and event types
- English and Russian localization
- Validated configuration with safe value limits

## Commands

| Command | Description |
|---|---|
| `/companio create` | Create the default Steve companion |
| `/companio create <username>` | Create a companion using a player's skin |
| `/companio name <name>` | Set a custom name up to 32 characters |
| `/companio name clear` | Remove the custom name |
| `/companio event` | Start a random animation |
| `/companio event spin` | Perform a 360° spin |
| `/companio event twirl` | Perform an extended spin |
| `/companio event orbit` | Fly around the owner |
| `/companio reload` | Reload the configuration |
| `/companio remove` | Remove the companion |

## Installation

Companio requires:

- Minecraft Java Edition **26.1.2**
- Fabric Loader **0.19.2** or newer
- Fabric API for **26.1.2**

Install Fabric API and Companio in your `mods` folder. The mod is entirely client-side and does not need to be installed on the server.

Only the player running the mod can see the companion. It has no hitbox, cannot interact with the world, and provides no gameplay advantage.

## Configuration

The configuration file is created at `config/companio.json` after the first launch. It controls flight height, wandering radius, return distance, speed, and acceleration. Unsupported values are corrected automatically.

## Русский

Companio добавляет небольшого летающего компаньона с головой любого лицензированного игрока Minecraft. Он следует за владельцем, свободно летает рядом, иногда вращается или кружит вокруг игрока. Компаньону можно дать собственное имя.

Мод полностью клиентский: устанавливать его на сервер не нужно, а видеть компаньона будет только его владелец. Компаньон не имеет хитбокса и не влияет на игровой процесс.

Это ранняя версия проекта. Движение, анимации и настройки будут дорабатываться в следующих обновлениях.

Исходный код и сообщения об ошибках: [GitHub](https://github.com/ExRizzve/Companio)

Companio is not affiliated with Mojang Studios or Microsoft.
