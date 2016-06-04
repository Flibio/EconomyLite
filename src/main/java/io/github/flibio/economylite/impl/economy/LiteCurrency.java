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
package io.github.flibio.economylite.impl.economy;

import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class LiteCurrency implements Currency {

    private String singular;
    private String plural;
    private String symbol;
    private boolean defaultCur;
    private int digits;

    public LiteCurrency(String singular, String plural, String symbol, boolean defaultCur, int digits) {
        this.singular = singular;
        this.plural = plural;
        this.symbol = symbol;
        this.defaultCur = defaultCur;
        this.digits = digits;
    }

    @Override
    public int getDefaultFractionDigits() {
        return digits;
    }

    @Override
    public Text getDisplayName() {
        return Text.of(singular);
    }

    @Override
    public Text getPluralDisplayName() {
        return Text.of(plural);
    }

    @Override
    public Text getSymbol() {
        return Text.of(symbol);
    }

    @Override
    public boolean isDefault() {
        return defaultCur;
    }

    @Override
    public Text format(BigDecimal amount, int digits) {
        Text label = getPluralDisplayName();
        if (amount.equals(BigDecimal.ONE)) {
            label = getDisplayName();
        }
        return Text.of(String.format(Locale.ENGLISH, "%,.2f", amount.setScale(digits, RoundingMode.HALF_UP)) + " ", label);
    }

    @Override
    public String getId() {
        return "economylite:" + singular.toLowerCase().replaceAll(" ", "_");
    }

    @Override
    public String getName() {
        return singular;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Currency))
            return false;
        return this.getId().equals(((Currency) other).getId());
    }

}
