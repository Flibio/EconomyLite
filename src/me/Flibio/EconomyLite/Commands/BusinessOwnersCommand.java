package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Optional;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class BusinessOwnersCommand implements CommandExecutor{
	
	private BusinessManager businessManager = new BusinessManager();
	private PlayerManager playerManager = new PlayerManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a separate thread
		taskBuilder.execute(() -> {
			//Make sure the source is a player
			if (!(source instanceof Player)) {
				source.sendMessage(TextUtils.basicText("You must be a player to view the owners of a business!", TextColors.RED));
				return;
			}

			Player player = (Player) source;

			Optional<String> rawBusiness = args.<String>getOne("business");
			if (rawBusiness.isPresent()) {
				//Parameter is present
				String businessName = rawBusiness.get().trim();
				String correctName = businessManager.getCorrectBusinessName(businessName);

				//Check if the business exists
				if (!businessManager.businessExists(businessName)) {
					player.sendMessage(TextUtils.basicText("That business doesn't exist!", TextColors.RED));
					return;
				}
				//Check if the player is an owner
				if (!businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
					player.sendMessage(TextUtils.basicText("You don't have permission to view the owners of that business!", TextColors.RED));
					return;
				}
				//Send the message:
				ArrayList<String> owners = businessManager.getBusinessOwners(businessName);
				player.sendMessage(TextUtils.ownersTitle(correctName));
				for (String owner : owners) {
					player.sendMessage(TextUtils.owner(playerManager.getName(owner)));
				}
			} else {
				//An error occurred
				player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
				return;
			}

		}).async().submit(EconomyLite.access);
		return CommandResult.success();
	}
	
	
	
}
