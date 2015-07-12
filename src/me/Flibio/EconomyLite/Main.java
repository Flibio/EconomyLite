package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.sql.SqlService;

import com.google.common.base.Optional;
import com.google.inject.Inject;

@Plugin(id = "EconomyLite", name = "EconomyLite", version = "0.0.4")
public class Main {
	
	@Inject
	private Logger logger;
	
	private static String currencyPlural = "";
	private static String currencySingular = "";
	private static boolean sql = false;
	private static String hostname = "";
	private static String port = "";
	private static String database = "";
	private static String username = "";
	private static String password = "";
	private static MySQL mySQL;
	
	@Subscribe
    public void onServerInitialize(InitializationEvent event) {
        logger.info("EconomyLite by Flibio initializing!");
        
        createFiles();
		configurationDefaults();
		loadOptions();
		
        event.getGame().getEventManager().register(this, new PlayerJoin(logger));
        
        event.getGame().getCommandDispatcher().register(this, new EconCommand(logger), "econ");
        event.getGame().getCommandDispatcher().register(this, new BalanceCommand(logger), "balance");
        
        Optional<SqlService> sqlServiceOptional = event.getGame().getServiceManager().provide(SqlService.class);

        if(sql&&sqlServiceOptional.isPresent()){
        	logger.info("MySQL Storage Enabled...");
        	mySQL = new MySQL(hostname,port,database,username,password,logger,sqlServiceOptional.get());
        }
    }
	
	@Subscribe
	public void onServerStart(ServerStartedEvent event){
		logger.info("EconomyLite by Flibio enabled: ");
		String type = "Local File";
		if(sql){
			type = "MySQL";
		}
		logger.info("Storage type: "+type);
	}
	
	private void createFiles(){
		//Create EconomyLite folder if doesn't exist
        File folder = new File("config/EconomyLite");
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
        	logger.warn("Error creating EconomyLite folder! Detailed Error:");
        	logger.warn(e.getMessage());
        	logger.warn("END EconomyLite Error");
        }
        
        //EconomyLite File Generation
        File data = new File("config/EconomyLite/data.conf");
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
        File conf = new File("config/EconomyLite/config.conf");
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
			}
			if(!root.getChildrenMap().containsKey("Currency-Singular")){
				root.getNode("Currency-Singular").setValue("Coin");
			}
			//check if MySQL Node exists
			if(!root.getChildrenMap().containsKey("MySQL")){
				root.getNode("MySQL").getNode("enabled").setValue(false);
			}
			//Check all MySQL child nodes
			if(!root.getNode("MySQL").getChildrenMap().containsKey("hostname")){
				root.getNode("MySQL").getNode("hostname").setValue("hostname");
			}
			if(!root.getNode("MySQL").getChildrenMap().containsKey("port")){
				root.getNode("MySQL").getNode("port").setValue("3306");
			}
			if(!root.getNode("MySQL").getChildrenMap().containsKey("database")){
				root.getNode("MySQL").getNode("database").setValue("database");
			}
			if(!root.getNode("MySQL").getChildrenMap().containsKey("username")){
				root.getNode("MySQL").getNode("username").setValue("username");
			}
			if(!root.getNode("MySQL").getChildrenMap().containsKey("password")){
				root.getNode("MySQL").getNode("password").setValue("password");
			}
			manager.saveFile(root);
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
			if(root.getChildrenMap().containsKey("MySQL")){
				if(root.getNode("MySQL").getChildrenMap().containsKey("enabled")){
					sql = root.getNode("MySQL").getNode("enabled").getBoolean();
				}
				if(root.getNode("MySQL").getChildrenMap().containsKey("hostname")){
					hostname = root.getNode("MySQL").getNode("hostname").getString();
				}
				if(root.getNode("MySQL").getChildrenMap().containsKey("port")){
					port = root.getNode("MySQL").getNode("port").getString();
				}
				if(root.getNode("MySQL").getChildrenMap().containsKey("database")){
					database = root.getNode("MySQL").getNode("database").getString();
				}
				if(root.getNode("MySQL").getChildrenMap().containsKey("username")){
					username = root.getNode("MySQL").getNode("username").getString();
				}
				if(root.getNode("MySQL").getChildrenMap().containsKey("password")){
					password = root.getNode("MySQL").getNode("password").getString();
				}
			}
		}
	}
	
	public static String getCurrencyPlural(){
		return currencyPlural;
	}
	
	public static String getCurrencySingular(){
		return currencySingular;
	}
	
	public static boolean sqlEnabled(){
		return sql;
	}
	
	public static MySQL getSQL(){
		return mySQL;
	}
}