/**
 * TagGame - Package: syam.taggame.listener
 * Created: 2012/11/08 19:31:56
 */
package syam.taggame.listener;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import syam.taggame.TagGame;
import syam.taggame.enums.GameState;
import syam.taggame.enums.GameTeam;
import syam.taggame.manager.GameManager;

/**
 * InventoryListener (InventoryListener.java)
 * @author syam(syamn)
 */
public class InventoryListener implements Listener{
	public static final Logger log = TagGame.log;
	private static final String logPrefix = TagGame.logPrefix;
	private static final String msgPrefix = TagGame.msgPrefix;

	private final TagGame plugin;

	public InventoryListener(final TagGame plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */
	// プレイヤーがインベントリをクリックした
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event){
		// getSlot() == 39: 装備(頭)インベントリ
		if (event.getSlotType() != SlotType.ARMOR || event.getSlot() != 39){
			return;
		}

		// プレイヤーインスタンスを持たなければ返す
		if (!(event.getWhoClicked() instanceof Player)){
			return;
		}
		Player player = (Player) event.getWhoClicked();

		if (!GameManager.getGame().getState().equals(GameState.RUNNING)){
			return;
		}
		GameTeam team = GameManager.getGame().getPlayerTeam(player);
		if (team != null){
			// ゲーム参加中のプレイヤーはイベントキャンセル
			event.setCurrentItem(new ItemStack(team.getBlockID(), 1, (short)0, team.getBlockData()));

			event.setCancelled(true);
			event.setResult(Result.DENY);
		}
	}
}
