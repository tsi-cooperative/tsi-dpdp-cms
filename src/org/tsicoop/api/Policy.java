package org.tsicoop.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tsicoop.framework.InputProcessor;
import org.tsicoop.framework.OutputProcessor;
import org.tsicoop.framework.PoolDB;
import org.tsicoop.framework.REST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Policy implements REST {

    private static final String FUNCTION = "_func";

    private static final String GET_POLICY = "get_policy";

    @Override
    public void get(HttpServletRequest req, HttpServletResponse res) {

    }

    @Override
    public void post(HttpServletRequest req, HttpServletResponse res) {
        JSONObject input = null;
        JSONObject output = null;
        JSONArray outputArray = null;
        String func = null;

        try {
            input = InputProcessor.getInput(req);
            func = (String) input.get(FUNCTION);
            if(func != null){
                if(func.equalsIgnoreCase(GET_POLICY)){
                    output = getPolicy(input);
                }
            }
            if(outputArray != null)
                OutputProcessor.send(res, HttpServletResponse.SC_OK, outputArray);
            else if(output != null)
                OutputProcessor.send(res, HttpServletResponse.SC_OK, output);
        }catch(Exception e){
            OutputProcessor.sendError(res,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Unknown server error");
            e.printStackTrace();
        }
    }

    public JSONObject getPolicy(JSONObject input){
        JSONObject policy = new JSONObject();
        Statement stmt = null;
        StringBuffer buff = null;
        Connection con = null;
        ResultSet rs = null;
        String fiduciaryId = (String) input.get("fiduciary_id");
        String policyId = (String) input.get("policy_id");

        try {
            con = new PoolDB().getConnection();
            buff = new StringBuffer();
            buff.append("select policy_content from _policy where fiduciary_id='"+fiduciaryId+"' and policy_id='"+policyId+"'");
            stmt = con.createStatement();
            rs = stmt.executeQuery(buff.toString());
            if (rs.next()) {
                policy = (JSONObject) new JSONParser().parse(rs.getString("policy_content"));
                policy.remove("_func");
                policy.remove("fiduciary_id");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }finally{
            PoolDB.close(rs);
            PoolDB.close(stmt);
            PoolDB.close(con);
        }
        return policy;
    }

    @Override
    public void delete(HttpServletRequest req, HttpServletResponse res) {

    }

    @Override
    public void put(HttpServletRequest req, HttpServletResponse res) {

    }

    @Override
    public boolean validate(String method, HttpServletRequest req, HttpServletResponse res) {
        return true;
    }
}
