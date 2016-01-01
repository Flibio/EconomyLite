package me.Flibio.EconomyLite.API;

import me.Flibio.EconomyLite.EconomyLite;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class LiteTransactionResult implements TransactionResult {
	
	private Account account;
	private BigDecimal amount;
	private Set<Context> contexts;
	private Currency currency;
	private ResultType result;
	private TransactionType transactionType;
	
	public LiteTransactionResult(Account account, BigDecimal amount, ResultType result, TransactionType transactionType) {
		this.account = account;
		this.amount = amount;
		this.contexts = new HashSet<Context>();
		this.currency = EconomyLite.getCurrency();
		this.result = result;
		this.transactionType = transactionType;
	}

	@Override
	public Account getAccount() {
		return this.account;
	}

	@Override
	public BigDecimal getAmount() {
		return this.amount;
	}

	@Override
	public Set<Context> getContexts() {
		return this.contexts;
	}

	@Override
	public Currency getCurrency() {
		return this.currency;
	}

	@Override
	public ResultType getResult() {
		return this.result;
	}

	@Override
	public TransactionType getType() {
		return this.transactionType;
	}

}
