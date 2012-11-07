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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import syam.taggame.command.BaseCommand;
import syam.taggame.command.HelpCommand;
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
	//ServerListener serverListener = new ServerListener(this);

	// ** Commands **
	private List<BaseCommand> commands = new ArrayList<BaseCommand>();

	// ** Private Classes **
	private ConfigurationManager config;

	// ** Replaces **
	public String mcVersion = "";

	// ** Instance **
	private static TagGame instance;

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

		// プラグインを無効にした場合進まないようにする
		if (!pm.isPluginEnabled(this)){
			return;
		}

		// Regist Listeners
		//pm.registerEvents(serverListener, this);

		// コマンド登録
		registerCommands();

		// Building replaces
		try {
			buildReplaces();
		}catch (Exception ex){
			log.warning("Could not build replace strings! (Check plugin update!)");
			ex.printStackTrace();
		}

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
		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	public void buildReplaces(){
		// mcVersion
		final Matcher matcher = Pattern.compile("\\(MC: (.+)\\)").matcher(Bukkit.getVersion());
		this.mcVersion = (matcher.find()) ? matcher.group(1) : "Unknown";
	}

	/**
	 * コマンドを登録
	 */
	private void registerCommands(){
		// Intro Commands
		commands.add(new HelpCommand());

		// Game Commands

		// Other Commands

		// Admin Commands
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
		if (cmd.getName().equalsIgnoreCase("tagagme")){
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
	 * インスタンスを返す
	 * @return TagGameインスタンス
	 */
	public static TagGame getInstance(){
		return instance;
	}
}
