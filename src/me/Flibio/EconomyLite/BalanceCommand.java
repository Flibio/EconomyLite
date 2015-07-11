package me.Flibio.EconomyLite;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import com.google.common.base.Optional;

public class BalanceCommand implements CommandCallable {
	
	private DataEditor dataEditor;
	private String plural;
	private String singular;
	
	public BalanceCommand(Logger log){
		dataEditor = new DataEditor(log);
		
		plural = Main.getCurrencyPlural();
		singular = Main.getCurrencySingular();
	}

	@Override
	public Optional<Text> getHelp(CommandSource source) {
		return Optional.of(Texts.builder("Usage: /balance").build());
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource source) {
		return Optional.of(Texts.builder("Check your EconomyLite balance").build());
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String args)
			throws CommandException {
		return Arrays.asList("/balance");
	}

	@Override
	public Text getUsage(CommandSource source) {
		return Texts.builder("/balance").build();
	}

	@Override
	public Optional<CommandResult> process(CommandSource source, String arg_string)
			throws CommandException {
		if(!(source instanceof Player)){
			source.sendMessage(Texts.builder("Error: You must a player to use /balance").color(TextColors.RED).build());
			return Optional.of(CommandResult.builder().successCount(0).build());
		}
		
		Player player = (Player) source;

		String name = player.getName();
		
		String[] args = arg_string.split(" ");
		
		if(!args[0].isEmpty()){
			player.sendMessage(Texts.builder("Usage: /balance").color(TextColors.RED).build());
			return Optional.of(CommandResult.builder().successCount(0).build());
		}
		
		if(!dataEditor.playerExists(name)){
			player.sendMessage(Texts.builder("Error: Player not found").color(TextColors.RED).build());
			return Optional.of(CommandResult.builder().successCount(0).build());
		}
		
		int playerBalance = dataEditor.getBalance(name);
		
		String currencyName = plural;
		if(playerBalance == 1){
			currencyName = singular;
		}
		
		player.sendMessage(Texts.builder("Your").color(TextColors.YELLOW).append(
				Texts.builder(" EconomyLite ").color(TextColors.GREEN).build()
				).append(
						Texts.builder("balance: ").color(TextColors.YELLOW).build()
						).append(
								Texts.builder(""+playerBalance).color(TextColors.DARK_GREEN).build()
								).append(
										Texts.builder(" "+currencyName).color(TextColors.YELLOW).build()
										)
								
										.build());
		
		return Optional.of(CommandResult.success());
	}

	@Override
	public boolean testPermission(CommandSource source) {
		return true;
	}

}
