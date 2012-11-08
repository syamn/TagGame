/**
 * TagGame - Package: syam.taggame
 * Created: 2012/11/02 6:14:14
 */
package syam.taggame;

import org.bukkit.permissions.Permissible;

/**
 * Perms (Perms.java)
 * @author syam(syamn)
 */
public enum Perms {
	/* 権限ノード */
	// User Commands
	INFO ("user.info"),
	JOIN ("user.join"),

	// Game Commands
	READY ("admin.ready"),
	START ("admin.start"),
	SET ("admin.set"),

	// Admin Commands
	RELOAD ("admin.reload"),

	;

	// ノードヘッダー
	final String HEADER = "taggame.";
	private String node;

	/**
	 * コンストラクタ
	 * @param node 権限ノード
	 */
	Perms(final String node){
		this.node = HEADER + node;
	}

	/**
	 * 指定したプレイヤーが権限を持っているか
	 * @param player Permissible. Player, CommandSender etc
	 * @return boolean
	 */
	public boolean has(final Permissible perm){
		if (perm == null) return false;
		return perm.hasPermission(node); // only support SuperPerms
	}
}
