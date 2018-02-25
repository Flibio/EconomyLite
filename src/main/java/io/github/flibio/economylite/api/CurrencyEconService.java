/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.api;

import org.spongepowered.api.service.economy.Currency;

import java.util.Set;

public interface CurrencyEconService {

    /**
     * Gets all currencies.
     * 
     * @return A set of all currencies.
     */
    public Set<Currency> getCurrencies();

    /**
     * Adds a currency to the currencies.
     * 
     * @param currency The currency to add.
     */
    public void addCurrency(Currency currency);

    /**
     * Gets the default currency.
     * 
     * @return The default currency.
     */
    public Currency getDefaultCurrency();

    /**
     * Sets the currency in use by the server.
     * 
     * @param currency The currency in use.
     */
    public void setCurrentCurrency(Currency currency);

    /**
     * Deletes a currency.
     * 
     * @param currency The currency to delete.
     */
    public void deleteCurrency(Currency currency);

    /**
     * Gets the currency in use by the server.
     * 
     * @return The currency in use.
     */
    public Currency getCurrentCurrency();

}
