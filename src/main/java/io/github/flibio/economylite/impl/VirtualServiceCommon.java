/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
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
    private boolean log = true;
    private Logger logger = EconomyLite.getInstance().getLogger();

    public VirtualServiceCommon(SqlManager manager, boolean h2) {
        this.manager = manager;
        this.log = EconomyLite.isEnabled("debug-logging");
        if (manager.initialTestConnection()) {
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS economylitevirts(id VARCHAR(36), balance DECIMAL(11,2), currency VARCHAR(1024))");
            if (h2) {
                manager.executeUpdate("ALTER TABLE `economylitevirts` ALTER COLUMN `id` VARCHAR(1024)");
            } else {
                manager.executeUpdate("ALTER TABLE `economylitevirts` CHANGE `id` `id` VARCHAR(1024)");
            }
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
        debug("virtcommon: Balance of '" + id + "' - " + cause.toString() + " = " + result.toPlainString());
        return result;
    }

    public boolean setBalance(String id, BigDecimal balance, Currency currency, Cause cause) {
        if (accountExists(id, currency, cause)) {
            boolean result = manager.executeUpdate("UPDATE economylitevirts SET balance = ? WHERE id = ? AND currency = ?", balance.toString(), id,
                    currency.getId());
            debug("virtcommon: +Account Exists+ Setting balance of '" + id + "' to '" + balance.toPlainString() + "' with '"
                    + currency.getId() + "' - " + cause.toString() + " = " + result);
            return result;
        } else {
            boolean result =
                    manager.executeUpdate("INSERT INTO economylitevirts (`id`, `balance`, `currency`) VALUES (?, ?, ?)", id, balance.toString(),
                            currency.getId());
            debug("virtcommon: +Account Does Not Exist+ Setting balance of '" + id + "' to '" + balance.toPlainString()
                    + "' with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
            return result;
        }
    }

    public boolean accountExists(String id, Cause cause) {
        boolean result = manager.queryExists("SELECT id FROM economylitevirts WHERE id = ?", id);
        debug("virtcommon: '" + id + "' exists - " + cause.toString() + " = " + result);
        return result;
    }

    public boolean accountExists(String id, Currency currency, Cause cause) {
        boolean result = manager.queryExists("SELECT id FROM economylitevirts WHERE id = ? AND currency = ?", id, currency.getId());
        debug("virtcommon: Checking if '" + id + "' exists with '" + currency.getId() + "' - " + cause.toString() + " = "
                + result);
        return result;
    }

    public void clearCurrency(Currency currency, Cause cause) {
        boolean result = manager.executeUpdate("DELETE FROM economylitevirts WHERE currency = ?", currency.getId());
        debug("virtcommon: Clearing currency '" + currency.getId() + "' - " + cause.toString() + " = " + result);
    }

    public List<VirtualAccount> getTopAccounts(int start, int end, Cause cause) {
        debug("virtcommon: Getting top accounts - " + cause.toString());
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

    private void debug(String message) {
        if (log) {
            logger.debug(message);
        }
    }
}
