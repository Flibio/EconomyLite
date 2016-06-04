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
package io.github.flibio.economylite;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.economylite.commands.MigrateCommand;
import io.github.flibio.economylite.commands.PayCommand;
import io.github.flibio.economylite.commands.admin.AddCommand;
import io.github.flibio.economylite.commands.admin.EconCommand;
import io.github.flibio.economylite.commands.admin.RemoveCommand;
import io.github.flibio.economylite.commands.admin.SetCommand;
import io.github.flibio.economylite.commands.balance.BalTopCommand;
import io.github.flibio.economylite.commands.balance.BalanceCommand;
import io.github.flibio.economylite.commands.currency.CurrencyCommand;
import io.github.flibio.economylite.commands.currency.CurrencyCreateCommand;
import io.github.flibio.economylite.commands.currency.CurrencyDeleteCommand;
import io.github.flibio.economylite.commands.currency.CurrencySetCommand;
import io.github.flibio.economylite.commands.virtual.VirtualAddCommand;
import io.github.flibio.economylite.commands.virtual.VirtualBalanceCommand;
import io.github.flibio.economylite.commands.virtual.VirtualEconCommand;
import io.github.flibio.economylite.commands.virtual.VirtualRemoveCommand;
import io.github.flibio.economylite.commands.virtual.VirtualSetCommand;
import io.github.flibio.economylite.impl.CurrencyService;
import io.github.flibio.economylite.impl.PlayerDataService;
import io.github.flibio.economylite.impl.VirtualDataService;
import io.github.flibio.economylite.impl.economy.LiteCurrency;
import io.github.flibio.economylite.impl.economy.LiteEconomyService;
import io.github.flibio.economylite.impl.economy.registry.CurrencyRegistryModule;
import io.github.flibio.economylite.modules.Module;
import io.github.flibio.economylite.modules.sql.SqlModule;
import io.github.flibio.utils.commands.CommandLoader;
import io.github.flibio.utils.file.ConfigManager;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.updatifier.Updatifier;
import net.minecrell.mcstats.SpongeStatsLite;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;

@Updatifier(repoName = "EconomyLite", repoOwner = "Flibio", version = "v" + PluginInfo.VERSION)
@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION)
public class EconomyLite {

    @Inject private Logger logger;

    @Inject private Game game;

    @Inject private SpongeStatsLite statsLite;

    @Inject private PluginContainer container;

    private static ConfigManager configManager;
    private static MessageStorage messageStorage;
    private static EconomyLite instance;

    public static LiteEconomyService economyService;
    private static VirtualEconService virtualEconService;
    private static PlayerEconService playerEconService;
    public static CurrencyEconService currencyEconService;

    @Listener
    public void onServerInitialize(GameInitializationEvent event) {
        logger.info("EconomyLite " + PluginInfo.VERSION + " is initializing!");
        instance = this;
        // Start Statslite
        this.statsLite.start();
        // File setup
        configManager = ConfigManager.createInstance(this).get();
        initializeFiles();
        initializeCurrencies();
        // Load Message Storage
        messageStorage = MessageStorage.createInstance(this);
        initializeMessage();
        // Load modules
        getModules().forEach(m -> {
            m.initializeConfig();
            if (m.isEnabled()) {
                if (m.initialize(logger, instance)) {
                    logger.info("Loaded the " + m.getName() + " module!");
                } else {
                    logger.error("Failed to load the " + m.getName() + " module!");
                }
            }
        });
        // If the services have not been set, set them to default.
        if (playerEconService == null || virtualEconService == null) {
            playerEconService = new PlayerDataService();
            virtualEconService = new VirtualDataService();
        }
        // Load the economy service
        economyService = new LiteEconomyService();
        // Register the Economy Service
        game.getServiceManager().setProvider(this, EconomyService.class, economyService);
        CommandLoader.registerCommands(this, TextSerializers.FORMATTING_CODE.serialize(messageStorage.getMessage("command.invalidsource")),
                new CurrencyCommand(),
                new CurrencySetCommand(),
                new CurrencyDeleteCommand(),
                new CurrencyCreateCommand(),
                new BalanceCommand(),
                new EconCommand(),
                new SetCommand(),
                new RemoveCommand(),
                new AddCommand(),
                new PayCommand(),
                new BalTopCommand(),
                new VirtualBalanceCommand(),
                new VirtualEconCommand(),
                new VirtualAddCommand(),
                new VirtualSetCommand(),
                new VirtualRemoveCommand(),
                new MigrateCommand()
                );
        // Register currency registry
        game.getRegistry().registerModule(Currency.class, new CurrencyRegistryModule());
    }

    private void initializeFiles() {
        configManager.setDefault("config.conf", "default-balance", Double.class, 0.0);
        configManager.setDefault("config.conf", "virt-default-balance", Double.class, 0.0);
    }

    public static boolean isEnabled(String path) {
        if (configManager.getValue("config.conf", path, Boolean.class).isPresent()) {
            return configManager.getValue("config.conf", path, Boolean.class).get();
        } else {
            getInstance().getLogger().error("An error has occurred loading config value " + path + "! Defaulting to false.");
            return false;
        }
    }

    private void initializeCurrencies() {
        // Initialize the default currency into file
        configManager.setDefault("currencies.conf", "current", String.class, "coin");
        configManager.setDefault("currencies.conf", "coin.singular", String.class, "Coin");
        configManager.setDefault("currencies.conf", "coin.plural", String.class, "Coins");
        configManager.setDefault("currencies.conf", "coin.symbol", String.class, "C");
        Currency defaultCurrency =
                new LiteCurrency(configManager.getValue("currencies.conf", "coin.singular", String.class).get(), configManager.getValue(
                        "currencies.conf", "coin.plural", String.class).get(), configManager.getValue("currencies.conf", "coin.symbol", String.class)
                        .get(), true, 2);
        currencyEconService = new CurrencyService(defaultCurrency);
        // Load all of the currencies
        Optional<ConfigurationNode> fOpt = configManager.getFile("currencies.conf");
        if (fOpt.isPresent()) {
            ConfigurationNode root = fOpt.get();
            for (Object raw : root.getChildrenMap().keySet()) {
                if (raw instanceof String) {
                    String currencyId = (String) raw;
                    Optional<String> sOpt = configManager.getValue("currencies.conf", currencyId + ".singular", String.class);
                    Optional<String> pOpt = configManager.getValue("currencies.conf", currencyId + ".plural", String.class);
                    Optional<String> syOpt = configManager.getValue("currencies.conf", currencyId + ".symbol", String.class);
                    if (sOpt.isPresent() && pOpt.isPresent() && syOpt.isPresent() && !currencyId.equals("coin")) {
                        Currency currency = new LiteCurrency(sOpt.get(), pOpt.get(), syOpt.get(), false, 2);
                        currencyEconService.addCurrency(currency);
                    }
                }
            }
        }
        // Attempt to load the current currency
        Optional<String> cOpt = configManager.getValue("currencies.conf", "current", String.class);
        if (cOpt.isPresent()) {
            String currentCur = cOpt.get();
            currencyEconService.getCurrencies().forEach(c -> {
                if (("economylite:" + currentCur).equalsIgnoreCase(c.getId())) {
                    // This is the current currency
                    currencyEconService.setCurrentCurrency(c);
                }
            });
        }
        // If the current currency string failed to load set it to default
        if (currencyEconService.getCurrentCurrency().equals(defaultCurrency)) {
            configManager.setValue("currencies.conf", "current", String.class, "coin");
        }
        logger.info("Using currency: " + currencyEconService.getCurrentCurrency().getName());
    }

    private void initializeMessage() {
        messageStorage.defaultMessages("messages");
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public PluginContainer getPluginContainer() {
        return container;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static MessageStorage getMessageStorage() {
        return messageStorage;
    }

    public static PlayerEconService getPlayerService() {
        return playerEconService;
    }

    public static VirtualEconService getVirtualService() {
        return virtualEconService;
    }

    public static CurrencyEconService getCurrencyService() {
        return currencyEconService;
    }

    public static EconomyService getEconomyService() {
        return economyService;
    }

    public static EconomyLite getInstance() {
        return instance;
    }

    public static List<Module> getModules() {
        return ImmutableList.of(new SqlModule());
    }

    // Setters

    public static void setPlayerService(PlayerEconService serv) {
        playerEconService = serv;
    }

    public static void setVirtualService(VirtualEconService serv) {
        virtualEconService = serv;
    }

}
