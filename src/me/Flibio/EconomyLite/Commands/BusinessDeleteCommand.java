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

import java.util.ArrayList;
import java.util.Optional;

public class BusinessDeleteCommand implements CommandExecutor {
	
	private TextUtils textUtils = new TextUtils();
	private BusinessManager businessManager = new BusinessManager();
	private PlayerManager playerManager = new PlayerManager();
	private Builder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(new Runnable() {
			public void run() {
				//Make sure the source is a player
				if(!(source instanceof Player)) {
					source.sendMessage(textUtils.basicText("You must be a player to delete a business!", TextColors.RED));
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
								Thread expireThread = new Thread(new Runnable(){
									public void run() {
										try{
											Thread.sleep(60000);
											businessManager.setConfirmationNeeded(businessName, true);
										} catch(InterruptedException e) {
											businessManager.setConfirmationNeeded(businessName, true);
										}
									}
								});
								expireThread.start();
								player.sendMessage(textUtils.aboutToDelete(correctName));
								player.sendMessage(textUtils.clickToContinue("/business delete "+businessName));
								return;
							} else {
								//Get balance
								int balance = businessManager.getBusinessBalance(businessName);
								if(balance<0) {
									//Error occured
									player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
									return;
								}
								int eachGet = (int) Math.floor(balance/businessManager.getBusinessOwners(businessName).size());
								ArrayList<String> owners = businessManager.getBusinessOwners(businessName);
								//Try to delete business
								if(businessManager.deleteBusiness(businessName)) {
									//Success
									player.sendMessage(textUtils.deleteSuccess(correctName));
									//Distribute funds to all owners
									for(String uuid : owners) {
										playerManager.addCurrency(uuid, eachGet);
									}
									return;
								} else {
									//Error occured
									player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
									return;
								}
							}
						} else {
							//Player doesn't have permission
							player.sendMessage(textUtils.basicText("You don't have permission to delete that business!", TextColors.RED));
							return;
						}
					} else {
						//Business doesn't exist
						player.sendMessage(textUtils.basicText("That business could not be found!", TextColors.RED));
						return;
					}
				} else {
					//Send error message
					player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
					return;
				}
			}
		}).async().submit(Main.access);
		return CommandResult.success();
	}

}
