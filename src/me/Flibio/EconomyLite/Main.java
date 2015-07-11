package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

@Plugin(id = "EconomyLite", name = "EconomyLite", version = "0.0.2")
public class Main {
	
	@Inject
	private Logger logger;
	
	@Subscribe
    public void onServerStart(ServerStartedEvent event) {
        logger.info("EconomyLite by Flibio enabling!");
        
        event.getGame().getEventManager().register(this, new PlayerJoin(logger));
        
        event.getGame().getCommandDispatcher().register(this, new EconCommand(logger), "econ");
        event.getGame().getCommandDispatcher().register(this, new BalanceCommand(logger), "balance");
        
    }
	
	@Subscribe
	public void onServerStarting(ServerStartingEvent event){
		createFiles();
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
        
	}
	
}