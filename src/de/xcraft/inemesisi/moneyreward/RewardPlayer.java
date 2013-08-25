package de.xcraft.inemesisi.moneyreward;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RewardPlayer {

	public int onlinetime;
	public Location camp;
	public int campkills;
	public long lastkill;

	public RewardPlayer(Player player) {
		onlinetime = 0;
		camp = player.getLocation();
		campkills = 0;
		lastkill = player.getPlayerTime();
	}
}