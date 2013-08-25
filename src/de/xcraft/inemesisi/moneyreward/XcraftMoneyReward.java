package de.xcraft.inemesisi.moneyreward;

import java.util.Calendar;
import java.util.Date;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import de.xcraft.INemesisI.Library.XcraftPlugin;

public class XcraftMoneyReward extends XcraftPlugin {
	public Economy economy = null;
	public Permission permission = null;
	public Essentials essentials = null;


	@Override
	public void onDisable() {
		if (((ConfigManager) configManager).isremoveBlacklistedOnChunkunload()) {
			for (World world : this.getServer().getWorlds()) {
				for (Entity e : world.getEntities()) {
					if (e.hasMetadata("SpawnReason")
							&& ((ConfigManager) configManager).getBlacklist().contains(
									e.getMetadata("SpawnReason").get(0).asString())) {
						e.remove();
					}
				}
			}
		}
		super.onDisable();
	}

	@Override
	protected void setup() {
		Msg.init(this);
		configManager = new ConfigManager(this);
		pluginManager = new RewardManager(this);
		commandManager = new CommandManager(this);
		eventListener = new EventListener(this);
		configManager.load();
		if (((ConfigManager) configManager).isUseEssentials()) {
			this.setupEssentials();
		}
		for (Player player : this.getServer().getOnlinePlayers()) {
			((RewardManager) pluginManager).players.put(player, new RewardPlayer(player));
		}
		if (((ConfigManager) configManager).isOnlineRewardActive()) {
			this.startScheduler();
		}
		this.setupEconomy();
		this.setupPermissions();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = this.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return permission != null;
	}

	private boolean setupEssentials() {
		essentials = (Essentials) this.getServer().getPluginManager().getPlugin("Essentials");
		return essentials != null;
	}

	private void startScheduler() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int sec = cal.get(Calendar.SECOND);
		sec = 60 - sec;
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				((RewardManager) pluginManager).checkOnlineTime();
				((RewardManager) pluginManager).checkBlacklist();
			}
		}, sec * 20, 60 * 20); // every minute
	}



	public Economy getEconomy() {
		return economy;
	}

	public Permission getPermission() {
		return permission;
	}
}
