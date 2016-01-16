package me.Flibio.EconomyLite.Events;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

public class LiteEconomyTransactionEvent implements EconomyTransactionEvent {
	
	private TransactionResult result;
	
	public LiteEconomyTransactionEvent(TransactionResult result) {
		this.result = result;
	}

	@Override
	public Cause getCause() {
		return Cause.of("EconomyLite");
	}

	@Override
	public TransactionResult getTransactionResult() {
		return this.result;
	}

}
