package me.Flibio.EconomyLite.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.format.TextColors;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

public class PlayerBalanceCommand implements CommandExecutor {

    private EconomyService economyService = EconomyLite.getService();
    private Currency currency = EconomyLite.getService().getDefaultCurrency();
    private PlayerManager playerManager = new PlayerManager();
    private Builder taskBuilder = EconomyLite.access.game.getScheduler().createTaskBuilder();
    
    @Override
    public CommandResult execute(CommandSource source, CommandContext args)
            throws CommandException {
        //Run in a separate thread
        taskBuilder.execute(() -> {

            Optional<String> target = args.<String>getOne("player");
            if(target.isPresent()) {
                //Check if player has permission
                if(!source.hasPermission("econ.playerbalance")) {
                    source.sendMessage(TextUtils.basicText("You do not have permission to use this command!", TextColors.RED));
                    return;
                }
                //Player wants to view another player's balance
                String targetName = target.get();
                String uuid = playerManager.getUUID(targetName);
                Optional<UniqueAccount> oAct = economyService.getOrCreateAccount(UUID.fromString(uuid));
                if(oAct.isPresent()) {
                    int balance = oAct.get().getBalance(currency).setScale(0, RoundingMode.HALF_UP).intValue();
                    if(balance<0) {
                        source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
                    } else {
                        source.sendMessage(TextUtils.playerBalanceText(balance, targetName));
                    }
                } else {
                    source.sendMessage(TextUtils.basicText("An internal error has occurred!", TextColors.RED));
                }
            } else {
                source.sendMessage(TextUtils.basicText("You need to specify a player to get the balance of!", TextColors.RED));
            }
        }).submit(EconomyLite.access);
        return CommandResult.success();
    }

}
