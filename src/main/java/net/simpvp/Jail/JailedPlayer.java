package net.simpvp.Jail;

import java.text.SimpleDateFormat;
import java.util.Date;
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
	public boolean online;

	public JailedPlayer(UUID uuid, String playername, String reason, String jailer, Location location, int jailed_time, boolean to_be_released, boolean online) {
		this.uuid = uuid;
		this.playername = playername;
		this.reason = reason;
		this.jailer = jailer;
		this.location = location;
		this.jailed_time = jailed_time;
		this.to_be_released = to_be_released;
		this.online = online;
	}

	public void add() {
		Jail.jailed_players.add(this.uuid);
	}

	public void insert() {
		SQLite.insert_player_info(this);
	}

	public int get_to_be_released() {
		int ret = 0;
		if (this.to_be_released)
			ret ^= 1;
		if (!this.online)
			ret ^= 2;
		return ret;
	}

	/**
	 * Returns a text description of this jailed player.
	 */
	public String get_info() {
		SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy, H:m:s");

		String msg = this.playername + " (" + this.uuid + ")"
			+ " was jailed on " + sdf.format(new Date(this.jailed_time * 1000L))
			+ " by " + this.jailer
			+ " for" + this.reason + ".";

		if (this.to_be_released) {
			msg += "\nThis player is set to be released";
		}

		return msg;
	}

}

