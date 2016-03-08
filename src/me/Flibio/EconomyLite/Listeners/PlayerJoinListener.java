package me.Flibio.EconomyLite.Listeners;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.ScoreboardUtils;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class PlayerJoinListener {

	private ScoreboardUtils scoreboardUtils = new ScoreboardUtils();
	private EconomyService economyService = EconomyLite.getService();
	private Currency currency = EconomyLite.getService().getDefaultCurrency();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();
	
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		Player player = (Player) event.getTargetEntity();
		
		economyService.getOrCreateAccount(player.getUniqueId());
		
		//Show scoreboard if it is enabled
		if(EconomyLite.optionEnabled("scoreboard")) {
			Text displayName = Text.builder("EconomyLite").color(TextColors.YELLOW).build();
			Text balanceLabel = Text.builder("Balance: ").color(TextColors.GREEN).build();
			
			HashMap<Text, Integer> objectiveValues = new HashMap<Text, Integer>();
			Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(player.getUniqueId());
			if(uOpt.isPresent()) {
				UniqueAccount account = uOpt.get();
				objectiveValues.put(balanceLabel, account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue());
				
				player.setScoreboard(scoreboardUtils.createScoreboard("EconomyLite", displayName, objectiveValues));
			}
		}

		//Check if the player has any invites
		taskBuilder.execute(new Runnable() {
			public void run() {
				BusinessManager manager = new BusinessManager();
				TextUtils textUtils = new TextUtils();
				
				ArrayList<String> businesses = manager.getAllBusinesses();
				for(String business : businesses) {
					if(manager.businessExists(business)) {
						if(manager.isInvited(business, player.getUniqueId().toString())) {
							//Tell player that he/she is invited
							player.sendMessage(textUtils.invited(manager.getCorrectBusinessName(business)));
							player.sendMessage(textUtils.clickToContinue("/business inviteAccept "+business));
						}
					}
				}
			}
		}).async().submit(EconomyLite.access);
	}
	
}
