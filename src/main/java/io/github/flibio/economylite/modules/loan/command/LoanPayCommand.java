/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
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
import org.spongepowered.api.text.serializer.TextSerializers;

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
                        src.sendMessage(messages.getMessage("module.loan.payed", "amount", String.format(Locale.ENGLISH, "%,.2f", loanBal),
                                "label", getPrefix(loanBal, cur)));
                    } else {
                        // Failed to pay
                        src.sendMessage(messages.getMessage("module.loan.payedfail"));
                    }
                } else {
                    // Pay entire request
                    if (module.getLoanManager().removeLoanBalance(uuid, payment)) {
                        // Successfully payed loan
                        src.sendMessage(messages.getMessage("module.loan.payed", "amount", String.format(Locale.ENGLISH, "%,.2f", payment),
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

    private String getPrefix(double amnt, Currency cur) {
        Text label = cur.getPluralDisplayName();
        if (amnt == 1.0) {
            label = cur.getDisplayName();
        }
        return TextSerializers.FORMATTING_CODE.serialize(label);
    }
}
