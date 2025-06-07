package org.tsicoop.app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tsicoop.framework.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class DataFiduciary implements REST {

    private static final String FUNCTION = "_func";

    private static final String ADD_FIDUCIARY = "add_fiduciary";

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
                if(func.equalsIgnoreCase(ADD_FIDUCIARY)){
                      output = addFiduciary(input);
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

    protected JSONObject addFiduciary(JSONObject input){
        JSONObject output = new JSONObject();
        boolean created = false;
        DBQuery query = null;
        String name = (String) input.get("name");
        String contact_person = (String) input.get("contact_person");
        String email = (String) input.get("email");
        String phone = (String) input.get("phone");
        String domain = (String) input.get("domain");
        boolean is_significant_data_fiduciary = (boolean) input.get("is_significant_data_fiduciary");
        String dpb_registration_id = (String) input.get("dpb_registration_id");
        UUID dfuuid = UUID.randomUUID();
        try {
            Connection conn = new PoolDB().getConnection();
            String sql = "INSERT INTO _data_fiduciary (fiduciary_id,name,contact_person,email,phone,domain,is_significant_data_fiduciary,dpb_registration_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, dfuuid);
            pstmt.setString(2, name);
            pstmt.setString(3, contact_person);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.setString(6, domain);
            pstmt.setBoolean(7, is_significant_data_fiduciary);
            pstmt.setString(8, dpb_registration_id);
            pstmt.executeUpdate();
            created = true;
        }catch(Exception e){
            e.printStackTrace();
        }

        if(created) {
            output.put("_added", true);
            output.put("fiduciary_id",dfuuid.toString());
        }else {
            output.put("_added", false);
        }
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
