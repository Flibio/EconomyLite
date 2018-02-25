/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl.economy.event;

import io.github.flibio.economylite.EconomyLite;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.util.UUID;

public class LiteEconomyTransactionEvent extends AbstractEvent implements EconomyTransactionEvent {

    private TransactionResult result;
    private Cause cause;

    public LiteEconomyTransactionEvent(TransactionResult result) {
        this.result = result;
    }

    public LiteEconomyTransactionEvent(TransactionResult result, Cause cause) {
        this.result = result;
        this.cause = cause;
    }

    public LiteEconomyTransactionEvent(TransactionResult result, UUID user, Cause cause) {
        this.result = result;
        this.cause = Cause.of(EventContext.empty(), user);
    }

    @Override
    public Cause getCause() {
        if (cause != null) {
            return cause;
        } else {
            return Cause.of(EventContext.empty(), EconomyLite.getInstance().getPluginContainer());
        }
    }

    @Override
    public TransactionResult getTransactionResult() {
        return this.result;
    }

}
