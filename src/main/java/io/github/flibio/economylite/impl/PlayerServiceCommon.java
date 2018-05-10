/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl;

import io.github.flibio.economylite.CauseFactory;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.utils.sql.CacheManager;
import io.github.flibio.utils.sql.SqlManager;
import org.slf4j.Logger;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerServiceCommon implements PlayerEconService {

    private SqlManager manager;
    private boolean log;
    private Logger logger = EconomyLite.getInstance().getLogger();

    private CacheManager<String, BigDecimal> balCache;
    private CacheManager<String, Boolean> exCache;
    private CacheManager<String, List<UniqueAccount>> topCache;

    public PlayerServiceCommon(SqlManager manager, boolean h2) {
        this.manager = manager;
        this.log = EconomyLite.getConfigManager().getValue(Boolean.class, false, "debug-logging");
        if (manager.initialTestConnection()) {
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS economyliteplayers(uuid VARCHAR(36), balance DECIMAL(11,2), currency VARCHAR(1024))");
        }
        repair(h2);
        // Create caches
        balCache = CacheManager.create(logger, 64, 360);
        exCache = CacheManager.create(logger, 128, 360);
        topCache = CacheManager.create(logger, 16, 30);
    }

    private void repair(boolean h2) {
        if (h2) {
            try {
                ResultSet rs = manager.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE TABLE_NAME = 'ECONOMYLITEPLAYERS'").get();
                rs.next();
                rs.getString(1);
                logger.info("Repairing the database...");
            } catch (Exception e) {
                logger.debug("Database repairs not necessary!");
                return;
            }
            logger.info("Renaming database...");
            manager.executeUpdate("ALTER TABLE economyliteplayers RENAME TO economyliteplayersold");
            logger.info("Recreating database...");
            manager.executeUpdate("CREATE TABLE economyliteplayers AS SELECT * FROM economyliteplayersold");
            logger.info("Dropping database...");
            manager.executeUpdate("DROP TABLE economyliteplayersold");
            logger.info("Repairs complete!");
            return;
        } else {
            try {
                ResultSet rs = manager.executeQuery("show index from economyliteplayers where Column_name='uuid'").get();
                rs.next();
                rs.getString(1);
                logger.info("Repairing the database...");
            } catch (Exception e) {
                logger.debug("Database repairs not necessary!");
                return;
            }
            logger.info("Renaming database...");
            manager.executeUpdate("RENAME TABLE economyliteplayers TO economyliteplayersold");
            logger.info("Recreating database...");
            manager.executeUpdate("CREATE TABLE economyliteplayers AS SELECT * FROM economyliteplayersold");
            logger.info("Dropping database...");
            manager.executeUpdate("DROP TABLE economyliteplayersold");
            logger.info("Repairs complete!");
            return;
        }
    }

    public boolean isWorking() {
        return manager.testConnection();
    }

    public BigDecimal getBalance(UUID uuid, Currency currency, Cause cause, boolean cache) {
        BigDecimal result = balCache.getIfPresent(formId(uuid, currency));
        if (cache && result != null) {
            debug("playercommon: {C} Balance of '" + uuid.toString() + "' - " + cause.toString() + " = " + result.toPlainString());
            return result;
        }
        Optional<BigDecimal> bOpt =
                manager.queryType("balance", BigDecimal.class, "SELECT balance FROM economyliteplayers WHERE uuid = ? AND currency = ?",
                        uuid.toString(), currency.getId());
        result = (bOpt.isPresent()) ? bOpt.get() : BigDecimal.ZERO;
        balCache.update(formId(uuid, currency), result);
        exCache.update(formId(uuid, currency), true);
        debug("playercommon: Balance of '" + uuid.toString() + "' - " + cause.toString() + " = " + result.toPlainString());
        return result;
    }

    public boolean setBalance(UUID uuid, BigDecimal balance, Currency currency, Cause cause) {
        boolean result;
        if (accountExists(uuid, currency, cause)) {
            result = manager.executeUpdate("UPDATE economyliteplayers SET balance = ? WHERE uuid = ? AND currency = ?", balance.toString(),
                    uuid.toString(), currency.getId());
            debug("playercommon: +Account Exists+ Setting balance of '" + uuid.toString() + "' to '" + balance.toPlainString() + "' with '"
                    + currency.getId() + "' - " + cause.toString() + " = " + result);
        } else {
            result = manager.executeUpdate("INSERT INTO economyliteplayers (`uuid`, `balance`, `currency`) VALUES (?, ?, ?)",
                    uuid.toString(), balance.toString(), currency.getId());
            debug("playercommon: +Account Does Not Exist+ Setting balance of '" + uuid.toString() + "' to '" + balance.toPlainString()
                    + "' with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
        }
        if (result) {
            balCache.update(formId(uuid, currency), balance);
            exCache.update(formId(uuid, currency), true);
        }
        return result;
    }

    public boolean accountExists(UUID uuid, Currency currency, Cause cause) {
        Boolean result = exCache.getIfPresent(formId(uuid, currency));
        if (result != null) {
            debug("playercommon: {C} Checking if '" + uuid.toString() + "' exists with '" + currency.getId() + "' - " + cause.toString() + " = "
                    + result);
            return result;
        }
        result = manager.queryExists("SELECT uuid FROM economyliteplayers WHERE uuid = ? AND currency = ?", uuid.toString(), currency.getId());
        debug("playercommon: Checking if '" + uuid.toString() + "' exists with '" + currency.getId() + "' - " + cause.toString() + " = " + result);
        exCache.update(formId(uuid, currency), result);
        return result;
    }

    public void clearCurrency(Currency currency, Cause cause) {
        boolean result = manager.executeUpdate("DELETE FROM economyliteplayers WHERE currency = ?", currency.getId());
        debug("playercommon: Clearing currency '" + currency.getId() + "' - " + cause.toString() + " = " + result);
        balCache.clear();
        exCache.clear();
        topCache.clear();
    }

    public List<UniqueAccount> getTopAccounts(int start, int end, Cause cause) {
        debug("playercommon: Getting top accounts - " + cause.toString());
        String mid = start + "-" + end + ":" + EconomyLite.getEconomyService().getDefaultCurrency().getId();
        List<UniqueAccount> accounts = topCache.getIfPresent(mid);
        if (accounts != null) {
            return accounts;
        }
        int offset = start - 1;
        int limit = end - offset;
        accounts = new ArrayList<>();
        List<String> uuids =
                manager.queryTypeList("uuid", String.class,
                        "SELECT uuid FROM economyliteplayers WHERE currency = ? ORDER BY balance DESC LIMIT ?, ?",
                        EconomyLite.getEconomyService().getDefaultCurrency().getId(), offset, limit);
        EconomyService ecoService = EconomyLite.getEconomyService();
        for (String uuid : uuids) {
            Optional<UniqueAccount> uOpt = ecoService.getOrCreateAccount(UUID.fromString(uuid));
            if (uOpt.isPresent()) {
                accounts.add(uOpt.get());
            }
        }
        topCache.update(mid, accounts);
        return accounts;
    }

    public boolean setBalanceAll(BigDecimal balance, Currency currency, Cause cause) {
        boolean result = manager.executeUpdate("UPDATE economyliteplayers SET balance = ? WHERE currency = ?", balance.toString(), currency.getId());
        debug("playercommon: +Account Exists+ Setting balance of ALL to '" + balance.toPlainString() + "' with '"
                + currency.getId() + "' - " + cause.toString() + " = " + result);
        topCache.clear();
        balCache.clear();
        return result;
    }

    public List<String> getAccountsMigration() {
        List<String> accounts = new ArrayList<>();
        Optional<ResultSet> rOpt = manager.executeQuery("SELECT * FROM economyliteplayers");
        if (rOpt.isPresent()) {
            ResultSet rs = rOpt.get();
            try {
                while (rs.next()) {
                    accounts.add(rs.getString("uuid") + "%-%" + rs.getDouble("balance") + "%-%" + rs.getString("currency"));
                }
                return accounts;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return accounts;
            }
        }
        return accounts;
    }

    public void setRawData(String uuid, String bal, Currency currency) {
        if (accountExists(UUID.fromString(uuid), currency, CauseFactory.stringCause("Migration"))) {
            manager.executeUpdate("UPDATE economyliteplayers SET balance = ? WHERE uuid = ? AND currency = ?", bal, uuid, currency.getId());
        } else {
            manager.executeUpdate("INSERT INTO economyliteplayers (`uuid`, `balance`, `currency`) VALUES (?, ?, ?)", uuid, bal, currency.getId());
        }
    }

    private void debug(String message) {
        if (log) {
            logger.debug(message);
        }
    }

    private String formId(UUID id, Currency currency) {
        return id.toString() + ":" + currency.getId();
    }
}
