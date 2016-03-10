package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class AddCommand implements CommandExecutor {
	
	private EconomyService economyService = EconomyLite.getService();
	private Currency currency = EconomyLite.getService().getDefaultCurrency();
	private PlayerManager playerManager = new PlayerManager();
	private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run on a separate thread
		taskBuilder.execute(() -> {
			//Retrieve arguments
			Optional<String> playerNameOptional = args.<String>getOne("player");
			Optional<Integer> amountOptional = args.<Integer>getOne("amount");
			if (playerNameOptional.isPresent() && amountOptional.isPresent()) {
				//Set the variables
				String playerName = playerNameOptional.get();
				int amount = amountOptional.get();
				source.sendMessage(TextUtils.editingBalance(playerName));
				//Get the players UUID
				String uuid = playerManager.getUUID(playerName);
				if (!uuid.isEmpty()) {
					Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(UUID.fromString(uuid));
					if (!uOpt.isPresent()) {
						//Account is not present
						source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
					} else {
						UniqueAccount account = uOpt.get();
						//Set amount
						int newAmount = amount + account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue();
						//Check if the amount is in-between of the parameters
						if (newAmount < 0 || newAmount > 1000000) {
							//New balance is too big or small
							source.sendMessage(TextUtils.basicText("The new balance must be in-between 0 and 1,000,000 " + EconomyLite.access.currencyPlural + "!", TextColors.RED));
							return;
						}
						//Set the player's balance
						if (account.setBalance(currency, BigDecimal.valueOf(newAmount), Cause.of("EconomyLite")).getResult().equals(ResultType.SUCCESS)) {
							//Successful
							source.sendMessage(TextUtils.successfulBalanceChangeText(playerName, newAmount));
						} else {
							//Send error message
							source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
						}
					}
				} else {
					//UUID is empty
					source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
				}
			} else {
				//Send error message
				source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
			}
		}).async().submit(EconomyLite.access);
		return CommandResult.success();
	}

}
