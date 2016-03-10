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

public class BusinessInviteAcceptCommand implements CommandExecutor {
	
	private BusinessManager businessManager = new BusinessManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(() -> {
			//Make sure the source is a player
			if (!(source instanceof Player)) {
				source.sendMessage(TextUtils.basicText("You must be a player to accept an invite!", TextColors.RED));
				return;
			}

			Player player = (Player) source;

			String uuid = player.getUniqueId().toString();

			//Retrieve arguments
			Optional<String> businessNameOptional = args.<String>getOne("business");
			if (businessNameOptional.isPresent()) {
				String businessName = businessNameOptional.get();
				//Check if the business exists
				if (businessManager.businessExists(businessName)) {
					//Check if the player is an owner
					if (businessManager.ownerExists(businessName, uuid)) {
						//Already an owner
						player.sendMessage(TextUtils.basicText("You are already an owner of that business!", TextColors.RED));
						return;
					} else {
						//Check if the player has an invite
						if (businessManager.isInvited(businessName, uuid)) {
							//Accept the invite
							if (businessManager.setInvited(businessName, uuid, false) && businessManager.addOwner(businessName, uuid)) {
								//Tell the player they were accepted
								player.sendMessage(TextUtils.inviteAccept(businessManager.getCorrectBusinessName(businessName)));
								return;
							} else {
								//Error
								player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
								return;
							}
						} else {
							player.sendMessage(TextUtils.basicText("You are not invited to join that business!", TextColors.RED));
							return;
						}
					}
				} else {
					player.sendMessage(TextUtils.basicText("Business was not found!", TextColors.RED));
					return;
				}
			} else {
				//Send error message
				player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
				return;
			}
		}).async().submit(EconomyLite.access);
		return CommandResult.success();
	}

}
