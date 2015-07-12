package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;

public class DataEditor {
	
	private Logger logger;
	private boolean mySQL;
	private MySQL sql;
	
	public DataEditor(Logger log){
		this.logger = log;
		mySQL = Main.sqlEnabled();
	}
	
	protected boolean setBalance(String name, int balance) {
		if(mySQL){
			sql = Main.getSQL();
			return sql.setCurrency(name, balance);
		} else {
			ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/data.conf")).build();
			ConfigurationNode root;	
			try {
				root = manager.load();
			} catch (IOException e) {
				logger.error("Error loading data file!");
				logger.error(e.getMessage());
				return false;
			}
			
			if(playerExists(name)){
				Map<Object, ? extends ConfigurationNode> playerMap = root.getChildrenMap();
				for(Entry<Object, ? extends ConfigurationNode> entry : playerMap.entrySet()){
					String uuid = (String) entry.getKey();
					if(root.getNode(uuid).getNode("name").getValue().equals(name)){
						root.getNode(uuid).getNode("balance").setValue(""+balance);
					}
				}
			} else {
				return false;
			}
			
			try {
				manager.save(root);
			} catch (IOException e) {
				logger.error("Error saving data file!");
				logger.error(e.getMessage());
				return false;
			}
			return true;
		}
	}
	
	protected boolean playerExists(String name) {
		if(mySQL){
			sql = Main.getSQL();
			return sql.playerExists(name);
		} else {
			ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/data.conf")).build();
			ConfigurationNode root;
			
			try {
				root = manager.load();
			} catch (IOException e) {
				logger.error("Error loading data file!");
				logger.error(e.getMessage());
				return false;
			}
			
			//Iterate and find the name
			Map<Object, ? extends ConfigurationNode> playerMap = root.getChildrenMap();
			
			for(Entry<Object, ? extends ConfigurationNode> entry : playerMap.entrySet()){
				String uuid = (String) entry.getKey();
				if(root.getNode(uuid).getNode("name").getValue().equals(name)){
					return true;
				}
			}
			return false;
		}
	}
	
	protected int getBalance(String name) {
		if(mySQL){
			sql = Main.getSQL();
			return sql.getCurrency(name);
		} else {
			ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/data.conf")).build();
			ConfigurationNode root;
			
			try {
				root = manager.load();
			} catch (IOException e) {
				logger.error("Error loading data file!");
				logger.error(e.getMessage());
				return 0;
			}
	
			Map<Object, ? extends ConfigurationNode> playerMap = root.getChildrenMap();
			
			for(Entry<Object, ? extends ConfigurationNode> entry : playerMap.entrySet()){
				String uuid = (String) entry.getKey();
				if(root.getNode(uuid).getNode("name").getValue().equals(name)){
					String amnt = root.getNode(uuid).getNode("balance").getString();
					try{
						return Integer.parseInt(amnt);
					} catch(NumberFormatException e){
						logger.error("Invalid number read from data file!");
						logger.error(e.getMessage());
						return 0;
					}
				}
			}
			return 0;
		}
	}
	
	protected boolean addCurrency(String name, int amount){
		return setBalance(name, amount+getBalance(name));
	}
	
	protected boolean removeCurrency(String name, int amount){
		return setBalance(name, getBalance(name)-amount);
	}
}
