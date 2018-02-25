/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands.currency;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.economylite.impl.economy.LiteCurrency;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.file.FileManager;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;

@AsyncCommand
@ParentCommand(parentCommand = CurrencyCommand.class)
@Command(aliases = {"create"}, permission = "economylite.admin.currency.create")
public class CurrencyCreateCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();
    private FileManager configManager = EconomyLite.getFileManager();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec
                .builder()
                .executor(this)
                .arguments(GenericArguments.string(Text.of("singular")), GenericArguments.string(Text.of("plural")),
                        GenericArguments.string(Text.of("symbol")));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        if (args.getOne("singular").isPresent() && args.getOne("plural").isPresent() && args.getOne("symbol").isPresent()) {
            String singular = args.<String>getOne("singular").get();
            String plural = args.<String>getOne("plural").get();
            String symbol = args.<String>getOne("symbol").get();
            boolean found = false;
            for (Currency c : currencyService.getCurrencies()) {
                if (c.getDisplayName().toPlain().equalsIgnoreCase(singular)) {
                    found = true;
                }
            }
            if (found) {
                src.sendMessage(messageStorage.getMessage("command.currency.exists"));
            } else {
                Currency currency = new LiteCurrency(singular, plural, symbol, false, 2);
                currencyService.addCurrency(currency);
                String configId = currency.getId().replaceAll("economylite:", "");
                configManager.setValue("currencies.conf", configId + ".singular", String.class, currency.getDisplayName().toPlain());
                configManager.setValue("currencies.conf", configId + ".plural", String.class, currency.getPluralDisplayName().toPlain());
                configManager.setValue("currencies.conf", configId + ".symbol", String.class, currency.getSymbol().toPlain());
                src.sendMessage(messageStorage.getMessage("command.currency.created", "currency", currency.getDisplayName().toPlain()));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.error"));
        }
    }
}
