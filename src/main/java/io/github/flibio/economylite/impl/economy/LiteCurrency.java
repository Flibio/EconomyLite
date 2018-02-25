/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
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
