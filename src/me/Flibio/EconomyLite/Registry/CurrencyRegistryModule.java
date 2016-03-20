package me.Flibio.EconomyLite.Registry;

import me.Flibio.EconomyLite.EconomyLite;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.service.economy.Currency;

import java.util.*;

public class CurrencyRegistryModule implements CatalogRegistryModule<Currency> {

    @Override
    public Optional<Currency> getById(String id) {
        Currency currency = EconomyLite.getCurrency();
        if (currency.getId().equals(id))
            return Optional.of(currency);
        return Optional.empty();
    }

    @Override
    public Collection<Currency> getAll() {
        return Collections.singleton(EconomyLite.getCurrency());
    }
}
