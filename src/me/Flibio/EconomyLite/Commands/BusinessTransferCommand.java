package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
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

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class BusinessTransferCommand implements CommandExecutor {

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
                source.sendMessage(TextUtils.basicText("You must be a player to transfer funds to your account!", TextColors.RED));
                return;
            }

            Player player = (Player) source;

            Optional<Integer> rawAmount = args.<Integer>getOne("amount");
            Optional<String> rawBusiness = args.<String>getOne("business");
            if (rawAmount.isPresent() && rawBusiness.isPresent()) {
                //Both parameters are present
                int amount = rawAmount.get();
                String businessName = rawBusiness.get().trim();
                String correctName = businessManager.getCorrectBusinessName(businessName);
                Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(player.getUniqueId());
                if (!uOpt.isPresent()) {
                    //Account is not present
                    source.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
                } else {
                    UniqueAccount account = uOpt.get();
                    //Check if the business exists
                    if (!businessManager.businessExists(businessName)) {
                        player.sendMessage(TextUtils.basicText("That business doesn't exist!", TextColors.RED));
                        return;
                    }
                    //Check if the player is an owner
                    if (!businessManager.ownerExists(businessName, player.getUniqueId().toString())) {
                        player.sendMessage(TextUtils.basicText("You don't have permission to draw funds from that business!", TextColors.RED));
                        return;
                    }
                    int businessBalance = businessManager.getBusinessBalance(businessName);
                    int playerBalance = account.getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue();
                    //Check for an error
                    if (businessBalance < 0 || playerBalance < 0) {
                        player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
                        return;
                    }
                    //Check if the business has enough funds
                    if (amount > businessBalance) {
                        //Not enough funds
                        player.sendMessage(TextUtils.basicText("That business doesn't have enough funds!", TextColors.RED));
                        return;
                    }
                    int newBalance = businessBalance + playerBalance;
                    //Make sure the new balance is within the parameters
                    if (newBalance < 0 || newBalance > 1000000) {
                        player.sendMessage(TextUtils.basicText("Your balance must stay within 0 and 1,000,000 " + EconomyLite.access.currencyPlural + "!", TextColors.RED));
                        return;
                    }
                    //Attempt to transfer the money
                    if (!businessManager.removeCurrency(businessName, amount)) {
                        player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
                        return;
                    }
                    if (!account.setBalance(currency, BigDecimal.valueOf(playerBalance + amount), Cause.of("EconomyLite")).getResult().equals(ResultType.SUCCESS)) {
                        player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
                        return;
                    }
                    //Success!
                    player.sendMessage(TextUtils.transferSuccess(correctName, amount));
                }
            } else {
                //An error occurred
                player.sendMessage(TextUtils.basicText("An internal error has occured!", TextColors.RED));
            }

        }).async().submit(EconomyLite.access);
        return CommandResult.success();
    }


}
