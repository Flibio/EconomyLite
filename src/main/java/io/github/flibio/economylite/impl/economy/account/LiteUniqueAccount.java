/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl.economy.account;

import io.github.flibio.economylite.CauseFactory;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.economylite.impl.economy.event.LiteEconomyTransactionEvent;
import io.github.flibio.economylite.impl.economy.result.LiteTransactionResult;
import io.github.flibio.economylite.impl.economy.result.LiteTransferResult;
import io.github.flibio.utils.player.NameUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class LiteUniqueAccount implements UniqueAccount {

    private PlayerEconService playerService = EconomyLite.getPlayerService();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    static private Semaphore lock = new Semaphore(1);
    static private long threadID = 0;
    static private int numLock = 0;

    private UUID uuid;
    private String name;

    public LiteUniqueAccount(UUID uuid) {
        this.uuid = uuid;
        try {
            Optional<String> nOpt = NameUtils.getName(uuid);
            if (nOpt.isPresent()) {
                this.name = nOpt.get();
            } else {
                this.name = uuid.toString();
            }
        } catch (Exception e) {
            this.name = uuid.toString();
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.of(name);
    }

    @Override
    public BigDecimal getDefaultBalance(Currency currency) {
        Optional<Double> bOpt = EconomyLite.getConfigManager().getValue(Double.class, "default-balance", "player");
        if (bOpt.isPresent()) {
            return BigDecimal.valueOf(bOpt.get());
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean hasBalance(Currency currency, Set<Context> contexts) {
        return playerService.accountExists(uuid, currency, CauseFactory.create("Has Balance"));
    }

    @Override
    public BigDecimal getBalance(Currency currency, Set<Context> contexts) {
        acquireLock();
        if (!hasBalance(currency, contexts)) {
            playerService.setBalance(uuid, getDefaultBalance(currency), currency, CauseFactory.create("New Account"));
        }
        releaseLock();
        return playerService.getBalance(uuid, currency, CauseFactory.create("Get Balance"));
    }

    @Override
    public Map<Currency, BigDecimal> getBalances(Set<Context> contexts) {
        HashMap<Currency, BigDecimal> balances = new HashMap<Currency, BigDecimal>();
        for (Currency currency : currencyService.getCurrencies()) {
            if (playerService.accountExists(uuid, currency, CauseFactory.create("Get Balances"))) {
                balances.put(currency, playerService.getBalance(uuid, currency, CauseFactory.create("Get Balances Put")));
            }
        }
        return balances;
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        // Check if the new balance is in bounds
        if (amount.compareTo(BigDecimal.ZERO) == -1 || amount.compareTo(BigDecimal.valueOf(999999999)) == 1) {
            return resultAndEvent(this, amount, currency, ResultType.ACCOUNT_NO_SPACE, TransactionTypes.DEPOSIT, cause);
        }
        acquireLock();
        if (playerService.setBalance(uuid, amount, currency, cause)) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.SUCCESS, TransactionTypes.DEPOSIT, cause);
        } else {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.FAILED, TransactionTypes.DEPOSIT, cause);
        }
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause, Set<Context> contexts) {
        HashMap<Currency, TransactionResult> results = new HashMap<>();
        acquireLock();
        for (Currency currency : currencyService.getCurrencies()) {
            if (playerService.accountExists(uuid, currency, cause)) {
                if (playerService.setBalance(uuid, getDefaultBalance(currency), currency, cause)) {
                    results.put(currency, resultAndEvent(this, getBalance(currency), currency, ResultType.SUCCESS, TransactionTypes.WITHDRAW, cause));
                } else {
                    results.put(currency, resultAndEvent(this, getBalance(currency), currency, ResultType.FAILED, TransactionTypes.WITHDRAW, cause));
                }
            }
        }
        releaseLock();
        return results;
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause, Set<Context> contexts) {
        acquireLock();
        if (playerService.setBalance(uuid, getDefaultBalance(currency), currency, cause)) {
            releaseLock();
            return resultAndEvent(this, BigDecimal.ZERO, currency, ResultType.SUCCESS, TransactionTypes.WITHDRAW, cause);
        } else {
            releaseLock();
            return resultAndEvent(this, BigDecimal.ZERO, currency, ResultType.FAILED, TransactionTypes.WITHDRAW, cause);
        }
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        acquireLock();
        BigDecimal newBal = getBalance(currency).add(amount);
        // Check if the new balance is in bounds
        if (newBal.compareTo(BigDecimal.ZERO) == -1 || newBal.compareTo(BigDecimal.valueOf(999999999)) == 1) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.ACCOUNT_NO_SPACE, TransactionTypes.DEPOSIT, cause);
        }
        if (playerService.deposit(uuid, amount, currency, cause)) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.SUCCESS, TransactionTypes.DEPOSIT, cause);
        } else {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.FAILED, TransactionTypes.DEPOSIT, cause);
        }
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        acquireLock();
        BigDecimal newBal = getBalance(currency).subtract(amount);
        // Check if the new balance is in bounds
        if (newBal.compareTo(BigDecimal.ZERO) == -1) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.ACCOUNT_NO_FUNDS, TransactionTypes.WITHDRAW, cause);
        }
        if (newBal.compareTo(BigDecimal.valueOf(999999999)) == 1) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.ACCOUNT_NO_SPACE, TransactionTypes.WITHDRAW, cause);
        }
        if (playerService.withdraw(uuid, amount, currency, cause)) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.SUCCESS, TransactionTypes.WITHDRAW, cause);
        } else {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.FAILED, TransactionTypes.WITHDRAW, cause);
        }
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        acquireLock();
        BigDecimal newBal = to.getBalance(currency).add(amount);
        // Check if the new balance is in bounds
        if (newBal.compareTo(BigDecimal.ZERO) == -1 || newBal.compareTo(BigDecimal.valueOf(999999999)) == 1) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.ACCOUNT_NO_SPACE, to, cause);
        }
        // Check if the account has enough funds
        if (amount.compareTo(getBalance(currency)) == 1) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.ACCOUNT_NO_FUNDS, to, cause);
        }
        if (withdraw(currency, amount, cause).getResult().equals(ResultType.SUCCESS)
                && to.deposit(currency, amount, cause).getResult().equals(ResultType.SUCCESS)) {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.SUCCESS, to, cause);
        } else {
            releaseLock();
            return resultAndEvent(this, amount, currency, ResultType.FAILED, to, cause);
        }
    }

    @Override
    public String getIdentifier() {
        return uuid.toString();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return new HashSet<Context>();
    }

    private TransactionResult resultAndEvent(Account account, BigDecimal amount, Currency currency, ResultType resultType,
            TransactionType transactionType, Cause cause) {
        TransactionResult result = new LiteTransactionResult(account, amount, currency, resultType, transactionType);
        Sponge.getEventManager().post(new LiteEconomyTransactionEvent(result, UUID.fromString(account.getIdentifier()), cause));
        return result;
    }

    private TransferResult resultAndEvent(Account account, BigDecimal amount, Currency currency, ResultType resultType, Account toWho, Cause cause) {
        TransferResult result = new LiteTransferResult(account, amount, currency, resultType, toWho);
        Sponge.getEventManager().post(new LiteEconomyTransactionEvent(result, UUID.fromString(account.getIdentifier()), cause));
        return result;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    static private void acquireLock() {
        if (threadID > 0 && Thread.currentThread().getId() == threadID) {
            numLock++;
            return;
        }
        try {
            lock.acquire();
            threadID = Thread.currentThread().getId();
	    } catch (InterruptedException e) {
            e.printStackTrace();
	    }
    }
    
    static private void releaseLock() {
        if (numLock == 0) {
            threadID = 0;
            lock.release();
        } else {
        	numLock--;
        }
    }
}
