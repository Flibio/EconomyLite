package me.Flibio.EconomyLite.API;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;

public class EconomyLiteAPI {

	private BusinessManager businessAPI = new BusinessManager();

	private PlayerManager playerAPI = new PlayerManager();
	
	/**
	 * EconomyLite's API. 
	 * 
	 * Methods will query a MySQL Database if the EconomyLite user has opted to save data to a database. 
	 * 
	 * If possible, you should run these methods in a seperate thread.
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
	
	/**
	 * Gets the version of EconomyLite currently running
	 * @return
	 * 	String of the version in format X.Y.Z
	 */
	public String getVersion() {
		return Main.access.version;
	}
	
	/**
	 * Gets the singular label for currency
	 * @return
	 * 	String of the singular label
	 */
	public String getSingularCurrencyLabel() {
		return Main.access.currencySingular;
	}
	
	/**
	 * Gets the plural label for currency
	 * @return
	 * 	String of the plural label
	 */
	public String getPluralCurrencyLabel() {
		return Main.access.currencyPlural;
	}

}
