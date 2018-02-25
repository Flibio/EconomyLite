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
package io.github.flibio.economylite.modules.loan;

import com.google.common.collect.ImmutableMap;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.modules.Module;
import io.github.flibio.economylite.modules.loan.command.LoanAcceptCommand;
import io.github.flibio.economylite.modules.loan.command.LoanBalanceCommand;
import io.github.flibio.economylite.modules.loan.command.LoanCommand;
import io.github.flibio.economylite.modules.loan.command.LoanDenyCommand;
import io.github.flibio.economylite.modules.loan.command.LoanPayCommand;
import io.github.flibio.economylite.modules.loan.command.LoanTakeCommand;
import io.github.flibio.utils.commands.CommandLoader;
import io.github.flibio.utils.file.FileManager;
import io.github.flibio.utils.message.MessageStorage;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LoanModule implements Module {

    private MessageStorage messages = EconomyLite.getMessageStorage();
    private FileManager configManager = EconomyLite.getFileManager();
    private LoanManager loanManager;
    private double interestRate = 1.0;
    private double maxLoan = 1000.0;

    public HashMap<UUID, Double> tableLoans = new HashMap<>();

    @Override
    public boolean initialize(Logger logger, Object plugin) {
        Optional<Double> iOpt = configManager.getValue("config.conf", "modules.loan.interest-rate", Double.class);
        Optional<Double> mOpt = configManager.getValue("config.conf", "modules.loan.max-loan-balance", Double.class);
        if (iOpt.isPresent() && mOpt.isPresent()) {
            interestRate = iOpt.get();
            maxLoan = mOpt.get();
            if (interestRate < 1) {
                logger.error("The interest rate must be greater than or equal to 1!");
                return false;
            }
            if (maxLoan <= 0) {
                logger.error("The interest rate must be greater than 0!");
                return false;
            }
            logger.info("Interest rate set to " + interestRate + "!");
            logger.info("Maximum loan balance set to " + maxLoan + "!");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void postInitialization(Logger logger, Object plugin) {
        // Create loan manager
        loanManager = new LoanManager(this);
        // Register listeners
        Sponge.getEventManager().registerListeners(plugin, new LoanListener(this));
        // Register commands
        CommandLoader.registerCommands(plugin, TextSerializers.FORMATTING_CODE.serialize(messages.getMessage("command.invalidsource")),
                new LoanCommand(),
                new LoanBalanceCommand(this),
                new LoanPayCommand(this),
                new LoanTakeCommand(this),
                new LoanAcceptCommand(this),
                new LoanDenyCommand(this)
                );
    }

    @Override
    public void initializeConfig() {
        configManager.setDefault("config.conf", "modules.loan.enabled", Boolean.class, false);
        configManager.setDefault("config.conf", "modules.loan.interest-rate", Double.class, 1.0);
        configManager.setDefault("config.conf", "modules.loan.max-loan-balance", Double.class, 1000.0);

        Optional<ConfigurationNode> cOpt = configManager.getFile("config.conf");
        if (cOpt.isPresent()) {
            ConfigurationNode con = cOpt.get();
            Map<String, Boolean> defaultPerms = ImmutableMap.of("reward.permission", true);
            if (con.getNode("modules").getNode("loan").getNode("debtor-perms").isVirtual()) {
                con.getNode("modules").getNode("loan").getNode("debtor-perms").setValue(defaultPerms);
                configManager.saveFile("config.conf", con);
            }
        }
    }

    @Override
    public String getName() {
        return "Loan";
    }

    @Override
    public boolean isEnabled() {
        Optional<Boolean> eOpt = configManager.getValue("config.conf", "modules.loan.enabled", Boolean.class);
        return (eOpt.isPresent()) ? eOpt.get() : false;
    }

    public LoanManager getLoanManager() {
        return loanManager;
    }

    public double getMaxLoan() {
        return maxLoan;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public Map<String, Boolean> getPermissions() {
        Optional<ConfigurationNode> cOpt = configManager.getFile("config.conf");
        Map<String, Boolean> perms = new HashMap<>();
        if (cOpt.isPresent()) {
            ConfigurationNode con = cOpt.get();

            Map<Object, ? extends ConfigurationNode> map = con.getNode("modules").getNode("loan").getNode("debtor-perms").getChildrenMap();

            map.keySet().forEach(perm -> {
                perms.put(perm.toString(), map.get(perm).getBoolean());
            });
        }
        return perms;
    }
}
