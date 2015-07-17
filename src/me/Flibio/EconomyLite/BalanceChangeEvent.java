package me.Flibio.EconomyLite;

import org.spongepowered.api.event.AbstractEvent;
import org.spongepowered.api.event.Cancellable;

public class BalanceChangeEvent extends AbstractEvent implements Cancellable {
	
	private boolean cancelled = false;
	private String player;
	
	public BalanceChangeEvent(String player){
		this.player = player;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public String getPlayerName(){
		return player;
	}

}
