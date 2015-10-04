package me.Flibio.EconomyLite.Commands;

import me.Flibio.EconomyLite.Main;
import me.Flibio.EconomyLite.Utils.PlayerManager;
import me.Flibio.EconomyLite.Utils.TextUtils;

import org.spongepowered.api.service.scheduler.TaskBuilder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.Optional;

public class AddCommand implements CommandExecutor {

	private TextUtils textUtils = new TextUtils();
	private PlayerManager playerManager = new PlayerManager();
	private TaskBuilder taskBuilder = Main.access.game.getScheduler().createTaskBuilder();

	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		//Run on a seperate thread
		taskBuilder.execute(new Runnable(){
			public void run() {
				//Retrieve arguments
				Optional<String> playerNameOptional = args.<String>getOne("player");
				Optional<Integer> amountOptional = args.<Integer>getOne("amount");
				if(playerNameOptional.isPresent()&&amountOptional.isPresent()) {
					//Set the variables
					String playerName = playerNameOptional.get();
					int amount = amountOptional.get();
					//Run the thread
					source.sendMessage(textUtils.editingBalance(playerName));
					//Get the players UUID
					String uuid = playerManager.getUUID(playerName);
					if(!uuid.isEmpty()) {
						//Set amount
						int newAmount = amount + playerManager.getBalance(uuid);
						//Check if the amount is in-between of the parameters
						if(newAmount<0||newAmount>1000000) {
							//New balance is to big or small
							source.sendMessage(textUtils.basicText("The new balance must be in-between 0 and 1,000,000 "+Main.access.currencyPlural+"!", TextColors.RED));
							return;
						}
						//Set the player's balance
						if(playerManager.setBalance(uuid, newAmount)) {
							//Successful
							source.sendMessage(textUtils.successfulBalanceChangeText(playerName, newAmount));
							return;
						} else {
							//Send error message
							source.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
							return;
						}
					} else {
						//UUID is empty
						source.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
						return;
					}
				} else {
					//Send error message
					source.sendMessage(textUtils.basicText("An internal error has occured!", TextColors.RED));
					return;
				}
			}
		}).async().submit(Main.access);
		return CommandResult.success();
	}

}
