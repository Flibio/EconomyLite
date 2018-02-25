/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands.virtual;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;
import com.google.common.collect.ImmutableMap;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@AsyncCommand
@Command(aliases = {"vbalance", "vbal"}, permission = "economylite.admin.virtual.balance")
public class VirtualBalanceCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("account")));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        Currency currency = currencyService.getCurrentCurrency();
        if (args.getOne("account").isPresent()) {
            if (src.hasPermission("economylite.admin.virtualbalance")) {
                String target = args.<String>getOne("account").get();
                Optional<Account> aOpt = EconomyLite.getEconomyService().getOrCreateAccount(target);
                if (aOpt.isPresent()) {
                    BigDecimal bal = aOpt.get().getBalance(currency);
                    Text label = currency.getPluralDisplayName();
                    if (bal.equals(BigDecimal.ONE)) {
                        label = currency.getDisplayName();
                    }
                    src.sendMessage(messageStorage.getMessage("command.balanceother",
                            ImmutableMap.of("player", aOpt.get().getDisplayName(), "balance", Text.of(String.format(Locale.ENGLISH, "%,.2f", bal)),
                                    "label", label)));
                } else {
                    src.sendMessage(messageStorage.getMessage("command.error"));
                }
            } else {
                src.sendMessage(messageStorage.getMessage("command.noperm"));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.error"));
        }
    }
}
