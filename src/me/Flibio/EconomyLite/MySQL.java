package me.Flibio.EconomyLite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.spongepowered.api.service.sql.SqlService;

public class MySQL {
	private final String user;
	private final String database;
	private final String password;
	private final String port;
	private final String hostname;
	private final Logger logger;
	private Connection c;
	private Statement s;
	private SqlService sql;


	/**
	 * Creates a new MySQL instance
	 * 
	 * @param plugin
	 *            Plugin instance
	 * @param hostname
	 *            Name of the host
	 * @param port
	 *            Port number
	 * @param database
	 *            Database name
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 */
	public MySQL(String hostname, String port, String database,
			String username, String password, Logger logger, SqlService sql) {
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.user = username;
		this.password = password;
		this.logger = logger;
		this.sql = sql;
		
		c = openConnection();
		
		createStatement();
		if(s!=null){
			try {
				PreparedStatement ps = c.prepareStatement("CREATE TABLE IF NOT EXISTS EconomyLite(uuid VARCHAR(100), name VARCHAR(100) ,currency INT(100))");
				ps.executeUpdate();
			} catch (SQLException e) {
				logger.error("Error creating EconomyLite database...");
				e.printStackTrace();
			}
		}
		closeConnection();
	}

	protected Connection openConnection() {
		try {
			DataSource source = sql.getDataSource("jdbc:mysql://"+hostname+":"+port+"/"+database+"?user="+user+"&password="+password);
			return source.getConnection();
		} catch (SQLException e) {
			logger.error("Error opening MySQL connection...");
			logger.error("Invalid credentials, hostname, database?");
			return null;
		}
	}
	
	protected void closeConnection(){
		try {
			c.close();
		} catch (SQLException e) {
			logger.error("Error closing MySQL connection...");
			e.printStackTrace();
		}
	}
	
	protected void createStatement() {
		if(c == null) return;
		try {
			s = c.createStatement();
		} catch (SQLException e) {
			logger.error("Error creating MySQL statement...");
			e.printStackTrace();
		}
	}
	
	protected void reconnect() {
		closeConnection();
		c = openConnection();
		createStatement();
	}
	
	protected boolean playerExists(String name){
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = c.prepareStatement("SELECT name FROM EconomyLite WHERE name = ?");
			ps.setString(1, name);
			res = ps.executeQuery();
			if(!(res.next())) {
				closeConnection();
				return false;
			} else {
				closeConnection();
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error checking if player exists...");
			e.printStackTrace();
			closeConnection();
			return false;
		}
	}
	
	protected boolean playerExists(UUID uuid){
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = c.prepareStatement("SELECT uuid FROM EconomyLite WHERE uuid = ?");
			ps.setString(1, uuid.toString());
			res = ps.executeQuery();
			if(!(res.next())) {
				closeConnection();
				return false;
			} else {
				closeConnection();
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error checking if player exists...");
			e.printStackTrace();
			closeConnection();
			return false;
		}
	}
	
	protected int getCurrency(String name) {
		/*
		 * create custom event that is fired
		 */
		reconnect();
		int amount;
		ResultSet res;
		try {
			PreparedStatement ps = c.prepareStatement("SELECT currency FROM EconomyLite WHERE name = ?");
			ps.setString(1, name);
			res = ps.executeQuery();
			res.next();
			amount = res.getInt("currency");
		} catch (SQLException e) {
			logger.error("Error getting currency of player..."+e.getMessage());
			amount = 0;
		}
		closeConnection();
		return amount;
	}
	
	protected boolean setCurrency(String name, int amount) {
		reconnect();
		try {
			PreparedStatement ps = c.prepareStatement("UPDATE EconomyLite SET currency = ? WHERE name = ?");
			ps.setString(1, Integer.toString(amount));
			ps.setString(2, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error setting currency of player...");
			e.printStackTrace();
			closeConnection();
			return false;
		}
		closeConnection();
		return true;
	}
	
	protected boolean addPlayer(String uuid, String name) {
		reconnect();
		try {
			PreparedStatement ps = c.prepareStatement("INSERT INTO EconomyLite (`uuid`, `name`, `currency`) VALUES (?, ?, '0');");
			ps.setString(1, uuid);
			ps.setString(2, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error setting currency of player...");
			e.printStackTrace();
			closeConnection();
			return false;
		}
		closeConnection();
		return true;
	}
	
	protected boolean updateName(String uuid, String name){
		reconnect();
		try {
			PreparedStatement ps = c.prepareStatement("UPDATE EconomyLite SET name = ? WHERE uuid = ?");
			ps.setString(1, name);
			ps.setString(2, uuid);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error updating name of player...");
			e.printStackTrace();
			closeConnection();
			return false;
		}
		closeConnection();
		return true;
	}
}