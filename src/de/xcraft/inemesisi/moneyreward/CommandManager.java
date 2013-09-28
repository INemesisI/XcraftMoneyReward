package de.xcraft.inemesisi.moneyreward;

import org.bukkit.command.CommandSender;

import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;

public class CommandManager extends XcraftCommandManager {

	public CommandManager(XcraftMoneyReward plugin) {
		super(plugin);
	}

	@Override
	protected void registerCommands() {
		registerBukkitCommand("moneyreward");
	}
	
	@Override
	public void onReload(CommandSender sender) {
		Msg.init(plugin);
		super.onReload(sender);
	}

}
