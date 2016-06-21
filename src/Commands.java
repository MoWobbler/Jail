package net.simpvp.Jail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class Commands implements CommandExecutor {
	//FIXME: Write docs for entire class
	private static Jail plugin = Jail.instance;

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Player player = null;
		if (sender instanceof Player){
			player = (Player) sender;
		}

		if (player != null && !player.isOp()) {
			send_message("You do not have permission to use this command.", player, ChatColor.RED);
			return true;
		}

		if (label.equals("jail")) {
			jail_player(player, args);
		} else if (label.equals("unjail")) {
			unjail_player(player, args);
		} else if (label.equals("jailinfo")) {
			show_info(player, args);
		} else if (label.equals("jailreload")) {
			Config.loadConfig();
			send_message("Jail config has been reloaded.", player, ChatColor.GOLD);
		}

		return true;
	}

	private void send_message(String message, Player player, ChatColor color) {
		plugin.getLogger().info(message);
		if (player != null)
			player.sendMessage(color + message);
	}

	private void announce_message(String message, Player player) {
		String playername = "";
		if (player == null) {
			playername = "CONSOLE";
		} else {
			playername = player.getName();
		}

		plugin.getLogger().info("[" + playername + ": " + message + "]");
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			if (onlinePlayer.isOp()) {
				onlinePlayer.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "[" + playername + ": " + message + "]");
			}
		}
	}

	private void jail_player(Player player, String[] args) {
		if (args.length < 2) {
			send_message("Invalid usage. Correct usage is /jail <player> <reason>", player, ChatColor.RED);
			return;
		}

		@SuppressWarnings("deprecation") /* Warnings have been read */
		Player target = plugin.getServer().getPlayer(args[0]);

		UUID jailer_uuid = null;
		String jailer = null;
		if (player == null) {
			jailer = "CONSOLE";
		} else {
			jailer = player.getName();
			jailer_uuid = player.getUniqueId();
		}

		boolean announce = false;
		String reason = "";
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-a")) {
				announce = true;
			} else {
				reason += " " + args[i];
			}
		}

		if (target == null) {
			get_uuid(args[0], reason, jailer_uuid, jailer, announce);
		} else {
			effect_jail(target.getUniqueId(), target.getName(), reason, jailer_uuid, jailer, announce);
		}

	}

	private void get_uuid(final String target_name, final String reason, final UUID jailer_uuid, final String jailer, final boolean announce) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + target_name);
					URLConnection conn = url.openConnection();
					conn.connect();
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String resp = reader.readLine();
					if (resp == null)
						resp = "";

					Pattern p = Pattern.compile("\"id\":\"(\\S+?)\"");
					Matcher m = p.matcher(resp);
					m.find();
					String uuid_str = m.group(1);
					uuid_str = uuid_str.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

					final UUID uuid = UUID.fromString(uuid_str);
					new BukkitRunnable() {
						@Override
						public void run() {
							effect_jail(uuid, target_name, reason,
									jailer_uuid, jailer, announce);
						}
					}.runTask(plugin);

				} catch (Exception e) {
					new BukkitRunnable() {
						@Override
						public void run() {
							send_message("Error retrieving data for " + target_name,
								       plugin.getServer().getPlayer(jailer_uuid),
								       ChatColor.RED);
						}
					}.runTask(plugin);
				}

			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Actually carries out the jailing of the player.
	 */
	private void effect_jail(UUID uuid, String target_name, String reason,
			UUID jailer_uuid, String sjailer, boolean announce) {

		Player jailer = null;
		if (jailer_uuid != null) {
			jailer = plugin.getServer().getPlayer(jailer_uuid);
			if (jailer != null)
				sjailer = jailer.getName();
		}

		JailedPlayer tmp = SQLite.get_player_info(uuid);
		if (tmp != null) {
			send_message("That player is already jailed, see /jailinfo " + tmp.playername + " for more information.", jailer, ChatColor.RED);
			return;
		}

		OfflinePlayer offline_tmp = plugin.getServer().getOfflinePlayer(uuid);
		if (offline_tmp.getFirstPlayed() == 0) {
			send_message("That player has never played on this server, you cannot jail them. If you wish to prevent them from joining, ban them instead.", jailer, ChatColor.RED);
			return;
		}

		boolean is_online;
		Location location;
		Player target = plugin.getServer().getPlayer(uuid);
		if (target == null) {
			is_online = false;
			location = new Location(plugin.getServer().getWorld("world"), 0, 63, 0);
		} else {
			is_online = true;
			location = target.getLocation();
			target_name = target.getName();
		}

		JailedPlayer jailedplayer = new JailedPlayer(uuid, target_name,
				reason, sjailer, location,
				(int) (System.currentTimeMillis() / 1000L),
				false, is_online);

		if (target != null) {
			target.teleport(Config.get_spawn());
			jailedplayer.add();
		}

		announce_message("Jailing " + target_name + " for:" + reason, jailer);
		if (announce) {
			for (Player p : plugin.getServer().getOnlinePlayers())
				p.sendMessage(ChatColor.LIGHT_PURPLE + "[Server] Jailing " + target_name + " for:" + reason);
		} else if (target != null) {
			target.sendMessage(ChatColor.RED + "You have been jailed for:" + reason);
		}

		jailedplayer.insert();
	}

	private void unjail_player(Player player, String[] args) {
		@SuppressWarnings("deprecation") /* deprecated for reasons not relevant here */
		Player target = plugin.getServer().getPlayer(args[0]);

		if (target != null) {
			JailedPlayer jailedplayer = SQLite.get_player_info(target.getUniqueId());

			if (jailedplayer != null) {
				Jail.jailed_players.remove(jailedplayer.uuid);
				target.teleport(jailedplayer.location);
				SQLite.delete_player_info(jailedplayer.uuid);
				announce_message("Unjailing " + target.getName(), player);
				target.sendMessage(ChatColor.GREEN + "You have been released from jail.");
				return;
			}
		}

		/* at this point it is assumed the player is not online */
		JailedPlayer jailedplayer = SQLite.get_player_info(args[0]);

		if (jailedplayer.online == false) {
			SQLite.delete_player_info(jailedplayer.uuid);
			announce_message("Unjailing never-jailed player " + jailedplayer.playername, player);
			return;
		}

		if (jailedplayer != null) {
			SQLite.set_to_be_released(jailedplayer.uuid);
			announce_message("Unjailing offline player " + jailedplayer.playername, player);
			return;		
		}

		send_message("No such player found.", player, ChatColor.RED);
	}

	private void show_info(Player player, String[] args) {
		if (args.length > 1) {
			send_message("Invalid arguments. Correct usage is /jailinfo [player]", player, ChatColor.RED);
			return;
		}

		if (args.length == 1 ) {
			JailedPlayer jailedplayer = SQLite.get_player_info(args[0]);

			if (jailedplayer == null) {
				send_message("That player does not appear to be jailed.", player, ChatColor.RED);
				return;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy, H:m:s");
			String msg = jailedplayer.playername + " (" + jailedplayer.uuid + ")"
				+ " was jailed on " + sdf.format(new Date(jailedplayer.jailed_time * 1000L))
				+ " by " + jailedplayer.jailer
				+ " for" + jailedplayer.reason + ".";
			if (jailedplayer.to_be_released)
				msg += "\nThis player is set to be released";

			send_message(msg, player, ChatColor.GOLD);

		} else if (args.length == 0) {
			send_message("The jail is in world " + Config.world + ". The two jail corners are at: " + Config.x1 + " " + Config.y1 + " " + Config.z1 + ", and " + Config.x2 + " " + Config.y2 + " " + Config.z2, player, ChatColor.GREEN);
			send_message("The jail spawn point is at " + Config.spawn.getWorld().getName() + " " + Config.spawn.getBlockX() + " " + Config.spawn.getBlockY() + " " + Config.spawn.getBlockZ(), player, ChatColor.GREEN);
			send_message("There are " + Jail.jailed_players.size() + " jailed players online.", player, ChatColor.GREEN);
			send_message("In total there are " + SQLite.amount_of_jailed_players() + " jailed players.", player, ChatColor.GREEN);
		}
	}

}

