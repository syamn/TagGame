/**
 * TagGame - Package: syam.taggame.game
 * Created: 2012/11/07 20:52:17
 */
package syam.taggame.game;

import java.util.logging.Logger;

import syam.taggame.TagGame;

/**
 * GameTimerTask (GameTimerTask.java)
 * @author syam(syamn)
 */
public class GameTimerTask implements Runnable{
	// Logger
	private static final Logger log = TagGame.log;
	private static final String logPrefix = TagGame.logPrefix;
	private static final String msgPrefix = TagGame.msgPrefix;

	private final TagGame plugin;
	private Game game;

	/**
	 * コンストラクタ
	 * @param plugin TagGame
	 * @param game Game
	 */
	public GameTimerTask(final TagGame plugin, final Game game){
		this.plugin = plugin;
		this.game = game;
	}

	@Override
	public void run() {
		/* 1秒ごとに呼ばれる */

		// 残り時間がゼロになった
		if (game.getRemainTime() <= 0){
			game.cancelTimerTask(); // タイマー停止
			game.timeout(); // ゲーム終了
			return;
		}

		// 15秒以下
		if (game.getRemainTime() <= 15){
			game.message(msgPrefix+ "&a鬼ごっこ終了まで あと "+game.getRemainTime()+" 秒です！");
		}
		// 30秒前
		else if (game.getRemainTime() == 30){
			game.message(msgPrefix+ "&a鬼ごっこ終了まで あと "+game.getRemainTime()+" 秒です！");
		}
		// 60秒間隔
		else if ((game.getRemainTime() % 60) == 0){
			int remainMin = game.getRemainTime() / 60;
			game.message(msgPrefix+ "&a鬼ごっこ終了まで あと "+remainMin+" 分です！");
		}

		// remainsec--
		game.tickRemainTime();
	}
}
