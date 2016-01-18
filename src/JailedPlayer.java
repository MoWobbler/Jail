package org.c4k3.Jail;

import java.util.UUID;

import org.bukkit.Location;

/**
 * Class representing the stored information about a jailed player
 */
public class JailedPlayer {
	public UUID uuid;
	public String playername;
	public String reason;
	public String jailer;
	public Location location;
	public int jailed_time;
	public boolean to_be_released;

	public JailedPlayer(UUID uuid, String playername, String reason, String jailer, Location location, int jailed_time, boolean to_be_released) {
		this.uuid = uuid;
		this.playername = playername;
		this.reason = reason;
		this.jailer = jailer;
		this.location = location;
		this.jailed_time = jailed_time;
		this.to_be_released = to_be_released;
	}

	public void add() {
		Jail.jailed_players.add(this.uuid);
	}

	public void insert() {
		SQLite.insert_player_info(this);
	}

}
