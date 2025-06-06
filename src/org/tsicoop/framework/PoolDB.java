package org.tsicoop.framework;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;


@SuppressWarnings("unchecked")
public class PoolDB extends DB{
    private  BasicDataSource basicDataSource = null;

    private synchronized void initBasicDataSource() {
        if (basicDataSource != null) {
            basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName("org.postgresql.Driver");
            basicDataSource.setUrl(SystemConfig.getAppConfig().getProperty("framework.db.host")+"/"+ SystemConfig.getAppConfig().getProperty("framework.db.name"));
            basicDataSource.setUsername(SystemConfig.getAppConfig().getProperty("framework.db.user"));
            basicDataSource.setPassword(SystemConfig.getAppConfig().getProperty("framework.db.password"));
            basicDataSource.setInitialSize(10);
            basicDataSource.setMaxTotal(300);
            basicDataSource.setTestOnBorrow(true);
            basicDataSource.setTestOnReturn(true);
            basicDataSource.setTestWhileIdle(true);
            basicDataSource.setTimeBetweenEvictionRunsMillis(300000);
            basicDataSource.setMinEvictableIdleTimeMillis(600000);
        }
    }

    public PoolDB() throws SQLException{
        super();
        con = createConnection(true);
    }

    public Connection getConnection(){
        return con;
    }

    public Connection createConnection(boolean autocommit) throws SQLException {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(   SystemConfig.getAppConfig().getProperty("framework.db.host")+"/"+ SystemConfig.getAppConfig().getProperty("framework.db.name"),
                                                            SystemConfig.getAppConfig().getProperty("framework.db.user"),
                                                            SystemConfig.getAppConfig().getProperty("framework.db.password"));
            if(!autocommit)
                connection.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;

//		if(basicDataSource == null) {
//			initBasicDataSource();
//		}
//		connection = basicDataSource.getConnection();
//		connection.setAutoCommit(false);
//		return connection;
    }


}