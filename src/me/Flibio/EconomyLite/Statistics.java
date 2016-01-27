package me.Flibio.EconomyLite;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Statistics {

	private final String USER_AGENT = "EconomyLite/2.0";
	private String ip;
	private boolean working = false;
	
	private String accessKey = "";
	
	public Statistics() {
		Optional<InetSocketAddress> addrOptional = EconomyLite.access.game.getServer().getBoundAddress();
		EconomyLite.access.game.getScheduler().createTaskBuilder().execute(r -> {
			String ipAddrResponse = "";
			try {
				ipAddrResponse = post("http://checkip.amazonaws.com","");
			} catch(Exception e) {
				EconomyLite.access.logger.error(e.getMessage());
			}
			if(addrOptional.isPresent()&&!ipAddrResponse.isEmpty()) {
				InetSocketAddress addr = addrOptional.get();
				this.ip = ipAddrResponse+":"+addr.getPort();
				//Register the server as started
				this.working = true;
				try {
					String response = post("http://api.flibio.net/serverStarted.php","ip="+ip);
					if(!response.contains("error")&&response.length()>10) {
						accessKey = response;
						EconomyLite.access.game.getScheduler().createTaskBuilder().execute(t -> {
							try {
								post("http://api.flibio.net/pinger.php","key="+accessKey+"&ip="+this.ip+"&pl="+
										EconomyLite.access.game.getServer().getOnlinePlayers().size());
							} catch(Exception e) {
								EconomyLite.access.logger.error(e.getMessage());
							}
						}).async().interval(15, TimeUnit.MINUTES).delay(1, TimeUnit.MINUTES).submit(EconomyLite.access);
					}
				} catch(Exception e) {
					EconomyLite.access.logger.error(e.getMessage());
				}
			} else {
				this.ip = "";
			}
		}).async().submit(EconomyLite.access);
	}
	
	@Listener
	public void onServerStop(GameStoppingServerEvent event) {
		if(!working) return;
		try {
			post("http://api.flibio.net/stopped.php","key="+accessKey+"&ip="+this.ip);
		} catch (Exception e) {
			EconomyLite.access.logger.error(e.getMessage());
		}
	}
	
	private String post(String urlString, String urlParameters) throws Exception {
		// Send data
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(urlParameters);
		wr.flush();
		
		// Get the response
		BufferedReader rd = new BufferedReader(
		new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null) {
			return line;
		}
		wr.close();
		rd.close();
		return "";
	}
	
}
