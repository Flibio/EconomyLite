/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2017 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
