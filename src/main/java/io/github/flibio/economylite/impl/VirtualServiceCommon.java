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
package io.github.flibio.economylite.impl;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.utils.sql.SqlManager;
import org.slf4j.Logger;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VirtualServiceCommon implements VirtualEconService {

    private SqlManager manager;
    private Logger logger = EconomyLite.getInstance().getLogger();

    public VirtualServiceCommon(SqlManager manager) {
        this.manager = manager;
        if (isWorking()) {
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS economylitevirts(id VARCHAR(36), balance DECIMAL(11,2), currency VARCHAR(1024))");
            manager.executeUpdate("ALTER TABLE `economylitevirts` CHANGE `id` `id` VARCHAR(1024)");
        }
    }

    public boolean isWorking() {
        return manager.testConnection();
    }

    public BigDecimal getBalance(String id, Currency currency, Cause cause) {
        Optional<BigDecimal> bOpt =
                manager.queryType("balance", BigDecimal.class, "SELECT balance FROM economylitevirts WHERE id = ? AND currency = ?", id,
                        currency.getId());
        BigDecimal result = (bOpt.isPresent()) ? bOpt.get() : BigDecimal.ZERO;
        logger.debug("virtcommon: Balance of '" + id + "' - " + cause.toString() + " = " + result.toPlainString());
        return result;
    }

    public boolean setBalance(String id, BigDecimal balance, Currency currency, Cause cause) {
        if (accountExists(id, currency, cause)) {
            boolean result = manager.executeUpdate("UPDATE economylitevirts SET balance = ? WHERE id = ? AND currency = ?", balance.toString(), id,
                    currency.getId());
            logger.debug("virtcommon: +Account Exists+ Setting balance of '" + id + "' to '" + balance.toPlainString() + "' with '"
                    + currency.getId() + "' - " + cause.toString() + " = " + result);
            return result;
        } else {
            boolean result =
                    manager.executeUpdate("INSERT INTO economylitevirts (`id`, `balance`, `currency`) VALUES (?, ?, ?)", id, balance.toString(),
                            currency.getId());
            logger.debug("virtcommon: +Account Does Not Exist+ Setting balance of '" + id + "' to '" + balance.toPlainString()
                    + "' with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
            return result;
        }
    }

    public boolean accountExists(String id, Cause cause) {
        boolean result = manager.queryExists("SELECT id FROM economylitevirts WHERE id = ?", id);
        logger.debug("virtcommon: '" + id + "' exists - " + cause.toString() + " = " + result);
        return result;
    }

    public boolean accountExists(String id, Currency currency, Cause cause) {
        boolean result = manager.queryExists("SELECT id FROM economylitevirts WHERE id = ? AND currency = ?", id, currency.getId());
        logger.debug("virtcommon: Checking if '" + id + "' exists with '" + currency.getId() + "' - " + cause.toString() + " = "
                + result);
        return result;
    }

    public void clearCurrency(Currency currency, Cause cause) {
        boolean result = manager.executeUpdate("DELETE FROM economylitevirts WHERE currency = ?", currency.getId());
        logger.debug("virtcommon: Clearing currency '" + currency.getId() + "' - " + cause.toString() + " = " + result);
    }

    public List<VirtualAccount> getTopAccounts(int start, int end, Cause cause) {
        logger.debug("virtcommon: Getting top accounts - " + cause.toString());
        int offset = start - 1;
        int limit = end - offset;
        ArrayList<VirtualAccount> accounts = new ArrayList<>();
        List<String> ids =
                manager.queryTypeList("id", String.class, "SELECT id FROM economylitevirts WHERE currency = ? ORDER BY balance DESC LIMIT ?, ?",
                        EconomyLite.getEconomyService().getDefaultCurrency().getId(), String.valueOf(offset), String.valueOf(limit));
        EconomyService ecoService = EconomyLite.getEconomyService();
        for (String id : ids) {
            Optional<Account> vOpt = ecoService.getOrCreateAccount(id);
            if (vOpt.isPresent() && (vOpt.get() instanceof VirtualAccount)) {
                accounts.add((VirtualAccount) vOpt.get());
            }
        }
        return accounts;
    }
}
