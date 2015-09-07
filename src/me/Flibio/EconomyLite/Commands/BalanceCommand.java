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

import com.google.common.base.Optional;

public class BalanceCommand implements CommandExecutor{
	
	private TextUtils textUtils = new TextUtils();
	private PlayerManager playerManager = new PlayerManager();
	private BusinessManager businessManager = new BusinessManager();
	private TaskBuilder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a seperate thread
		taskBuilder.execute(new Runnable() {
			public void run() {
				//Make sure the source is a player
				if(!(source instanceof Player)) {
					source.sendMessage(textUtils.basicText("You must be a player to use /balance!", TextColors.RED));
					return;
				}
				
				Player player = (Player) source;
				
				Optional<String> business = args.<String>getOne("business");
				if(business.isPresent()&&Main.optionEnabled("businesses")) {
					//Check if player has permission
					if(!player.hasPermission("econ.business.balance")) {
						source.sendMessage(textUtils.basicText("You do not have permission to use this command!", TextColors.RED));
						return;
					}
					//Player wants to view a business balance
					String businessName = args.<String>getOne("business").get();
					//Check if the business exists
					if(businessManager.businessExists(businessName)) {
						//Check if the player is an owner
						if(businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
							//Attempt to send the player the businesses balance
							int balance = businessManager.getBusinessBalance(businessName);
							if(balance<0) {
								//Send error message
								player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
								return;
							} else {
								//Send business balance
								player.sendMessage(textUtils.businessBalanceText(businessManager.getCorrectBusinessName(businessName), balance));
								return;
							}
						} else {
							//Player not an owner
							player.sendMessage(textUtils.basicText("You do not have permission to view the balance of that business!", TextColors.RED));
							return;
						}
					} else {
						//Business not found
						player.sendMessage(textUtils.basicText("Business could not be found!", TextColors.RED));
						return;
					}
				} else {
					//Player wants to view their balance
					int balance = playerManager.getBalance(player.getUniqueId().toString());
					if(balance<0) {
						//Send error message
						player.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					} else {
						//Send player their balance
						player.sendMessage(textUtils.playerBalanceText(balance));
						return;
					}
				}
			}
		}).submit(Main.access);
		return CommandResult.success();
	}
	
}
