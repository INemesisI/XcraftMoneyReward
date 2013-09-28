package de.xcraft.inemesisi.moneyreward;

import java.util.Calendar;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;

import de.xcraft.INemesisI.Library.XcraftEventListener;
import de.xcraft.inemesisi.moneyreward.Msg.Replace;

public class EventListener extends XcraftEventListener {

	private RewardManager rManager;
	private ConfigManager cManager;

	public EventListener(XcraftMoneyReward plugin) {
		super(plugin);
		rManager = plugin.getPluginManager();
		cManager = plugin.getConfigManager();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		rManager.players.put(event.getPlayer(), new RewardPlayer(event.getPlayer()));
		if (!event.getPlayer().hasPermission("XcraftMoneyReward.Daily"))
			return;
		Calendar current = Calendar.getInstance();
		current.setTime(new Date());
		Calendar lastplayed = Calendar.getInstance();
		lastplayed.setTimeInMillis(event.getPlayer().getLastPlayed());
		if (lastplayed.get(Calendar.DAY_OF_MONTH) < current.get(Calendar.DAY_OF_MONTH)) {
			double reward = cManager.getDailyReward(event.getPlayer());
			if (rManager.reward(event.getPlayer().getName(), reward) && cManager.isDailyRewardNotify()) {
				plugin.getMessenger()
						.sendInfo(
								event.getPlayer(),
								Msg.REWARD_DAILY.toString(Replace.PLAYER(event.getPlayer().getName()),
										Replace.REWARD(rManager.economy.format(reward))), true);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		rManager.players.remove(event.getPlayer());
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (cManager.isUseBlacklist() && event.getEntity().hasMetadata("SpawnReason") && event.getEntity().getMetadata("SpawnReason").size() > 0
				&& cManager.getBlacklist().contains(event.getEntity().getMetadata("SpawnReason").get(0).asString()))
			return;
		if (!(event.getEntity().getKiller() instanceof Player))
			return;
		Player player = event.getEntity().getKiller();
		if (!player.hasPermission("XcraftMoneyReward.Mob") || player.getGameMode().equals(GameMode.CREATIVE))
			return;
		if ((event.getEntity().getType() == EntityType.SLIME) && (((Slime) event.getEntity()).getSize() != 4))
			return;
		double reward = cManager.getMobReward(player, event.getEntity());
		if (!this.updateCamping(player, event.getEntity(), reward > 0)) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}

		if (this.isCamping(player)) {
			// if ((reward != 0) && plugin.getCfg().isMobRewardNotify()) {
			// Messenger.tellPlayer(player, Msg.ERR_CAMPING.toString());
			// }
		} else {
			RewardPlayer rwp = rManager.players.get(player);
			if (rwp.campkills > 0) {
				reward *= Math.pow(cManager.getCampingReducement(), rwp.campkills - 1);
			}
			if (rManager.reward(player.getName(), reward) && cManager.isMobRewardNotify()) {
				Replace[] replace = { Replace.PLAYER(player.getName()), Replace.REWARD(rManager.economy.format(reward)),
						Replace.MOB(event.getEntityType().getName().toLowerCase()) };
				if (reward > 0) {
					plugin.getMessenger().sendInfo(player, Msg.REWARD_MOB.toString(replace), true);
				} else {
					plugin.getMessenger().sendInfo(player, Msg.PENALTY_MOB.toString(replace), true);
				}
			}
		}
	}

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		event.getEntity().setMetadata("SpawnReason", new FixedMetadataValue(plugin, event.getSpawnReason().toString()));
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (cManager.isUseBlacklist() && cManager.isremoveBlacklistedOnChunkunload()) {
			for (Entity e : event.getChunk().getEntities()) {
				if (e.hasMetadata("SpawnReason") && e.getMetadata("SpawnReason").size() > 0
						&& cManager.getBlacklist().contains(e.getMetadata("SpawnReason").get(0).asString())) {
					e.remove();
				}
			}
		}
	}

	public boolean updateCamping(Player player, Entity e, boolean inform) {
		if (cManager.isUseCamping()) {
			RewardPlayer rp = rManager.players.get(player);
			long lastkill = rp.lastkill;
			rp.lastkill = player.getPlayerTime();
			Location camp = rp.camp;
			Location curr = player.getLocation();
			int r = cManager.getCampingRadius();
			if ((((camp.getX() - r) <= curr.getX()) && ((camp.getX() + r) >= curr.getX())) //
					&& (((camp.getY() - r) <= curr.getY()) && ((camp.getY() + r) >= curr.getY())) //
					&& (((camp.getZ() - r) <= curr.getZ()) && ((camp.getZ() + r) >= curr.getZ()))
					&& (player.getPlayerTime() - lastkill) < (cManager.getCampingResetTime() * 20)) {
				// camping!
				rp.campkills++;
				if (rp.campkills == cManager.getCampingCap()) {
					plugin.getMessenger().sendInfo(player, Msg.ERR_CAMPING.toString(), true);
				}
				// check for penalty
				if (e.hasMetadata("SpawnReason") && e.getMetadata("SpawnReason").size() > 0
						&& e.getMetadata("SpawnReason").get(0).asString().equals("SPAWNER")
						&& rp.campkills >= cManager.getPenaltyAfter()) {
					int penalty = cManager.getPenaltyAmount() * (rp.campkills - cManager.getPenaltyAfter());
					if (rManager.reward(player.getName(), penalty)) {
						if (rp.campkills == cManager.getPenaltyAfter() && inform) {
							plugin.getMessenger().sendInfo(player, Msg.PENALTY_CAMP.toString(), true);
						}
						if (rp.campkills % 10 == 0) {
							int amount = rp.campkills - cManager.getPenaltyAfter();
							int cost = 0;
							for (int i = 1; i < amount; i--) {
								cost += i;
							}
							cost *= 0.01;
							plugin.getMessenger().sendInfo(player,
									Msg.PENALTY_INFO.toString(Replace.PLAYER(player.getName()), Replace.REWARD(rManager.economy.format(cost))), true);
						}
					} else
						return false;
				}
			} else {
				// no camping... reset
				rp.camp = curr;
				rp.campkills = 1;
			}
		}
		return true;
	}

	public boolean isCamping(Player player) {
		if (!cManager.isUseCamping())
			return false;
		else
			return (rManager.players.get(player).campkills >= cManager.getCampingCap());
	}
}
