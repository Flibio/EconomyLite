package me.Flibio.EconomyLite;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

public class PlayerJoin {
	
	private Logger logger;
	private boolean mySQL;
	private MySQL sql;
	private Game game;
	
	public PlayerJoin(Logger logger, Game game){
		this.logger = logger;
		this.game = game;
		mySQL = Main.sqlEnabled();
	}
	
	@Subscribe
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getUser();
		
		String uuid = player.getUniqueId().toString();
		UUID UUID = player.getUniqueId();
		
		//Economy setup
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
		
		if(Main.scoreboardEnabled()){
			int playerBalance = (new DataEditor(logger,game)).getBalance(player.getName());
			
			if(player!=null){
				Scoreboard board = game.getRegistry().getScoreboardBuilder().build();
				Objective obj = game.getRegistry().getObjectiveBuilder().name("EconomyLite").criterion(Criteria.DUMMY).displayName(Texts.builder("Economy").color(TextColors.YELLOW).build()).build();
				obj.getScore(Texts.builder("Balance: ").color(TextColors.GREEN).build()).setScore(playerBalance);
				board.addObjective(obj);
				board.addObjective(obj, DisplaySlots.SIDEBAR);
				
				player.setScoreboard(board);
			}
		}
	}
}
