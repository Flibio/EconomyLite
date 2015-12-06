package me.Flibio.EconomyLite.Commands;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class BusinessInviteCommand implements CommandExecutor {
	
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
					source.sendMessage(textUtils.basicText("You must be a player to invite someone to a business!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;
				
				//Retrieve arguments
				Optional<String> playerNameOptional = args.<String>getOne("player");
				Optional<String> businessNameOptional = args.<String>getOne("business");
				if(playerNameOptional.isPresent()&&businessNameOptional.isPresent()) {
					String playerName = playerNameOptional.get();
					String businessName = businessNameOptional.get();
					String uuid = playerManager.getUUID(playerName);
					//Check if the uuid is an error
					if(uuid.isEmpty()) {
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
					//Check if the business exists
					if(businessManager.businessExists(businessName)) {
						//Check if the player is an owner
						if(businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
							//Check if the target is already an owner
							if(businessManager.ownerExists(businessName, uuid)) {
								player.sendMessage(textUtils.basicText("That player is already an owner of that business!", TextColors.RED));
								return;
							} else {
								//Check if the player is already invited
								if(businessManager.isInvited(businessName, uuid)) {
									//Player is already invited
									player.sendMessage(textUtils.basicText("That has already been invited to join that business!", TextColors.RED));
									return;
								} else {
									//Attempt to send the invite
									if(businessManager.setInvited(businessName, uuid, true)) {
										//Success! - Check if the player is online and send them the invite if they are
										for(Player p : Main.access.game.getServer().getOnlinePlayers()) {
											if(p.getUniqueId().toString().equals(uuid)) {
												//Player was found - send invite message
												p.sendMessage(textUtils.invited(businessManager.getCorrectBusinessName(businessName)));
												p.sendMessage(textUtils.clickToContinue("/business inviteAccept "+businessName));
											}
										}
										//Tell the command sender invite was sent
										player.sendMessage(textUtils.successfulInvite(businessManager.getCorrectBusinessName(businessName), playerName));
										return;
									} else {
										//Error
										player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
										return;
									}
								}
							}
						} else {
							player.sendMessage(textUtils.basicText("You don't have permission to add owners to this business!", TextColors.RED));
							return;
						}
					} else {
						player.sendMessage(textUtils.basicText("Business was not found!", TextColors.RED));
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
