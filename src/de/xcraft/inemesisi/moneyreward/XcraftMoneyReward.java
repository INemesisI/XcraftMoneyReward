package de.xcraft.inemesisi.moneyreward;

import java.util.Calendar;
import java.util.Date;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import de.xcraft.INemesisI.Library.XcraftPlugin;
import de.xcraft.INemesisI.Library.Message.Messenger;

public class XcraftMoneyReward extends XcraftPlugin {

	private RewardManager pluginManager = null;
	private ConfigManager configManager = null;
	private CommandManager commandManager = null;
	private EventListener eventListener = null;
	private Messenger messenger = null;
	private Economy economy = null;
	private Permission permission = null;
	private Essentials essentials = null;

	@Override
	protected void setup() {
		this.messenger = Messenger.getInstance(this);
		this.configManager = new ConfigManager(this);
		this.pluginManager = new RewardManager(this);
		this.eventListener = new EventListener(this);
		this.commandManager = new CommandManager(this);
		configManager.load();
		this.setupEconomy();
		this.setupPermissions();
		this.setupEssentials();
		startScheduler();
	}

	@Override
	public RewardManager getPluginManager() {
		return pluginManager;
	}

	@Override
	public ConfigManager getConfigManager() {
		return configManager;
	}

	@Override
	public CommandManager getCommandManager() {
		return commandManager;
	}

	@Override
	public EventListener getEventListener() {
		return eventListener;
	}

	@Override
	public Messenger getMessenger() {
		return messenger;
	}

	public Economy getEconomy() {
		return economy;
	}

	public Permission getPermission() {
		return permission;
	}

	public Essentials getEssentials() {
		return essentials;
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
		if (getConfigManager().isUseEssentials()) {
			essentials = (Essentials) this.getServer().getPluginManager().getPlugin("Essentials");
		}
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
				pluginManager.checkOnlineTime();
				pluginManager.checkBlacklist();
			}
		}, sec * 20, 60 * 20); // every minute
	}

	@Override
	public void onDisable() {
		if (configManager.isremoveBlacklistedOnChunkunload()) {
			for (World world : this.getServer().getWorlds()) {
				for (Entity e : world.getEntities()) {
					if (e.hasMetadata("SpawnReason") && configManager.getBlacklist().contains(e.getMetadata("SpawnReason").get(0).asString())) {
						e.remove();
					}
				}
			}
		}
		super.onDisable();
	}
}
