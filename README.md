ManagementPanel provides server administrators with powerful tools to manage players, perform complete profile wipes (inventory, enderchest, economy) with built-in backup and restore systems, and issue advanced punishments seamlessly integrated with EssentialsX and Vanilla Minecraft.

## Features

* **Complete Player Wipes**: Wipe a player's entire server profile with a single command. Supports wiping:
  * Vanilla Inventory
  * Vanilla Enderchest
  * EssentialsX Economy
  * YueMi Libs Economy
* **Safe Backup & Restore (Unwipe)**: Worried about false wipes? ManagementPanel automatically generates a secure backup of a player's data *before* wiping them. If you make a mistake, simply use `/unwipe` to restore their inventory, enderchest, and economy!
* **Integrated Punishments**: Deep integration with EssentialsX and Vanilla Minecraft to handle Warnings, Mutes, Kicks, and Bans securely and safely.
* **Developer API**: Built from the ground up to be extensible. Other developers can easily hook into the `core-api` to register custom `WipeHandler`s or `PunishmentHandler`s for third-party plugins.

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/managementpanel reload` | Reloads the configuration files. | `managementpanel.admin` |
| `/wipe <player> [reason]` | Safely backs up and wipes a player's profile. | `managementpanel.command.wipe` |
| `/unwipe <player> [backupId]` | Restores a wiped player's profile from a backup. | `managementpanel.command.unwipe` |
| `/warn <player> <reason>` | Issues a warning to a player. | `managementpanel.command.warn` |
| `/mute <player> <duration> <reason>` | Mutes a player for a specified duration. | `managementpanel.command.mute` |
| `/ban <player> <duration> <reason>` | Bans a player for a specified duration. | `managementpanel.command.ban` |
| `/status <player>` | Checks the punishment/wipe status of a player. | `managementpanel.command.status` |

*Note: All management commands require the user to have the base permission `managementpanel.use`.*

## Configuration

ManagementPanel offers a highly customizable `config.yml` that lets you toggle specific wipe integrations, enable/disable the backup system, and define fallback mechanisms.

```yaml
# Enable or disable specific integrations
integrations:
  essentials-economy: true
  yuemi-economy: true

# Configure how player wipes are handled
wipe:
  # Whether to create a backup before wiping (highly recommended)
  enable-backups: true
```

## Installation

1. Download the latest `ManagementPanel-x.x.x.jar` from the releases page.
2. Place the downloaded `.jar` file into your PaperMC server's `plugins/` folder.
3. Make sure you have any soft-dependencies installed (e.g., EssentialsX, YueMiLibs) if you plan on using those integrations.
4. Restart your server.
5. Configure the plugin in `plugins/ManagementPanel/config.yml` and use `/managementpanel reload`.

## For Developers

You can build ManagementPanel locally using Gradle:

```bash
git clone https://github.com/YourName/MC-Management-Panel.git
cd MC-Management-Panel
./gradlew build
```

The compiled plugin will be available in `core-plugin/build/libs/`.

---
*Built for PaperMC 1.21+ and Java 21.*