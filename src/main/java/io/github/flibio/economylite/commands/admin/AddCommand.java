/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands.admin;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Optional;

@AsyncCommand
@ParentCommand(parentCommand = EconCommand.class)
@Command(aliases = {"add"}, permission = "economylite.admin.econ.add")
public class AddCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.user(Text.of("player")), GenericArguments.string(Text.of("currency")), GenericArguments.doubleNum(Text.of("amount")));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        if (args.getOne("player").isPresent() && args.getOne("currency").isPresent() && args.getOne("amount").isPresent()) {
            User target = args.<User>getOne("player").get();
            String targetName = target.getName();
            String currency = args.<String>getOne("currency").get();
            BigDecimal toAdd = BigDecimal.valueOf(args.<Double>getOne("amount").get());
            boolean found = false;
            Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(target.getUniqueId());
            if (uOpt.isPresent()) {
                UniqueAccount targetAccount = uOpt.get();
                for (Currency c : currencyService.getCurrencies()) {
                    if (c.getDisplayName().toPlain().equalsIgnoreCase(currency)) {
                        found = true;
                        if (targetAccount.deposit(c, toAdd,
                                Cause.of(EventContext.empty(), (EconomyLite.getInstance()))).getResult().equals(ResultType.SUCCESS)) {
                            src.sendMessage(messageStorage.getMessage("command.econ.addsuccess", "name", targetName));
                            attemptNotify(target);
                        } else {
                            src.sendMessage(messageStorage.getMessage("command.econ.addfail", "name", targetName));
                        }
                    }
                }
                if (!found) {
                    src.sendMessage(messageStorage.getMessage("command.econ.currency.invalid", "currency", currency));
                }
            } else {
                src.sendMessage(messageStorage.getMessage("command.error"));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.error"));
        }
    }

    private void attemptNotify(User target) {
        if (target instanceof Player) {
            ((Player) target).sendMessage(messageStorage.getMessage("command.econ.notify"));
        }
    }

}