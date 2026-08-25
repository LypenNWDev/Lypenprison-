LypenPrison
LypenPrison is an advanced, highly optimized screenshare (SS) and player isolation plugin designed for Minecraft servers. It allows server administrators to securely isolate suspected cheaters in an unbreakable, void-generated cage.

Developed by @kerem and LypenNW.

📌 Features
Void Cage Generation: Automatically creates an isolated void world (prison_world) hidden from your main server directories.
Unbreakable Cells: Players are trapped in a 5x5 cage made of Bedrock and Iron Bars. Breaking or placing blocks is completely disabled.
Infinite Spacing: Every new suspected player is placed exactly 15,000 blocks away from the previous one, ensuring suspects never see or interact with each other.
Strict Movement Restrictions: Admins can fly smoothly but are bounded by a 15-block radius barrier. Neither admins nor players can take damage or fall into the void.
Automated IP Ban System (bans.yml): If a suspected player disconnects (quits) while in prison, they are automatically permanently banned via their IP and UUID to prevent ban evasion.
Advanced Logging (logs.yml): Keeps track of every prison event. Admins can view a player's prison history in-game using the info command.
RGB Support: Fully supports Hex (&#FF0000) and standard (&c) color codes for all ActionBars, Titles, and chat messages.
Smart Reloading: The /prison reload command safely reconstructs missing configuration files and alerts the admin in-game if there are any YAML syntax errors.
Note: The plugin currently only supports the Turkish (TR) language natively out-of-the-box. All configurable messages in config.yml are written in Turkish by default.

⚙️ Commands & Permissions
Permissions
All commands below require a single permission node: lypenprison.admin (Default: OP)

Commands
/prison <type> <player> <duration> [message] Places a player into the specified type of prison (e.g., kontrol, uyari). Duration formats supported: Seconds (60 or 60s), Minutes (60m), Hours (1h), Days (1d). Example: /prison kontrol Notch 30m Suspicious Combat

/unprison <player> Safely removes the player from the prison, restores their previous location, clears their screen titles, and restores their inventory/movement states.

/prison info <player> [page] Displays the prison history log of the specified player (Admins involved, dates, and types of prison).

/prison reload Reloads config.yml, bans.yml, and logs.yml smoothly without requiring a server restart.

🚀 Installation
Download the latest LypenPrison-1.0-SNAPSHOT.jar release.
Drop the .jar file into your server's plugins/ directory.
Restart your server.
Modify the plugins/LypenPrison/config.yml to your liking (add new prison types, adjust RGB colors, change constraints).
Use /prison reload to apply your changes.
🛡️ Compatibility
Spigot / Paper: 1.20.4 (Built against 1.20.4-R0.1-SNAPSHOT)
Java: Java 17 or higher
If you are using ViaVersion on older versions, ensure it is fully updated, as outdated ViaVersion builds might conflict with 1.20+ Title packets.

📄 License
This project is made for LypenNW. All rights reserved by the original creators.

Disclaimer: Approximately 70% of the development and coding process for this plugin was assisted by Artificial Intelligence.
