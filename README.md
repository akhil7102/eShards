# eShards

A simple and lightweight shards economy plugin for Minecraft servers (Paper).

## Features
- Independent shard economy system.
- Reward players with shards for staying in a specific AFK region.
- Easy to use commands to view, pay, and manage shards.
- PlaceholderAPI support to display shard balance in scoreboards or holograms.
- Configurable messages and settings.

## Commands
- `/shards` - View your shard balance.
- `/shards pay <player> <amount>` - Pay shards to another player.
- `/shards wand` - Get the AFK region selection wand (Admin).
- `/shards region <create|delete|info>` - Manage the AFK region (Admin).
- `/shards set <player> <amount>` - Set a player's shard balance (Admin).
- `/shards give <player|all> <amount>` - Give shards to a player or all online players (Admin).
- `/shards take <player> <amount>` - Take shards from a player (Admin).
- `/shards reset <player|all>` - Reset the shard balance of a player or everyone (Admin).
- `/shards reload` - Reload the plugin configuration (Admin).

## Permissions
- `eshards.use` - Allows checking balance (default: true).
- `eshards.pay` - Allows paying players (default: true).
- `eshards.admin` - Allows access to all admin commands (default: op).

## Placeholders
The plugin hooks into PlaceholderAPI.
- `%eshards_balance%` - Displays the player's current shards balance.

## Configuration
The plugin generates a `config.yml` file where you can modify all messages, theme colors, default balance, and AFK reward intervals. The default theme color is `#7A4DFF`.

## Installation
1. Download the `eShards.jar` file.
2. Place it in your server's `plugins` folder.
3. Restart the server.
4. Edit the `config.yml` to your liking and type `/shards reload`.

## Building
To build this project from source, you need Maven and JDK 21.
Run the following command in the project root:
```
mvn clean package
```
The compiled JAR will be located in the `target` directory.

## Requirements
- Java 21
- PaperMC 1.21.x
- PlaceholderAPI (optional, but recommended)

## Note
This project is maintained and developed as an independent Minecraft plugin project.

## License
All Rights Reserved.
