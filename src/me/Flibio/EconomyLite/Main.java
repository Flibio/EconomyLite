package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

@Plugin(id = "EconomyLite", name = "EconomyLite", version = "0.0.3")
public class Main {
	
	@Inject
	private Logger logger;
	
	private static String currencyPlural = "";
	private static String currencySingular = "";
	
	@Subscribe
    public void onServerStart(InitializationEvent event) {
        logger.info("EconomyLite by Flibio enabling!");
        
        createFiles();
		configurationDefaults();
		loadOptions();
		
        event.getGame().getEventManager().register(this, new PlayerJoin(logger));
        
        event.getGame().getCommandDispatcher().register(this, new EconCommand(logger), "econ");
        event.getGame().getCommandDispatcher().register(this, new BalanceCommand(logger), "balance");
        
    }

	private void createFiles(){
		//Create EconomyLite folder if doesn't exist
        File folder = new File("EconomyLite");
        try{
        	if(!folder.exists()){
        		logger.info("No EconomyLite folder found, attempting to create one");
        		if(folder.mkdir()){
        			logger.info("Successfully created EconomyLite folder!");
        		} else {
        			logger.warn("Error creating EconomyLite folder!");
        		}
        		
        	}
        } catch(Exception e){
        	logger.warn("Error creating EconomyLite folder! DETAILED ERROR:");
        	logger.warn(e.getMessage());
        	logger.warn("END EconomyLite Error");
        }
        
        //EconomyLite File Generation
        File data = new File("EconomyLite/data.conf");
        if(!data.exists()){
        	logger.info("No existing data file was found, attempting to create a new one!");
        	try {
				data.createNewFile();
				logger.info("Successfully created the data file!");
			} catch (IOException e) {
				logger.error("Error while creating data file!");
				e.printStackTrace();
			}
        }
        //Configuration file
        File conf = new File("EconomyLite/config.conf");
        if(!conf.exists()){
        	logger.info("No existing configuration file was found, attempting to create a new one!");
        	try {
        		conf.createNewFile();
				logger.info("Successfully created the configuration file!");
			} catch (IOException e) {
				logger.error("Error while creating configuration file!");
				e.printStackTrace();
			}
        }
        
	}
	
	private void configurationDefaults(){
		//Check if file contains configuration options
		ConfigurationManager manager = new ConfigurationManager(logger);
		
		ConfigurationNode root = manager.getFile();
		
		if(root!=null){
			if(!root.getChildrenMap().containsKey("Currency-Plural")){
				root.getNode("Currency-Plural").setValue("Coins");
				manager.saveFile(root);
			}
			if(!root.getChildrenMap().containsKey("Currency-Singular")){
				root.getNode("Currency-Singular").setValue("Coin");
				manager.saveFile(root);
			}
		}
	}
	
	private void loadOptions(){
		logger.info("Loading configuration options into memory...");
		ConfigurationManager manager = new ConfigurationManager(logger);
		
		ConfigurationNode root = manager.getFile();
		
		if(root!=null){
			if(root.getChildrenMap().containsKey("Currency-Plural")){
				currencyPlural = root.getNode("Currency-Plural").getString();
			}
			if(root.getChildrenMap().containsKey("Currency-Singular")){
				currencySingular = root.getNode("Currency-Singular").getString();
			}
		}
	}
	
	public static String getCurrencyPlural(){
		return currencyPlural;
	}
	
	public static String getCurrencySingular(){
		return currencySingular;
	}
}