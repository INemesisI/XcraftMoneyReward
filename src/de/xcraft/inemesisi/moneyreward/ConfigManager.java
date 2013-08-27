package de.xcraft.inemesisi.moneyreward;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import de.xcraft.INemesisI.Library.Manager.XcraftConfigManager;

public class ConfigManager extends XcraftConfigManager {

	// Daily-Reward
	private boolean dailyRewardActive = true;
	private boolean dailyRewardNotify = true;
	// Online-Reward
	private boolean onlineRewardActive = true;
	private boolean onlineRewardNotify = false;
	private int onlineRewardIntervall = 0;
	private boolean useEssentialsafk = false;
	// Mob-Reward
	private boolean mobRewardActive = true;
	private boolean mobRewardNotify = true;
	// Camping
	private boolean useCamping = false;
	private int campingRadius = 0;
	private int campingCap = 0;
	private double campingReducement = 1;
	private int campingResetTime = 30;
	private int penaltyAfter = 100;
	private int penaltyAmount = -10;
	// Blacklist
	private boolean useBacklist = false;
	private List<String> blacklist = new ArrayList<String>();
	private boolean removeBlacklistedOnChunkunload = false;
	private int removeBlacklistedAfterMins = 5;
	private int removeBlacklistedStacked = 200;
	// Multiplier
	private boolean useMultiplier = false;

	public ConfigManager(XcraftMoneyReward plugin) {
		super(plugin);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load() {
		dailyRewardActive = config.getBoolean("Daily.Options.Active");
		dailyRewardNotify = config.getBoolean("Daily.Options.Notify");
		onlineRewardActive = config.getBoolean("Online.Options.Active");
		onlineRewardNotify = config.getBoolean("Online.Options.Notify");
		onlineRewardIntervall = config.getInt("Online.Options.Intervall");
		useEssentialsafk = config.getBoolean("Online.Options.UseEssentialsAFK");
		mobRewardActive = config.getBoolean("Mob.Options.Active");
		mobRewardNotify = config.getBoolean("Mob.Options.Notify");
		useCamping = config.getBoolean("Mob.Options.Camping.Use");
		campingRadius = config.getInt("Mob.Options.Camping.Radius");
		campingCap = config.getInt("Mob.Options.Camping.Cap");
		campingReducement = config.getDouble("Mob.Options.Camping.Reducement");
		campingResetTime = config.getInt("Mob.Options.Camping.ResetTime");
		penaltyAfter = config.getInt("Mob.Options.Camping.Penalty.After");
		penaltyAmount = config.getInt("Mob.Options.Camping.Penalty.Amount");
		useMultiplier = config.getBoolean("Mob.Multiplier.Use");
		useBacklist = config.getBoolean("Mob.Options.BlackList.Use");
		blacklist = (List<String>) config.getList("Mob.Options.BlackList.Deny");
		removeBlacklistedOnChunkunload = config.getBoolean("Mob.Options.BlackList.KillOnChunkUnload");
		removeBlacklistedAfterMins = config.getInt("Mob.Options.BlackList.KillAfterMinutes");
		removeBlacklistedStacked = config.getInt("Mob.Options.BlackList.KillStacked");
	}

	@Override
	public void save() {
	}

	public double getDailyReward(Player player) {
		for (String group : config.getConfigurationSection("Daily.Reward").getKeys(false)) {
			for (String pgroup : ((XcraftMoneyReward) plugin).getPermission().getPlayerGroups((String) null, player.getName())) {
				if (group.equals(pgroup))
					return config.getDouble("Daily.Reward." + group);
			}
			if (group.equals("default"))
				return config.getDouble("Daily.Reward.default");
		}
		return 0;
	}

	public double getOnlineReward(Player player) {
		for (String group : config.getConfigurationSection("Online.Reward").getKeys(false)) {
			for (String pgroup : ((XcraftMoneyReward) plugin).getPermission().getPlayerGroups((String) null, player.getName())) {
				if (group.equals(pgroup))
					return config.getDouble("Online.Reward." + group);
			}
			if (group.equals("default"))
				return config.getDouble("Online.Reward.default");
		}
		return 0;
	}

	public double getMobReward(Player player, LivingEntity mob) {
		EntityType type = mob.getType();
		double reward = 0;
		if (config.contains("Mob.Reward." + type.toString())) {
			reward = config.getDouble("Mob.Reward." + type.toString());
		}
		if ((type == EntityType.CREEPER) && ((Creeper) mob).isPowered()) {
			reward = config.getDouble("Mob.Reward.CREEPER_POWERED");
		} else if ((type == EntityType.WOLF) && ((Wolf) mob).isTamed()) {
			if (((Wolf) mob).getOwner().getName().equals(player.getName())) {
				reward = config.getDouble("Mob.Reward.WOLF_TAMED_SELF");
			} else {
				reward = config.getDouble("Mob.Reward.WOLF_TAMED");
			}
		} else if ((type == EntityType.OCELOT) && ((Ocelot) mob).isTamed()) {
			if (((Ocelot) mob).getOwner().getName().equals(player.getName())) {
				reward = config.getDouble("Mob.Reward.OCELOT_TAMED_SELF");
			} else {
				reward = config.getDouble("Mob.Reward.OCELOT_TAMED");
			}
		}
		if (useMultiplier) {
			reward = this.getMultipliedReward(player, reward);
		}
		return reward;
	}

	private double getMultipliedReward(Player player, double reward) {
		double worldmp = 1.0;
		double groupmp = 1.0;
		double timemp = 1.0;
		if (config.contains("Mob.Multiplier.World." + player.getWorld().getName())) {
			worldmp = config.getDouble("Mob.Multiplier.World." + player.getWorld().getName());
		}
		breakpoint: for (String group : config.getConfigurationSection("Mob.Multiplier.Group").getKeys(false)) {
			for (String pgroup : ((XcraftMoneyReward) plugin).getPermission().getPlayerGroups((String) null, player.getName())) {
				if (group.equals(pgroup)) {
					groupmp = config.getDouble("Mob.Multiplier.Group." + group);
					break breakpoint;
				}
			}
			if (group.equals("default")) {
				groupmp = config.getDouble("Mob.Multiplier.Group.default");
			}
		}
		long time = player.getWorld().getTime();
		if ((time > 00000) && (time < 06000)) {
			timemp = config.getDouble("Mob.Multiplier.Time.Sunrise");
		}
		if ((time > 06000) && (time < 12000)) {
			timemp = config.getDouble("Mob.Multiplier.Time.Day");
		}
		if ((time > 12000) && (time < 18000)) {
			timemp = config.getDouble("Mob.Multiplier.Time.Sunset");
		}
		if ((time > 18000) && (time < 24000)) {
			timemp = config.getDouble("Mob.Multiplier.Time.Night");
		}
		double mp = (worldmp + groupmp + timemp) / 3;
		return reward * mp;
	}

	public boolean isDailyRewardActive() {
		return dailyRewardActive;
	}

	public void setDailyRewardActive(boolean dailyRewardActive) {
		this.dailyRewardActive = dailyRewardActive;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public boolean isOnlineRewardActive() {
		return onlineRewardActive;
	}

	public boolean isMobRewardActive() {
		return mobRewardActive;
	}

	public boolean isDailyRewardNotify() {
		return dailyRewardNotify;
	}

	public boolean isOnlineRewardNotify() {
		return onlineRewardNotify;
	}

	public boolean isMobRewardNotify() {
		return mobRewardNotify;
	}

	public int getOnlineRewardIntervall() {
		return onlineRewardIntervall;
	}

	public boolean isUseEssentials() {
		return useEssentialsafk;
	}

	public boolean isUseCamping() {
		return useCamping;
	}

	public int getCampingRadius() {
		return campingRadius;
	}

	public int getCampingCap() {
		return campingCap;
	}

	public double getCampingReducement() {
		return campingReducement;
	}

	public boolean isUseMultiplier() {
		return useMultiplier;
	}

	public boolean isUseBlacklist() {
		return useBacklist;
	}

	public List<String> getBlacklist() {
		return blacklist;
	}

	public boolean isremoveBlacklistedOnChunkunload() {
		return removeBlacklistedOnChunkunload;
	}

	public int getRemoveBlacklistedAfterMins() {
		return removeBlacklistedAfterMins;
	}

	public void setRemoveBlacklistedAfterMins(int removeBlacklistedAfterMins) {
		this.removeBlacklistedAfterMins = removeBlacklistedAfterMins;
	}

	public int getRemoveBlacklistedStacked() {
		return removeBlacklistedStacked;
	}

	public void setRemoveBlacklistedStacked(int removeBlacklistedStacked) {
		this.removeBlacklistedStacked = removeBlacklistedStacked;
	}

	public int getPenaltyAfter() {
		return penaltyAfter;
	}

	public void setPenaltyAfter(int penaltyAfter) {
		this.penaltyAfter = penaltyAfter;
	}

	public int getPenaltyAmount() {
		return penaltyAmount;
	}

	public void setPenaltyAmount(int penaltyAmount) {
		this.penaltyAmount = penaltyAmount;
	}

	public int getCampingResetTime() {
		return campingResetTime;
	}

	public void setCampingResetTime(int campingResetTime) {
		this.campingResetTime = campingResetTime;
	}
}