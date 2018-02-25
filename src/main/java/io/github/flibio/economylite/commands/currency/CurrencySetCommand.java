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
@Command(aliases = {"set"}, permission = "economylite.admin.currency.set")
public class CurrencySetCommand extends BaseCommandExecutor<CommandSource> {

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
                    currencyService.setCurrentCurrency(c);
                    found = true;
                    src.sendMessage(messageStorage.getMessage("command.currency.changed", "currency", c.getDisplayName()));
                }
            }
            if (!found) {
                src.sendMessage(messageStorage.getMessage("command.currency.invalid"));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.currency.current", "currency", currencyService.getCurrentCurrency().getDisplayName()));
            src.sendMessage(messageStorage.getMessage("command.currency.selectnew"));
            currencyService.getCurrencies().forEach(currency -> {
                src.sendMessage(Text.of(currency.getDisplayName()).toBuilder().onClick(TextActions.executeCallback(c -> {
                    src.sendMessage(messageStorage.getMessage("command.currency.confirm", "currency", currency.getDisplayName()).toBuilder()
                            .onClick(TextActions.executeCallback(c2 -> {
                                currencyService.setCurrentCurrency(currency);
                                src.sendMessage(messageStorage.getMessage("command.currency.changed", "currency", currency.getDisplayName()));
                            })).build());
                })).build());
            });
        }
    }
}
