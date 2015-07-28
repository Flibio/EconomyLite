package me.Flibio.EconomyLite.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import me.Flibio.EconomyLite.Main;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class TextUtils {
	
	public TextUtils() {
		
	}
	
	public Text basicText(String text, TextColor color) {
		return Texts.builder(text).color(color).build();
	}
	
	public Text basicText(String text) {
		return Texts.builder(text).build();
	}
	
	public Text playerBalanceText(int balance) {
		Text balanceText = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = Main.access.currencyPlural;
		if(balance==1) label = Main.access.currencySingular;

		balanceText = balanceText.builder().append(basicText("Your balance: ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText(balance+" ",TextColors.GREEN)).build();
		balanceText = balanceText.builder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
		
		return balanceText;
	}
	
	public Text businessBalanceText(String businessName, int balance) {
		Text balanceText = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = Main.access.currencyPlural;
		if(balance==1) label = Main.access.currencySingular;

		balanceText = balanceText.builder().append(basicText(businessName+" balance: ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText(balance+" ",TextColors.GREEN)).build();
		balanceText = balanceText.builder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
		
		return balanceText;
	}
	
	public Text successfulBalanceChangeText(String playerName, int balance) {
		Text balanceText = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = Main.access.currencyPlural;
		if(balance==1) label = Main.access.currencySingular;
		
		balanceText = balanceText.builder().append(basicText("Successfully set ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText(playerName+"'s",TextColors.GREEN)).build();
		balanceText = balanceText.builder().append(basicText(" balance to ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText(balance+" ",TextColors.GREEN)).build();
		balanceText = balanceText.builder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
		
		return balanceText;
	}
	
	public Text successfulBusinessRegister(String businessName) {
		Text balanceText = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		balanceText = balanceText.builder().append(basicText("Successfully registered ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText(businessName,TextColors.GREEN)).build();
		balanceText = balanceText.builder().append(basicText(" as a business!",TextColors.YELLOW)).build();
		
		return balanceText;
	}
	
	public Text updateAvailable(String version, String url) {
		Text text = Texts.builder("EconomyLite").color(TextColors.GREEN).build();
		text = text.builder().append(basicText(" v"+version,TextColors.LIGHT_PURPLE)).build();
		text = text.builder().append(basicText(" is now available to download! ",TextColors.YELLOW)).build();
		try {
			text = text.builder().append(Texts.builder(url).color(TextColors.GRAY).onClick(TextActions.openUrl(new URL(url))).build()).build();
		} catch (MalformedURLException e) {}
		
		return text;
	}
	
	public Text editingBalance(String player) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.builder().append(basicText("Changing ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(player+"'s ",TextColors.GREEN)).build();
		text = text.builder().append(basicText(Main.access.currencySingular,TextColors.DARK_GREEN)).build();
		text = text.builder().append(basicText(" balance!",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text payOptionPlayer(String playerName, int amount) {
		Text text = Texts.builder("[").color(TextColors.GRAY).build();
		
		text = text.builder().append(basicText("Pay ", TextColors.YELLOW)).build();
		text = text.builder().append(basicText("PLAYER ", TextColors.LIGHT_PURPLE)).build();
		text = text.builder().append(basicText(playerName, TextColors.YELLOW)).build();
		text = text.builder().append(basicText("]", TextColors.GRAY)).build();
		
		Text hover = basicText("Pay the player ",TextColors.YELLOW);
		hover = hover.builder().append(basicText(playerName, TextColors.GREEN)).build();
		
		text = text.builder().onHover(TextActions.showText(hover)).build();
		text = text.builder().onClick(TextActions.runCommand("/paySpecified player "+amount+" "+playerName)).build();
		
		return text;
	}
	
	public Text payOptionBusiness(String businessName, int amount) {
		Text text = Texts.builder("[").color(TextColors.GRAY).build();
		
		text = text.builder().append(basicText("Pay ", TextColors.YELLOW)).build();
		text = text.builder().append(basicText("BUSINESS ", TextColors.LIGHT_PURPLE)).build();
		text = text.builder().append(basicText(businessName, TextColors.YELLOW)).build();
		text = text.builder().append(basicText("]", TextColors.GRAY)).build();
		
		Text hover = basicText("Pay the business ",TextColors.YELLOW);
		hover = hover.builder().append(basicText(businessName, TextColors.GREEN)).build();
		
		text = text.builder().onHover(TextActions.showText(hover)).build();
		text = text.builder().onClick(TextActions.runCommand("/paySpecified business "+amount+" "+businessName)).build();
		
		return text;
	}
	
	public Text payOption(String who) {
		Text balanceText = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		balanceText = balanceText.builder().append(basicText(who,TextColors.GREEN)).build();
		balanceText = balanceText.builder().append(basicText(" is both a ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText("player",TextColors.LIGHT_PURPLE)).build();
		balanceText = balanceText.builder().append(basicText(" and a ",TextColors.YELLOW)).build();
		balanceText = balanceText.builder().append(basicText("business!",TextColors.LIGHT_PURPLE)).build();
		balanceText = balanceText.builder().append(basicText(" Please select which one you would like to pay: ",TextColors.YELLOW)).build();
		
		return balanceText;
	}
	
	public Text paySuccess(String who, int amount) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = Main.access.currencyPlural;
		if(amount==1) label = Main.access.currencySingular;
		
		text = text.builder().append(basicText("Successfully payed ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(amount+" ",TextColors.GREEN)).build();
		text = text.builder().append(basicText(label,TextColors.DARK_GREEN)).build();
		text = text.builder().append(basicText(" to ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(who+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text leaveSuccess(String business) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.builder().append(basicText("Successfully left ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(business+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text leaveOnlyOwner(String business) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.builder().append(basicText("Since you are the only owner of ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(business,TextColors.GREEN)).build();
		text = text.builder().append(basicText(", it will be ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText("deleted!",TextColors.RED)).build();
		
		return text;
	}
	
	public Text clickToContinue(String command) {
		Text text = basicText("[",TextColors.GRAY);
		text = text.builder().append(basicText("CLICK TO CONTINUE",TextColors.GREEN)).build();
		text = text.builder().append(basicText("]",TextColors.GRAY)).build();
		
		Text hover = basicText("Click me",TextColors.GREEN);
		hover = hover.builder().append(basicText(" to continue!", TextColors.YELLOW)).build();
		
		text = text.builder().onHover(TextActions.showText(hover)).build();
		text = text.builder().onClick(TextActions.runCommand(command)).build();
		
		return text;
	}
	
	public Text deleteSuccess(String what) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.builder().append(basicText("Sucessfully ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText("deleted ",TextColors.RED)).build();
		text = text.builder().append(basicText(what+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text invited(String what) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.builder().append(basicText("You have been invited to join ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(what+"!",TextColors.GREEN)).build();
		text = text.builder().append(basicText(" Click continue to accept!",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text inviteAccept(String what) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.builder().append(basicText("You are now an owner of ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(what+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text successfulInvite(String what, String who) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.builder().append(basicText("Successfully invited ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(who,TextColors.GREEN)).build();
		text = text.builder().append(basicText(" to join ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(what+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text aboutToDelete(String what) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.builder().append(basicText(what,TextColors.GREEN)).build();
		text = text.builder().append(basicText(" is about to be ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText("deleted. ",TextColors.RED)).build();
		text = text.builder().append(basicText("Are you sure you wish to continue?",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text transferSuccess(String businessName, int amount) {
		Text text = Texts.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = Main.access.currencyPlural;
		if(amount==1) label = Main.access.currencySingular;
		
		text = text.builder().append(basicText("Successfully transfered ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(amount+" ",TextColors.GREEN)).build();
		text = text.builder().append(basicText(label,TextColors.DARK_GREEN)).build();
		text = text.builder().append(basicText(" from ",TextColors.YELLOW)).build();
		text = text.builder().append(basicText(businessName,TextColors.GREEN)).build();
		text = text.builder().append(basicText(" into your account!",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public String getDownloadUrl(String jsonRelease) {
		return jsonRelease.split("browser_download_url")[1].split("}",2)[0].replaceAll("\"", "").replaceFirst(":", "").trim();
	}
	
	public Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}
}
