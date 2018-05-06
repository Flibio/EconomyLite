/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.loan.command;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.service.economy.Currency;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.message.MessageStorage;
import io.github.flibio.economylite.modules.loan.LoanModule;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Locale;
import java.util.Optional;

@AsyncCommand
@ParentCommand(parentCommand = LoanCommand.class)
@Command(aliases = {"balance", "bal"}, permission = "economylite.loan.balance")
public class LoanBalanceCommand extends BaseCommandExecutor<Player> {

    private MessageStorage messages = EconomyLite.getMessageStorage();
    private LoanModule module;

    public LoanBalanceCommand(LoanModule module) {
        this.module = module;
    }

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this);
    }

    @Override
    public void run(Player src, CommandContext args) {
        Optional<Double> bOpt = module.getLoanManager().getLoanBalance(src.getUniqueId());
        if (bOpt.isPresent()) {
            Currency cur = EconomyLite.getEconomyService().getDefaultCurrency();
            src.sendMessage(messages.getMessage("module.loan.balance", "balance", String.format(Locale.ENGLISH, "%,.2f", bOpt.get()),
                    "label", getPrefix(bOpt.get(), cur)));
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
