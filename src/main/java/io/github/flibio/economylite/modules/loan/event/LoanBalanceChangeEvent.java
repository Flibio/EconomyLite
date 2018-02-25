/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.loan.event;

import io.github.flibio.economylite.EconomyLite;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.UUID;

public class LoanBalanceChangeEvent extends AbstractEvent {

    private UUID user;
    private Double balance;

    public LoanBalanceChangeEvent(double newBalance, UUID user) {
        this.user = user;
        this.balance = newBalance;
    }

    @Override
    public Cause getCause() {
        return Cause.of(EventContext.empty(), EconomyLite.getInstance().getPluginContainer());
    }

    public UUID getUser() {
        return user;
    }

    public Double getNewBalance() {
        return balance;
    }

}
