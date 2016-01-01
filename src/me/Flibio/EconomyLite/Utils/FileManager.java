package me.Flibio.EconomyLite.Utils;

import java.io.File;
import java.io.IOException;

import me.Flibio.EconomyLite.EconomyLite;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;

import com.typesafe.config.ConfigException;


public class FileManager {
	
	//Declare FileType enumerator
	public enum FileType {
		CONFIGURATION,
		DATA,
		BUSINESS_DATA
	}

	private Logger logger;
	private ConfigurationNode configRoot;
	private ConfigurationNode businessRoot;
	private ConfigurationNode dataRoot;
	
	public FileManager() {
		this.logger = EconomyLite.access.logger;
	}
	
	public void testDefault(String path, Object value) {
		if(configRoot!=null) {
			//Check if the configuration file doesn't contain the path
			if(configRoot.getNode((Object[]) path.split("\\.")).getValue()==null) {
				//Set the path to the default value
				configRoot.getNode((Object[]) path.split("\\.")).setValue(value);
				saveFile(FileType.CONFIGURATION,configRoot);
			}
		}
	}

	public String getConfigValue(String path) {
		if(configRoot!=null) {
			//Check if the configuration file contains the path
			if(configRoot.getNode((Object[]) path.split("\\.")).getValue()!=null){
				//Get the value and return it
				return configRoot.getNode((Object[]) path.split("\\.")).getString();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	
	public void generateFolder(String path) {
		File folder = new File(path);
		try{
			if(!folder.exists()){
				logger.info(path+" not found, generating...");
				if(folder.mkdir()){
					logger.info("Successfully generated "+path);
				} else {
					logger.warn("Error generating "+path);
				}
				
			}
		} catch(Exception e){
			logger.warn("Error generating "+path+": ");
			logger.warn(e.getMessage());
		}
	}
	
	public void generateFile(String path) {
		File file = new File(path);
		try{
			if(!file.exists()){
				logger.info(path+" not found, generating...");
				try {
	        		file.createNewFile();
	        		logger.info("Successfully generated "+path);
				} catch (IOException e) {
					logger.error("Error generating "+path);
					logger.error(e.getMessage());
				}
			}
		} catch(Exception e){
			logger.warn("Error generating "+path+": ");
			logger.warn(e.getMessage());
		}
	}
	
	public void loadFile(FileType file) {
		String fileName = "";
		switch(file) {
			case CONFIGURATION:
				fileName = "config.conf";
				break;
			
			case DATA:
				fileName = "data.conf";
				break;
				
			case BUSINESS_DATA:
				fileName = "businesses.conf";
				break;
		}
		
		ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/"+fileName)).build();
		ConfigurationNode root;
		try {
			root = manager.load();
		} catch (IOException e) {
			logger.error("Error loading "+fileName+"!");
			logger.error(e.getMessage());
			return;
		} catch (ConfigException e) {
			logger.error("Error loading "+fileName+"!");
			logger.error("Did you edit something wrong? For a blank value use double quotes.");
			logger.error(e.getMessage());
			return;
		}
		
		switch(file) {
		case CONFIGURATION:
			configRoot = root;
			break;
		
		case DATA:
			dataRoot = root;
			break;
			
		case BUSINESS_DATA:
			businessRoot = root;
			break;
		}
	}
	
	public ConfigurationNode getFile(FileType file) {
		switch(file) {
			case CONFIGURATION:
				return configRoot;
			
			case DATA:
				return dataRoot;
				
			case BUSINESS_DATA:
				return businessRoot;
				
			default:
				return null;
		}
	}
	
	public void saveFile(FileType file, ConfigurationNode root) {
		String fileName = "";
		switch(file) {
			case CONFIGURATION:
				fileName = "config.conf";
				break;
			
			case DATA:
				fileName = "data.conf";
				break;
				
			case BUSINESS_DATA:
				fileName = "businesses.conf";
				break;
		}
		ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/"+fileName)).build();
		
		try {
			manager.save(root);
		} catch (IOException e) {
			logger.error("Error saving "+fileName+"!");
			logger.error(e.getMessage());
		}
	}
	
}
