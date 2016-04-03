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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import com.google.common.collect.ImmutableMap;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@AsyncCommand
@Command(aliases = {"baltop"}, permission = "economylite.baltop")
public class BalTopCommand extends BaseCommandExecutor<CommandSource> {

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this);
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        Currency currency = EconomyLite.getCurrencyService().getCurrentCurrency();
        src.sendMessage(EconomyLite.getMessageStorage().getMessage("command.baltop.head"));
        Currency current = EconomyLite.getEconomyService().getDefaultCurrency();
        TreeMap<String, BigDecimal> bals = new TreeMap<>();
        for (UniqueAccount account : EconomyLite.getPlayerService().getTopAccounts()) {
            bals.put(account.getDisplayName().toPlain(), account.getBalance(current));
        }
        bals.entrySet().stream().sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()).limit(3).forEachOrdered(e -> {
            Text label = current.getPluralDisplayName();
            if (e.getValue().equals(BigDecimal.ONE)) {
                label = current.getDisplayName();
            }
            src.sendMessage(EconomyLite.getMessageStorage().getMessage("command.baltop.data",
                    ImmutableMap.of("name", Text.of(e.getKey()), "balance", currency.format(e.getValue()), "label", label)));
        });
    }
}
