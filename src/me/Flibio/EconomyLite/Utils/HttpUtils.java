package me.Flibio.EconomyLite.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpUtils {
	
	private final String USER_AGENT = "Mozilla/5.0";
	
	public HttpUtils() {
		
	}
	
	public String requestData(String url) {
		URL obj;
		try {
			obj = new URL(url);
		} catch (MalformedURLException e) {
			return "";
		}
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			return "";
		}
 
		// optional default is GET
		try {
			con.setRequestMethod("GET");
		} catch (ProtocolException e) {
			return "";
		}
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} catch (IOException e) {
			return "";
		}
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		try {
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		} catch (IOException e) {
			return "";
		}
		try {
			in.close();
		} catch (IOException e) {
			return "";
		}
		con.disconnect();
		//return result
		return response.toString();
	}
}
