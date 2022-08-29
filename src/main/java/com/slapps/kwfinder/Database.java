package com.slapps.kwfinder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.tools.Server;

import com.google.common.base.Throwables;

/**
 * The H2 base Database with Persistent Implementation
 * 
 * @author Muthukumaran
 *
 */
public final class Database {

	/*
	 * CREATE TABLE IF NOT EXISTS PROXY_LIST (ID INT NOT NULL, PROXY_IP_PORT
	 * VARCHAR(50) UNIQUE NOT NULL, IS_ACTIVE INT NOT NULL, REGION_ID INT,
	 * FAILED_COUNT INT NOT NULL, USAGE_COUNT INT NOT NULL, CREATED TIMESTAMP NOT
	 * NULL, UPDATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)
	 * 
	 * DROP TABLE PROXY_LIST;
	 * 
	 * SELECT * FROM PROXY_LIST;
	 * 
	 * INSERT INTO PROXY_LIST VALUES(1, '168.168.1.1:8082', 1, 1, 0, 0, now(),
	 * now()); INSERT INTO PROXY_LIST VALUES((SELECT max(ID) + 1 FROM PROXY_LIST),
	 * '168.168.1.1:8082', 1, 1, 0, 0, now(), now()); INSERT INTO PROXY_LIST
	 * VALUES((SELECT max(ID) + 1 FROM PROXY_LIST), '168.168.168.1:8082', 1, 1, 1,
	 * 0, now(), now());
	 * 
	 * INSERT INTO PROXY_LIST VALUES((SELECT max(ID) + 1 FROM PROXY_LIST),
	 * '172.168.1.1:8082', 1, 1, 0, 0, now(), now() - INTERVAL 10 DAY);
	 * 
	 * 
	 * UPDATE PROXY_LIST SET FAILED_COUNT=4 WHERE ID=2;
	 * 
	 * UPDATE PROXY_LIST SET USAGE_COUNT = 3, UPDATED=NOW() - INTERVAL 1 DAY WHERE
	 * ID=3
	 * 
	 * SELECT * FROM PROXY_LIST WHERE IS_ACTIVE=1 AND UPDATED < NOW() - INTERVAL 1
	 * DAY AND FAILED_COUNT < 2 ORDER BY UPDATED, FAILED_COUNT, USAGE_COUNT
	 */					
	private static final String FQCN = Database.class.getName();
	private static final Database db = new Database();
	private Connection connection;
	private String CREATE_PROXY_TABLE = "CREATE TABLE IF NOT EXISTS PROXY_LIST "
			+ "(ID INT NOT NULL, PROXY_IP_PORT VARCHAR(50) UNIQUE NOT NULL, IS_ACTIVE INT NOT NULL, "
			+ "REGION_ID INT, FAILED_COUNT INT NOT NULL, USAGE_COUNT INT NOT NULL, "
			+ "CREATED DATE NOT NULL, UPDATED DATE NOT NULL)";
	private String SELECT_PROXY = "SELECT * FROM PROXY_LIST WHERE "
			+ "IS_ACTIVE=1 ORDER BY UPDATED, FAILED_COUNT, USAGE_COUNT";
	private final int port = 4048;
	private Server server = null;
	
	private Database() { 
		start(); 
	}
	
	public static Database getInstance() { 
		return db; 
	}
	
	public synchronized void start() {
		if (server != null) return;
		
        try {
            server = Server.createTcpServer("-tcpAllowOthers").start();
            Class.forName("org.h2.Driver");
            connection = DriverManager.
                getConnection("jdbc:h2:file://localhost/~/slapps", "admin", "admin");
            Logit.log(FQCN, "Connection Established: "
                    + connection.getMetaData().getDatabaseProductName() + 
                    "/" + connection.getCatalog());
         
        } catch (Exception e) {
            Logit.log(FQCN, Throwables.getStackTraceAsString(e));
        }
	}
	
	public void shutdown() {
		if (server != null) {
			try {
				connection.close();
				server.shutdown();
				server = null;
			} catch (SQLException e) {
			}
			
		}
	}
	
	public void createTable(String query) throws Exception {
			PreparedStatement pst = (PreparedStatement) connection.createStatement();
			pst.executeUpdate(CREATE_PROXY_TABLE);
	}
}
