/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl.economy.registry;

import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.service.economy.Currency;

import io.github.flibio.economylite.EconomyLite;

import java.util.Collection;
import java.util.Optional;

public class CurrencyRegistryModule implements CatalogRegistryModule<Currency> {

    @Override
    public Optional<Currency> getById(String id) {
        for (Currency cur : EconomyLite.getCurrencyService().getCurrencies()) {
            if (cur.getId().equals(id)) {
                return Optional.of(cur);
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<Currency> getAll() {
        return EconomyLite.getCurrencyService().getCurrencies();
    }
}
