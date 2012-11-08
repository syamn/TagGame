/**
 * TagGame - Package: syam.taggame.command
 * Created: 2012/11/08 17:08:50
 */
package syam.taggame.command;

import java.util.Map;
import java.util.Set;

import syam.taggame.Perms;
import syam.taggame.enums.GameState;
import syam.taggame.enums.GameTeam;
import syam.taggame.exception.CommandException;
import syam.taggame.game.Game;
import syam.taggame.manager.GameManager;
import syam.taggame.util.Actions;

/**
 * InfoCommand (InfoCommand.java)
 * @author syam(syamn)
 */
public class InfoCommand extends BaseCommand {
	public InfoCommand(){
		bePlayer = false;
		name = "info";
		argLength = 0;
		usage = "<- show info";
	}

	@Override
	public void execute() throws CommandException {
		Game game = GameManager.getGame();
		if (game == null){
			throw new CommandException("&cゲームが見つかりません！");
		}

		// ゲームステータス取得
		String status = "&7待機中";
		if (game.getState().equals(GameState.RUNNING)){
			String time = Actions.getTimeString(game.getRemainTime());
			status = "&c開始中&7(あと:"+time+")";
		}
		else if (game.getState().equals(GameState.READYING)){
			status = "&6受付中";
		}

		// プレイヤーリスト構築
		String players = "";
		int cnt_players = 0;
		for (Map.Entry<GameTeam, Set<String>> entry : game.getPlayersMap().entrySet()){
			String color = entry.getKey().getColor();
			for (String name : entry.getValue()){
				players = players + color + name + "&f, ";
				cnt_players++;
			}
		}

		if (players != "") players = players.substring(0, players.length() - 2);
		else players = "&7参加プレイヤーなし";


		// テキスト構築
		String s1 = "&b ステータス: " + status;
		String s2 = "&b プレイヤーリスト&7("+cnt_players+"人)&b: "+players;

		// メッセージ送信
		Actions.message(sender, "&a ==================&b GameDetail &a==================");
		Actions.message(sender, s1);
		Actions.message(sender, s2);
		Actions.message(sender, "&a ================================================");
	}

	@Override
	public boolean permission() {
		return Perms.INFO.has(sender);
	}
}