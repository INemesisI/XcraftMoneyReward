package de.xcraft.inemesisi.moneyreward;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import de.xcraft.INemesisI.Library.XcraftPlugin;
import de.xcraft.INemesisI.Library.Message.Messenger;

public enum Msg {
	REWARD_DAILY("&aYou recieved &6$REWARD$&a for logging in today!"), //
	REWARD_ONLINE("&aYou just recieved &6$REWARD$&a for playing"), //
	REWARD_MOB("&aYou just recieved &6$REWARD$&a for killing a &3$MOB$"), //
	PENALTY_MOB("&cYou lost &6$REWARD$&c for killing a &3$MOB$!"), //
	PENALTY_CAMP("&cYou will lose now a small amount of money for killing Mobs!"), //
	PENALTY_INFO("&cYou lost &6$REWARD$&c in total for killing too many mobs!"), //
	ERR_CAMPING("&cYou dont recieve any rewards anymore, because you are camping!");

	public enum Replace {
		$PLAYER$("$Player$"), $REWARD$("$Reward$"), $MOB$("$Mob$");

		private String key;

		Replace(String key) {
			this.set(key);
		}

		private void set(String output) {
			key = output;
		}

		private String get() {
			return key;
		}

		public static Replace PLAYER(String replace) {
			$PLAYER$.set(replace);
			return $PLAYER$;
		}

		public static Replace REWARD(String replace) {
			$REWARD$.set(replace);
			return $REWARD$;
		}

		public static Replace MOB(String replace) {
			$MOB$.set(replace);
			return $MOB$;
		}
	}


	private String msg;

	Msg(String msg) {
		this.set(msg);
	}

	private void set(String output) {
		msg = output;
	}

	private String get() {
		return msg;
	}

	@Override
	public String toString() {
		String message = msg.replaceAll("&([0-9a-z])", "\u00a7$1");
		message = message.replace("\\n", "\n");
		return message;
	}

	public String toString(Replace r1) {
		String message = toString();
		message = message.replace(r1.name(), r1.get());
		return message;
	}

	public String toString(Replace r1, Replace r2) {
		String message = toString();
		message = message.replace(r1.name(), r1.get());
		message = message.replace(r2.name(), r2.get());
		return message;
	}

	public String toString(Replace[] repl) {
		String message = toString();
		for (Replace r : repl) {
			message = message.replace(r.name(), r.get());
		}
		return message;
	}

	public static void init(XcraftPlugin plugin) {
		File msgFile = new File(plugin.getDataFolder(), "locale.yml");
		if (!load(msgFile)) {
			return;
		}
		parseFile(msgFile);
	}

	private static boolean load(File file) {
		if (file.exists()) {
			return true;
		}
		try {
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (Msg m : Msg.values()) {
				String msg = m.get();
				if (msg.contains("\n")) {
					msg = msg.replace("\n", "\\n");
				}
				bw.write(m.name() + ": " + msg);
				bw.newLine();
			}
			bw.close();
			return true;
		} catch (Exception e) {
			Messenger.warning("Couldn't initialize locale.yml. Using defaults.");
			return false;
		}
	}

	private static void parseFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			// Check for BOM character.
			br.mark(1);
			int bom = br.read();
			if (bom != 65279) {
				br.reset();
			}
			String s;
			while ((s = br.readLine()) != null) {
				process(s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			Messenger.warning("Problem with locale.yml. Using defaults.");
			return;
		}
	}

	/**
	 * Helper-method for parsing the strings from the announcements-file.
	 */
	private static void process(String s) {
		String[] split = s.split(": ", 2);
		try {
			Msg msg = Msg.valueOf(split[0]);
			msg.set(split[1]);
		} catch (Exception e) {
			Messenger.warning(split[0] + " is not a valid key. Check locale.yml.");
			return;
		}
	}

}
