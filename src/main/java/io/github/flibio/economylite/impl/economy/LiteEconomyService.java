/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl.economy;

import io.github.flibio.economylite.CauseFactory;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.economylite.api.PlayerEconService;
import io.github.flibio.economylite.api.VirtualEconService;
import io.github.flibio.economylite.impl.economy.account.LiteUniqueAccount;
import io.github.flibio.economylite.impl.economy.account.LiteVirtualAccount;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LiteEconomyService implements EconomyService {

    private PlayerEconService playerService = EconomyLite.getPlayerService();
    private VirtualEconService virtualService = EconomyLite.getVirtualService();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    @Override
    public void registerContextCalculator(ContextCalculator<Account> arg0) {
        return;
    }

    @Override
    public Optional<UniqueAccount> getOrCreateAccount(UUID uuid) {
        if (playerService.accountExists(uuid, getDefaultCurrency(), CauseFactory.create("New account check"))) {
            // Return the account
            return Optional.of(new LiteUniqueAccount(uuid));
        } else {
            // Make a new account
            UniqueAccount account = new LiteUniqueAccount(uuid);
            if (playerService.setBalance(uuid, account.getDefaultBalance(getDefaultCurrency()), getDefaultCurrency(),
                    CauseFactory.create("Creating account"))) {
                return Optional.of(account);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Account> getOrCreateAccount(String id) {
        if (virtualService.accountExists(id, getDefaultCurrency(), CauseFactory.create("New account check"))) {
            // Return the account
            return Optional.of(new LiteVirtualAccount(id));
        } else {
            // Make a new account
            VirtualAccount account = new LiteVirtualAccount(id);
            if (virtualService.setBalance(id, account.getDefaultBalance(getDefaultCurrency()), getDefaultCurrency(),
                    CauseFactory.create("Creating account"))) {
                return Optional.of(account);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Set<Currency> getCurrencies() {
        return currencyService.getCurrencies();
    }

    @Override
    public Currency getDefaultCurrency() {
        return currencyService.getCurrentCurrency();
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return playerService.accountExists(uuid, CauseFactory.create("Checking account existance"));
    }

    @Override
    public boolean hasAccount(String identifier) {
        return virtualService.accountExists(identifier, CauseFactory.create("Checking account existance"));
    }

}
