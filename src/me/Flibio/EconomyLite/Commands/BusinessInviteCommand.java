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
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class BusinessInviteCommand implements CommandExecutor {

	private BusinessManager businessManager = new BusinessManager();
	private PlayerManager playerManager = new PlayerManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a new thread
		taskBuilder.execute(() -> {
			//Make sure the source is a player
			if (!(source instanceof Player)) {
				source.sendMessage(TextUtils.basicText("You must be a player to invite someone to a business!", TextColors.RED));
				return;
			}

			Player player = (Player) source;

			//Retrieve arguments
			Optional<String> playerNameOptional = args.<String>getOne("player");
			Optional<String> businessNameOptional = args.<String>getOne("business");
			if (playerNameOptional.isPresent() && businessNameOptional.isPresent()) {
				String playerName = playerNameOptional.get();
				String businessName = businessNameOptional.get();
				String uuid = playerManager.getUUID(playerName);
				//Check if the uuid is an error
				if (uuid.isEmpty()) {
					player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
					return;
				}
				//Check if the business exists
				if (businessManager.businessExists(businessName)) {
					//Check if the player is an owner
					if (businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
						//Check if the target is already an owner
						if (businessManager.ownerExists(businessName, uuid)) {
							player.sendMessage(TextUtils.basicText("That player is already an owner of that business!", TextColors.RED));
						} else {
							//Check if the player is already invited
							if (businessManager.isInvited(businessName, uuid)) {
								//Player is already invited
								player.sendMessage(TextUtils.basicText("That has already been invited to join that business!", TextColors.RED));
							} else {
								//Attempt to send the invite
								if (businessManager.setInvited(businessName, uuid, true)) {
									//Success! - Check if the player is online and send them the invite if they are
									//Player was found - send invite message
									EconomyLite.access.game.getServer().getOnlinePlayers().stream().filter(p -> p.getUniqueId().toString().equals(uuid)).forEach(p -> {
										//Player was found - send invite message
										p.sendMessage(TextUtils.invited(businessManager.getCorrectBusinessName(businessName)));
										p.sendMessage(TextUtils.clickToContinue("/business inviteAccept " + businessName));
									});
									//Tell the command sender invite was sent
									player.sendMessage(TextUtils.successfulInvite(businessManager.getCorrectBusinessName(businessName), playerName));
								} else {
									//Error
									player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
								}
							}
						}
					} else {
						player.sendMessage(TextUtils.basicText("You don't have permission to add owners to this business!", TextColors.RED));
					}
				} else {
					player.sendMessage(TextUtils.basicText("Business was not found!", TextColors.RED));
				}
			} else {
				//Send error message
				player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
			}
		}).async().submit(EconomyLite.access);
		return CommandResult.success();
	}

}
