package me.Flibio.EconomyLite.API;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;

public class EconomyLiteAPI {

	private BusinessManager businessAPI = new BusinessManager();

	private PlayerManager playerAPI = new PlayerManager();
	
	/**
	 * EconomyLite's API
	 */
	public EconomyLiteAPI() {
		if(!Main.optionEnabled("businesses")) {
			businessAPI = null;
		}
	}
	
	public BusinessManager getBusinessAPI() {
		return businessAPI;
	}
	
	public PlayerManager getPlayerAPI() {
		return playerAPI;
	}

}
