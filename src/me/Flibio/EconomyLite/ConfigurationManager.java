package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;

public class ConfigurationManager {
	
	
	private Logger logger;
	
	public ConfigurationManager(Logger log){
		this.logger = log;
	}
	
	protected ConfigurationNode getFile(){
		ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/config.conf")).build();
		ConfigurationNode root;	
		try {
			root = manager.load();
		} catch (IOException e) {
			logger.error("Error loading configuation file!");
			logger.error(e.getMessage());
			return null;
		}
		return root;
	}
	
	protected void saveFile(ConfigurationNode root){
		ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/config.conf")).build();
		
		try {
			manager.save(root);
		} catch (IOException e) {
			logger.error("Error saving config file!");
			logger.error(e.getMessage());
		}
	}
}
