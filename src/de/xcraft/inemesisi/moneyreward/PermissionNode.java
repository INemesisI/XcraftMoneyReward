package de.xcraft.inemesisi.moneyreward;

public enum PermissionNode {
	DAILY("XcraftMoneyReward.Daily"), //
	ONLINE("XcraftMoneyReward.Online"), //
	MOB("XcraftMoneyReward.Mob"), //
	; //

	private String	node;

	PermissionNode(String node) {
		this.node = node;
	}

	public String get() {
		return node;
	}
}
