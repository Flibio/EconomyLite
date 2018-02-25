/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.loan.command;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.modules.loan.LoanModule;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

@AsyncCommand
@ParentCommand(parentCommand = LoanCommand.class)
@Command(aliases = {"deny"}, permission = "economylite.loan.deny")
public class LoanDenyCommand extends BaseCommandExecutor<Player> {

    private LoanModule module;
    private MessageStorage messages;

    public LoanDenyCommand(LoanModule module) {
        this.module = module;
        this.messages = EconomyLite.getMessageStorage();
    }

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this);
    }

    @Override
    public void run(Player src, CommandContext args) {
        UUID uuid = src.getUniqueId();
        // Check if the player has a loan being offered
        if (module.tableLoans.containsKey(uuid)) {
            module.tableLoans.remove(uuid);
            src.sendMessage(messages.getMessage("module.loan.no"));
        } else {
            src.sendMessage(messages.getMessage("module.loan.noloan"));
        }
    }

}
