package de.xcraft.inemesisi.moneyreward;

import java.util.Calendar;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import de.xcraft.inemesisi.moneyreward.Msg.Key;

public class EventListener implements Listener {

	private MoneyReward plugin = null;
	int added = 0, denied = 0, despawned = 0;

	public EventListener(MoneyReward instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.players.put(event.getPlayer(), new RewardPlayer(event.getPlayer()));
		if (!event.getPlayer().hasPermission(PermissionNode.DAILY.get())) {
			return;
		}
		Calendar current = Calendar.getInstance();
		current.setTime(new Date());
		Calendar lastplayed = Calendar.getInstance();
		lastplayed.setTimeInMillis(event.getPlayer().getLastPlayed());
		if (lastplayed.get(Calendar.DAY_OF_MONTH) < current.get(Calendar.DAY_OF_MONTH)) {
			double reward = plugin.getCfg().getDailyReward(event.getPlayer());
			if (plugin.reward(event.getPlayer().getName(), reward) && plugin.getCfg().isDailyRewardNotify()) {
				Messenger.tellPlayer(
						event.getPlayer(),
						Msg.REWARD_DAILY.toString(Key.$Player$(event.getPlayer().getName()),
								Key.$Reward$(plugin.getEconomy().format(reward)), Key.$Mob$));
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.players.remove(event.getPlayer());
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (plugin.getCfg().isUseBlacklist() && plugin.blacklist.contains(event.getEntity().getEntityId())) {
			plugin.blacklist.remove((Integer) event.getEntity().getEntityId());
			denied++;
			return;
		}
		if (!(event.getEntity().getKiller() instanceof Player)) {
			return;
		}
		Player player = event.getEntity().getKiller();
		if (!player.hasPermission(PermissionNode.MOB.get())) {
			return;
		}
		if ((event.getEntity().getType() == EntityType.SLIME) && (((Slime) event.getEntity()).getSize() != 4)) {
			return;
		}
		this.updateCamping(player);
		double reward = plugin.getCfg().getMobReward(player, event.getEntity());
		if (this.isCamping(player)) {
			// if ((reward != 0) && plugin.getCfg().isMobRewardNotify()) {
			// Messenger.tellPlayer(player, Msg.ERR_CAMPING.toString());
			// }
		} else {
			RewardPlayer rwp = plugin.players.get(player);
			if (rwp.campkills > 0) {
				reward *= Math.pow(plugin.getCfg().getCampingReducement(), rwp.campkills - 1);
			}
			if (plugin.reward(player.getName(), reward) && plugin.getCfg().isMobRewardNotify()) {
				if (reward > 0) {
					Messenger.tellPlayer(
							player,
							Msg.REWARD_MOB.toString(Key.$Player$(player.getName()),
									Key.$Reward$(plugin.getEconomy().format(reward)),
									Key.$Mob$(event.getEntityType().getName())));
				} else {
					Messenger.tellPlayer(
							player,
							Msg.PENALTY_MOB.toString(Key.$Player$(player.getName()),
									Key.$Reward$(plugin.getEconomy().format(reward)),
									Key.$Mob$(event.getEntityType().getName())));
				}
			}
		}
	}

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		SpawnReason sr = event.getSpawnReason();
		if (plugin.getCfg().isUseBlacklist() && plugin.getCfg().getBlacklist().contains(sr.toString())) {
			Entity e = event.getEntity();
			// Dont block Entitys that only spawn in mobspawners
			if ((sr == SpawnReason.SPAWNER)
					&& ((e.getType() == EntityType.BLAZE) || (e.getType() == EntityType.CAVE_SPIDER))) {
				return;
			}
			plugin.blacklist.add(event.getEntity().getEntityId());
			added++;
		}
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (plugin.getCfg().isUseBlacklist() && plugin.getCfg().isremoveBlacklistedOnChunkunload()) {
			for (Entity e : event.getChunk().getEntities()) {
				if (plugin.blacklist.contains(e.getEntityId())) {
					plugin.blacklist.remove((Integer) e.getEntityId());
					despawned++;
					e.remove();
				}
			}
		}
	}

	public void updateCamping(Player pl) {
		if (!plugin.getCfg().isUseCamping()) {
			return;
		}
		RewardPlayer rp = plugin.players.get(pl);
		Location camp = rp.camp;
		Location curr = pl.getLocation();
		int r = plugin.getCfg().getCampingRadius();
		if ((((camp.getX() - r) <= curr.getX()) && ((camp.getX() + r) >= curr.getX())) //
				&& (((camp.getY() - r) <= curr.getY()) && ((camp.getY() + r) >= curr.getY())) //
				&& (((camp.getZ() - r) <= curr.getZ()) && ((camp.getZ() + r) >= curr.getZ()))) {
			rp.campkills++;
		} else {
			rp.camp = curr;
			rp.campkills = 1;
		}
	}

	public boolean isCamping(Player player) {
		if (!plugin.getCfg().isUseCamping()) {
			return false;
		} else {
			return (plugin.players.get(player).campkills >= plugin.getCfg().getCampingCap());
		}
	}
}
