package de.xcraft.inemesisi.moneyreward;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;


public enum Msg {
	REWARD_DAILY("&aYou recieved &6$Reward$&a for logging in today!"), //
	REWARD_ONLINE("&aYou just recieved &6$Reward$&a for playing"), //
	REWARD_MOB("&aYou just recieved &6$Reward$&a for killing a &3$Mob$"), //
	PENALTY_MOB("&cYou lost &6$Reward$&c for killing a &3$Mob$!"), //
	ERR_CAMPING("&cYou dont recieve any rewards anymore, because you are camping!");

	private String	msg;

	public enum Key {
		$Player$("$Player$"), $Reward$("$Reward$"), $Mob$("$Mob$");

		private String	replace;

		Key(String replace) {
			this.setReplace(replace);
		}

		public String getReplace() {
			return replace;
		}

		public void setReplace(String replace) {
			this.replace = replace;
		}

		public static Key $Player$(String replace) {
			$Player$.setReplace(replace);
			return $Player$;
		}

		public static Key $Reward$(String replace) {
			$Reward$.setReplace(replace);
			return $Reward$;
		}

		public static Key $Mob$(String replace) {
			$Mob$.setReplace(replace);
			return $Mob$;
		}
	}

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
		String message = msg.replaceAll("&([a-f0-9])", "\u00A7$1");
		return message;
	}

	public String toString(Key key1, Key key2, Key key3) {
		String message = msg.replaceAll("&([a-f0-9])", "\u00A7$1");
		message = message.replace(key1.name(), key1.getReplace());
		message = message.replace(key2.name(), key2.getReplace());
		message = message.replace(key3.name(), key3.getReplace());
		return message;
	}

	public static void init(MoneyReward plugin) {
		File msgFile = new File(plugin.getDataFolder(), "locale.yml");
		if (!load(msgFile)) { return; }
		parseFile(msgFile);
	}

	private static boolean load(File file) {
		if (file.exists()) { return true; }
		try {
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (Msg m : Msg.values()) {
				bw.write(m.name() + ": " + m.get());
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
		String[] split = s.split(": ");
		try {
			Msg msg = Msg.valueOf(split[0]);
			msg.set(split[1]);
		} catch (Exception e) {
			Messenger.warning(split[0] + " is not a valid key. Check locale.yml.");
			return;
		}
	}
}
