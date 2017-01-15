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
package io.github.flibio.economylite.commands;

import io.github.flibio.economylite.CauseFactory;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.UUID;

@AsyncCommand
@Command(aliases = {"migrate"}, permission = "economylite.admin.migrate")
public class MigrateCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private PlayerEconService playerService = EconomyLite.getPlayerService();
    private VirtualEconService virtualService = EconomyLite.getVirtualService();
    private Logger logger = EconomyLite.getInstance().getLogger();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.string(Text.of("mode")), GenericArguments.optional(GenericArguments.bool(Text.of("confirm"))));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        if (args.getOne("mode").isPresent()) {
            // Check if migration is confirmed
            if (args.getOne("confirm").isPresent() && args.<Boolean>getOne("confirm").get()) {
                // Attempt to migrate
                String mode = args.<String>getOne("mode").get();
                if (mode.equalsIgnoreCase("economylite1")) {
                    // Migrate from EconomyLite 1
                    File file = new File("config/EconomyLite/data.conf");
                    File bFile = new File("config/EconomyLite/businesses.conf");
                    try {
                        if (file.exists()) {
                            ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(file).build();
                            ConfigurationNode root = manager.load();
                            root.getChildrenMap().keySet().forEach(raw -> {
                                if (raw instanceof String) {
                                    UUID uuid = UUID.fromString((String) raw);
                                    if (root.getNode(uuid.toString()).getNode("balance") != null) {
                                        int balance = root.getNode(uuid.toString()).getNode("balance").getInt();
                                        playerService.setBalance(uuid, BigDecimal.valueOf(balance),
                                                EconomyLite.getEconomyService().getDefaultCurrency(), CauseFactory.create("Migration"));
                                        logger.debug(uuid.toString() + ": " + balance);
                                    }
                                }
                            });
                        }
                        if (bFile.exists()) {
                            ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(bFile).build();
                            ConfigurationNode root = manager.load();
                            root.getChildrenMap().keySet().forEach(raw -> {
                                if (raw instanceof String) {
                                    String name = (String) raw;
                                    if (root.getNode(name).getNode("balance") != null) {
                                        int balance = root.getNode(name).getNode("balance").getInt();
                                        virtualService.setBalance(name, BigDecimal.valueOf(balance),
                                                EconomyLite.getEconomyService().getDefaultCurrency(), CauseFactory.create("Migration"));
                                        logger.debug(name + ": " + balance);
                                    }
                                }
                            });
                        }
                        src.sendMessage(messageStorage.getMessage("command.migrate.completed"));
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        src.sendMessage(messageStorage.getMessage("command.migrate.fail"));
                    }
                } else if (mode.equalsIgnoreCase("totaleconomy")) {
                    try {
                        // Migrate from TotalEconomy
                        String configDir = EconomyLite.getInstance().getMainDir();
                        if (!configDir.substring(configDir.length() - 1).equalsIgnoreCase("/")) {
                            configDir += "/";
                        }
                        // Check if the file exists
                        File file = new File(configDir + "totaleconomy/accounts.conf");
                        if (!file.exists()) {
                            logger.error("Could not find TotalEconomy file!");
                            throw new FileNotFoundException();
                        }
                        // Attempt to load the file
                        ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(file).build();
                        ConfigurationNode root = manager.load();
                        root.getChildrenMap().keySet().forEach(raw -> {
                            if (raw instanceof String) {
                                String uuid = (String) raw;
                                ConfigurationNode sub = root.getNode(uuid).getNode("dollar-balance");
                                if (!sub.isVirtual()) {
                                    playerService.setBalance(UUID.fromString(uuid), BigDecimal.valueOf(sub.getDouble()),
                                            EconomyLite.getEconomyService().getDefaultCurrency(), CauseFactory.create("Migration"));
                                    logger.debug(uuid.toString() + ": " + sub.getDouble());
                                }
                            }
                        });
                        src.sendMessage(messageStorage.getMessage("command.migrate.completed"));
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        src.sendMessage(messageStorage.getMessage("command.migrate.fail"));
                    }
                } else {
                    // Invalid mode
                    src.sendMessage(messageStorage.getMessage("command.migrate.nomode"));
                }
            } else {
                src.sendMessage(messageStorage.getMessage("command.migrate.confirm"));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.migrate.nomode"));
        }
    }
}
