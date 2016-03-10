package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.format.TextColors;

import java.math.RoundingMode;
import java.util.Optional;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class BalanceCommand implements CommandExecutor{

	private EconomyService economyService = EconomyLite.getService();
	private Currency currency = EconomyLite.getService().getDefaultCurrency();
	private BusinessManager businessManager = new BusinessManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run in a separate thread
		taskBuilder.execute(() -> {
			//Make sure the source is a player
			if (!(source instanceof Player)) {
				source.sendMessage(TextUtils.basicText("You must be a player to use /balance!", TextColors.RED));
				return;
			}

			Player player = (Player) source;

			Optional<String> business = args.<String>getOne("business");
			if (business.isPresent() && EconomyLite.optionEnabled("businesses")) {
				//Check if player has permission
				if (!player.hasPermission("econ.business.balance")) {
					source.sendMessage(TextUtils.basicText("You do not have permission to use this command!", TextColors.RED));
					return;
				}
				//Player wants to view a business balance
				String businessName = args.<String>getOne("business").get();
				//Check if the business exists
				if (businessManager.businessExists(businessName)) {
					//Check if the player is an owner
					if (businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
						//Attempt to send the player the businesses balance
						int balance = businessManager.getBusinessBalance(businessName);
						if (balance < 0) {
							//Send error message
							player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
						} else {
							//Send business balance
							player.sendMessage(TextUtils.businessBalanceText(businessManager.getCorrectBusinessName(businessName), balance));
						}
					} else {
						//Player not an owner
						player.sendMessage(TextUtils.basicText("You do not have permission to view the balance of that business!", TextColors.RED));
					}
				} else {
					//Business not found
					player.sendMessage(TextUtils.basicText("Business could not be found!", TextColors.RED));
				}
			} else {
				//Player wants to view their balance
				Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(player.getUniqueId());
				if (!uOpt.isPresent()) {
					//Account is not present
					source.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
				} else {
					UniqueAccount account = uOpt.get();
					int balance = account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue();
					if (balance < 0) {
						//Send error message
						player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
					} else {
						//Send player their balance
						player.sendMessage(TextUtils.playerBalanceText(balance));
					}
				}
			}
		}).submit(EconomyLite.access);
		return CommandResult.success();
	}
	
}
