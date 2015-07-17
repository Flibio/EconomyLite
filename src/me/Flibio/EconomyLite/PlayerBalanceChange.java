package me.Flibio.EconomyLite;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

public class PlayerBalanceChange {
	
	private Logger logger;
	private Game game;
	
	public PlayerBalanceChange(Logger logger, Game game){
		this.logger = logger;
		this.game = game;
	}
	
	@Subscribe
	public void onBalanceChange(BalanceChangeEvent event){	
		
		//Update the scoreboard
		if(Main.scoreboardEnabled()) {
			int playerBalance = (new DataEditor(logger,game)).getBalance(event.getPlayerName());
			
			Player player = null;
			
			for(Player p : game.getServer().getOnlinePlayers()){
				if(p.getName().equalsIgnoreCase(event.getPlayerName())){
					player = p;
				}
			}
			
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
