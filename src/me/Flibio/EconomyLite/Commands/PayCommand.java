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

import java.util.Optional;

public class PayCommand implements CommandExecutor{
	
	private TextUtils textUtils = new TextUtils();
	private PlayerManager playerManager = new PlayerManager();
	private BusinessManager businessManager = new BusinessManager();
	private TaskBuilder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(final CommandSource source, final CommandContext args)
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
				
				Optional<Integer> rawAmount = args.<Integer>getOne("amount");
				Optional<String> rawWho = args.<String>getOne("who");
				if(rawWho.isPresent()&&rawAmount.isPresent()) {
					//Both parameters are present
					int amount = rawAmount.get();
					String who = rawWho.get();
					
					if(who.trim().split(" ").length>1) {
						//Who is a business
						String businessName = who;
						payBusiness(uuid,amount,player,businessName);
						return;
					} else {
						//Get the UUID and check if it exists
						String targetUuid = playerManager.getUUID(who);
						//Check if the uuid is a registered player
						if(playerManager.playerExists(targetUuid)) {
							//It is a player, is it also a business?
							if(businessManager.businessExists(who)) {
								//Present them with an option
								player.sendMessage(textUtils.payOption(who));
								player.sendMessage(textUtils.payOptionPlayer(who, amount));
								player.sendMessage(textUtils.payOptionBusiness(who, amount));
								return;
							} else {
								//It is a player
								String playerName = who;
								payPlayer(uuid,amount,player,playerName,targetUuid);
								return;
							}
						} else {
							//Not a registered player, search businesses
							if(businessManager.businessExists(who)) {
								//Who is a business
								String businessName = who;
								payBusiness(uuid,amount,player,businessName);
								return;
							} else {
								//Nothing found
								player.sendMessage(textUtils.basicText("No registered business/player could be found!", TextColors.RED));
								return;
							}
						}
					}
				} else {
					//An error occured
					player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
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
