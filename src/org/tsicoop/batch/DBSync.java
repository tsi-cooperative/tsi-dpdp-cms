package org.tsicoop.batch;

import org.json.simple.JSONObject;
import org.postgresql.core.BaseConnection;
import org.postgresql.copy.CopyManager;
import org.tsicoop.framework.BatchDB;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class DBSync{

    private static Properties configProps = new Properties();
    private static Properties schemaProps = new Properties();

    public static void main(String[] args) {
        String webinfdir = null;
        if(args.length != 0){
            webinfdir = args[0];
        }else{
            webinfdir = "C:/work/tsi-dpdp-cms/web/WEB-INF";
        }
        try {
            String configpath = webinfdir+"/_config.tsi";//args[0]
            FileInputStream configFileReader = new FileInputStream(configpath);
            configProps.load(configFileReader);
            String schemapath = webinfdir+"/db/_schema.tsi";
            FileInputStream schemaFileReader = new FileInputStream(schemapath);
            schemaProps.load(schemaFileReader);
            DBSync sync = new DBSync();
            sync.syncSchema(configProps, schemaProps);

           // sync.loadAdminAccount(configProps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncSchema(Properties configProps, Properties schemaProps) throws Exception{

        // No. of sql statements in property file
        Set masterKeys = schemaProps.keySet();
        int lastkey = masterKeys.size();
        //System.out.println("Last Key "+lastkey);

        // Check if master schema registry is present. If not create it
        createSchemaRegistry(configProps);

        // Get sql executed count in master schema registry
        int mrcount = getRegistryCount(configProps);
        //System.out.println("mrcount "+mrcount);

        if(lastkey>mrcount){
            for(int i=mrcount+1; i<=lastkey; i++){
                executeSQL(configProps, i, schemaProps.getProperty(i+""));
                System.out.println("Master sync "+i+" completed");
            }
        }
        System.out.println("Synced master schema registry");
    }
    private void createSchemaRegistry(Properties configProps) throws Exception {
        PreparedStatement pstmt = null;
        StringBuffer buff = null;
        Connection con = null;

        try {
            con = new BatchDB(configProps).getConnection();
            // insert schema mgr
            buff = new StringBuffer();
            buff.append("CREATE TABLE IF NOT EXISTS _sys_schema_registry (");
            buff.append("schema_no INTEGER NOT NULL,");
            buff.append("created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            pstmt = con.prepareStatement(buff.toString());
            int i = pstmt.executeUpdate();
            System.out.println("registry created:"+i);
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            BatchDB.close(pstmt);
            BatchDB.close(con);
        }
    }

    private int getRegistryCount(Properties configProps) throws Exception {
        Statement stmt = null;
        StringBuffer buff = null;
        Connection con = null;
        ResultSet rs = null;
        int count = 0;

        try {
            con = new BatchDB(configProps).getConnection();
            // insert schema mgr
            buff = new StringBuffer();
            buff.append("select count(*) from _sys_schema_registry");
            stmt = con.createStatement();
            rs = stmt.executeQuery(buff.toString());
            rs.next();
            count = rs.getInt(1);
        } finally {
            BatchDB.close(rs);
            BatchDB.close(stmt);
            BatchDB.close(con);
        }
        return count;
    }

    private void executeSQL(Properties configProps, int schemano, String sql) throws Exception {
        Statement stmt = null;
        Connection con = null;
        ResultSet rs = null;
        int count = 0;
        StringBuffer buff = null;
        PreparedStatement pstmt = null;

        try {
            con = new BatchDB(configProps).getConnection();

            // execute sql
            stmt = con.createStatement();
            stmt.executeUpdate(sql);

            // update registry
            buff = new StringBuffer();
            buff.append("INSERT INTO _sys_schema_registry (schema_no) values(?)");
            pstmt = con.prepareStatement(buff.toString());
            pstmt.setInt(1,schemano);
            pstmt.executeUpdate();

            //con.commit();
        } finally {
            BatchDB.close(stmt);
            BatchDB.close(con);
        }
    }

    private void loadAdminAccount(Properties configProps) throws Exception {
        Statement stmt = null;
        Connection con = null;
        StringBuffer buff = null;
        PreparedStatement pstmt = null;

        try {
            con = new BatchDB(configProps).getConnection();

            // insert admin role
            buff = new StringBuffer();
            buff.append("INSERT INTO _user(account_slug,org_name,account_type,start_year,industry_slug,state_slug,city_slug,latitude,longitude) SELECT 'tsicoop.org', 'TSI TECH SOLUTIONS COOP FOUNDATION','ADMIN',2024,'IT','TN','Coimbatore',0,0 WHERE NOT EXISTS (SELECT account_slug FROM _organization_account WHERE account_slug='tsicoop.org')");
            pstmt = con.prepareStatement(buff.toString());
            pstmt.executeUpdate();

            // insert admin user `
            buff = new StringBuffer();
            buff.append("INSERT INTO _USER (name,role_slug,email,mobile,account_type,account_slug) SELECT 'Satish Ayyaswami','SYSADMIN','admin@tsicoop.org','9940161886','ADMIN','tsicoop.org' WHERE NOT EXISTS (SELECT email FROM _user WHERE email='admin@tsicoop.org')");
            pstmt = con.prepareStatement(buff.toString());
            pstmt.executeUpdate();

            //con.commit();
        } finally {
            BatchDB.close(stmt);
            BatchDB.close(con);
        }
    }

    private void loadMasterData(Properties configProps, String dataDir, String entity) throws Exception{
        Connection con = null;
        Statement stmt = null;
        String ddpath = null;

        try {
            con = new BatchDB(configProps).getConnection();
            stmt = con.createStatement();
            // Load Data Master
            ddpath = dataDir+"/"+entity+".csv";
            stmt.executeUpdate("DELETE FROM "+entity);
            //stmt.executeUpdate("COPY "+entity+" FROM '"+ddpath+"' DELIMITER ',' CSV");
            BaseConnection baseConn = con.unwrap(BaseConnection.class);
            CopyManager copyManager = new CopyManager(baseConn);
            BufferedReader reader =  new BufferedReader(new InputStreamReader(
                    new FileInputStream(ddpath), "UTF-8"));
            long numrowsinserted = copyManager.copyIn("COPY "+entity+" FROM STDIN WITH DELIMITER '|'", reader);
            System.out.println("Loaded "+entity+" Inserted "+numrowsinserted);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(stmt != null) stmt.close();
            if(con != null) con.close();
        }
    }

}
