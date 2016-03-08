package me.Flibio.EconomyLite.API;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Events.LiteEconomyTransactionEvent;
import me.Flibio.EconomyLite.Utils.PlayerManager;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
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
import java.util.UUID;
//Change whole deal to go through the EconomyService, add event to VirtualAccount
public class LiteUniqueAccount implements UniqueAccount {
	
	private UUID uuid;
	private PlayerManager playerManager;
	private LiteCurrency liteCurrency;
	
	public LiteUniqueAccount(UUID uuid) {
		this.uuid = uuid;
		this.playerManager = new PlayerManager();
		this.liteCurrency = (LiteCurrency) EconomyLite.getCurrency();
	}

	@Override
	public TransactionResult deposit(Currency currency, BigDecimal amount,
			Cause cause, Set<Context> contexts) {
		if(playerManager.addCurrency(uuid.toString(),amount.setScale(0, RoundingMode.HALF_UP).intValue())) {
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
	public BigDecimal getBalance(Currency currency, Set<Context> context) {
		return BigDecimal.valueOf(playerManager.getBalance(uuid.toString()));	
	}

	@Override
	public Map<Currency, BigDecimal> getBalances(Set<Context> arg0) {
		int currentBal = playerManager.getBalance(uuid.toString());
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
		return Text.of((new PlayerManager()).getName(uuid.toString()));
	}

	@Override
	public boolean hasBalance(Currency currency, Set<Context> contexts) {
		return playerManager.playerExists(uuid.toString());
	}

	@Override
	public TransactionResult resetBalance(Currency currency, Cause cause,
			Set<Context> contexts) {
		return setBalance(currency,BigDecimal.ZERO,cause,contexts);
	}

	@Override
	public TransactionResult resetBalances(Cause cause, Set<Context> contexts) {
		return resetBalance(liteCurrency,cause,contexts);
	}

	@Override
	public TransactionResult setBalance(Currency currency, BigDecimal amount,
			Cause cause, Set<Context> contexts) {
		if(playerManager.setBalance(uuid.toString(),amount.setScale(0, RoundingMode.HALF_UP).intValue())) {
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
		if(playerManager.removeCurrency(uuid.toString(),amount.setScale(0, RoundingMode.HALF_UP).intValue())) {
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
		return this.uuid.toString();
	}

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

}
