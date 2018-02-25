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
package io.github.flibio.economylite.commands.virtual;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Optional;

@AsyncCommand
@Command(aliases = {"payvirtual", "payv"}, permission = "economylite.virtual.pay")
public class PayVirtualCommand extends BaseCommandExecutor<Player> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private EconomyService ecoService = EconomyLite.getEconomyService();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec
                .builder()
                .executor(this)
                .arguments(GenericArguments.string(Text.of("target")), GenericArguments.doubleNum(Text.of("amount")));
    }

    @Override
    public void run(Player src, CommandContext args) {
        if (args.getOne("target").isPresent() && args.getOne("amount").isPresent()) {
            String target = args.<String>getOne("target").get();
            BigDecimal amount = BigDecimal.valueOf(args.<Double>getOne("amount").get());
            Optional<Account> aOpt = EconomyLite.getEconomyService().getOrCreateAccount(target);
            Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(src.getUniqueId());
            // Check for negative payments
            if (amount.doubleValue() <= 0) {
                src.sendMessage(messageStorage.getMessage("command.pay.invalid"));
            } else {
                if (aOpt.isPresent() && uOpt.isPresent()) {
                    Account receiver = aOpt.get();
                    UniqueAccount payer = uOpt.get();
                    if (payer.transfer(receiver, ecoService.getDefaultCurrency(), amount, Cause.of(EventContext.empty(),(EconomyLite.getInstance())))
                            .getResult().equals(ResultType.SUCCESS)) {
                        src.sendMessage(messageStorage.getMessage("command.pay.success", "target", receiver.getDisplayName()));
                    } else {
                        src.sendMessage(messageStorage.getMessage("command.pay.failed", "target", receiver.getDisplayName()));
                    }
                }
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.error"));
        }
    }

}
