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

import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.utils.sql.SqlManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VirtualServiceCommon implements VirtualEconService {

    private SqlManager manager;

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

    public BigDecimal getBalance(String id, Currency currency) {
        Optional<BigDecimal> bOpt =
                manager.queryType("balance", BigDecimal.class, "SELECT balance FROM economylitevirts WHERE id = ? AND currency = ?", id,
                        currency.getId());
        return (bOpt.isPresent()) ? bOpt.get() : BigDecimal.ZERO;
    }

    public boolean setBalance(String id, BigDecimal balance, Currency currency) {
        if (accountExists(id, currency)) {
            return manager.executeUpdate("UPDATE economylitevirts SET balance = ? WHERE id = ? AND currency = ?", balance.toString(), id,
                    currency.getId());
        } else {
            return manager.executeUpdate("INSERT INTO economylitevirts (`id`, `balance`, `currency`) VALUES (?, ?, ?)", id, balance.toString(),
                    currency.getId());
        }
    }

    public boolean accountExists(String id) {
        return manager.queryExists("SELECT id FROM economylitevirts WHERE id = ?", id);
    }

    public boolean accountExists(String id, Currency currency) {
        return manager.queryExists("SELECT id FROM economylitevirts WHERE id = ? AND currency = ?", id, currency.getId());
    }

    public void clearCurrency(Currency currency) {
        manager.executeUpdate("DELETE FROM economylitevirts WHERE currency = ?", currency.getId());
    }

    public List<VirtualAccount> getTopAccounts() {
        ArrayList<VirtualAccount> accounts = new ArrayList<>();
        List<String> ids =
                manager.queryTypeList("id", String.class, "SELECT id FROM economylitevirts WHERE currency = ? ORDER BY balance DESC LIMIT 3",
                        EconomyLite.getEconomyService().getDefaultCurrency().getId());
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
