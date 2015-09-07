package me.Flibio.EconomyLite.Commands;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.scheduler.TaskBuilder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import com.google.common.base.Optional;

public class BusinessTransferCommand implements CommandExecutor{
	
	private TextUtils textUtils = new TextUtils();
	private PlayerManager playerManager = new PlayerManager();
	private BusinessManager businessManager = new BusinessManager();
	private TaskBuilder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a seperate thread
		taskBuilder.execute(new Runnable() {
			public void run() {
				//Make sure the source is a player
				if(!(source instanceof Player)) {
					source.sendMessage(textUtils.basicText("You must be a player to transfer funds to your account!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;
				
				Optional<Integer> rawAmount = args.<Integer>getOne("amount");
				Optional<String> rawBusiness = args.<String>getOne("business");
				if(rawAmount.isPresent()&&rawBusiness.isPresent()) {
					//Both parameters are present
					int amount = rawAmount.get();
					String businessName = rawBusiness.get().trim();
					String correctName = businessManager.getCorrectBusinessName(businessName);
					
					//Check if the business exists
					if(!businessManager.businessExists(businessName)) {
						player.sendMessage(textUtils.basicText("That business doesn't exist!", TextColors.RED));
						return;
					}
					//Check if the player is an owner
					if(!businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
						player.sendMessage(textUtils.basicText("You don't have permission to draw funds from that business!", TextColors.RED));
						return;
					}
					int businessBalance = businessManager.getBusinessBalance(businessName);
					int playerBalance = playerManager.getBalance(player.getUniqueId().toString());
					//Check for an error
					if(businessBalance<0||playerBalance<0) {
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
					//Check if the business has enough funds
					if(amount>businessBalance) {
						//Not enough funds
						player.sendMessage(textUtils.basicText("That business doesn't have enough funds!", TextColors.RED));
						return;
					}
					int newBalance = businessBalance + playerBalance;
					//Make sure the new balance is within the parameters
					if(newBalance<0||newBalance>1000000) {
						player.sendMessage(textUtils.basicText("Your balance must stay within 0 and 1,000,000 "+Main.access.currencyPlural+"!", TextColors.RED));
						return;
					}
					//Attempt to transfer the money
					if(!businessManager.setBusinessBalance(businessName, businessBalance-amount)) {
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
					if(!playerManager.setBalance(player.getUniqueId().toString(), newBalance)) {
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
					//Success!
					player.sendMessage(textUtils.transferSuccess(correctName, amount));
					
				} else {
					//An error occured
					player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
					return;
				}

			}
		}).async().submit(Main.access);
		return CommandResult.success();
	}
	
	
	
}
