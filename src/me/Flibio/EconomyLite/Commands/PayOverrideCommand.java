package me.Flibio.EconomyLite.Commands;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.scheduler.Task.Builder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.Optional;

public class PayOverrideCommand implements CommandExecutor{
	
	private TextUtils textUtils = new TextUtils();
	private PlayerManager playerManager = new PlayerManager();
	private BusinessManager businessManager = new BusinessManager();
	private Builder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a seperate thread
		taskBuilder.execute(new Runnable() {
			public void run() {
				//Make sure the source is a player
				if(!(source instanceof Player)) {
					source.sendMessage(textUtils.basicText("You must be a player to use /pay!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;
				
				String uuid = player.getUniqueId().toString();
				
				Optional<String> rawWhoType = args.<String>getOne("whoType");
				Optional<Integer> rawAmount = args.<Integer>getOne("amount");
				Optional<String> rawWho = args.<String>getOne("who");
				if(rawWhoType.isPresent()&&rawWho.isPresent()&&rawAmount.isPresent()) {
					//Both parameters are present
					String whoType = rawWhoType.get();
					int amount = rawAmount.get();
					String who = rawWho.get();
					
					if(whoType.equalsIgnoreCase("player")) {
						//Who is a player
						String playerName = who;
						String playerUUID = playerManager.getUUID(playerName);
						if(playerManager.playerExists(playerUUID)) {
							//Pay player
							payPlayer(uuid, amount, player, playerName, playerUUID);
							return;
						} else {
							//Player not found
							player.sendMessage(textUtils.basicText("That player could not be found!", TextColors.RED));
							return;
						}
					} else if(whoType.equalsIgnoreCase("business")) {
						//Who is a business
						String businessName = who;
						if(businessManager.businessExists(businessName)) {
							//Pay business
							payBusiness(uuid, amount, player, businessName);
							return;
						} else {
							//Business not found
							player.sendMessage(textUtils.basicText("That business could not be found!", TextColors.RED));
							return;
						}
					} else {
						//An error occured
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
				} else {
					//An error occured
					player.sendMessage(textUtils.basicText("An internal error has occured!pp", TextColors.RED));
					return;
				}

			}
		}).async().submit(Main.access);
		return CommandResult.success();
	}
	
	private void payBusiness(String uuid, int amount, Player player, String businessName) {
		int balance = playerManager.getBalance(uuid);
		//Check for an error
		if(balance>-1) {
			//Check if the player has enough money
			if(amount>balance) {
				//Player doesn't have enough funds
				player.sendMessage(textUtils.basicText("You don't have enough money to pay!", TextColors.RED));
				return;
			} else {
				//Check if the new balance is within parameters
				int newBalance = businessManager.getBusinessBalance(businessName) + amount;
				if(newBalance<0||newBalance>1000000) {
					//Out of range
					player.sendMessage(textUtils.basicText("The new balance must be in-between 0 and 1,000,000 "+Main.access.currencyPlural+"!", TextColors.RED));
					return;
				} else {
					//Process transaction
					if(playerManager.removeCurrency(uuid, amount)&&businessManager.setBusinessBalance(businessName, newBalance)) {
						//Success
						player.sendMessage(textUtils.paySuccess(businessName, amount));
						return;
					} else {
						//Error
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
				}
			}
		} else {
			//Error
			player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
			return;
		}
	}
	
	private void payPlayer(String uuid, int amount, Player player, String playerName, String targetUUID) {
		int balance = playerManager.getBalance(uuid);
		//Check for an error
		if(balance>-1) {
			//Check if the player has enough money
			if(amount>balance) {
				//Player doesn't have enough funds
				player.sendMessage(textUtils.basicText("You don't have enough money to pay!", TextColors.RED));
				return;
			} else {
				//Check if the new balance is within parameters
				int newBalance = playerManager.getBalance(targetUUID) + amount;
				if(newBalance<0||newBalance>1000000) {
					//Out of range
					player.sendMessage(textUtils.basicText("The new balance must be in-between 0 and 1,000,000 "+Main.access.currencyPlural+"!", TextColors.RED));
					return;
				} else {
					//Process transaction
					if(playerManager.removeCurrency(uuid, amount)&&playerManager.setBalance(targetUUID, newBalance)) {
						//Success
						player.sendMessage(textUtils.paySuccess(playerName, amount));
						return;
					} else {
						//Error
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
				}
			}
		} else {
			//Error
			player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
			return;
		}
	}
	
}
