package de.xcraft.inemesisi.moneyreward;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import de.xcraft.INemesisI.Library.XcraftPlugin;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;
import de.xcraft.inemesisi.moneyreward.Msg.Replace;

public class RewardManager extends XcraftPluginManager {

	private ConfigManager configManager;
	public Map<Player, RewardPlayer> players = new HashMap<Player, RewardPlayer>();
	public Essentials essentials;
	public Economy economy;

	public RewardManager(XcraftMoneyReward plugin) {
		super(plugin);
		this.configManager = plugin.getConfigManager();
		this.essentials = plugin.getEssentials();
		this.economy = plugin.getEconomy();
	}

	public void checkOnlineTime() {
		for (Player player : players.keySet()) {
			if (!player.hasPermission("XcraftMoneyReward.Online"))
				return;
			if (essentials != null) {
				User user = essentials.getUser(player);
				if ((user != null) && user.isAfk()) {
					continue;
				}
			}
			RewardPlayer rp = players.get(player);
			rp.onlinetime++;
			if (rp.onlinetime == configManager.getOnlineRewardIntervall()) {
				double reward = configManager.getOnlineReward(player);
				if (this.reward(player.getName(), reward) && configManager.isOnlineRewardNotify()) {
					plugin.getMessenger().sendInfo(player,
							Msg.REWARD_DAILY.toString(Replace.PLAYER(player.getName()), Replace.REWARD(economy.format(reward))), true);

				}
			}
		}
	}

	public void checkBlacklist() {
		if (configManager.isremoveBlacklistedOnChunkunload()) {
			for (World world : plugin.getServer().getWorlds()) {
				for (Entity e : world.getEntities()) {
					if (e.hasMetadata("SpawnReason") && e.getMetadata("SpawnReason").size() > 0
							&& configManager.getBlacklist().contains(e.getMetadata("SpawnReason").get(0).asString())) {
						if (e.getTicksLived() > (configManager.getRemoveBlacklistedAfterMins() * 20 * 60)) {
							e.remove();
							continue;
						}
						List<Entity> list = e.getNearbyEntities(5, 5, 5);
						if (list.size() > configManager.getRemoveBlacklistedStacked()) {
							for (int i = configManager.getRemoveBlacklistedStacked(); i < list.size(); i++) {
								list.get(i).remove();
							}
						}
					}
				}
			}
		}
	}

	public boolean reward(String player, double amount) {
		if (amount > 0) {
			economy.depositPlayer(player, amount);
			return true;
		}
		if (amount < 0 && (this.economy.getBalance(player) + amount) > 0) {
			economy.withdrawPlayer(player, -amount);
			return true;
		}
		return false;
	}

	@Override
	public XcraftPlugin getPlugin() {
		return null;
	}

}
