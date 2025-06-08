package org.tsicoop.app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tsicoop.framework.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Policy implements REST {

    private static final String FUNCTION = "_func";

    private static final String ADD_POLICY = "add_policy";

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
                if(func.equalsIgnoreCase(ADD_POLICY)){
                    String fiduciaryId = req.getParameter("fid");
                    output = addPolicy(input,fiduciaryId);
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

    protected JSONObject addPolicy(JSONObject input, String fiduciaryId){
        JSONObject output = new JSONObject();
        boolean created = false;
        DBQuery query = null;
        String name = (String) input.get("name");
        String version = (String) input.get("version");
        String effective_date = (String) input.get("effective_date");
        String jurisdiction = (String) input.get("jurisdiction");
        UUID policyuuid = UUID.randomUUID();
        try {
            Connection conn = new PoolDB().getConnection();
            String sql = "INSERT INTO _policy (policy_id,fiduciary_id,name,version,effective_date,jurisdiction,policy_content) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, policyuuid);
            pstmt.setObject(2, UUID.fromString(fiduciaryId));
            pstmt.setString(3, name);
            pstmt.setString(4, version);
            pstmt.setTimestamp(5, convertStringToTimestamp(effective_date, "yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, jurisdiction);
            pstmt.setString(7, input.toJSONString());
            pstmt.executeUpdate();
            created = true;
        }catch(Exception e){
            e.printStackTrace();
        }

        if(created) {
            output.put("_added", true);
            output.put("policy_id",policyuuid.toString());
        }else {
            output.put("_added", false);
        }
        return output;
    }

    public static Timestamp convertStringToTimestamp(String dateString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
        return Timestamp.valueOf(localDateTime);
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
