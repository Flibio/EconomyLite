package me.Flibio.EconomyLite.Listeners;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Events.BalanceChangeEvent;
import me.Flibio.EconomyLite.Utils.ScoreboardUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class BalanceChangeListener {
	
	private EconomyService economyService = EconomyLite.getService();
	private Currency currency = EconomyLite.getService().getDefaultCurrency();
	private ScoreboardUtils scoreboardUtils = new ScoreboardUtils();
	
	@Listener
	public void onPlayerBalanceChange(BalanceChangeEvent event) {
		UUID uuid = event.getPlayerUUID();
		
		//Check if the scoreboard is enabled
		if(EconomyLite.optionEnabled("scoreboard")) {
			//Get the player's balance
			Text displayName = Text.builder("EconomyLite").color(TextColors.YELLOW).build();
			Text balanceLabel = Text.builder("Balance: ").color(TextColors.GREEN).build();
			
			HashMap<Text, Integer> objectiveValues = new HashMap<>();
			Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(uuid);
			if(uOpt.isPresent()) {
				UniqueAccount account = uOpt.get();
				objectiveValues.put(balanceLabel, account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue());
				
				//Get the player
				Optional<Player> player = EconomyLite.access.game.getServer().getPlayer(uuid);
				//Check if the player exists
				if(!player.isPresent()) return;
				
				player.get().setScoreboard(scoreboardUtils.createScoreboard("EconomyLite", displayName, objectiveValues));
			}
		}
	}
	
}
