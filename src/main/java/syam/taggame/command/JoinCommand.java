/**
 * TagGame - Package: syam.taggame.command
 * Created: 2012/11/08 17:17:44
 */
package syam.taggame.command;

import java.util.ArrayList;

import syam.taggame.Perms;
import syam.taggame.enums.GameState;
import syam.taggame.enums.GameTeam;
import syam.taggame.exception.CommandException;
import syam.taggame.game.Game;
import syam.taggame.manager.GameManager;
import syam.taggame.util.Actions;

/**
 * JoinCommand (JoinCommand.java)
 * @author syam(syamn)
 */
public class JoinCommand extends BaseCommand {
	public JoinCommand(){
		bePlayer = true;
		name = "join";
		argLength = 0;
		usage = "[tagger] <- join the game";
	}

	@Override
	public void execute() throws CommandException {
		Game game = GameManager.getGame();

		if (!game.getState().equals(GameState.READYING)){
			throw new CommandException("&c現在参加受付中ではありません！");
		}

		// 既に参加していないかチェック
		if (game.isJoined(player)){
			throw new CommandException("&cあなたは既にこのゲームにエントリーしています！");
		}

		// TODO call event here

		game.addPlayer(player, (args.size() > 0) ? GameTeam.TAGGER : GameTeam.RUNNER);


		// 所属チーム取得
		GameTeam team = game.getPlayerTeam(player);
		Actions.broadcastMessage(msgPrefix+"&aプレイヤー'&6"+player.getName()+"&a'が"+team.getColor()+team.getTeamName()+"チーム&aに参加しました！");
		//game.message(msgPrefix+"&aプレイヤー'&6"+player.getName()+"&a'が"+team.getColor()+team.getTeamName()+"チーム&aに参加しました！");
	}

	@Override
	public boolean permission() {
		return Perms.JOIN.has(sender);
	}
}
