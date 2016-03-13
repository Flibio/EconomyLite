package me.Flibio.EconomyLite.Events;

import me.Flibio.EconomyLite.EconomyLite;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

public class LiteEconomyTransactionEvent implements EconomyTransactionEvent {
	
	private TransactionResult result;
	
	public LiteEconomyTransactionEvent(TransactionResult result) {
		this.result = result;
	}

	@Override
	public Cause getCause() {
		return Cause.of(NamedCause.owner(EconomyLite.access));
	}

	@Override
	public TransactionResult getTransactionResult() {
		return this.result;
	}

}
