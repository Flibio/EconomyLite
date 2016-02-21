package me.Flibio.EconomyLite.Utils;

import me.Flibio.EconomyLite.EconomyLite;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;

public class TextUtils {
	
	public TextUtils() {
		
	}
	
	public Text basicText(String text, TextColor color) {
		return Text.builder(text).color(color).build();
	}
	
	public Text basicText(String text) {
		return Text.builder(text).build();
	}
	
	public Text playerBalanceText(int balance) {
		Text balanceText = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = EconomyLite.access.currencyPlural;
		if(balance==1) label = EconomyLite.access.currencySingular;

		balanceText = balanceText.toBuilder().append(basicText("Your balance: ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText(balance+" ",TextColors.GREEN)).build();
		balanceText = balanceText.toBuilder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
		
		return balanceText;
	}
	
	public Text playerBalanceText(int balance, String who) {
        Text balanceText = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
        String label = EconomyLite.access.currencyPlural;
        if(balance==1) label = EconomyLite.access.currencySingular;

        balanceText = balanceText.toBuilder().append(basicText(who+"'s balance: ",TextColors.YELLOW)).build();
        balanceText = balanceText.toBuilder().append(basicText(balance+" ",TextColors.GREEN)).build();
        balanceText = balanceText.toBuilder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
        
        return balanceText;
    }
	
	public Text businessBalanceText(String businessName, int balance) {
		Text balanceText = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = EconomyLite.access.currencyPlural;
		if(balance==1) label = EconomyLite.access.currencySingular;

		balanceText = balanceText.toBuilder().append(basicText(businessName+" balance: ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText(balance+" ",TextColors.GREEN)).build();
		balanceText = balanceText.toBuilder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
		
		return balanceText;
	}
	
	public Text successfulBalanceChangeText(String playerName, int balance) {
		Text balanceText = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = EconomyLite.access.currencyPlural;
		if(balance==1) label = EconomyLite.access.currencySingular;
		
		balanceText = balanceText.toBuilder().append(basicText("Successfully set ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText(playerName+"'s",TextColors.GREEN)).build();
		balanceText = balanceText.toBuilder().append(basicText(" balance to ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText(balance+" ",TextColors.GREEN)).build();
		balanceText = balanceText.toBuilder().append(basicText(label+"!",TextColors.DARK_GREEN)).build();
		
		return balanceText;
	}
	
	public Text successfulBusinessRegister(String businessName) {
		Text balanceText = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		balanceText = balanceText.toBuilder().append(basicText("Successfully registered ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText(businessName,TextColors.GREEN)).build();
		balanceText = balanceText.toBuilder().append(basicText(" as a business!",TextColors.YELLOW)).build();
		
		return balanceText;
	}
	
	public Text updateAvailable(String version, String url) {
		Text text = Text.builder("EconomyLite").color(TextColors.GREEN).build();
		text = text.toBuilder().append(basicText(" v"+version,TextColors.LIGHT_PURPLE)).build();
		text = text.toBuilder().append(basicText(" is now available to download! ",TextColors.YELLOW)).build();
		try {
			text = text.toBuilder().append(Text.builder(url).color(TextColors.GRAY).onClick(TextActions.openUrl(new URL(url))).build()).build();
		} catch (MalformedURLException e) {}
		
		return text;
	}
	
	public Text editingBalance(String player) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.toBuilder().append(basicText("Changing ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(player+"'s ",TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(EconomyLite.access.currencySingular,TextColors.DARK_GREEN)).build();
		text = text.toBuilder().append(basicText(" balance!",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text payOptionPlayer(String playerName, int amount) {
		Text text = Text.builder("[").color(TextColors.GRAY).build();
		
		text = text.toBuilder().append(basicText("Pay ", TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("PLAYER ", TextColors.LIGHT_PURPLE)).build();
		text = text.toBuilder().append(basicText(playerName, TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("]", TextColors.GRAY)).build();
		
		Text hover = basicText("Pay the player ",TextColors.YELLOW);
		hover = hover.toBuilder().append(basicText(playerName, TextColors.GREEN)).build();
		
		text = text.toBuilder().onHover(TextActions.showText(hover)).build();
		text = text.toBuilder().onClick(TextActions.runCommand("/paySpecified player "+amount+" "+playerName)).build();
		
		return text;
	}
	
	public Text payOptionBusiness(String businessName, int amount) {
		Text text = Text.builder("[").color(TextColors.GRAY).build();
		
		text = text.toBuilder().append(basicText("Pay ", TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("BUSINESS ", TextColors.LIGHT_PURPLE)).build();
		text = text.toBuilder().append(basicText(businessName, TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("]", TextColors.GRAY)).build();
		
		Text hover = basicText("Pay the business ",TextColors.YELLOW);
		hover = hover.toBuilder().append(basicText(businessName, TextColors.GREEN)).build();
		
		text = text.toBuilder().onHover(TextActions.showText(hover)).build();
		text = text.toBuilder().onClick(TextActions.runCommand("/paySpecified business "+amount+" "+businessName)).build();
		
		return text;
	}
	
	public Text payOption(String who) {
		Text balanceText = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		balanceText = balanceText.toBuilder().append(basicText(who,TextColors.GREEN)).build();
		balanceText = balanceText.toBuilder().append(basicText(" is both a ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText("player",TextColors.LIGHT_PURPLE)).build();
		balanceText = balanceText.toBuilder().append(basicText(" and a ",TextColors.YELLOW)).build();
		balanceText = balanceText.toBuilder().append(basicText("business!",TextColors.LIGHT_PURPLE)).build();
		balanceText = balanceText.toBuilder().append(basicText(" Please select which one you would like to pay: ",TextColors.YELLOW)).build();
		
		return balanceText;
	}
	
	public Text paySuccess(String who, int amount) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = EconomyLite.access.currencyPlural;
		if(amount==1) label = EconomyLite.access.currencySingular;
		
		text = text.toBuilder().append(basicText("Successfully payed ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(amount+" ",TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(label,TextColors.DARK_GREEN)).build();
		text = text.toBuilder().append(basicText(" to ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(who+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text payed(String from, int amount) {
        Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
        String label = EconomyLite.access.currencyPlural;
        if(amount==1) label = EconomyLite.access.currencySingular;
        
        text = text.toBuilder().append(basicText("You have received ",TextColors.YELLOW)).build();
        text = text.toBuilder().append(basicText(amount+" ",TextColors.GREEN)).build();
        text = text.toBuilder().append(basicText(label,TextColors.DARK_GREEN)).build();
        text = text.toBuilder().append(basicText(" from ",TextColors.YELLOW)).build();
        text = text.toBuilder().append(basicText(from+"!",TextColors.GREEN)).build();
        
        return text;
    }
	
	public Text bPayed(String from, int amount, String business) {
        Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
        String label = EconomyLite.access.currencyPlural;
        if(amount==1) label = EconomyLite.access.currencySingular;
        
        text = text.toBuilder().append(basicText(business+" has received ",TextColors.YELLOW)).build();
        text = text.toBuilder().append(basicText(amount+" ",TextColors.GREEN)).build();
        text = text.toBuilder().append(basicText(label,TextColors.DARK_GREEN)).build();
        text = text.toBuilder().append(basicText(" from ",TextColors.YELLOW)).build();
        text = text.toBuilder().append(basicText(from+"!",TextColors.GREEN)).build();
        
        return text;
    }
	
	public Text leaveSuccess(String business) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.toBuilder().append(basicText("Successfully left ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(business+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text leaveOnlyOwner(String business) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.toBuilder().append(basicText("Since you are the only owner of ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(business,TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(", it will be ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("deleted!",TextColors.RED)).build();
		
		return text;
	}
	
	public Text clickToContinue(String command) {
		Text text = basicText("[",TextColors.GRAY);
		text = text.toBuilder().append(basicText("CLICK TO CONTINUE",TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText("]",TextColors.GRAY)).build();
		
		Text hover = basicText("Click me",TextColors.GREEN);
		hover = hover.toBuilder().append(basicText(" to continue!", TextColors.YELLOW)).build();
		
		text = text.toBuilder().onHover(TextActions.showText(hover)).build();
		text = text.toBuilder().onClick(TextActions.runCommand(command)).build();
		
		return text;
	}
	
	public Text deleteSuccess(String what) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.toBuilder().append(basicText("Sucessfully ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("deleted ",TextColors.RED)).build();
		text = text.toBuilder().append(basicText(what+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text invited(String what) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.toBuilder().append(basicText("You have been invited to join ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(what+"!",TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(" Click continue to accept!",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text inviteAccept(String what) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.toBuilder().append(basicText("You are now an owner of ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(what+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text successfulInvite(String what, String who) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.toBuilder().append(basicText("Successfully invited ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(who,TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(" to join ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(what+"!",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text aboutToDelete(String what) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		text = text.toBuilder().append(basicText(what,TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(" is about to be ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText("deleted. ",TextColors.RED)).build();
		text = text.toBuilder().append(basicText("Are you sure you wish to continue?",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text transferSuccess(String businessName, int amount) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		String label = EconomyLite.access.currencyPlural;
		if(amount==1) label = EconomyLite.access.currencySingular;
		
		text = text.toBuilder().append(basicText("Successfully transfered ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(amount+" ",TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(label,TextColors.DARK_GREEN)).build();
		text = text.toBuilder().append(basicText(" from ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(businessName,TextColors.GREEN)).build();
		text = text.toBuilder().append(basicText(" into your account!",TextColors.YELLOW)).build();
		
		return text;
	}
	
	public Text ownersTitle(String businessName) {
		Text text = Text.builder("EconomyLite » ").color(TextColors.GOLD).build();
		
		text = text.toBuilder().append(basicText("Owners of ",TextColors.YELLOW)).build();
		text = text.toBuilder().append(basicText(businessName+":",TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text owner(String owner) {
		Text text = Text.builder(" + ").color(TextColors.YELLOW).build();
		
		text = text.toBuilder().append(basicText(owner,TextColors.GREEN)).build();
		
		return text;
	}
	
	public Text change(String change) {
		Text text = Text.builder(" + ").color(TextColors.YELLOW).build();
		
		text = text.toBuilder().append(basicText(change,TextColors.GREEN)).build();
		
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
