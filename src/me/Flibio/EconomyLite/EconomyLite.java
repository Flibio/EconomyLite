package me.Flibio.EconomyLite;

import me.Flibio.EconomyLite.API.EconomyLiteAPI;
import me.Flibio.EconomyLite.API.LiteCurrency;
import me.Flibio.EconomyLite.API.LiteEconomyService;
import me.Flibio.EconomyLite.Commands.AddCommand;
import me.Flibio.EconomyLite.Commands.BalanceCommand;
import me.Flibio.EconomyLite.Commands.BusinessDeleteCommand;
import me.Flibio.EconomyLite.Commands.BusinessInviteAcceptCommand;
import me.Flibio.EconomyLite.Commands.BusinessInviteCommand;
import me.Flibio.EconomyLite.Commands.BusinessLeaveCommand;
import me.Flibio.EconomyLite.Commands.BusinessOwnersCommand;
import me.Flibio.EconomyLite.Commands.BusinessRegisterCommand;
import me.Flibio.EconomyLite.Commands.BusinessTransferCommand;
import me.Flibio.EconomyLite.Commands.PayCommand;
import me.Flibio.EconomyLite.Commands.PayOverrideCommand;
import me.Flibio.EconomyLite.Commands.PlayerBalanceCommand;
import me.Flibio.EconomyLite.Commands.RemoveCommand;
import me.Flibio.EconomyLite.Commands.SetCommand;
import me.Flibio.EconomyLite.Listeners.BalanceChangeListener;
import me.Flibio.EconomyLite.Listeners.PlayerJoinListener;
import me.Flibio.EconomyLite.Utils.BusinessManager;
import me.Flibio.EconomyLite.Utils.FileManager;
import me.Flibio.EconomyLite.Utils.FileManager.FileType;
import me.Flibio.EconomyLite.Utils.MySQLManager;
import me.flibio.updatifier.Updatifier;
import net.minecrell.mcstats.SpongeStatsLite;
import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Optional;

@Updatifier(repoName = "EconomyLite", repoOwner = "Flibio", version = "v1.1.7")
@Plugin(id = "EconomyLite", name = "EconomyLite", version = "1.1.7")
public class EconomyLite {
	
	@Inject
	public Logger logger;
	
	@Inject
	public Game game;
	
	@Inject
    private SpongeStatsLite statsLite;
	private FileManager fileManager;
	private BusinessManager businessManager;
	private static EconomyService economyService;
	private static Currency currency;
	
	public static EconomyLite access;
	
	public String currencySingular = "";
	public String currencyPlural = "";
	
	public boolean sqlEnabled = false;
	
	private static MySQLManager mySQL = null;
	
	public String version = EconomyLite.class.getAnnotation(Plugin.class).version();
	
	private static HashMap<String, String> configOptions = new HashMap<String, String>();
	
	@Listener
	public void onServerInitialize(GameInitializationEvent event) {
		logger.info("EconomyLite v"+version+" by Flibio initializing!");
		this.statsLite.start();
		//Set the access
		access = this;

		fileManager = new FileManager();
		businessManager = new BusinessManager();
		
		//Create files and load config options
		initializeFiles();
		loadConfigurationOptions();
		//Attempt to load MySQL if it is enabled
		Optional<SqlService> sqlServiceOptional = game.getServiceManager().provide(SqlService.class);
		if(optionEnabled("mysql.enabled")&&sqlServiceOptional.isPresent()) {
			//Enable MySQL
			sqlEnabled = true;
			//Connect to the database
			logger.info("Enabling MySQL...");
			mySQL = new MySQLManager(getOption("mysql.hostname"), getOption("mysql.port"), getOption("mysql.database"), 
					getOption("mysql.username"), getOption("mysql.password"), sqlServiceOptional.get());
		}
		//Register events and commands
		registerEvents();
		registerCommands();
		//Register EconomyLiteAPI
		game.getServiceManager().setProvider(this, EconomyLiteAPI.class, new EconomyLiteAPI());
		economyService = new LiteEconomyService();
		game.getServiceManager().setProvider(this, EconomyService.class, economyService);
		logger.info("API registered successfully!");
		//Reset business confirmations
		game.getScheduler().createTaskBuilder().execute(new Runnable() {
			public void run() {
				fileManager.loadFile(FileType.BUSINESS_DATA);
				ConfigurationNode root = fileManager.getFile(FileType.BUSINESS_DATA);
				for(ConfigurationNode raw : root.getChildrenMap().values()) {
					businessManager.setConfirmationNeeded(raw.getKey().toString(), true);
				}
			}
		}).async().submit(this);
	}
	
	private void registerEvents() {
		game.getEventManager().registerListeners(this, new PlayerJoinListener());
		game.getEventManager().registerListeners(this, new BalanceChangeListener());
	}
	
	private void registerCommands() {
		CommandSpec balanceCommand = CommandSpec.builder()
			    .description(Text.of("View EconomyLite balance"))
			    .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("business"))))
			    .executor(new BalanceCommand())
			    .build();
		game.getCommandManager().register(this, balanceCommand, "balance", "bal");
		CommandSpec pBalanceCommand = CommandSpec.builder()
                .description(Text.of("View EconomyLite balance of another player"))
                .arguments(GenericArguments.string(Text.of("player")))
                .executor(new PlayerBalanceCommand())
                .build();
		game.getCommandManager().register(this, pBalanceCommand, "playerbalance", "pbal");
		//Add Child Command
		CommandSpec addCommand = CommandSpec.builder()
			    .description(Text.of("Add currency to a player's balance"))
			    .arguments(GenericArguments.integer(Text.of("amount")), GenericArguments.string(Text.of("player")))
			    .executor(new AddCommand())
			    .build();
		//Remove Child Command
		CommandSpec removeCommand = CommandSpec.builder()
			    .description(Text.of("Remove currency from a player's balance"))
			    .arguments(GenericArguments.integer(Text.of("amount")), GenericArguments.string(Text.of("player")))
			    .executor(new RemoveCommand())
			    .build();
		//Set Child Command
		CommandSpec setCommand = CommandSpec.builder()
			    .description(Text.of("Set a player's balance"))
			    .arguments(GenericArguments.string(Text.of("player")), GenericArguments.integer(Text.of("amount")))
			    .executor(new SetCommand())
			    .build();
		//Main Econ Command
		CommandSpec econCommand = CommandSpec.builder()
			    .description(Text.of("Edit player balances"))
			    .permission("econ.admin")
			    .child(addCommand, "add")
			    .child(removeCommand, "remove")
			    .child(setCommand, "set")
			    .build();
		game.getCommandManager().register(this, econCommand, "econ");
		//Business Commands
		if(optionEnabled("businesses")) {
			//Register Child
			CommandSpec businessRegisterCommand = CommandSpec.builder()
				    .description(Text.of("Register a new business"))
				    .permission("econ.busines.register")
				    .arguments(GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessRegisterCommand())
				    .build();
			//Delete Child
			CommandSpec businessDeleteCommand = CommandSpec.builder()
				    .description(Text.of("Delete a business"))
				    .permission("econ.busines.delete")
				    .arguments(GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessDeleteCommand())
				    .build();
			//Leave Child
			CommandSpec businessLeaveCommand = CommandSpec.builder()
				    .description(Text.of("Leave a business as an owner"))
				    .permission("econ.busines.leave")
				    .arguments(GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessLeaveCommand())
				    .build();
			//Invite Child
			CommandSpec businessInviteCommand = CommandSpec.builder()
				    .description(Text.of("Invite an owner to a business"))
				    .permission("econ.busines.invite")
				    .arguments(GenericArguments.string(Text.of("player")),GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessInviteCommand())
				    .build();
			//Invite Accept Child
			CommandSpec businessInviteAcceptCommand = CommandSpec.builder()
				    .description(Text.of("Accept an invite to a business"))
				    .permission("econ.busines.invite")
				    .arguments(GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessInviteAcceptCommand())
				    .build();
			//Transfer Child
			CommandSpec businessTransferCommand = CommandSpec.builder()
				    .description(Text.of("Transfer some of the business funds to your account"))
				    .permission("econ.busines.transfer")
				    .arguments(GenericArguments.integer(Text.of("amount")),GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessTransferCommand())
				    .build();
			//Owners Child
			CommandSpec businessOwnersCommand = CommandSpec.builder()
				    .description(Text.of("View the owners of a business"))
				    .permission("econ.busines.owners")
				    .arguments(GenericArguments.remainingJoinedStrings(Text.of("business")))
				    .executor(new BusinessOwnersCommand())
				    .build();
			//Main Command
			CommandSpec businessCommand = CommandSpec.builder()
				    .description(Text.of("Business management commands"))
				    .permission("econ.business")
				    .child(businessRegisterCommand, "register", "reg")
				    .child(businessDeleteCommand, "delete", "del")
				    .child(businessLeaveCommand, "leave")
				    .child(businessInviteCommand, "invite", "inv")
				    .child(businessInviteAcceptCommand, "inviteAccept")
				    .child(businessTransferCommand, "transfer")
				    .child(businessOwnersCommand, "owners")
				    .build();
			game.getCommandManager().register(this, businessCommand, "business");
		}
		//Pay Commands
		CommandSpec payCommand = CommandSpec.builder()
			    .description(Text.of("Pay another player or business"))
			    .permission("econ.pay")
			    .arguments(GenericArguments.integer(Text.of("amount")), GenericArguments.remainingJoinedStrings(Text.of("who")))
			    .executor(new PayCommand())
			    .build();
		game.getCommandManager().register(this, payCommand, "pay");
		CommandSpec payOverrideCommand = CommandSpec.builder()
			    .description(Text.of("Pay another player or business"))
			    .permission("econ.pay")
			    .arguments(GenericArguments.string(Text.of("whoType")), GenericArguments.integer(Text.of("amount")), GenericArguments.remainingJoinedStrings(Text.of("who")))
			    .executor(new PayOverrideCommand())
			    .build();
		game.getCommandManager().register(this, payOverrideCommand, "paySpecified");
	}

	//Generates all files and sets default configuration using FileManager class
	private void initializeFiles() {
		fileManager.generateFolder("config/EconomyLite");
		fileManager.generateFile("config/EconomyLite/config.conf");
		fileManager.generateFile("config/EconomyLite/data.conf");
		fileManager.generateFile("config/EconomyLite/businesses.conf");
		
		fileManager.loadFile(FileType.CONFIGURATION);
		
		fileManager.testDefault("Currency-Singular", "Coin");
		fileManager.testDefault("Currency-Plural", "Coins");
		fileManager.testDefault("Scoreboard", "disabled");
		fileManager.testDefault("Businesses", "enabled");
		fileManager.testDefault("Default-Currency", 0);
		fileManager.testDefault("MySQL.Enabled", "disabled");
		fileManager.testDefault("MySQL.Hostname", "hostname");
		fileManager.testDefault("MySQL.Port", 3306);
		fileManager.testDefault("MySQL.Database", "database");
		fileManager.testDefault("MySQL.Username", "username");
		fileManager.testDefault("MySQL.Password", "password");
	}
	
	//Loads all of the config options from the configuration file
	private void loadConfigurationOptions() {
		fileManager.loadFile(FileType.CONFIGURATION);
		configOptions.put("currencySingular", fileManager.getConfigValue("Currency-Singular"));
		currencySingular = configOptions.get("currencySingular");
		configOptions.put("currencyPlural", fileManager.getConfigValue("Currency-Plural"));
		currencyPlural = configOptions.get("currencyPlural");
		configOptions.put("scoreboard", fileManager.getConfigValue("Scoreboard"));
		configOptions.put("businesses", fileManager.getConfigValue("Businesses"));
		configOptions.put("defaultCurrency", fileManager.getConfigValue("Default-Currency"));
		configOptions.put("mysql.enabled", fileManager.getConfigValue("MySQL.Enabled"));
		configOptions.put("mysql.hostname", fileManager.getConfigValue("MySQL.Hostname"));
		configOptions.put("mysql.port", fileManager.getConfigValue("MySQL.Port"));
		configOptions.put("mysql.database", fileManager.getConfigValue("MySQL.Database"));
		configOptions.put("mysql.username", fileManager.getConfigValue("MySQL.Username"));
		configOptions.put("mysql.password", fileManager.getConfigValue("MySQL.Password"));
	}
	
	public static boolean optionEnabled(String optionName) {
		if(configOptions.get(optionName).equalsIgnoreCase("enabled")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getOption(String optionName) {
		if(!configOptions.containsKey(optionName)) return "";
		return configOptions.get(optionName);
	}
	
	public static int getOptionInteger(String optionName) {
	    if(!configOptions.containsKey(optionName)) return 0;
	    try {
	        return Integer.parseInt(configOptions.get(optionName));
	    } catch(Exception e) {
	        return 0;
	    }
	}
	
	public static Currency getCurrency() {
		if(currency == null) {
			currency = new LiteCurrency();
		}
		return currency;
	}
	
	public static EconomyService getService() {
		if(economyService==null) {
			economyService = new LiteEconomyService();
			return economyService;
		} else {
			return economyService;
		}
	}
	
	public static MySQLManager getMySQL() {
		return mySQL;
	}
	
}
