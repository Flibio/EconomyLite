/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.economylite.impl.PlayerServiceCommon;
import io.github.flibio.economylite.impl.VirtualServiceCommon;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;

@AsyncCommand
@Command(aliases = {"ecorefresh"}, permission = "economylite.admin.refresh")
public class RefreshCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();

    @Override
    public CommandSpec.Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this);
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        PlayerEconService ps = EconomyLite.getPlayerService();
        if (!(ps instanceof PlayerServiceCommon)) {
            src.sendMessage(messageStorage.getMessage("command.refresh.fail"));
            return;
        }
        VirtualEconService vs = EconomyLite.getVirtualService();
        if (!(vs instanceof VirtualServiceCommon)) {
            src.sendMessage(messageStorage.getMessage("command.refresh.fail"));
            return;
        }

        PlayerServiceCommon playerService = (PlayerServiceCommon) ps;
        VirtualServiceCommon virtualService = (VirtualServiceCommon) vs;

        playerService.resetCache();
        virtualService.resetCache();

        src.sendMessage(messageStorage.getMessage("command.refresh.success"));
    }

}
