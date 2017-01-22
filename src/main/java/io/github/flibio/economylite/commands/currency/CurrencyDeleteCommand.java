/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2017 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
