package net.simpvp.Jail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerLogin implements Listener {

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
			return;

		final JailedPlayer jailedplayer = SQLite.get_player_info(event.getUniqueId());
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
		}

		Jail.instance.getLogger().info("Jailed player " + event.getName() + " has connected.");
		jailedplayer.add();
	}

}
