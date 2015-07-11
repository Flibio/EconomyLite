package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;

public class PlayerJoin {
	
	Logger logger;
	
	public PlayerJoin(Logger logger){
		this.logger = logger;
	}
	
	@Subscribe
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getUser();
		
		String uuid = player.getUniqueId().toString();
		
		ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("EconomyLite/data.conf")).build();
		
		ConfigurationNode root;
		
		try {
			root = manager.load();
		} catch (IOException e) {
			logger.error("Error loading data file!");
			logger.error(e.getMessage());
			return;
		}
		
		//If UUID doesn't exist add it
		if(!root.getChildrenMap().containsKey(uuid)){
			root.getNode(uuid).getNode("name").setValue(player.getName());
			root.getNode(uuid).getNode("balance").setValue(0);
		} else {
			//UUID Exists set the name in case of name change
			root.getNode(uuid).getNode("name").setValue(player.getName());
		}
		
		try {
			manager.save(root);
		} catch (IOException e) {
			logger.error("Error loading data file!");
			logger.error(e.getMessage());
			return;
		}
	}
}
