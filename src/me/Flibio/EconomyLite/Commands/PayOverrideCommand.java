package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class PayOverrideCommand implements CommandExecutor{

	private EconomyService economyService = EconomyLite.getService();
	private Currency currency = EconomyLite.getService().getDefaultCurrency();
	private PlayerManager playerManager = new PlayerManager();
	private BusinessManager businessManager = new BusinessManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a separate thread
		taskBuilder.execute(() -> {
            //Make sure the source is a player
            if(!(source instanceof Player)) {
                source.sendMessage(TextUtils.basicText("You must be a player to use /pay!", TextColors.RED));
                return;
            }

            Player player = (Player) source;

            String uuid = player.getUniqueId().toString();
            if(!playerManager.playerExists(uuid)) {
                player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
                return;
            }

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
                    if(who.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(TextUtils.basicText("Why would you pay yourself!?", TextColors.RED));
                        return;
                    }
					String playerUUID = playerManager.getUUID(who); //Who is a player name
                    if(playerManager.playerExists(playerUUID)) {
                        //Pay player
                        payPlayer(uuid, amount, player, who/*Who is a player name*/, playerUUID);
                    } else {
                        //Player not found
                        player.sendMessage(TextUtils.basicText("That player could not be found!", TextColors.RED));
                    }
                } else if(whoType.equalsIgnoreCase("business")) {
                    //Who is a business
					if(businessManager.businessExists(who /*Who is a business*/)) {
                        //Pay business
                        payBusiness(uuid, amount, player, who);
                    } else {
                        //Business not found
                        player.sendMessage(TextUtils.basicText("That business could not be found!", TextColors.RED));
                    }
                } else {
                    //An error occurred
                    player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
                }
            } else {
                //An error occurred
                player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
            }

        }).async().submit(EconomyLite.access);
		return CommandResult.success();
	}
	
	private void payBusiness(String uuid, int amount, Player player, String businessName) {
		UniqueAccount account = economyService.getOrCreateAccount(UUID.fromString(uuid)).get();
		int balance = account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue();
		//Check for an error
		if(balance>-1) {
			//Check if the player has enough money
			if(amount>balance) {
				//Player doesn't have enough funds
				player.sendMessage(TextUtils.basicText("You don't have enough money to pay!", TextColors.RED));
			} else {
				//Check if the new balance is within parameters
				int newBalance = businessManager.getBusinessBalance(businessName) + amount;
				if(newBalance<0||newBalance>1000000) {
					//Out of range
					player.sendMessage(TextUtils.basicText("The new balance must be in-between 0 and 1,000,000 "+EconomyLite.access.currencyPlural+"!", TextColors.RED));
				} else {
					//Process transaction
					if(account.withdraw(currency,BigDecimal.valueOf(amount),Cause.of("EconomyLite")).getResult().equals(ResultType.SUCCESS)&&
							businessManager.setBusinessBalance(businessName, newBalance)) {
						//Success
						player.sendMessage(TextUtils.paySuccess(businessName, amount));
						for(String owner : businessManager.getBusinessOwners(businessName)) {
						    Sponge.getServer().getOnlinePlayers().forEach(p -> {
						        if(p.getName().equalsIgnoreCase(owner)) {
						            p.sendMessage(TextUtils.bPayed(player.getName(), amount, businessName));
						        }
						    });
						}
					} else {
						//Error
						player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
					}
				}
			}
		} else {
			//Error
			player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
		}
	}
	
	private void payPlayer(String uuid, int amount, Player player, String playerName, String targetUUID) {
		UniqueAccount account = economyService.getOrCreateAccount(UUID.fromString(uuid)).get();
		UniqueAccount targetAccount = economyService.getOrCreateAccount(UUID.fromString(targetUUID)).get();
		int balance = account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue();
		//Check for an error
		if(balance>-1) {
			//Check if the player has enough money
			if(amount>balance) {
				//Player doesn't have enough funds
				player.sendMessage(TextUtils.basicText("You don't have enough money to pay!", TextColors.RED));
			} else {
				//Check if the new balance is within parameters
				int newBalance = targetAccount.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue() + amount;
				if(newBalance<0||newBalance>1000000) {
					//Out of range
					player.sendMessage(TextUtils.basicText("The new balance must be in-between 0 and 1,000,000 "+EconomyLite.access.currencyPlural+"!", TextColors.RED));
				} else {
					//Process transaction
					if(account.withdraw(currency,BigDecimal.valueOf(amount),Cause.of("EconomyLite")).getResult().equals(ResultType.SUCCESS)&&
							targetAccount.setBalance(currency,BigDecimal.valueOf(newBalance),Cause.of("EconomyLite")).getResult().equals(ResultType.SUCCESS)) {
						//Success
						player.sendMessage(TextUtils.paySuccess(playerName, amount));
						Sponge.getServer().getOnlinePlayers().stream().filter(oPlayer -> oPlayer.getUniqueId().toString().equals(targetUUID)).forEach(oPlayer -> {
							oPlayer.sendMessage(TextUtils.payed(player.getName(), amount));
						});
					} else {
						//Error
						player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
					}
				}
			}
		} else {
			//Error
			player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
		}
	}
	
}
