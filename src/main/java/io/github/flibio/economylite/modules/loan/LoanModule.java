/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
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
import io.github.flibio.utils.config.ConfigManager;
import io.github.flibio.utils.file.FileManager;
import io.github.flibio.utils.message.MessageStorage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LoanModule implements Module {

    private MessageStorage messages = EconomyLite.getMessageStorage();
    private ConfigManager configManager = EconomyLite.getConfigManager();
    private LoanManager loanManager;
    private double interestRate = 1.0;
    private double maxLoan = 1000.0;

    public HashMap<UUID, Double> tableLoans = new HashMap<>();

    @Override
    public boolean initialize(Logger logger, Object plugin) {
        Optional<Double> iOpt = configManager.getValue(Double.class, "modules", "loan", "interest-rate");
        Optional<Double> mOpt = configManager.getValue(Double.class, "modules", "loan", "max-loan-balance");
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
        configManager.setDefault(Boolean.class, false, "modules", "loan", "enabled");
        configManager.setDefault(Double.class, 1.0, "modules", "loan", "interest-rate");
        configManager.setDefault(Double.class, 1000.0, "modules", "loan", "max-loan-balance");

        CommentedConfigurationNode con = configManager.getNode();
        Map<String, Boolean> defaultPerms = ImmutableMap.of("reward.permission", true);
        if (con.getNode("modules").getNode("loan").getNode("debtor-perms").isVirtual()) {
            con.getNode("modules").getNode("loan").getNode("debtor-perms").setValue(defaultPerms);
            configManager.overwriteNode(con);
        }
    }

    @Override
    public String getName() {
        return "Loan";
    }

    @Override
    public boolean isEnabled() {
        return configManager.getValue(Boolean.class, false, "modules", "loan", "enabled");
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
        Map<String, Boolean> perms = new HashMap<>();
        CommentedConfigurationNode con = configManager.getNode();

        Map<Object, ? extends ConfigurationNode> map = con.getNode("modules").getNode("loan").getNode("debtor-perms").getChildrenMap();

        map.keySet().forEach(perm -> {
            perms.put(perm.toString(), map.get(perm).getBoolean());
        });
        return perms;
    }
}
