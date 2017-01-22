/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2017 Flibio
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

import io.github.flibio.economylite.CauseFactory;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.file.FileManager;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class LoanManager {

    private FileManager manager = EconomyLite.getFileManager();
    private EconomyService eco = EconomyLite.getEconomyService();
    private LoanModule module;

    public LoanManager(LoanModule module) {
        this.module = module;
        // Initialize the file
        manager.getFile("player-loans.data");
    }

    /**
     * Gets the loan balance of a player.
     * 
     * @param uuid The player to get the balance of.
     * @return The player's balance, if no error has occurred.
     */
    public Optional<Double> getLoanBalance(UUID uuid) {
        Currency cur = eco.getDefaultCurrency();
        manager.setDefault("player-loans.data", uuid.toString() + "." + cur.getId() + ".balance", Double.class, 0.0);
        return manager.getValue("player-loans.data", uuid.toString() + "." + cur.getId() + ".balance", Double.class);
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
        if (amount < 0 || amount > module.getMaxLoan())
            return false;
        Currency cur = eco.getDefaultCurrency();
        return manager.setValue("player-loans.data", uuid.toString() + "." + cur.getId() + ".balance", Double.class, amount);
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
            if (amount < 0 || bal + (amount * module.getInterestRate()) > module.getMaxLoan())
                return false;
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
            if (bal - amount < 0 || amount < 0)
                return false;
            return (uOpt.get().withdraw(cur, BigDecimal.valueOf(amount), CauseFactory.stringCause("loan")).getResult()
                    .equals(ResultType.SUCCESS) && setLoanBalance(uuid, bal - amount));
        }
        return false;
    }
}
