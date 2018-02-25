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
package io.github.flibio.economylite.modules.loan.command;

import org.spongepowered.api.service.economy.Currency;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.modules.loan.LoanModule;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@AsyncCommand
@ParentCommand(parentCommand = LoanCommand.class)
@Command(aliases = {"pay"}, permission = "economylite.loan.pay")
public class LoanPayCommand extends BaseCommandExecutor<Player> {

    private MessageStorage messages = EconomyLite.getMessageStorage();
    private LoanModule module;

    public LoanPayCommand(LoanModule module) {
        this.module = module;
    }

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .arguments(GenericArguments.doubleNum(Text.of("amount")))
                .executor(this);
    }

    @Override
    public void run(Player src, CommandContext args) {
        if (args.getOne("amount").isPresent()) {
            UUID uuid = src.getUniqueId();
            double payment = args.<Double>getOne("amount").get();
            Currency cur = EconomyLite.getEconomyService().getDefaultCurrency();
            Optional<Double> bOpt = module.getLoanManager().getLoanBalance(uuid);
            if (bOpt.isPresent()) {
                double loanBal = bOpt.get();
                if (payment > loanBal) {
                    // Pay only what is needed
                    if (module.getLoanManager().removeLoanBalance(uuid, loanBal)) {
                        // Successfully payed loan
                        src.sendMessage(messages.getMessage("module.loan.payed", "amount", Text.of(String.format(Locale.ENGLISH, "%,.2f", loanBal)),
                                "label", getPrefix(loanBal, cur)));
                    } else {
                        // Failed to pay
                        src.sendMessage(messages.getMessage("module.loan.payedfail"));
                    }
                } else {
                    // Pay entire request
                    if (module.getLoanManager().removeLoanBalance(uuid, payment)) {
                        // Successfully payed loan
                        src.sendMessage(messages.getMessage("module.loan.payed", "amount", Text.of(String.format(Locale.ENGLISH, "%,.2f", payment)),
                                "label", getPrefix(payment, cur)));
                    } else {
                        // Failed to pay
                        src.sendMessage(messages.getMessage("module.loan.payedfail"));
                    }
                }
            }
        } else {
            src.sendMessage(messages.getMessage("command.error"));
        }
    }

    private Text getPrefix(double amnt, Currency cur) {
        Text label = cur.getPluralDisplayName();
        if (amnt == 1.0) {
            label = cur.getDisplayName();
        }
        return label;
    }
}
