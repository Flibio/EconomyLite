package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class BusinessLeaveCommand implements CommandExecutor {

	private BusinessManager businessManager = new BusinessManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(() -> {
            //Make sure the source is a player
            if(!(source instanceof Player)) {
                source.sendMessage(TextUtils.basicText("You must be a player to leave a business!", TextColors.RED));
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
                            player.sendMessage(TextUtils.leaveOnlyOwner(correctName));
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
                            player.sendMessage(TextUtils.clickToContinue("/business delete "+businessName));
                        } else {
                            //Leave the business
                            if(businessManager.removeOwner(businessName, player.getUniqueId().toString())) {
                                //Success
                                player.sendMessage(TextUtils.leaveSuccess(businessName));
                            } else {
                                //Error
                                player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
                            }
                        }
                    } else {
                        //Player doesn't have permission
                        player.sendMessage(TextUtils.basicText("You are not an owner of that business!", TextColors.RED));
                    }
                } else {
                    //Business doesn't exist
                    player.sendMessage(TextUtils.basicText("That business could not be found!", TextColors.RED));
                }
            } else {
                //Send error message
                player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
            }
        }).async().submit(EconomyLite.access);
		return CommandResult.success();
	}

}
