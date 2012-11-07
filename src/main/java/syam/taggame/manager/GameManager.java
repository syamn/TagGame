/**
 * TagGame - Package: syam.taggame.manager
 * Created: 2012/11/07 19:27:21
 */
package syam.taggame.manager;

import java.util.logging.Logger;

import syam.taggame.TagGame;
import syam.taggame.game.Game;

/**
 * GameManager (GameManager.java)
 * @author syam(syamn)
 */
public class GameManager {
	// Logger
	private static final Logger log = TagGame.log;
	private static final String logPrefix = TagGame.logPrefix;
	private static final String msgPrefix = TagGame.msgPrefix;

	private final TagGame plugin;
	public GameManager(final TagGame plugin){
		this.plugin = plugin;
	}

	private static Game game = null;

	public static void setGame(final Game game){
		GameManager.game = game;
	}

	public static Game getGame(){
		return GameManager.game;
	}
}
