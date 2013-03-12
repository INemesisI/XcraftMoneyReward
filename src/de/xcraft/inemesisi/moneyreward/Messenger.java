package de.xcraft.inemesisi.moneyreward;

import java.util.logging.Logger;

import org.bukkit.command.CommandSender;


public class Messenger {

	private static final Logger	log		= Logger.getLogger("Minecraft");
	private static final String	prefix	= "[MoneyReward] ";

	public static boolean tellPlayer(CommandSender p, String msg) {
		if ((p == null) || msg.equals(" ")) { return false; }
		p.sendMessage(msg);
		return true;
	}

	public static void info(String msg) {
		log.info(prefix + msg);
	}

	public static void warning(String msg) {
		log.warning(prefix + msg);
	}

	public static void severe(String msg) {
		log.severe(prefix + msg);
	}
}
