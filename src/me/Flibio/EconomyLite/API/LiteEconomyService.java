package me.Flibio.EconomyLite.API;

import me.Flibio.EconomyLite.EconomyLite;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.PlayerManager;

import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LiteEconomyService implements EconomyService {
	
	private PlayerManager playerManager = new PlayerManager();
	private BusinessManager businessManager = new BusinessManager();

	@Override
	public void registerContextCalculator(ContextCalculator<Account> arg0) {
		return;
	}

	@Override
	public Optional<UniqueAccount> createAccount(UUID uuid) {
		if(playerManager.playerExists(uuid.toString())) {
			return Optional.of(new LiteUniqueAccount(uuid));
		} else {
			if(playerManager.registerPlayer(uuid.toString(),0)) {
				return Optional.of(new LiteUniqueAccount(uuid));
			} else {
				return Optional.empty();
			}
		}
	}

	@Override
	public Optional<VirtualAccount> createVirtualAccount(String id) {
		if(businessManager.businessExists(id)) {
			return Optional.of(new LiteVirtualAccount(id));
		} else {
			if(businessManager.createBusiness(id)) {
				return Optional.of(new LiteVirtualAccount(id));
			} else {
				return Optional.empty();
			}
		}
	}

	@Override
	public Optional<UniqueAccount> getAccount(UUID uuid) {
		if(playerManager.playerExists(uuid.toString())) {
			return Optional.of(new LiteUniqueAccount(uuid));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Account> getAccount(String id) {
		if(businessManager.businessExists(id)) {
			return Optional.of(new LiteVirtualAccount(id));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Set<Currency> getCurrencies() {
		HashSet<Currency> set = new HashSet<Currency>();
		set.add(new LiteCurrency());
		return set;
	}

	@Override
	public Currency getDefaultCurrency() {
		return EconomyLite.getCurrency();
	}

}
