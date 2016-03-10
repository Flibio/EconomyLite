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

public class BusinessRegisterCommand implements CommandExecutor {
	
	private BusinessManager businessManager = new BusinessManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(() -> {
			//Make sure the source is a player
			if (!(source instanceof Player)) {
				source.sendMessage(TextUtils.basicText("You must be a player to create a business!", TextColors.RED));
				return;
			}

			Player player = (Player) source;

			//Retrieve arguments
			Optional<String> businessNameOptional = args.<String>getOne("business");
			if (businessNameOptional.isPresent()) {
				String businessName = businessNameOptional.get();
				//Check if the business name is too long
				if (businessName.length() > 1000) {
					//Name to long
					player.sendMessage(TextUtils.basicText("That business name is too long!", TextColors.RED));
					return;
				}
				//Check if the business already exists
				if (!businessManager.businessExists(businessName)) {
					//Try to create business
					if (businessManager.createBusiness(businessName)) {
						//Attempt to add player as an owner
						if (businessManager.addOwner(businessName, player.getUniqueId().toString())) {
							//Successful
							player.sendMessage(TextUtils.successfulBusinessRegister(businessManager.getCorrectBusinessName(businessName)));
						} else {
							//Error occurred - delete business
							businessManager.deleteBusiness(businessName);
							player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
						}
					} else {
						//Error occurred
						player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
					}
				} else {
					//Business exists
					player.sendMessage(TextUtils.basicText("A business with that name already exists!", TextColors.RED));
				}
			} else {
				//Send error message
				player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
			}
		}).async().submit(EconomyLite.access);
		return CommandResult.success();
	}

}
