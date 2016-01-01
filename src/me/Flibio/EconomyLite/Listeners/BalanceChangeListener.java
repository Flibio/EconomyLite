package me.Flibio.EconomyLite.Listeners;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Events.BalanceChangeEvent;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.ScoreboardUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class BalanceChangeListener {
	
	private PlayerManager playerManager = new PlayerManager();
	private ScoreboardUtils scoreboardUtils = new ScoreboardUtils();
	
	@Listener
	public void onPlayerBalanceChange(BalanceChangeEvent event) {
		UUID uuid = event.getPlayerUUID();
		
		//Check if the scoreboard is enabled
		if(EconomyLite.optionEnabled("scoreboard")) {
			//Get the player's balance
			Text displayName = Text.builder("EconomyLite").color(TextColors.YELLOW).build();
			Text balanceLabel = Text.builder("Balance: ").color(TextColors.GREEN).build();
			
			HashMap<Text, Integer> objectiveValues = new HashMap<Text, Integer>();
			objectiveValues.put(balanceLabel, playerManager.getBalance(uuid.toString()));
			
			//Get the player
			Optional<Player> player = EconomyLite.access.game.getServer().getPlayer(uuid);
			//Check if teh player exists
			if(!player.isPresent()) return;
			
			player.get().setScoreboard(scoreboardUtils.createScoreboard("EconomyLite", displayName, objectiveValues));
		}
	}
	
}
