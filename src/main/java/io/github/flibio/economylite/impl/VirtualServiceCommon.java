/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.utils.sql.CacheManager;
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
import java.util.concurrent.TimeUnit;

public class VirtualServiceCommon implements VirtualEconService {

    private SqlManager manager;
    private boolean log;
    private Logger logger = EconomyLite.getInstance().getLogger();

    private CacheManager<String, BigDecimal> balCache;
    private CacheManager<String, Boolean> exCache;
    private CacheManager<String, List<VirtualAccount>> topCache;

    public VirtualServiceCommon(SqlManager manager, boolean h2) {
        this.manager = manager;
        this.log = EconomyLite.getConfigManager().getValue(Boolean.class, false, "debug-logging");
        if (manager.initialTestConnection()) {
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS economylitevirts(id VARCHAR(36), balance DECIMAL(11,2), currency VARCHAR(1024))");
            if (h2) {
                manager.executeUpdate("ALTER TABLE `economylitevirts` ALTER COLUMN `id` VARCHAR(1024)");
            } else {
                manager.executeUpdate("ALTER TABLE `economylitevirts` CHANGE `id` `id` VARCHAR(1024)");
            }
        }
        // Create caches
        balCache = CacheManager.create(logger, 64, 360);
        exCache = CacheManager.create(logger, 128, 360);
        topCache = CacheManager.create(logger, 16, 30);
    }

    public boolean isWorking() {
        return manager.testConnection();
    }

    public BigDecimal getBalance(String id, Currency currency, Cause cause) {
        BigDecimal result = balCache.getIfPresent(formId(id, currency));
        if (result != null) {
            debug("virtcommon: {C} Balance of '" + id + "' - " + cause.toString() + " = " + result.toPlainString());
            return result;
        }
        Optional<BigDecimal> bOpt =
                manager.queryType("balance", BigDecimal.class, "SELECT balance FROM economylitevirts WHERE id = ? AND currency = ?", id,
                        currency.getId());
        result = (bOpt.isPresent()) ? bOpt.get() : BigDecimal.ZERO;
        balCache.update(formId(id, currency), result);
        exCache.update(formId(id, currency), true);
        debug("virtcommon: Balance of '" + id + "' - " + cause.toString() + " = " + result.toPlainString());
        return result;
    }

    public boolean setBalance(String id, BigDecimal balance, Currency currency, Cause cause) {
        boolean result;
        if (accountExists(id, currency, cause)) {
            result = manager.executeUpdate("UPDATE economylitevirts SET balance = ? WHERE id = ? AND currency = ?", balance.toString(), id,
                    currency.getId());
            debug("virtcommon: +Account Exists+ Setting balance of '" + id + "' to '" + balance.toPlainString() + "' with '"
                    + currency.getId() + "' - " + cause.toString() + " = " + result);
        } else {
            result = manager.executeUpdate("INSERT INTO economylitevirts (`id`, `balance`, `currency`) VALUES (?, ?, ?)", id, balance.toString(),
                    currency.getId());
            debug("virtcommon: +Account Does Not Exist+ Setting balance of '" + id + "' to '" + balance.toPlainString()
                    + "' with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
        }
        if (result) {
            balCache.update(formId(id, currency), balance);
            exCache.update(formId(id, currency), true);
        }
        return result;
    }

    public boolean accountExists(String id, Currency currency, Cause cause) {
        Boolean result = exCache.getIfPresent(formId(id, currency));
        if (result != null) {
            debug("virtcommon: {C} Checking if '" + id + "' exists with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
            return result;
        }
        result = manager.queryExists("SELECT id FROM economylitevirts WHERE id = ? AND currency = ?", id, currency.getId());
        debug("virtcommon: Checking if '" + id + "' exists with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
        exCache.update(formId(id, currency), result);
        return result;
    }

    public void clearCurrency(Currency currency, Cause cause) {
        boolean result = manager.executeUpdate("DELETE FROM economylitevirts WHERE currency = ?", currency.getId());
        debug("virtcommon: Clearing currency '" + currency.getId() + "' - " + cause.toString() + " = " + result);
        balCache.clear();
        exCache.clear();
        topCache.clear();
    }

    public List<VirtualAccount> getTopAccounts(int start, int end, Cause cause) {
        debug("virtcommon: Getting top accounts - " + cause.toString());
        String mid = start + "-" + end + ":" + EconomyLite.getEconomyService().getDefaultCurrency().getId();
        List<VirtualAccount> accounts = topCache.getIfPresent(mid);
        if (accounts != null) {
            return accounts;
        }
        int offset = start - 1;
        int limit = end - offset;
        accounts = new ArrayList<>();
        List<String> ids =
                manager.queryTypeList("id", String.class, "SELECT id FROM economylitevirts WHERE currency = ? ORDER BY balance DESC LIMIT ?, ?",
                        EconomyLite.getEconomyService().getDefaultCurrency().getId(), offset, limit);
        EconomyService ecoService = EconomyLite.getEconomyService();
        for (String id : ids) {
            Optional<Account> vOpt = ecoService.getOrCreateAccount(id);
            if (vOpt.isPresent() && (vOpt.get() instanceof VirtualAccount)) {
                accounts.add((VirtualAccount) vOpt.get());
            }
        }
        topCache.update(mid, accounts);
        return accounts;
    }

    private void debug(String message) {
        if (log) {
            logger.debug(message);
        }
    }

    private String formId(String id, Currency currency) {
        return id + ":" + currency.getId();
    }
}
