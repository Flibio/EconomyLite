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

import java.util.ArrayList;
import java.util.Optional;

public class BusinessOwnersCommand implements CommandExecutor{
	
	private TextUtils textUtils = new TextUtils();
	private BusinessManager businessManager = new BusinessManager();
	private PlayerManager playerManager = new PlayerManager();
	private TaskBuilder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a seperate thread
		taskBuilder.execute(new Runnable() {
			public void run() {
				//Make sure the source is a player
				if(!(source instanceof Player)) {
					source.sendMessage(textUtils.basicText("You must be a player to view the owners of a business!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;

				Optional<String> rawBusiness = args.<String>getOne("business");
				if(rawBusiness.isPresent()) {
					//Parameter is present
					String businessName = rawBusiness.get().trim();
					String correctName = businessManager.getCorrectBusinessName(businessName);
					
					//Check if the business exists
					if(!businessManager.businessExists(businessName)) {
						player.sendMessage(textUtils.basicText("That business doesn't exist!", TextColors.RED));
						return;
					}
					//Check if the player is an owner
					if(!businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
						player.sendMessage(textUtils.basicText("You don't have permission to view the owners of that business!", TextColors.RED));
						return;
					}
					//Send the message:
					ArrayList<String> owners = businessManager.getBusinessOwners(businessName);
					player.sendMessage(textUtils.ownersTitle(correctName));
					for(String owner : owners) {
						player.sendMessage(textUtils.owner(playerManager.getName(owner)));
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
	
	
	
}
