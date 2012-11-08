/**
 * TagGame - Package: syam.taggame
 * Created: 2012/11/02 6:02:34
 */
package syam.taggame;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import syam.taggame.util.Util;

/**
 * ConfigurationManager (ConfigurationManager.java)
 * @author syam(syamn)
 */
public class ConfigurationManager {
	// Logger
	private static final Logger log = TagGame.log;
	private static final String logPrefix = TagGame.logPrefix;
	private static final String msgPrefix = TagGame.msgPrefix;

	private TagGame plugin;
	private FileConfiguration conf;

	private static File pluginDir = new File("plugins", "TagGame");

	// デフォルトの設定定数
	private final String defaultDetailDirectory = "plugins/TagGame/detail/";

	// 設定項目
	/* Basic Configs */

	/* Games Configs */
	private int startCountdownInSec = 10;

	/* Logging Config */
	private String detailDirectory = defaultDetailDirectory;

	/* Debug Config */
	private boolean debug = false;

	/**
	 * コンストラクタ
	 * @param plugin
	 */
	public ConfigurationManager(final TagGame plugin){
		this.plugin = plugin;
		pluginDir = this.plugin.getDataFolder();
	}

	/**
	 * 設定をファイルから読み込む
	 */
	public void loadConfig(final boolean initialLoad) throws Exception{
		// ディレクトリ作成
		createDirs();

		// 設定ファイルパス取得
		File file = new File(pluginDir, "config.yml");
		// 無ければデフォルトコピー
		if (!file.exists()){
			extractResource(System.getProperty("file.separator") + "config.yml", pluginDir, false, true);
			log.info(logPrefix+ "config.yml is not found! Created default config.yml!");
		}

		plugin.reloadConfig();

		// 先にバージョンチェック
		double version = plugin.getConfig().getDouble("DontTouchThisConfig", 0.1D);
		checkver(version);

		/* Basic Configs */

		/* Games Configs */
		startCountdownInSec = plugin.getConfig().getInt("StartCountdownInSec", 10);

		/* Logging Config */
		detailDirectory = plugin.getConfig().getString("DetailDirectory", defaultDetailDirectory);

		/* Debug Configs */
		debug = plugin.getConfig().getBoolean("Debug", false);

		// create dirs
		createDir(new File(detailDirectory));
	}

	// 設定 getter/setter ここから
	/* Basic Configs */

	/* Games Configs */
	public int getStartCountdownInSec(){
		return this.startCountdownInSec;
	}
	/* Logging Config */
	public String getDetailDirectory(){
		return this.detailDirectory;
	}
	/* Debug Configs */
	public boolean isDebug(){
		return this.debug;
	}
	// 設定 getter/setter ここまで

	/**
	 * 設定ファイルに設定を書き込む
	 * @throws Exception
	 */
	@Deprecated
	public boolean save(){
		try {
			plugin.getConfig().save(new File(plugin.getDataFolder() + System.getProperty("file.separator") + "config.yml"));
		} catch (IOException ex) {
			log.warning(logPrefix+ "Couldn't write config file!");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 必要なディレクトリ群を作成する
	 */
	private void createDirs(){
		createDir(plugin.getDataFolder());
	}

	/**
	 * 存在しないディレクトリを作成する
	 * @param dir File 作成するディレクトリ
	 */
	private static void createDir(final File dir) {
		// 既に存在すれば作らない
		if (dir.isDirectory()){
			return;
		}
		if (!dir.mkdir()){
			log.warning(logPrefix+ "Can't create directory: "+dir.getName());
		}
	}

	/**
	 * 設定ファイルのバージョンをチェックする
	 * @param ver
	 */
	private void checkver(final double ver){
		double configVersion = ver;
		double nowVersion = 0.1D;

		String versionString = plugin.getDescription().getVersion();
		try{
			// Support maven and Jenkins build number
			int index = versionString.indexOf("-");
			if (index > 0){
				versionString = versionString.substring(0, index);
			}
			nowVersion = Double.parseDouble(versionString);
		}catch (NumberFormatException ex){
			log.warning(logPrefix+ "Cannot parse version string!");
		}

		// 比較 設定ファイルのバージョンが古ければ config.yml を上書きする
		if (configVersion < nowVersion){
			// 先に古い設定ファイルをリネームする
			String destName = "oldconfig-v"+configVersion+".yml";
			String srcPath = new File(pluginDir, "config.yml").getPath();
			String destPath = new File(pluginDir, destName).getPath();
			try{
				copyTransfer(srcPath, destPath);
				log.info(logPrefix+ "Copied old config.yml to "+destName+"!");
			}catch(Exception ex){
				log.warning(logPrefix+ "Cannot copy old config.yml!");
			}

			// config.ymlを強制コピー
			extractResource("/config.yml", pluginDir, true, false);

			log.info(logPrefix+ "Deleted existing configuration file and generate a new one!");
		}
	}

	/**
	 * リソースファイルをファイルに出力する
	 * @param from 出力元のファイルパス
	 * @param to 出力先のファイルパス
	 * @param force jarファイルの更新日時より新しいファイルが既にあっても強制的に上書きするか
	 * @param checkenc 出力元のファイルを環境によって適したエンコードにするかどうか
	 * @author syam
	 */
	static void extractResource(String from, File to, boolean force, boolean checkenc){
		File of = to;

		// ファイル展開先がディレクトリならファイルに変換、ファイルでなければ返す
		if (to.isDirectory()){
			String filename = new File(from).getName();
			of = new File(to, filename);
		}else if(!of.isFile()){
			log.warning(logPrefix+ "not a file:" + of);
			return;
		}

		// ファイルが既に存在する場合は、forceフラグがtrueでない限り展開しない
		if (of.exists() && !force){
			return;
		}

		OutputStream out = null;
		InputStream in = null;
		InputStreamReader reader = null;
		OutputStreamWriter writer =null;
		DataInputStream dis = null;
		try{
			// jar内部のリソースファイルを取得
			URL res = TagGame.class.getResource(from);
			if (res == null){
				log.warning(logPrefix+ "Can't find "+ from +" in plugin Jar file");
				return;
			}
			URLConnection resConn = res.openConnection();
			resConn.setUseCaches(false);
			in = resConn.getInputStream();

			if (in == null){
				log.warning(logPrefix+ "Can't get input stream from " + res);
			}else{
				// 出力処理 ファイルによって出力方法を変える
				if (checkenc){
					// 環境依存文字を含むファイルはこちら環境

					reader = new InputStreamReader(in, "UTF-8");
					writer = new OutputStreamWriter(new FileOutputStream(of)); // 出力ファイルのエンコードは未指定 = 自動で変わるようにする

					int text;
					while ((text = reader.read()) != -1){
						writer.write(text);
					}
				}else{
					// そのほか

					out = new FileOutputStream(of);
					byte[] buf = new byte[1024]; // バッファサイズ
					int len = 0;
					while((len = in.read(buf)) >= 0){
						out.write(buf, 0, len);
					}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			// 後処理
			try{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			}catch (Exception ex){}
		}
	}

	/**
	 * コピー元のパス[srcPath]から、コピー先のパス[destPath]へファイルのコピーを行います。
	 * コピー処理にはFileChannel#transferToメソッドを利用します。
	 * コピー処理終了後、入力・出力のチャネルをクローズします。
	 * @param srcPath コピー元のパス
	 * @param destPath  コピー先のパス
	 * @throws IOException 何らかの入出力処理例外が発生した場合
	 */
	public static void copyTransfer(String srcPath, String destPath) throws IOException {
		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
		FileChannel destChannel = new FileOutputStream(destPath).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
			srcChannel.close();
			destChannel.close();
		}
	}

	public static File getJarFile(){
		return new File("plugins", "TagGame.jar");
	}
}
