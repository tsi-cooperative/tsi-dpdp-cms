package org.tsicoop.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tsicoop.framework.InputProcessor;
import org.tsicoop.framework.OutputProcessor;
import org.tsicoop.framework.PoolDB;
import org.tsicoop.framework.REST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class DataPrincipal implements REST {

    private static final String FUNCTION = "_func";

    private static final String GET_DATA_PRINCIPAL = "get_data_principal";

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
                if(func.equalsIgnoreCase(GET_DATA_PRINCIPAL)){
                    output = getDataPrincipal(input);
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

    protected JSONObject getDataPrincipal(JSONObject input){
        JSONObject output = new JSONObject();
        String principal = null;
        String fiduciaryId = (String) input.get("fiduciary_id");
        String name = (String) input.get("name");
        String email = (String) input.get("email");
        String mobile = (String) input.get("mobile");

        try {
            Connection conn = new PoolDB().getConnection();
            // Check if the principal is already there
            String fsql = "select principal_id from _data_principal where email=? or mobile=?";
            PreparedStatement fstmt = conn.prepareStatement(fsql);
            fstmt.setString(1, email);
            fstmt.setString(2, mobile);
            ResultSet rs = fstmt.executeQuery();
            if(rs.next()){
                principal = rs.getString("principal_id");
            }
            else {
                UUID dpuuid = UUID.randomUUID();
                String sql = "INSERT INTO _data_principal (principal_id,fiduciary_id,name,email,mobile) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setObject(1, dpuuid);
                pstmt.setObject(2, UUID.fromString(fiduciaryId));
                pstmt.setString(3, name);
                pstmt.setString(4, email);
                pstmt.setString(5, mobile);
                pstmt.executeUpdate();
                principal = dpuuid.toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        output.put("principal_id",principal);
        return output;
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
