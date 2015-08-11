package me.Flibio.EconomyLite.Commands;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.scheduler.TaskBuilder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import com.google.common.base.Optional;

public class BusinessLeaveCommand implements CommandExecutor {
	
	private TextUtils textUtils = new TextUtils();
	private BusinessManager businessManager = new BusinessManager();
	private TaskBuilder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(new Runnable() {
			public void run() {
				//Make sure the source is a player
				if(!(source instanceof Player)) {
					source.sendMessage(textUtils.basicText("You must be a player to leave a business!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;
				
				//Retrieve arguments
				Optional<String> businessNameOptional = args.<String>getOne("business");
				if(businessNameOptional.isPresent()) {
					String businessName = businessNameOptional.get();
					//Check if the business already exists
					if(businessManager.businessExists(businessName)) {
						String correctName = businessManager.getCorrectBusinessName(businessName);
						//Check if the player is an owner
						if(businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
							//Check if the player is the only owner
							if(businessManager.getBusinessOwners(businessName).size()==1) {
								//Tell player that the business will be deleted
								player.sendMessage(textUtils.leaveOnlyOwner(correctName));
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
								player.sendMessage(textUtils.clickToContinue("/business delete "+businessName));
								return;
							} else {
								//Leave the business
								if(businessManager.removeOwner(businessName, player.getUniqueId().toString())) {
									//Success
									player.sendMessage(textUtils.leaveSuccess(businessName));
									return;
								} else {
									//Error
									player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
									return;
								}
							}
						} else {
							//Player doesn't have permission
							player.sendMessage(textUtils.basicText("You are not an owner of that business!", TextColors.RED));
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
