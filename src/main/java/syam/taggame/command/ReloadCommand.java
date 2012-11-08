/**
 * TagGame - Package: syam.taggame.command
 * Created: 2012/11/08 16:56:33
 */
package syam.taggame.command;

import syam.taggame.Perms;
import syam.taggame.util.Actions;

/**
 * ReloadCommand (ReloadCommand.java)
 * @author syam(syamn)
 */
public class ReloadCommand extends BaseCommand {
	public ReloadCommand(){
		bePlayer = false;
		name = "reload";
		argLength = 0;
		usage = "<- reload config.yml";
	}

	@Override
	public void execute() {
		try{
			plugin.getConfigs().loadConfig(false);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
			return;
		}

		// 権限管理プラグイン再設定
		//Perms.setupPermissionHandler();

		Actions.message(sender, "&aConfiguration reloaded!");
	}

	@Override
	public boolean permission() {
		return Perms.RELOAD.has(sender);
	}
}