/**
 * TagGame - Package: syam.taggame.listener
 * Created: 2012/11/08 19:10:39
 */
package syam.taggame.listener;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import syam.taggame.TagGame;
import syam.taggame.enums.GameState;
import syam.taggame.enums.GameTeam;
import syam.taggame.game.Game;
import syam.taggame.manager.GameManager;
import syam.taggame.util.Actions;

/**
 * PlayerListener (PlayerListener.java)
 * @author syam(syamn)
 */
public class PlayerListener implements Listener{
	private static final Logger log = TagGame.log;
	private static final String logPrefix = TagGame.logPrefix;
	private static final String msgPrefix = TagGame.msgPrefix;

	private final TagGame plugin;

	public PlayerListener(final TagGame plugin){
		this.plugin = plugin;
	}

	// プレイヤーがコマンドを使おうとした
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();

		if (!GameManager.getGame().getState().equals(GameState.RUNNING)){
			return;
		}

		if (GameManager.getGame().isJoined(player)){
			String cmdMsg = event.getMessage().trim();
			String cmds[] = cmdMsg.split(" ");
			String cmd = null;

			if (cmds.length > 1){
				cmd = cmds[0].trim();
			}else{ // cmds.length == 1
				cmd = cmdMsg;
			}

			// ゲーム中のプレイヤー 禁止コマンドを操作
			for (String s : plugin.getConfigs().getDisableCommands()){
				// 禁止コマンドと同じコマンドがある
				if (s.trim().equalsIgnoreCase(cmd)){
					// コマンド実行キャンセル
					event.setCancelled(true);
					Actions.message(player, msgPrefix+"このコマンドは鬼ごっこ中に使えません！");
					return;
				}
			}
		}
	}

	// プレイヤーがリスポーンした
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRespawn(final PlayerRespawnEvent event){
		Player player = event.getPlayer();

		if (!GameManager.getGame().getState().equals(GameState.RUNNING)){
			return;
		}

		Game game = GameManager.getGame();
		GameTeam team = game.getPlayerTeam(player);

		if (team != null){
			Location loc = game.getSpawn(team);
			if (loc == null){
				Actions.message(player, msgPrefix+ "&cあなたのチームのスポーン地点が設定されていません");
				log.warning(logPrefix+ "Player "+player.getName()+" died, But undefined spawn-location. Team: " +team.name());
			}else{
				Actions.message(player, msgPrefix+ "&c[*]&6鬼ごっこはあと &a"+Actions.getTimeString(game.getRemainTime())+"&6 残っています！");
				event.setRespawnLocation(loc);
				player.getInventory().setHelmet(new ItemStack(team.getBlockID(), 1, (short)0, team.getBlockData()));
			}
		}
	}
}
