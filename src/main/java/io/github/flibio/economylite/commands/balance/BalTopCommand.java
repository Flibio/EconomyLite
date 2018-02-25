/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2018 Flibio
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

import io.github.flibio.economylite.CauseFactory;
import com.google.common.collect.ImmutableMap;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@AsyncCommand
@Command(aliases = {"baltop"}, permission = "economylite.baltop")
public class BalTopCommand extends BaseCommandExecutor<CommandSource> {

    MessageStorage messages = EconomyLite.getMessageStorage();

    private int count;

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("page"))));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        Currency current = EconomyLite.getCurrencyService().getCurrentCurrency();
        TreeMap<String, BigDecimal> bals = new TreeMap<>();
        Optional<Integer> pOpt = args.<Integer>getOne("page");
        int pageNumber = 1;
        if (pOpt.isPresent()) {
            pageNumber = pOpt.get();
        }
        if (pageNumber < 1) {
            src.sendMessage(messages.getMessage("command.baltop.invalidpage"));
            return;
        }
        int start, end;
        if (pageNumber == 1) {
            start = 1;
            end = 5;
        } else {
            start = ((pageNumber - 1) * 5) + 1;
            end = pageNumber * 10;
        }
        for (UniqueAccount account : EconomyLite.getPlayerService().getTopAccounts(start, end + 1, CauseFactory.create("Baltop command."))) {
            bals.put(account.getDisplayName().toPlain(), account.getBalance(current));
        }
        boolean nextPage = true;
        if (bals.size() < 1) {
            src.sendMessage(messages.getMessage("command.baltop.nodata"));
            return;
        }
        if (bals.size() < 6) {
            nextPage = false;
        }
        src.sendMessage(messages.getMessage("command.baltop.head", "page", String.valueOf(pageNumber)));
        count = start;
        bals.entrySet().stream().sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()).limit(5).forEachOrdered(e -> {
            Text label = current.getPluralDisplayName();
            if (e.getValue().equals(BigDecimal.ONE)) {
                label = current.getDisplayName();
            }
            src.sendMessage(messages.getMessage(
                    "command.baltop.data",
                    ImmutableMap.of("position", Text.of(count), "name", Text.of(e.getKey()), "balance",
                            Text.of(String.format(Locale.ENGLISH, "%,.2f", e.getValue())), "label", label)));
            count++;
        });
        Text toSend = Text.of();
        if (pageNumber > 1) {
            toSend = toSend.toBuilder().append(
                    messages.getMessage("command.baltop.navigation", "button", "<-").toBuilder()
                            .onClick(TextActions.runCommand("/baltop " + (pageNumber - 1)))
                            .onHover(TextActions.showText(Text.of(TextColors.GREEN, "BACK"))).build()).build();
            toSend = toSend.toBuilder().append(Text.of(" ")).build();
        }
        toSend = toSend.toBuilder().append(messages.getMessage("command.baltop.navigation", "button", String.valueOf(pageNumber))).build();
        if (nextPage) {
            toSend = toSend.toBuilder().append(Text.of(" ")).build();
            toSend = toSend.toBuilder().append(
                    messages.getMessage("command.baltop.navigation", "button", "->").toBuilder()
                            .onClick(TextActions.runCommand("/baltop " + (pageNumber + 1)))
                            .onHover(TextActions.showText(Text.of(TextColors.GREEN, "NEXT"))).build()).build();
        }
        if (!toSend.toPlain().isEmpty())
            src.sendMessage(toSend);
        count = 0;
    }
}
