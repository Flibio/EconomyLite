/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.loan;

import io.github.flibio.economylite.CauseFactory;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.modules.loan.event.LoanBalanceChangeEvent;
import io.github.flibio.utils.config.ConfigManager;
import io.github.flibio.utils.file.FileManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class LoanManager {

    private ConfigManager manager;
    private EconomyService eco = EconomyLite.getEconomyService();
    private LoanModule module;

    public LoanManager(LoanModule module) {
        this.module = module;
        // Initialize the file
        manager = ConfigManager.create(EconomyLite.getInstance().getConfigDir(), "player-loans.data", EconomyLite.getInstance().getLogger());
    }

    /**
     * Gets the loan balance of a player.
     *
     * @param uuid The player to get the balance of.
     * @return The player's balance, if no error has occurred.
     */
    public Optional<Double> getLoanBalance(UUID uuid) {
        Currency cur = eco.getDefaultCurrency();
        manager.setDefault(Double.class, 0.0, uuid.toString(), cur.getId(), "balance");
        manager.save();
        return manager.getValue(Double.class, uuid.toString(), cur.getId(), "balance");
    }

    /**
     * Sets the balance of a player.
     *
     * @param uuid The player to set the balance of.
     * @param amount The balance that will be set.
     * @return If the balance was successfully set.
     */
    public boolean setLoanBalance(UUID uuid, double amount) {
        // Make sure balance is within parameters
        if (amount < 0 || amount > module.getMaxLoan()) {
            return false;
        }
        // Fire loan balance change event
        Sponge.getEventManager().post(new LoanBalanceChangeEvent(amount, uuid));
        Currency cur = eco.getDefaultCurrency();
        manager.forceValue(amount, uuid.toString(), cur.getId(), "balance");
        manager.save();
        return true;
    }

    /**
     * Adds currency to a player's loan balance. Automatically charges interest
     * and adds to the player's account.
     *
     * @param uuid The player.
     * @param amount The amount to add.
     * @return If the amount was added successfully.
     */
    public boolean addLoanBalance(UUID uuid, double amount) {
        Currency cur = eco.getDefaultCurrency();
        Optional<Double> bOpt = getLoanBalance(uuid);
        Optional<UniqueAccount> uOpt = eco.getOrCreateAccount(uuid);
        if (bOpt.isPresent() && uOpt.isPresent()) {
            double bal = bOpt.get();
            if (amount < 0 || bal + (amount * module.getInterestRate()) > module.getMaxLoan()) {
                return false;
            }
            return (setLoanBalance(uuid, (amount * module.getInterestRate()) + bal) && uOpt.get()
                    .deposit(cur, BigDecimal.valueOf(amount), CauseFactory.stringCause("loan")).getResult().equals(ResultType.SUCCESS));
        }
        return false;
    }

    /**
     * Removes loan balance from a player. Automatically removes funds from the
     * player's account.
     *
     * @param uuid The player.
     * @param amount The amount to remove.
     * @return If the amount was removed successfully.
     */
    public boolean removeLoanBalance(UUID uuid, double amount) {
        Currency cur = eco.getDefaultCurrency();
        Optional<Double> bOpt = getLoanBalance(uuid);
        Optional<UniqueAccount> uOpt = eco.getOrCreateAccount(uuid);
        if (bOpt.isPresent() && uOpt.isPresent()) {
            double bal = bOpt.get();
            if (bal - amount < 0 || amount < 0) {
                return false;
            }
            return (uOpt.get().withdraw(cur, BigDecimal.valueOf(amount), CauseFactory.stringCause("loan")).getResult()
                    .equals(ResultType.SUCCESS) && setLoanBalance(uuid, bal - amount));
        }
        return false;
    }
}
