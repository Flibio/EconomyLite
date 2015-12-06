package me.Flibio.EconomyLite.Utils;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Events.BalanceChangeEvent;
import me.Flibio.EconomyLite.Utils.FileManager.FileType;
import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerManager {
	
	private Logger logger;
	private FileManager fileManager;
	private Game game;
	
	/**
	 * EconomyLite's Player API. 
	 * 
	 * Methods will query a MySQL Database if the EconomyLite user has opted to save data to a database. 
	 * 
	 * If possible, you should run these methods in a seperate thread.
	 */
	public PlayerManager() {
		this.game = Main.access.game;
		this.logger = Main.access.logger;
		
		fileManager = new FileManager();
	}
	
	/**
	 * Looks up a player's UUID
	 * @param name
	 * 	Name of the player whom to lookup
	 * @return
	 * 	String of the UUID found(blank string if an error occured)
	 */
	public String getUUID(String name) {
		GameProfileManager manager = game.getServer().getGameProfileManager();
		GameProfile profile;
		try {
			profile = manager.get(name).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error getting player's UUID");
			return "";
		}
		return profile.getUniqueId().toString();
	}
	
	/**
	 * Looks up a player's name
	 * @param uuid
	 * 	UUID of the player whom to lookup
	 * @return
	 * 	Name of the corresponding player
	 */
	public String getName(String uuid) {
		GameProfileManager manager = game.getServer().getGameProfileManager();
		GameProfile profile;
		try {
			profile = manager.get(UUID.fromString(uuid)).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error getting player's name");
			return "";
		}
		return profile.getName().toString();
	}
	
	/**
	 * Sets the balance of the given player to the given amount
	 * @param uuid
	 * 	UUID of the player whose balance will be changed
	 * @param balance
	 * 	What the player's balance will be set to
	 * @return
	 * 	If the method failed or was successful
	 */
	public boolean setBalance(String uuid, int balance) {
		//Check if the balance is withing parameters
		if(balance<0||balance>1000000) return false;
		if(Main.access.sqlEnabled) {
			//Use MySQL
			MySQLManager mySQL = Main.getMySQL();
			if(mySQL.playerExists(uuid)) {
				//Change balance
				if(!mySQL.setBalance(uuid, balance)) return false;
				game.getEventManager().post(new BalanceChangeEvent(uuid));
				return true;
			} else {
				//Register player
				if(!mySQL.newPlayer(uuid)) return false;
				if(!mySQL.setBalance(uuid, balance)) return false;
				game.getEventManager().post(new BalanceChangeEvent(uuid));
				return true;
			}
		} else {
			//Use local file
			fileManager.loadFile(FileType.DATA);
			ConfigurationNode root = fileManager.getFile(FileType.DATA);
			
			//Check if the player exists
			if(playerExists(uuid)) {
				//Player exists - change balance
				root.getNode(uuid).getNode("balance").setValue(balance);
				fileManager.saveFile(FileType.DATA, root);
				//Post a balance change event with the player's uuid
				game.getEventManager().post(new BalanceChangeEvent(uuid));
				return true;
			} else {
				//Player doesn't exists - register player
				if(registerPlayer(uuid,balance)) {
					//Success!
					game.getEventManager().post(new BalanceChangeEvent(uuid));
					return true;
				} else {
					//Failed
					return false;
				}
			}
		}
		
	}
	
	/**
	 * Registers a player with EconomyLite
	 * @param uuid
	 * 	UUID of the player to register
	 * @return
	 * 	Boolean based on if the command was successful or not
	 */
	public boolean registerPlayer(String uuid) {
		if(Main.access.sqlEnabled) {
			//MySQL
			MySQLManager mySQL = Main.getMySQL();
			if(mySQL.playerExists(uuid)) return false;
			return mySQL.newPlayer(uuid);
		} else {
			//Use local file
			fileManager.loadFile(FileType.DATA);
			ConfigurationNode root = fileManager.getFile(FileType.DATA);
			
			//Check if the player exists
			if(playerExists(uuid)) {
				//Player already exists
				return false;
			} else {
				//Register the player
				root.getNode(uuid).getNode("balance").setValue(0);
				fileManager.saveFile(FileType.DATA, root);
				return true;
			}
		}
	}
	
	/**
	 * Registers a new player with a preset balance
	 * @param uuid
	 * 	UUID of the player to register
	 * @param balance
	 * 	The balance that the player will be set to
	 * @return
	 * 	If the method was successful or not
	 */
	public boolean registerPlayer(String uuid, int balance) {
		if(Main.access.sqlEnabled) {
			//MySQL
			MySQLManager mySQL = Main.getMySQL();
			if(mySQL.playerExists(uuid)) return false;
			if(!mySQL.newPlayer(uuid)) return false;
			return mySQL.setBalance(uuid, balance);
		} else {
			//Use local file
			fileManager.loadFile(FileType.DATA);
			ConfigurationNode root = fileManager.getFile(FileType.DATA);
			
			//Check if the player exists
			if(playerExists(uuid)) {
				//Player already exists
				return false;
			} else {
				//Register the player
				root.getNode(uuid).getNode("balance").setValue(balance);
				fileManager.saveFile(FileType.DATA, root);
				return true;
			}
		}
	}
	
	/**
	 * Checks if the given player has data stored in the system
	 * @param uuid
	 * 	UUID of the player to check
	 * @return
	 * 	If the player was found or not
	 */
	public boolean playerExists(String uuid) {
		if(Main.optionEnabled("mysql.enabled")) {
			//Use MySQL
			MySQLManager mySQL = Main.getMySQL();
			return mySQL.playerExists(uuid);
		} else {
			//Use local file
			fileManager.loadFile(FileType.DATA);
			ConfigurationNode root = fileManager.getFile(FileType.DATA);
			
			//Check if the uuid is found in the file
			if(root.getChildrenMap().containsKey(uuid)) {
				return true;
			} else {
				return false;
			}
			
		}
		
	}
	
	/**
	 * Checks the balance of the given player
	 * @param uuid
	 * 	UUID of the player whose balance will be checked
	 * @return
	 * 	The balance of the player (-1 will be returned if there was an error)
	 */
	public int getBalance(String uuid) {
		if(Main.optionEnabled("mysql.enabled")) {
			//Use MySQL
			MySQLManager mySQL = Main.getMySQL();
			if(!mySQL.playerExists(uuid)) return -1;
			return mySQL.getBalance(uuid);
		} else {
			//Use local file
			fileManager.loadFile(FileType.DATA);
			ConfigurationNode root = fileManager.getFile(FileType.DATA);
			
			//Check if the player exists
			if(playerExists(uuid)) {
				//Retrieve their balance
				String raw = root.getNode(uuid).getNode("balance").getString();
				int balance = -1;
				try {
					balance = Integer.parseInt(raw);
				} catch(NumberFormatException e) {
					logger.error("Error getting player balance!");
					logger.error(e.getMessage());
				}
				return balance;
			} else {
				return -1;
			}
		}
	}
	
	/**
	 * Adds the specified amount of currency to the specified player
	 * @param uuid
	 * 	UUID of the player who will receive the currency
	 * @param amount
	 * 	Amount of currency the player will receive
	 * @return
	 * 	If the method failed or was successful
	 */
	public boolean addCurrency(String uuid, int amount) {
		return setBalance(uuid, getBalance(uuid) + amount);
	}
	
	/**
	 * Removes the specified amount of currency from the specified player
	 * @param uuid
	 * 	UUID of the player whom the currency will be taken from
	 * @param amount
	 * 	Amount of currency the player will lose
	 * @return
	 * 	If the method failed or was successful
	 */
	public boolean removeCurrency(String uuid, int amount) {
		return setBalance(uuid, getBalance(uuid) - amount);
	}
}
