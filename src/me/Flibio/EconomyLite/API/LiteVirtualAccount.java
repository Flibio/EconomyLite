package me.Flibio.EconomyLite.API;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Events.LiteEconomyTransactionEvent;
import me.Flibio.EconomyLite.Utils.BusinessManager;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.VirtualAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LiteVirtualAccount implements VirtualAccount {
	
	private String id;
	private Text displayName;
	private BusinessManager businessManager;
	private LiteCurrency liteCurrency;
	
	//VirtualAccounts are treated as businesses, this will possibly change in the future
	public LiteVirtualAccount(String id) {
		this.id = id;
		this.displayName = Text.of(id);
		this.businessManager = new BusinessManager();
		this.liteCurrency = (LiteCurrency) EconomyLite.getCurrency();
	}

	@Override
	public TransactionResult deposit(Currency currency, BigDecimal amount,
			Cause cause, Set<Context> contexts) {
		if(businessManager.addCurrency(id,amount.setScale(0, RoundingMode.HALF_UP).intValue())) {
			TransactionResult result = new LiteTransactionResult(this,amount,ResultType.SUCCESS,TransactionTypes.TRANSFER);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		} else {
			TransactionResult result = new LiteTransactionResult(this,amount,ResultType.FAILED,TransactionTypes.TRANSFER);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		}
	}

	@Override
	public BigDecimal getBalance(Currency currency, Set<Context> contexts) {
		return BigDecimal.valueOf(businessManager.getBusinessBalance(id));	
	}

	@Override
	public Map<Currency, BigDecimal> getBalances(Set<Context> contexts) {
		int currentBal = businessManager.getBusinessBalance(id);
		if(currentBal<0) {
			return new HashMap<Currency, BigDecimal>();
		} else {
			HashMap<Currency, BigDecimal> map = new HashMap<Currency, BigDecimal>();
			map.put(liteCurrency, BigDecimal.valueOf(currentBal));
			return map;
		}
	}

	@Override
	public BigDecimal getDefaultBalance(Currency currency) {
		return BigDecimal.valueOf(0);
	}

	@Override
	public Text getDisplayName() {
		return this.displayName;
	}

	@Override
	public boolean hasBalance(Currency currency, Set<Context> contexts) {
		return businessManager.businessExists(id);
	}

	@Override
	public TransactionResult resetBalance(Currency currency, Cause cause,
			Set<Context> contexts) {
		return setBalance(currency,BigDecimal.ZERO,cause,contexts);
	}

	@Override
	public Map<Currency, TransactionResult> resetBalances(Cause cause, Set<Context> contexts) {
		Map<Currency, TransactionResult> map = new HashMap<>();
		map.put(liteCurrency, resetBalance(liteCurrency,cause,contexts));
		return map;
	}

	@Override
	public TransactionResult setBalance(Currency currency, BigDecimal amount,
			Cause cause, Set<Context> contexts) {
		if(businessManager.setBusinessBalance(id,amount.setScale(0, RoundingMode.HALF_UP).intValue())) {
			TransactionResult result = new LiteTransactionResult(this,amount,ResultType.SUCCESS,TransactionTypes.TRANSFER);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		} else {
			TransactionResult result = new LiteTransactionResult(this,amount,ResultType.FAILED,TransactionTypes.TRANSFER);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		}
	}

	@Override
	public TransferResult transfer(Account account, Currency currency,
			BigDecimal bigAmount, Cause cause, Set<Context> contexts) {
		int toAccountBal = account.getBalance(liteCurrency).setScale(0, RoundingMode.HALF_UP).intValue();
		int amount = bigAmount.setScale(0, RoundingMode.HALF_UP).intValue();
		if(toAccountBal+amount>1000000) {
			TransferResult result = new LiteTransferResult(this,BigDecimal.valueOf(amount),ResultType.ACCOUNT_NO_SPACE,account);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		} else {
			if(getBalance(liteCurrency, new HashSet<Context>()).setScale(0, RoundingMode.HALF_UP).intValue()<amount) {
				TransferResult result = new LiteTransferResult(this,BigDecimal.valueOf(amount),ResultType.ACCOUNT_NO_FUNDS,account);
				Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
				return result;
			} else {
				if(withdraw(liteCurrency,BigDecimal.valueOf(amount),cause,new HashSet<Context>()).getResult().equals(ResultType.SUCCESS)
						&&account.deposit(liteCurrency,BigDecimal.valueOf(amount),cause,new HashSet<Context>()).getResult().
						equals(ResultType.SUCCESS)) {
					TransferResult result = new LiteTransferResult(this,BigDecimal.valueOf(amount),ResultType.SUCCESS,account);
					Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
					return result;
				} else {
					TransferResult result = new LiteTransferResult(this,BigDecimal.valueOf(amount),ResultType.FAILED,account);
					Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
					return result;
				}
			}
		}
	}

	@Override
	public TransactionResult withdraw(Currency currency, BigDecimal amount,
			Cause cause, Set<Context> contexts) {
		if(businessManager.removeCurrency(id,amount.setScale(0, RoundingMode.HALF_UP).intValue())) {
			TransactionResult result = new LiteTransactionResult(this,amount,ResultType.SUCCESS,TransactionTypes.WITHDRAW);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		} else {
			TransactionResult result = new LiteTransactionResult(this,amount,ResultType.FAILED,TransactionTypes.WITHDRAW);
			Sponge.getGame().getEventManager().post(new LiteEconomyTransactionEvent(result));
			return result;
		}
	}

	@Override
	public Set<Context> getActiveContexts() {
		return new HashSet<Context>();
	}

	@Override
	public String getIdentifier() {
		return this.id;
	}

}
