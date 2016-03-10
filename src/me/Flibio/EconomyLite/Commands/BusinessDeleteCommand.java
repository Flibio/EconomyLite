package me.Flibio.EconomyLite.Commands;

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
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class BusinessDeleteCommand implements CommandExecutor {
	
	private BusinessManager businessManager = new BusinessManager();private EconomyService economyService = EconomyLite.getService();
	private Currency currency = EconomyLite.getService().getDefaultCurrency();
	//private PlayerManager playerManager = new PlayerManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(() -> {
            //Make sure the source is a player
            if(!(source instanceof Player)) {
                source.sendMessage(TextUtils.basicText("You must be a player to delete a business!", TextColors.RED));
                return;
            }
            
            Player player = (Player) source;
            
            //Retrieve arguments
            Optional<String> businessNameOptional = args.<String>getOne("business");
            if(businessNameOptional.isPresent()) {
                String businessName = businessNameOptional.get();
                //Check if the business already exists
                if(businessManager.businessExists(businessName)) {
                    //Check if the player is an owner
                    if(businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
                        String correctName = businessManager.getCorrectBusinessName(businessName);
                        //Check if the business needs confirmation
                        if(businessManager.confirmationNeeded(businessName)) {
                            //Tell user that the business needs confirmation
                            businessManager.setConfirmationNeeded(businessName, false);
                            //Expire in 1 minute
                            Thread expireThread = new Thread(() -> {
                                try{
                                    Thread.sleep(60000);
                                    businessManager.setConfirmationNeeded(businessName, true);
                                } catch(InterruptedException e) {
                                    businessManager.setConfirmationNeeded(businessName, true);
                                }
                            });
                            expireThread.start();
                            player.sendMessage(TextUtils.aboutToDelete(correctName));
                            player.sendMessage(TextUtils.clickToContinue("/business delete "+businessName));
                        } else {
                            //Get balance
                            int balance = businessManager.getBusinessBalance(businessName);
                            if(balance<0) {
                                //Error occured
                                player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
                                return;
                            }
                            int eachGet = (int) Math.floor(balance/businessManager.getBusinessOwners(businessName).size());
                            ArrayList<String> owners = businessManager.getBusinessOwners(businessName);
                            //Try to delete business
                            if(businessManager.deleteBusiness(businessName)) {
                                //Success
                                player.sendMessage(TextUtils.deleteSuccess(correctName));
                                //Distribute funds to all owners
                                for(String uuid : owners) {
                                    Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(UUID.fromString(uuid));
                                    if(!uOpt.isPresent()) {
                                        //Account is not present
                                        source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
                                        return;
                                    } else {
                                        UniqueAccount account = uOpt.get();
                                        account.deposit(currency, BigDecimal.valueOf(eachGet), Cause.of("EconomyLite"));
                                    }
                                }
                            } else {
                                //Error occurred
                                player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
                            }
                        }
                    } else {
                        //Player doesn't have permission
                        player.sendMessage(TextUtils.basicText("You don't have permission to delete that business!", TextColors.RED));
                    }
                } else {
                    //Business doesn't exist
                    player.sendMessage(TextUtils.basicText("That business could not be found!", TextColors.RED));
                }
            } else {
                //Send error message
                player.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
            }
        }).async().submit(EconomyLite.access);
		return CommandResult.success();
	}

}
