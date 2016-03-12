package net.simpvp.Jail;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

		@SuppressWarnings("deprecation") /* is not stored by name, but by uuid */
		Player target = plugin.getServer().getPlayer(args[0]);
		if (target == null) {
			send_message("No such player found. The player must be online. If the player is offline, ban the player instead.", player, ChatColor.RED);
			return;
		}

		String jailer = null;
		if (player == null) {
			jailer = "CONSOLE";
		} else {
			jailer = player.getName();
		}

		if (Jail.jailed_players.contains(target.getUniqueId())) {
			send_message(target.getName() + " is already jailed.", player, ChatColor.RED);
			return;
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

		JailedPlayer jailedplayer = new JailedPlayer(target.getUniqueId(), target.getName(), reason, jailer, target.getLocation(), (int) (System.currentTimeMillis() / 1000L), false);
		target.teleport(Config.get_spawn());
		jailedplayer.add();
		announce_message("Jailing " + target.getName() + " for:" + reason, player);
		if (announce) {
			for (Player onplayer : plugin.getServer().getOnlinePlayers())
				onplayer.sendMessage(ChatColor.LIGHT_PURPLE + "[Server] Jailing " + target.getName() + " for:" + reason);
		} else {
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

