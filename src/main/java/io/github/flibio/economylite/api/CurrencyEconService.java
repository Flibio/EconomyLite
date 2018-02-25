/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2018 Flibio
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
