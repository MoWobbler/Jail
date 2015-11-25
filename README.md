Jail
=====

A simple Bukkit plugin that lets admins jail players as punishment. 

Commands
-----
All commands are usable only be admins.

```
/jail <player> <reason> [-a]
```
Jail *player* for *reason*. To also publically announce it, use the *-a* switch.

```
/unjail <player>
```
Unjail the given *player*. Works also for offline players.

```
/jailinfo [player]
```
If used without arguments, gives general info about the jail. If a player is specified, says when that player was jailed for what and by whom.

```
/jailreload
```
- Reloads the configuration.

Configuration
-----
The plugin is configured using the config.yml file. It should be automatically generated, from which it is possible to change the settings. For an example, see the config.yml file here.
