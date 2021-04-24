package net.simpvp.Jail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class PlayerLogin implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		SQLite.insert_ip(player);
		final JailedPlayer jailedplayer = SQLite.get_player_info(player.getUniqueId());
		if (jailedplayer == null) {
			String jailed_friends = SQLite.get_ip_jailed(
					player.getAddress().getHostString());
			if (jailed_friends != null) {
				Jail.instance.getLogger().info(player.getName()
						+ " shares IPs with jailed players: " + jailed_friends);
				for (Player p : Jail.instance.getServer().getOnlinePlayers()) {
					if (p.isOp()) {
						p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + player.getName()
								+ " shares IPs with jailed players: " + jailed_friends);
					}
				}
			}

			check_asn(player);

			return;
		}

		if (jailedplayer.to_be_released) {
			/* as the player will not have an entity associated with them at this
			 * stage, we have to try for a bit to see when they're done logging in
			 * and then teleport them then */
			final long start = System.currentTimeMillis();

			new BukkitRunnable() {
				@Override
				public void run() {
					if (System.currentTimeMillis() > start + 10 * 1000) {
						this.cancel();
						Jail.instance.getLogger().info("Unable to unjail " + jailedplayer.playername);
					}

					Player player = Jail.instance.getServer().getPlayer(jailedplayer.uuid);
					if (player != null) {
						player.teleport(jailedplayer.location);
						SQLite.delete_player_info(player.getUniqueId());
						Jail.instance.getLogger().info("Releasing " + jailedplayer.playername + " from jail.");
						this.cancel();
					}
				}
			}.runTaskTimer(Jail.instance, 20, 20);

			return;
		} else if (!jailedplayer.online) {
			jailedplayer.online = true;
			SQLite.update_player_location(jailedplayer.uuid,
					player.getLocation());
			SQLite.set_has_been_online(jailedplayer.uuid);
		}

		SQLite.insert_ip_jailed(player);
		Jail.instance.getLogger().info("Jailed player " + player.getName() + " has connected.");
		jailedplayer.add();
	}

	private void check_asn(Player player) {
		if (GeoIP.bad_asns == null || GeoIP.bad_asns.isEmpty()) {
			return;
		}

		String as = GeoIP.getAs(player.getAddress().getAddress());
		Integer asn = GeoIP.getAsn(as);
		if (asn == null) {
			return;
		}

		if (!GeoIP.bad_asns.contains(asn)) {
			Jail.instance.getLogger().info(String.format("%s is joining from %s", player.getName(), as));
			return;
		}

		String msg = player.getName() + " is joining from bad network " + as;
		Jail.instance.getLogger().info(msg);
		for (Player p : Jail.instance.getServer().getOnlinePlayers()) {
			if (!p.isOp()) {
				continue;
			}

			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + msg);
		}
	}
}
