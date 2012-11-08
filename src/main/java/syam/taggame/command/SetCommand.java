/**
 * TagGame - Package: syam.taggame.command
 * Created: 2012/11/08 0:35:12
 */
package syam.taggame.command;

import java.util.ArrayList;
import java.util.List;

import syam.taggame.Perms;
import syam.taggame.enums.GameState;
import syam.taggame.enums.GameTeam;
import syam.taggame.exception.CommandException;
import syam.taggame.game.Game;
import syam.taggame.manager.GameManager;
import syam.taggame.util.Actions;
import syam.taggame.util.Util;

/**
 * SetCommand (SetCommand.java)
 * @author syam(syamn)
 */
public class SetCommand extends BaseCommand{
	public SetCommand(){
		bePlayer = false;
		name = "set";
		argLength = 0;
		usage = "<option> [value] <- set option";
	}

	@Override
	public void execute() throws CommandException {
		if (args.size() <= 0){
			Actions.message(sender, "&c設定項目を指定してください！");
			sendAvailableConf();
			return;
		}

		Game game = GameManager.getGame();

		// 開始中チェック
		if (!game.getState().equals(GameState.WAITING)){
			throw new CommandException("&cこのゲームは現在待機中ではありません！");
		}

		// 設定可能項目名を回す
		Configables conf = null;
		for (Configables check : Configables.values()){
			if (check.name().equalsIgnoreCase(args.get(0))){
				conf = check;
				break;
			}
		}
		if (conf == null){
			Actions.message(sender, "&cその設定項目は存在しません！");
			sendAvailableConf();
			return;
		}

		// 設定タイプが ConfigType.SIMPLE の場合はサブ引数が2つ以上必要
		if (conf.getConfigType() == ConfigType.SIMPLE){
			if (args.size() < 2){
				throw new CommandException("&c引数が足りません！ 設定する値を入力してください！");
			}
		}

		// 処理を分ける
		switch (conf){
			case SPAWN:
				setSpawn(game); return;
			case GAMETIME:
				setGameTime(game); return;

			// 定義漏れ
			default:
				Actions.message(sender, "&c設定項目が不正です 開発者にご連絡ください");
				log.warning(logPrefix+ "Undefined configables! Please report this!");
				break;
		}
	}

	/* ***** ここから各設定関数 ****************************** */
	private void setSpawn(final Game game) throws CommandException{
		// 引数チェック
		if (args.size() < 2){
			throw new CommandException("&c引数が足りません！設定するチームを指定してください！");
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(1))){
				team = tm;
				break;
			}
		}
		if (team == null){
			throw new CommandException("&cチーム'"+args.get(1)+"'が見つかりません！");
		}

		// スポーン地点設定
		game.setSpawn(team, player.getLocation());

		Actions.message(player, "&a"+team.getTeamName()+"チームのスポーン地点を設定しました！");
	}
	private void setGameTime(final Game game) throws CommandException{
		int num = 60 * 10; // デフォルト10分
		try{
			num = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			throw new CommandException("&cオプションの値が整数ではありません！");
		}

		if (num <= 0){
			throw new CommandException("&c値が不正です！正数を入力してください！");
		}
		game.setGameTime(num);

		String sec = num+"秒";
		if (num >= 60) sec = sec + "("+Actions.getTimeString(num)+")";
		Actions.message(sender, "&a鬼ごっこのゲーム時間は "+sec+" に設定されました！");
	}
	/* ***** ここまで **************************************** */

	enum Configables{
		SPAWN ("スポーン地点", ConfigType.POINT),
		GAMETIME ("ゲームの制限時間(秒)", ConfigType.SIMPLE),
		;

		private String configName;
		private ConfigType configType;

		Configables(final String configName, final ConfigType configType){
			this.configName = configName;
			this.configType = configType;
		}

		String getConfigName(){
			return this.configName;
		}
		ConfigType getConfigType(){
			return this.configType;
		}
	}

	enum ConfigType{
		AREA,
		POINT,
		MANAGER,
		SIMPLE,
		;
	}

	/**
	 * 設定可能な設定とヘルプをsenderに送信する
	 */
	private void sendAvailableConf(){
		List<String> col = new ArrayList<String>();
		for (Configables conf : Configables.values()){
			col.add(conf.name());
		}

		Actions.message(sender, "&6 " + Util.join(col, "/").toLowerCase());
		//Actions.message(sender, "&6 stage / base / spawn / flag / chest / gametime / teamlimit / award / entryfee / protect");
	}

	@Override
	public boolean permission() {
		return Perms.SET.has(sender);
	}
}
