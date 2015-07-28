package me.Flibio.EconomyLite.Runnables;


import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.HttpUtils;
import me.Flibio.EconomyLite.Utils.JsonUtils;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.entity.player.Player;

public class UpdateRunnable implements Runnable{
	
	private HttpUtils httpUtils = new HttpUtils();
	private JsonUtils jsonUtils = new JsonUtils();
	private TextUtils textUtils = new TextUtils();
	private Player player;
	
	public UpdateRunnable(Player player) {
		this.player = player;
	}

	public void run() {
		//Check if the player has permission
		if(player.hasPermission("econ.admin.updates")&&Main.optionEnabled("updates")) {
			//Get the data
			String latest = httpUtils.requestData("https://api.github.com/repos/Flibio/EconomyLite/releases/latest");
			String version = jsonUtils.getVersion(latest).replace("v", "");
			String url = jsonUtils.getUrl(latest);
			boolean prerelease = jsonUtils.isPreRelease(latest);
			//Make sure the latest update is not a prerelease
			if(!prerelease) {
				//Check if the latest update is newer than the current one
				String currentVersion = Main.access.version;
				if(textUtils.versionCompare(version, currentVersion)>0) {
					player.sendMessage(textUtils.updateAvailable(version, url));
				}
			}
		}
	}
	
}
