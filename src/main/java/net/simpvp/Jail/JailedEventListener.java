package net.simpvp.Jail;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class JailedEventListener implements Listener {

	/**
	 * Ensures dead jailed players keep their inventory.
	 */
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!(Jail.jailed_players.contains(event.getEntity().getUniqueId())))
			return;

		event.setKeepInventory(true);
		event.getDrops().clear();
	}

	/**
	 * Ensure that jailed players respawn in the jail
	 */
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setRespawnLocation(Config.get_spawn());
	}

	/**
	 * Teleports all jailed players outside spawn back to the inside,
	 * preventing them from leaving
	 */
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
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
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setCancelled(true);
		event.getPlayer().sendMessage(ChatColor.RED + "Jailed players are not allowed to drop items.");
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		String command = event.getMessage().split(" ")[0].trim().substring(1).toLowerCase();
		if (!Config.is_command_blocked(command))
			return;

		event.setCancelled(true);
		event.getPlayer().sendMessage(ChatColor.RED + "Jailed players are not allowed to use that command.");
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(Jail.jailed_players.contains(event.getDamager().getUniqueId())))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId()))
				|| event.getInventory().getType() == InventoryType.PLAYER)
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		if (!(Jail.jailed_players.contains(event.getEntity().getUniqueId())))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onPlayerBlockPlace(BlockPlaceEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onVehicleEnterEvent(VehicleEnterEvent event) {
		if (!(Jail.jailed_players.contains(event.getEntered().getUniqueId())))
			return;

		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		if (!(Jail.jailed_players.contains(event.getPlayer().getUniqueId())))
			return;

		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (!(Jail.jailed_players.contains(event.getEntity().getUniqueId())))
			return;

		event.setCancelled(true);
	}
	
	/**
	 * Prevent jailed players from placing end crystals
	 */
	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onCrystalPlace(PlayerInteractEvent event) {
	    if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Jail.jailed_players.contains(event.getPlayer().getUniqueId())) {
	        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.END_CRYSTAL)) {
	        	event.setCancelled(true);  
	        }
	    }
	}
}

