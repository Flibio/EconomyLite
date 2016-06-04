/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
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
package io.github.flibio.economylite.commands.balance;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
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
@Command(aliases = {"balance", "money", "bal"}, permission = "economylite.balance")
public class BalanceCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        Currency currency = currencyService.getCurrentCurrency();
        if (args.getOne("player").isPresent()) {
            if (src.hasPermission("economylite.admin.balanceothers")) {
                User target = args.<User>getOne("player").get();
                String targetName = target.getName();
                Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(target.getUniqueId());
                if (uOpt.isPresent()) {
                    BigDecimal bal = uOpt.get().getBalance(currency);
                    Text label = currency.getPluralDisplayName();
                    if (bal.equals(BigDecimal.ONE)) {
                        label = currency.getDisplayName();
                    }
                    src.sendMessage(messageStorage.getMessage("command.balanceother",
                            ImmutableMap.of("player", Text.of(targetName), "balance", Text.of(String.format(Locale.ENGLISH, "%,.2f", bal)), "label",
                                    label)));
                } else {
                    src.sendMessage(messageStorage.getMessage("command.error"));
                }
            } else {
                src.sendMessage(messageStorage.getMessage("command.noperm"));
            }
        } else {
            // Get the balance of the running player
            if (src instanceof Player) {
                Player player = (Player) src;
                Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(player.getUniqueId());
                if (uOpt.isPresent()) {
                    BigDecimal bal = uOpt.get().getBalance(currency);
                    Text label = currency.getPluralDisplayName();
                    if (bal.equals(BigDecimal.ONE)) {
                        label = currency.getDisplayName();
                    }
                    src.sendMessage(messageStorage.getMessage("command.balance", "balance", Text.of(String.format(Locale.ENGLISH, "%,.2f", bal)),
                            "label", label));
                } else {
                    src.sendMessage(messageStorage.getMessage("command.error"));
                }
            } else {
                // If the there are no arguments the source must be a player
                src.sendMessage(messageStorage.getMessage("command.invalidsource", "sourcetype", "player"));
            }
        }
    }

}
