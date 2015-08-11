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

public class BusinessRegisterCommand implements CommandExecutor {
	
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
					source.sendMessage(textUtils.basicText("You must be a player to create a business!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;
				
				//Retrieve arguments
				Optional<String> businessNameOptional = args.<String>getOne("business");
				if(businessNameOptional.isPresent()) {
					String businessName = businessNameOptional.get();
					//Check if the business name is too long
					if(businessName.length()>1000) {
						//Name to long
						player.sendMessage(textUtils.basicText("That business name is too long!", TextColors.RED));
						return;
					}
					//Check if the business already exists
					if(!businessManager.businessExists(businessName)) {
						//Try to create business
						if(businessManager.createBusiness(businessName)) {
							//Attempt to add player as an owner
							if(businessManager.addOwner(businessName, player.getUniqueId().toString())) {
								//Successful
								player.sendMessage(textUtils.successfulBusinessRegister(businessManager.getCorrectBusinessName(businessName)));
								return;
							} else {
								//Error occured - delete business
								businessManager.deleteBusiness(businessName);
								player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
								return;
							}
						} else {
							//Error occured
							player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
							return;
						}
					} else {
						//Business exists
						player.sendMessage(textUtils.basicText("A business with that name already exists!", TextColors.RED));
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
