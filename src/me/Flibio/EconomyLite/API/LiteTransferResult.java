package me.Flibio.EconomyLite.API;

import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;

public class LiteTransferResult extends LiteTransactionResult implements TransferResult {
	
	private Account toWho;

	public LiteTransferResult(Account account, BigDecimal amount, ResultType result, Account toWho) {
		super(account, amount, result, TransactionTypes.TRANSFER);
		this.toWho = toWho;
	}

	@Override
	public Account getAccountTo() {
		return this.toWho;
	}
	
}
