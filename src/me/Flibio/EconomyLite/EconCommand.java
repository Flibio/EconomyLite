package me.Flibio.EconomyLite;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import com.google.common.base.Optional;

public class EconCommand implements CommandCallable {
	
	private DataEditor dataEditor;
	private String plural;
	private String singular;
	
	public EconCommand(Logger log){
		dataEditor = new DataEditor(log);
		
		plural = Main.getCurrencyPlural();
		singular = Main.getCurrencySingular();
	}

	@Override
	public Optional<Text> getHelp(CommandSource source) {
		return Optional.of(Texts.builder("Usage: /econ add|remove|set <amount of "+plural+"> <player>").build());
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource source) {
		return Optional.of(Texts.builder("Admin EconomyLite command").build());
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String args)
			throws CommandException {
		return Arrays.asList("/econ add|remove|set <amount of "+plural+"> <player>");
	}

	@Override
	public Text getUsage(CommandSource source) {
		return Texts.builder("/econ add|remove|set <amount of "+plural+"> <player>").build();
	}

	@Override
	public Optional<CommandResult> process(CommandSource source, String arg_string)
			throws CommandException {
		//Check if source has permission
		if(!source.hasPermission("econ.admin")) {
			source.sendMessage(Texts.builder("You do not have permission to run this command!").color(TextColors.RED).build());
			return Optional.of(CommandResult.builder().successCount(0).build());
		}
		
		String[] args = arg_string.split(" ");
		
		if(args.length<3){
			source.sendMessage(Texts.builder("Usage: /econ add|remove|set <amount of "+plural+"> <player>").color(TextColors.RED).build());
			return Optional.of(CommandResult.builder().successCount(0).build());
		}
		
		if(args[0].equalsIgnoreCase("add")){
			
			String name = args[2];
			int currentBalance = dataEditor.getBalance(name);
			
			//Check if player exists
			if(!dataEditor.playerExists(name)){
				source.sendMessage(Texts.builder("Error: Player not found").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			//Parse the amount
			int amnt;
			try{
				amnt = Integer.parseInt(args[1]);
			} catch(NumberFormatException e){
				source.sendMessage(Texts.builder("Error: Invalid amount").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			//Amount checks
			if(currentBalance+amnt<0||currentBalance+amnt>1000000||amnt<0){
				source.sendMessage(Texts.builder("Error: New balance must be greater than 0 and less than 1,000,000").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			if(dataEditor.addCurrency(name, amnt)){
				String currencyName = plural;
				if(amnt == 1){
					currencyName = singular;
				}
				source.sendMessage(Texts.builder("Successfully added "+amnt+" "+currencyName+" to "+name+"'s balance!").color(TextColors.GREEN).build());
				return Optional.of(CommandResult.success());
			} else {
				source.sendMessage(Texts.builder("Error: Internal plugin error occured while changing the balance").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
		} else if(args[0].equalsIgnoreCase("remove")){
			String name = args[2];
			int currentBalance = dataEditor.getBalance(name);
			
			//Check if player exists
			if(!dataEditor.playerExists(name)){
				source.sendMessage(Texts.builder("Error: Player not found").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			//Parse the amount
			int amnt;
			try{
				amnt = Integer.parseInt(args[1]);
			} catch(NumberFormatException e){
				source.sendMessage(Texts.builder("Error: Invalid amount").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			//Amount checks
			if(currentBalance-amnt<0||currentBalance-amnt>1000000||amnt<0){
				source.sendMessage(Texts.builder("Error: New balance must be greater than 0 and less than 1,000,000").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			if(dataEditor.removeCurrency(name, amnt)){
				String currencyName = plural;
				if(amnt == 1){
					currencyName = singular;
				}
				source.sendMessage(Texts.builder("Successfully removed "+amnt+" "+currencyName+" from "+name+"!").color(TextColors.GREEN).build());
				return Optional.of(CommandResult.success());
			} else {
				source.sendMessage(Texts.builder("Error: Internal plugin error occured while changing the balance").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
		} else if(args[0].equalsIgnoreCase("set")){
			String name = args[2];
			
			//Check if player exists
			if(!dataEditor.playerExists(name)){
				source.sendMessage(Texts.builder("Error: Player not found").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			//Parse the amount
			int amnt;
			try{
				amnt = Integer.parseInt(args[1]);
			} catch(NumberFormatException e){
				source.sendMessage(Texts.builder("Error: Invalid amount").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			//Amount checks
			if(amnt<0||amnt>1000000){
				source.sendMessage(Texts.builder("Error: Balance must be greater than 0 and less than 1,000,000").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
			if(dataEditor.setBalance(name, amnt)){
				source.sendMessage(Texts.builder("Successfully set the balance of "+name+" to "+amnt+"!").color(TextColors.GREEN).build());
				return Optional.of(CommandResult.success());
			} else {
				source.sendMessage(Texts.builder("Error: Internal plugin error occured while changing the balance").color(TextColors.RED).build());
				return Optional.of(CommandResult.builder().successCount(0).build());
			}
			
		} else {
			//Invalid first argument
			source.sendMessage(Texts.builder("Usage: /econ add|remove|set <amount of "+plural+"> <player>").color(TextColors.RED).build());
			return Optional.of(CommandResult.builder().successCount(0).build());
		}
	}

	@Override
	public boolean testPermission(CommandSource source) {
		return source.hasPermission("econ.admin");
	}

}
