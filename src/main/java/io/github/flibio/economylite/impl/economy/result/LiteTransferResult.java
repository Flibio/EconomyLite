/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl.economy.result;

import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;

public class LiteTransferResult extends LiteTransactionResult implements TransferResult {
    
    private Account toWho;

    public LiteTransferResult(Account account, BigDecimal amount, Currency currency, ResultType result, Account toWho) {
        super(account, amount, currency, result, TransactionTypes.TRANSFER);
        this.toWho = toWho;
    }

    @Override
    public Account getAccountTo() {
        return this.toWho;
    }
}
