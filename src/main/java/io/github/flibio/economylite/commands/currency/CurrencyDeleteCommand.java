/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands.currency;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.message.MessageStorage;

@AsyncCommand
@ParentCommand(parentCommand = CurrencyCommand.class)
@Command(aliases = {"delete"}, permission = "economylite.admin.currency.delete")
public class CurrencyDeleteCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("currency"))));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        if (args.getOne("currency").isPresent()) {
            String currencyName = args.<String>getOne("currency").get();
            boolean found = false;
            for (Currency c : currencyService.getCurrencies()) {
                if (c.getDisplayName().toPlain().equalsIgnoreCase(currencyName)) {
                    found = true;
                    if (currencyService.getDefaultCurrency().equals(c)) {
                        // You can not delete the default
                        src.sendMessage(messageStorage.getMessage("command.currency.deletedefault"));
                    } else {
                        if (currencyService.getCurrentCurrency().equals(c)) {
                            currencyService.setCurrentCurrency(currencyService.getDefaultCurrency());
                        }
                        currencyService.deleteCurrency(c);
                        src.sendMessage(messageStorage.getMessage("command.currency.deleted", "currency", c.getDisplayName()));
                    }
                }
            }
            if (!found) {
                src.sendMessage(messageStorage.getMessage("command.currency.invalid"));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.currency.delete"));
            currencyService.getCurrencies().forEach(currency -> {
                if (!currency.equals(currencyService.getDefaultCurrency())) {
                    src.sendMessage(Text.of(currency.getDisplayName()).toBuilder().onClick(TextActions.executeCallback(c -> {
                        src.sendMessage(messageStorage.getMessage("command.currency.deleteconfirm", "currency", currency.getDisplayName())
                                .toBuilder().onClick(
                                        TextActions.executeCallback(c2 -> {
                                            if (currencyService.getCurrentCurrency().equals(currency)) {
                                                currencyService.setCurrentCurrency(currencyService.getDefaultCurrency());
                                            }
                                            currencyService.deleteCurrency(currency);
                                            src.sendMessage(messageStorage.getMessage("command.currency.deleted", "currency",
                                                    currency.getDisplayName()));
                                        })).build());
                    })).build());
                }
            });
        }
    }
}
