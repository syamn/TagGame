/**
 * TagGame - Package: syam.taggame
 * Created: 2012/11/02 6:00:01
 */
package syam.taggame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import syam.taggame.command.BaseCommand;
import syam.taggame.command.HelpCommand;
import syam.taggame.command.InfoCommand;
import syam.taggame.command.JoinCommand;
import syam.taggame.command.ReadyCommand;
import syam.taggame.command.ReloadCommand;
import syam.taggame.command.SetCommand;
import syam.taggame.command.StartCommand;
import syam.taggame.enums.GameResult;
import syam.taggame.game.Game;
import syam.taggame.listener.InventoryListener;
import syam.taggame.listener.PlayerListener;
import syam.taggame.manager.GameManager;
import syam.taggame.util.Actions;
import syam.taggame.util.DynmapHandler;
import syam.taggame.util.Metrics;

/**
 * TagGame (TagGame.java)
 * @author syam(syamn)
 */
public class TagGame extends JavaPlugin{
	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[TagGame] ";
	public final static String msgPrefix = "&6[TagGame] &f";

	// ** Listener **
	PlayerListener playerListener = new PlayerListener(this);
	InventoryListener inventoryListener = new InventoryListener(this);

	// ** Commands **
	private List<BaseCommand> commands = new ArrayList<BaseCommand>();

	// ** Private Classes **
	private ConfigurationManager config;

	// ** Instance **
	private static TagGame instance;

	// Hookup plugins
	//public boolean usingDeathNotifier = false;
	private static Vault vault = null;
	private static Economy economy = null;
	private DynmapHandler dynmap = null;

	/**
	 * プラグイン起動処理
	 */
	@Override
	public void onEnable(){
		instance  = this;

		PluginManager pm = getServer().getPluginManager();
		config = new ConfigurationManager(this);

		// loadconfig
		try{
			config.loadConfig(true);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
		}

		// Vault
		setupVault();

		// プラグインを無効にした場合進まないようにする
		if (!pm.isPluginEnabled(this)){
			return;
		}

		// Regist Listeners
		pm.registerEvents(playerListener, this);
		pm.registerEvents(inventoryListener, this);

		// コマンド登録
		registerCommands();

		new Game(this).loadProfile("default");

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");

		setupMetrics(); // mcstats
	}

	/**
	 * プラグイン停止処理
	 */
	@Override
	public void onDisable(){
		commands.clear();

		// ゲームを終わらせる
		Game game = GameManager.getGame();
		if (game != null){
			switch (game.getState()){
				case RUNNING:
					game.cancelTimerTask();
					game.finish(GameResult.STOP, false, "Unloading TagGame Plugin");
					game.log("Game finished because disabling plugin..");
					break;
				case READYING:
					game.message(msgPrefix+ "&cあなたのエントリーはプラグインが無効になったため取り消されました");
					Actions.broadcastMessage(msgPrefix+ "&cプラグインが無効にされたため、参加受付中のゲームは削除されました");
					break;
				default:
					break;
			}
		}

		GameManager.getGame().saveProfile();

		getServer().getScheduler().cancelTasks(this);

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	/**
	 * コマンドを登録
	 */
	private void registerCommands(){
		// Intro Commands
		commands.add(new HelpCommand());

		// User Commands
		commands.add(new InfoCommand());
		commands.add(new JoinCommand());

		// Game Commands
		commands.add(new ReadyCommand());
		commands.add(new StartCommand());
		commands.add(new SetCommand());

		// Other Commands
		commands.add(new ReloadCommand());
	}

	/**
	 * Vaultプラグインにフック
	 */
	private void setupVault(){
		Plugin plugin = this.getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null & plugin instanceof Vault) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			// 経済概念のプラグインがロードされているかチェック
			if(economyProvider==null){
	        	log.warning(logPrefix+"Economy plugin not Fount. Disabling plugin.");
		        getPluginLoader().disablePlugin(this);
		        return;
			}

			try{
				vault = (Vault) plugin;
				economy = economyProvider.getProvider();
			} // 例外チェック
			catch(Exception e){
				log.warning(logPrefix+"Could NOT be hook to Vault. Disabling plugin.");
		        getPluginLoader().disablePlugin(this);
		        return;
			}
			log.info(logPrefix+"Hooked to Vault!");
		} else {
			// Vaultが見つからなかった
	        log.warning(logPrefix+"Vault was NOT found! Disabling plugin.");
	        getPluginLoader().disablePlugin(this);
	        return;
	    }
	}

	/**
	 * Metricsセットアップ
	 */
	private void setupMetrics(){
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException ex) {
			log.warning(logPrefix+"cant send metrics data!");
			ex.printStackTrace();
		}
	}

	/**
	 * コマンドが呼ばれた
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
		if (cmd.getName().equalsIgnoreCase("taggame")){
			if(args.length == 0){
				// 引数ゼロはヘルプ表示
				args = new String[]{"help"};
			}

			outer:
				for (BaseCommand command : commands.toArray(new BaseCommand[0])){
					String[] cmds = command.getName().split(" ");
					for (int i = 0; i < cmds.length; i++){
						if (i >= args.length || !cmds[i].equalsIgnoreCase(args[i])){
							continue outer;
						}
						// 実行
						return command.run(this, sender, args, commandLabel);
					}
				}
			// 有効コマンドなし ヘルプ表示
			new HelpCommand().run(this, sender, args, commandLabel);
			return true;
		}
		return false;
	}

	public void debug(final String msg){
		if (config.isDebug()){
			log.info(logPrefix+ "[DEBUG]" + msg);
		}
	}

	/* getter */
	/**
	 * コマンドを返す
	 * @return List<BaseCommand>
	 */
	public List<BaseCommand> getCommands(){
		return commands;
	}

	/**
	 * 設定マネージャを返す
	 * @return ConfigurationManager
	 */
	public ConfigurationManager getConfigs() {
		return config;
	}

	/**
	 * Vaultを返す
	 * @return Vault
	 */
	public Vault getVault(){
		return this.vault;
	}

	/**
	 * dynmapハンドラを返す
	 * @return DynmapHandler
	 */
	public DynmapHandler getDynmap(){
		return dynmap;
	}

	/**
	 * Economyを返す
	 * @return Economy
	 */
	public Economy getEconomy(){
		return this.economy;
	}


	/**
	 * インスタンスを返す
	 * @return TagGameインスタンス
	 */
	public static TagGame getInstance(){
		return instance;
	}
}
