package net.simpvp.Jail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerLogin implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerLogin(PlayerJoinEvent event) {
		final JailedPlayer jailedplayer = SQLite.get_player_info(event.getPlayer().getUniqueId());
		if (jailedplayer == null)
			return;

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
					event.getPlayer().getLocation());
			SQLite.set_has_been_online(jailedplayer.uuid);
		}

		Jail.instance.getLogger().info("Jailed player " + event.getPlayer().getName() + " has connected.");
		jailedplayer.add();
	}

}

