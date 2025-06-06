package org.tsicoop.framework;

import java.sql.*;
import java.util.Properties;


@SuppressWarnings("unchecked")
public class BatchDB extends DB{

    public BatchDB(Properties config) throws SQLException{
        super();
        con = createConnection(config, true);
    }

    public Connection getConnection(){
        return con;
    }

    public Connection createConnection(Properties config, boolean autocommit) throws SQLException {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(   config.getProperty("framework.db.host")+"/"+ config.getProperty("framework.db.name"),
                                                            config.getProperty("framework.db.user"),
                                                            config.getProperty("framework.db.password"));
            if(!autocommit)
                connection.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }
}