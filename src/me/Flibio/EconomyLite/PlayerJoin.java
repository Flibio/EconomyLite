package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;

public class PlayerJoin {
	
	private Logger logger;
	private boolean mySQL;
	private MySQL sql;
	
	public PlayerJoin(Logger logger){
		this.logger = logger;
		mySQL = Main.sqlEnabled();
	}
	
	@Subscribe
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getUser();
		
		String uuid = player.getUniqueId().toString();
		UUID UUID = player.getUniqueId();
		
		if(mySQL){
			sql = Main.getSQL();
			sql.reconnect();
			
			if(!sql.playerExists(UUID)){
				sql.addPlayer(uuid, player.getName());
			} else {
				sql.updateName(uuid, player.getName());
			}
			
		} else {
			ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/EconomyLite/data.conf")).build();
			
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
}
