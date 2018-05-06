/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl;

import io.github.flibio.economylite.CauseFactory;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import org.spongepowered.api.service.economy.Currency;

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
        EconomyLite.getCurrencyManager().forceValue(currency.getId().replaceAll("economylite:", ""), "current");
        EconomyLite.getCurrencyManager().save();
    }

    @Override
    public Currency getCurrentCurrency() {
        return currentCurrency;
    }

    @Override
    public void deleteCurrency(Currency currency) {
        if (currency != defaultCurrency) {
            EconomyLite.getPlayerService().clearCurrency(currency, CauseFactory.create("Currency deletion"));
            EconomyLite.getVirtualService().clearCurrency(currency, CauseFactory.create("Currency deletion"));
            currencies.remove(currency);
            EconomyLite.getCurrencyManager().forceValue(null, currency.getId().replaceAll("economylite:", ""));
            EconomyLite.getCurrencyManager().save();
        }
    }
}
