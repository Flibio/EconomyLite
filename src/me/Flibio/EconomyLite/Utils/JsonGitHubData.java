package me.Flibio.EconomyLite.Utils;

public class JsonGitHubData {
	
	private String tag_name;
	private boolean prerelease;
	private String html_url;
	
	public String getName() {
		return tag_name;
	}
	
	public String getUrl() {
		return html_url;
	}
	
	public boolean isPreRelease() {
		return prerelease;
	}
	
}
