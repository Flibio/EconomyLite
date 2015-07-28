package me.Flibio.EconomyLite.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {
	
	public JsonUtils() {
		
	}
	
	public String parseToUUID(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonPlayerData data = gson.fromJson(json, JsonPlayerData.class);
    	if(data.getID()==null) return "";
    	String uuid = data.getID();
    	return insertDashUUID(uuid);
	}
	
	public String getVersion(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonGitHubData data = gson.fromJson(json, JsonGitHubData.class);
    	if(data.getName()==null) return "";
    	String version = data.getName();
    	return version;
	}
	
	public String getUrl(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonGitHubData data = gson.fromJson(json, JsonGitHubData.class);
    	if(data.getUrl()==null) return "";
    	String url = data.getUrl();
    	return url;
	}
	
	public boolean isPreRelease(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonGitHubData data = gson.fromJson(json, JsonGitHubData.class);
    	boolean isPre = data.isPreRelease();
    	return isPre;
	}
	
	private String insertDashUUID(String uuid) {
		StringBuffer sb = new StringBuffer(uuid);
		sb.insert(8, "-");
		
		sb = new StringBuffer(sb.toString());
		sb.insert(13, "-");
		
		sb = new StringBuffer(sb.toString());
		sb.insert(18, "-");
		
		sb = new StringBuffer(sb.toString());
		sb.insert(23, "-");
		
		return sb.toString();
	}
}
