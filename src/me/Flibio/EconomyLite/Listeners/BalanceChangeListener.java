package me.Flibio.EconomyLite.Listeners;

import java.util.HashMap;
import java.util.UUID;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Events.BalanceChangeEvent;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.ScoreboardUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.base.Optional;

public class BalanceChangeListener {
	
	private PlayerManager playerManager = new PlayerManager();
	private ScoreboardUtils scoreboardUtils = new ScoreboardUtils();
	
	@Listener
	public void onPlayerBalanceChange(BalanceChangeEvent event) {
		String uuid = event.getPlayerUUID();
		
		//Check if the scoreboard is enabled
		if(Main.optionEnabled("scoreboard")) {
			//Get the player's balance
			Text displayName = Texts.builder("EconomyLite").color(TextColors.YELLOW).build();
			Text balanceLabel = Texts.builder("Balance: ").color(TextColors.GREEN).build();
			
			HashMap<Text, Integer> objectiveValues = new HashMap<Text, Integer>();
			objectiveValues.put(balanceLabel, playerManager.getBalance(uuid));
			
			//Get the player
			Optional<Player> player = Main.access.game.getServer().getPlayer(UUID.fromString(uuid));
			//Check if teh player exists
			if(!player.isPresent()) return;
			
			player.get().setScoreboard(scoreboardUtils.createScoreboard("EconomyLite", displayName, objectiveValues));
		}
	}
	
}
