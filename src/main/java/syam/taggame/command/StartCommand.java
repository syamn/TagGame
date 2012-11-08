/**
 * TagGame - Package: syam.taggame.command
 * Created: 2012/11/08 16:45:17
 */
package syam.taggame.command;

import java.util.ArrayList;
import java.util.Set;

import syam.taggame.Perms;
import syam.taggame.enums.GameState;
import syam.taggame.exception.CommandException;
import syam.taggame.game.Game;
import syam.taggame.manager.GameManager;

/**
 * StartCommand (StartCommand.java)
 * @author syam(syamn)
 */
public class StartCommand extends BaseCommand{
	public StartCommand(){
		bePlayer = false;
		name = "start";
		argLength = 0;
		usage = "<- start game";
	}

	@Override
	public void execute() throws CommandException {
		Game game = GameManager.getGame();

		if (!game.getState().equals(GameState.READYING)){
			throw new CommandException("&c鬼ごっこは参加受付状態ではありません");
		}

		for (Set<String> teamSet : game.getPlayersMap().values()){
			if (teamSet.size() <= 0){
				throw new CommandException("&cプレイヤーが参加していないチームがあります");
			}
		}

		// check starting countdown
		if (game.getStarttimerThreadID() != -1){
			throw new CommandException("&cこのゲームは既に開始カウントダウン中です！");
		}

		// start
		//game.start(sender);
		game.start_timer(sender);
	}

	@Override
	public boolean permission() {
		return Perms.START.has(sender);
	}
}
