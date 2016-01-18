package net.simpvp.Jail;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class JailedEventListener implements Listener {

	/**
	 * Ensures dead jailed players keep their inventory.
	 */
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!(Jail.jailed_players.contains(event.getEntity().getUniqueId())))
			return;

		event.setKeepInventory(true);
		event.setKeepLevel(true);
	}

	/**
	 * Ensure that jailed players respawn in the jail
	 */
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setRespawnLocation(Config.get_spawn());
	}

	/**
	 * Teleports all jailed players outside spawn back to the inside,
	 * preventing them from leaving
	 */
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		if (Config.is_inside_jail(event.getTo()))
			return;

		event.getPlayer().teleport(Config.get_spawn());
		event.getPlayer().sendMessage(ChatColor.RED + "You are jailed.");
	}

	/**
	 * Prevent jailed players from dropping any items
	 */
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setCancelled(true);
		event.getPlayer().sendMessage(ChatColor.RED + "Jailed players are not allowed to drop items.");
	}

	@EventHandler(priority=EventPriority.NORMAL,ignoreCancelled=true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		String command = event.getMessage().split(" ")[0].trim().substring(1).toLowerCase();
		if (!Config.is_command_blocked(command))
			return;

		event.setCancelled(true);
		event.getPlayer().sendMessage(ChatColor.RED + "Jailed players are not allowed to use that command.");
	}

}
