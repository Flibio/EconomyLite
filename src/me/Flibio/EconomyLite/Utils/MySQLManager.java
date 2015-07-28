package me.Flibio.EconomyLite.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import me.Flibio.EconomyLite.Main;

import org.slf4j.Logger;
import org.spongepowered.api.service.sql.SqlService;

public class MySQLManager {
	
	public enum ChangeAction {
		REMOVE,ADD
	}
	
	private String hostname;
	private String port;
	private String database;
	private String username;
	private String password;
	
	private Connection con;
	private Statement statement;
	private SqlService sqlService;
	
	private Logger logger = Main.access.logger;
	
	public MySQLManager(String hostname, String port, String database,
			String username, String password, SqlService sql) {
		//Set the connection variables
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		
		//Set the sql service variable
		sqlService = sql;
		
		//Open a connection
		con = openConnection();
		createStatement();
		if(statement!=null){
			try {
				PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS EconomyLite(uuid VARCHAR(100), currency INT(100))");
				ps.executeUpdate();
				PreparedStatement ps2 = con.prepareStatement("CREATE TABLE IF NOT EXISTS EconomyLiteBusinesses(name VARCHAR(1000),balance INT(100),needConfirm VARCHAR(5))");
				ps2.executeUpdate();
				PreparedStatement ps3 = con.prepareStatement("CREATE TABLE IF NOT EXISTS EconomyLiteBusinessOwners(uuid VARCHAR(100), business VARCHAR(1000))");
				ps3.executeUpdate();
				PreparedStatement ps4 = con.prepareStatement("CREATE TABLE IF NOT EXISTS EconomyLiteBusinessInvited(uuid VARCHAR(100), business VARCHAR(1000))");
				ps4.executeUpdate();
			} catch (SQLException e) {
				logger.error("Error creating EconomyLite databases...");
				logger.error(e.getMessage());
			}
		}
		closeConnection();
	}
	//Generic--
	private Connection openConnection() {
		try {
			DataSource source = sqlService.getDataSource("jdbc:mysql://"+hostname+":"+port+"/"+database+"?user="+username+"&password="+password);
			return source.getConnection();
		} catch (SQLException e) {
			logger.error("Error opening MySQL connection...");
			logger.error("Invalid credentials, hostname, database?");
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private void createStatement() {
		if(con == null) return;
		try {
			statement = con.createStatement();
		} catch (SQLException e) {
			logger.error("Error creating MySQL statement...");
			logger.error(e.getMessage());
		}
	}
	
	private void closeConnection(){
		try {
			con.close();
		} catch (SQLException e) {
			logger.error("Error closing MySQL connection...");
			logger.error(e.getMessage());
		}
	}
	
	private void reconnect() {
		closeConnection();
		con = openConnection();
		createStatement();
	}
	//Players--
	public boolean playerExists(String uuid) {
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT uuid FROM EconomyLite WHERE uuid = ?");
			ps.setString(1, uuid);
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
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public int getBalance(String uuid) {
		//Make sure the player exists
		if(!playerExists(uuid)) {
			return -1;
		}
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT currency FROM EconomyLite WHERE uuid = ?");
			ps.setString(1, uuid);
			res = ps.executeQuery();
			res.next();
			closeConnection();
			return res.getInt("currency");
		} catch (SQLException e) {
			logger.error("Error getting currency of player...");
			logger.error(e.getMessage());
			closeConnection();
			return -1;
		}
	}
	
	public boolean newPlayer(String uuid) {
		//Make sure the player doesn't exist
		if(playerExists(uuid)) {
			return false;
		}
		reconnect();
		try {
			PreparedStatement ps = con.prepareStatement("INSERT INTO EconomyLite (`uuid`, `currency`) VALUES (?, '0');");
			ps.setString(1, uuid);
			ps.executeUpdate();
			closeConnection();
			return true;
		} catch (SQLException e) {
			logger.error("Error registering new player...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public boolean setBalance(String uuid, int balance) {
		//Make sure the player exists
		if(!playerExists(uuid)) {
			return false;
		}
		reconnect();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE EconomyLite SET currency = ? WHERE uuid = ?");
			ps.setString(1, Integer.toString(balance));
			ps.setString(2, uuid);
			ps.executeUpdate();
			closeConnection();
			return true;
		} catch (SQLException e) {
			logger.error("Error setting currency of player...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	//Businesses--
	public boolean businessExists(String name) {
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT name FROM EconomyLiteBusinesses WHERE name = ?");
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
			logger.error("Error checking if business exists...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public int getBusinessBalance(String name) {
		//Make sure the business exists
		if(!businessExists(name)) {
			return -1;
		}
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT balance FROM EconomyLiteBusinesses WHERE name = ?");
			ps.setString(1, name);
			res = ps.executeQuery();
			res.next();
			closeConnection();
			return res.getInt("balance");
		} catch (SQLException e) {
			logger.error("Error getting balance of business...");
			logger.error(e.getMessage());
			closeConnection();
			return -1;
		}
	}
	
	public boolean newBusiness(String name) {
		//Make sure the business doesn't exist
		if(businessExists(name)) {
			return false;
		}
		reconnect();
		try {
			PreparedStatement ps = con.prepareStatement("INSERT INTO EconomyLiteBusinesses (`name`, `balance`, `needConfirm`) VALUES (?, '0', 'true');");
			ps.setString(1, name);
			ps.executeUpdate();
			closeConnection();
			return true;
		} catch (SQLException e) {
			logger.error("Error registering new business...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public boolean setBusinessBalance(String name, int balance) {
		//Make sure the business exists
		if(!businessExists(name)) {
			return false;
		}
		reconnect();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE EconomyLiteBusinesses SET balance = ? WHERE name = ?");
			ps.setString(1, Integer.toString(balance));
			ps.setString(2, name);
			ps.executeUpdate();
			closeConnection();
			return true;
		} catch (SQLException e) {
			logger.error("Error setting balance of business...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public boolean deleteBusiness(String name) {
		//Make sure the business exists
		if(!businessExists(name)) {
			return false;
		}
		reconnect();
		try {
			PreparedStatement ps = con.prepareStatement("DELETE FROM EconomyLiteBusinesses WHERE name = ?");
			ps.setString(1, name);
			ps.executeUpdate();
			PreparedStatement ps2 = con.prepareStatement("DELETE FROM EconomyLiteBusinessOwners WHERE business = ?");
			ps2.setString(1, name);
			ps2.executeUpdate();
			PreparedStatement ps3 = con.prepareStatement("DELETE FROM EconomyLiteBusinessInvited WHERE business = ?");
			ps3.setString(1, name);
			ps3.executeUpdate();
			closeConnection();
			return true;
		} catch (SQLException e) {
			logger.error("Error deleting business...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public String getCapitalizedBusinessName(String name) {
		//Make sure the business exists
		if(!businessExists(name)) {
			return "";
		}
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT name FROM EconomyLiteBusinesses WHERE name = ?");
			ps.setString(1, name);
			res = ps.executeQuery();
			res.next();
			closeConnection();
			return res.getString("name");
		} catch (SQLException e) {
			logger.error("Error getting name of business...");
			logger.error(e.getMessage());
			closeConnection();
			return "";
		}
	}
	
	public boolean needsConfirm(String business) {
		if(!businessExists(business)) {
			return true;
		}
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT needConfirm FROM EconomyLiteBusinesses WHERE name = ?");
			ps.setString(1, business);
			res = ps.executeQuery();
			res.next();
			closeConnection();
			return res.getBoolean("needConfirm");
		} catch (SQLException e) {
			logger.error("Error getting need confirm of business...");
			logger.error(e.getMessage());
			closeConnection();
			return true;
		}
	}
	
	public boolean setConfirm(String business, boolean needConfirm) {
		//Make sure the business exists
		if(!businessExists(business)) {
			return false;
		}
		reconnect();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE EconomyLiteBusinesses SET needConfirm = ? WHERE name = ?");
			ps.setString(1, Boolean.toString(needConfirm));
			ps.setString(2, business);
			ps.executeUpdate();
			closeConnection();
			return true;
		} catch (SQLException e) {
			logger.error("Error setting need confirm of business...");
			logger.error(e.getMessage());
			closeConnection();
			return false;
		}
	}
	
	public ArrayList<String> getAllBusinesses() {
		ArrayList<String> businesses = new ArrayList<String>();
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT name FROM EconomyLiteBusinesses");
			res = ps.executeQuery();
			while(res.next()) {
				businesses.add(res.getString("name"));
			}
			closeConnection();
			return businesses;
		} catch (SQLException e) {
			logger.error("Error getting businesses...");
			logger.error(e.getMessage());
			closeConnection();
			return businesses;
		}
	}
	
	public ArrayList<String> getBusinesses(String owner) {
		ArrayList<String> businesses = new ArrayList<String>();
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT name FROM EconomyLiteBusinesses");
			res = ps.executeQuery();
			while(res.next()) {
				String name = res.getString("name");
				if(getOwners(name).contains(owner)) {
					businesses.add(name);
				}
			}
			closeConnection();
			return businesses;
		} catch (SQLException e) {
			logger.error("Error getting businesses...");
			logger.error(e.getMessage());
			closeConnection();
			return businesses;
		}
	}
	
	public ArrayList<String> getOwners(String business) {
		ArrayList<String> owners = new ArrayList<String>();
		if(!businessExists(business)) {
			return owners;
		}
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT uuid FROM EconomyLiteBusinessOwners WHERE business = ?");
			ps.setString(1, business);
			res = ps.executeQuery();
			while(res.next()) {
				owners.add(res.getString("uuid"));
			}
			closeConnection();
			return owners;
		} catch (SQLException e) {
			logger.error("Error getting owners...");
			logger.error(e.getMessage());
			closeConnection();
			return owners;
		}
	}
	
	public ArrayList<String> getInvited(String business) {
		ArrayList<String> invited = new ArrayList<String>();
		if(!businessExists(business)) {
			return invited;
		}
		reconnect();
		ResultSet res;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT uuid FROM EconomyLiteBusinessInvited WHERE business = ?");
			ps.setString(1, business);
			res = ps.executeQuery();
			while(res.next()) {
				invited.add(res.getString("uuid"));
			}
			closeConnection();
			return invited;
		} catch (SQLException e) {
			logger.error("Error getting invited...");
			logger.error(e.getMessage());
			closeConnection();
			return invited;
		}
	}
	
	public boolean setOwner(ChangeAction action, String owner, String business) {
		if(!businessExists(business)) {
			return false;
		}
		if(action.equals(ChangeAction.REMOVE)) {
			//Delete the user
			reconnect();
			try {
				PreparedStatement ps = con.prepareStatement("DELETE FROM EconomyLiteBusinessOwners WHERE business = ? AND uuid = ?");
				ps.setString(1, business);
				ps.setString(2, owner);
				ps.executeUpdate();
				closeConnection();
				return true;
			} catch (SQLException e) {
				logger.error("Error setting owner...");
				logger.error(e.getMessage());
				closeConnection();
				return false;
			}
		} else if(action.equals(ChangeAction.ADD)) {
			//Add the user
			if(getOwners(business).contains(owner)) {
				//Owner exists
				return false;
			} else {
				reconnect();
				try {
					PreparedStatement ps = con.prepareStatement("INSERT INTO EconomyLiteBusinessOwners (`uuid`, `business`) VALUES (?, ?);");
					ps.setString(1, owner);
					ps.setString(2, business);
					ps.executeUpdate();
					closeConnection();
					return true;
				} catch (SQLException e) {
					logger.error("Error setting owner...");
					logger.error(e.getMessage());
					closeConnection();
					return false;
				}
			}
		} else {
			return false;
		}
	}
	
	public boolean setInvite(ChangeAction action, String owner, String business) {
		if(!businessExists(business)) {
			return false;
		}
		if(action.equals(ChangeAction.REMOVE)) {
			//Delete the user
			reconnect();
			try {
				PreparedStatement ps = con.prepareStatement("DELETE FROM EconomyLiteBusinessInvited WHERE business = ? AND uuid = ?");
				ps.setString(1, business);
				ps.setString(2, owner);
				ps.executeUpdate();
				closeConnection();
				return true;
			} catch (SQLException e) {
				logger.error("Error setting invited...");
				logger.error(e.getMessage());
				closeConnection();
				return false;
			}
		} else if(action.equals(ChangeAction.ADD)) {
			//Add the user
			if(getInvited(business).contains(owner)) {
				//Owner exists
				return false;
			} else {
				reconnect();
				try {
					PreparedStatement ps = con.prepareStatement("INSERT INTO EconomyLiteBusinessInvited (`uuid`, `business`) VALUES (?, ?);");
					ps.setString(1, owner);
					ps.setString(2, business);
					ps.executeUpdate();
					closeConnection();
					return true;
				} catch (SQLException e) {
					logger.error("Error setting invited...");
					logger.error(e.getMessage());
					closeConnection();
					return false;
				}
			}
		} else {
			return false;
		}
	}
}
