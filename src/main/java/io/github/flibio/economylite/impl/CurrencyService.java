/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
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
package io.github.flibio.economylite.impl;

import org.spongepowered.api.service.economy.Currency;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;

import java.util.HashSet;
import java.util.Set;

public class CurrencyService implements CurrencyEconService {

    private HashSet<Currency> currencies = new HashSet<Currency>();
    private Currency defaultCurrency;
    private Currency currentCurrency;

    public CurrencyService(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
        this.currentCurrency = defaultCurrency;
    }

    @Override
    public void addCurrency(Currency currency) {
        currencies.add(currency);
    }

    @Override
    public Set<Currency> getCurrencies() {
        HashSet<Currency> set = currencies;
        set.add(defaultCurrency);
        return set;
    }

    @Override
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public void setCurrentCurrency(Currency currency) {
        this.currentCurrency = currency;
        EconomyLite.getConfigManager().setValue("currencies.conf", "current", String.class, currency.getId().replaceAll("economylite:", ""));
    }

    @Override
    public Currency getCurrentCurrency() {
        return currentCurrency;
    }

    @Override
    public void deleteCurrency(Currency currency) {
        if (currency != defaultCurrency) {
            EconomyLite.getPlayerService().clearCurrency(currency);
            EconomyLite.getVirtualService().clearCurrency(currency);
            currencies.remove(currency);
            EconomyLite.getConfigManager().deleteValue("currencies.conf", currency.getId().replaceAll("economylite:", ""));
        }
    }
}
