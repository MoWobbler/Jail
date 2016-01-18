package net.simpvp.Jail;

import java.util.HashSet;

import org.bukkit.Location;

/**
 * Handles config file related things
 */
public class Config {

	private static Jail plugin = Jail.instance;

	public static String world;

	public static int x1;
	public static int y1;
	public static int z1;

	public static int x2;
	public static int y2;
	public static int z2;

	public static Location spawn;

	public static HashSet<String> blocked_commands;

	public static void loadConfig() {		
		plugin.saveDefaultConfig();
		plugin.reloadConfig();

		world = plugin.getConfig().getString("world");

		/* The following trickery is to ensure that the 1s are always lower than 2s
		 * for easy comparisons later on */
		if (plugin.getConfig().getInt("corner1.x") >= plugin.getConfig().getInt("corner2.x")) {
			x1 = plugin.getConfig().getInt("corner2.x");
			x2 = plugin.getConfig().getInt("corner1.x");
		} else {
			x1 = plugin.getConfig().getInt("corner1.x");
			x2 = plugin.getConfig().getInt("corner2.x");
		}

		if (plugin.getConfig().getInt("corner1.y") >= plugin.getConfig().getInt("corner2.y")) {
			y1 = plugin.getConfig().getInt("corner2.y");
			y2 = plugin.getConfig().getInt("corner1.y");
		} else {
			y1 = plugin.getConfig().getInt("corner1.y");
			y2 = plugin.getConfig().getInt("corner2.y");
		}

		if (plugin.getConfig().getInt("corner1.z") >= plugin.getConfig().getInt("corner2.z")) {
			z1 = plugin.getConfig().getInt("corner2.z");
			z2 = plugin.getConfig().getInt("corner1.z");
		} else {
			z1 = plugin.getConfig().getInt("corner1.z");
			z2 = plugin.getConfig().getInt("corner2.z");
		}

		blocked_commands = new HashSet<String>(plugin.getConfig().getStringList("blocked_commands"));

		spawn = new Location(plugin.getServer().getWorld(world),
				plugin.getConfig().getInt("spawn.x") + 0.5,
				plugin.getConfig().getInt("spawn.y"),
				plugin.getConfig().getInt("spawn.z") + 0.5);
	}

	/**
	 * Gets the jail's spawn location.
	 * @return The jail's spawn location.
	 */
	public static Location get_spawn() {
		return spawn;
	}

	/**
	 * Gets whether the passed location is inside the specified jail area.
	 * @param location
	 * @return
	 */
	public static boolean is_inside_jail(Location location) {

		if (x1 > location.getX() || location.getX() > x2)
			return false;

		if (y1 > location.getY() || location.getY() > y2)
			return false;

		if (z1 > location.getZ() || location.getZ() > z2)
			return false;

		if (!location.getWorld().getName().equals(world))
			return false;

		return true;
	}

	/**
	 * Checks if the given command should be blocked for jailed players
	 * @param command The command to test, should be without leading / and in all lowercase
	 * @return True if is blocked, else false
	 */
	public static boolean is_command_blocked(String command) {
		return blocked_commands.contains(command);
	}

}
