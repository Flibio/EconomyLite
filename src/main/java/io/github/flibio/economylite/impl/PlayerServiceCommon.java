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
import org.spongepowered.api.service.economy.account.UniqueAccount;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.utils.sql.SqlManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerServiceCommon implements PlayerEconService {

    private SqlManager manager;

    public PlayerServiceCommon(SqlManager manager) {
        this.manager = manager;
        if (isWorking())
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS economyliteplayers(uuid VARCHAR(36), balance DECIMAL(11,2), currency VARCHAR(1024))");
    }

    public boolean isWorking() {
        return manager.testConnection();
    }

    public BigDecimal getBalance(UUID uuid, Currency currency) {
        Optional<BigDecimal> bOpt =
                manager.queryType("balance", BigDecimal.class, "SELECT balance FROM economyliteplayers WHERE uuid = ? AND currency = ?",
                        uuid.toString(), currency.getId());
        return (bOpt.isPresent()) ? bOpt.get() : BigDecimal.ZERO;
    }

    public boolean setBalance(UUID uuid, BigDecimal balance, Currency currency) {
        if (accountExists(uuid, currency)) {
            return manager.executeUpdate("UPDATE economyliteplayers SET balance = ? WHERE uuid = ? AND currency = ?", balance.toString(),
                    uuid.toString(), currency.getId());
        } else {
            return manager.executeUpdate("INSERT INTO economyliteplayers (`uuid`, `balance`, `currency`) VALUES (?, ?, ?)", uuid.toString(),
                    balance.toString(), currency.getId());
        }
    }

    public boolean accountExists(UUID uuid) {
        return manager.queryExists("SELECT uuid FROM economyliteplayers WHERE uuid = ?", uuid.toString());
    }

    public boolean accountExists(UUID uuid, Currency currency) {
        return manager.queryExists("SELECT uuid FROM economyliteplayers WHERE uuid = ? AND currency = ?", uuid.toString(), currency.getId());
    }

    public void clearCurrency(Currency currency) {
        manager.executeUpdate("DELETE FROM economyliteplayers WHERE currency = ?", currency.getId());
    }

    public List<UniqueAccount> getTopAccounts(int start, int end) {
        int offset = start - 1;
        int limit = end - offset;
        ArrayList<UniqueAccount> accounts = new ArrayList<>();
        List<String> uuids =
                manager.queryTypeList("uuid", String.class,
                        "SELECT uuid FROM economyliteplayers WHERE currency = ? ORDER BY balance DESC LIMIT ?, ?",
                        EconomyLite.getEconomyService().getDefaultCurrency().getId(), String.valueOf(offset), String.valueOf(limit));
        EconomyService ecoService = EconomyLite.getEconomyService();
        for (String uuid : uuids) {
            Optional<UniqueAccount> uOpt = ecoService.getOrCreateAccount(UUID.fromString(uuid));
            if (uOpt.isPresent()) {
                accounts.add(uOpt.get());
            }
        }
        return accounts;
    }
}
