package org.tsicoop.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tsicoop.framework.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class Consent implements REST {

    private static final String FUNCTION = "_func";

    private static final String RECORD_CONSENT = "record_consent";
    private static final String CHECK_CONSENT = "check_consent";
    private static final String LINK_PRINCIPAL = "link_principal";

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
                if(func.equalsIgnoreCase(RECORD_CONSENT)){
                    output = recordConsent(input);
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

    protected JSONObject recordConsent(JSONObject input){
        JSONObject output = new JSONObject();
        boolean created = false;
        DBQuery query = null;
        String fiduciaryId = (String) input.get("fiduciary_id");
        String policyId = (String) input.get("policy_id");
        String version = (String) input.get("policy_version");
        String principal_id = (String) input.get("principal_id");
        String consent_id = (String) input.get("consent_id");
        String jurisdiction = (String) input.get("jurisdiction");
        String languageSelected = (String) input.get("language_selected");
        String consent_status_general = (String) input.get("consent_status_general");
        String consent_mechanism = (String) input.get("consent_mechanism");
        String ip_address = (String) input.get("ip_address");
        String user_agent = (String) input.get("user_agent");
        JSONArray data_point_consents = (JSONArray) input.get("data_point_consents");
        UUID consentuuid = UUID.randomUUID();
        try {
            Connection conn = new PoolDB().getConnection();
            String sql = "INSERT INTO _consent (consent_id,policy_id,fiduciary_id,principal_id,policy_version,jurisdiction,language_selected,consent_status_general,consent_mechanism,ip_address,user_agent,data_point_consents) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?::inet, ?, ?::jsonb)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, consentuuid);
            pstmt.setObject(2, UUID.fromString(policyId));
            pstmt.setObject(3, UUID.fromString(fiduciaryId));
            pstmt.setObject(4, null);
            pstmt.setString(5, version);
            pstmt.setString(6, jurisdiction);
            pstmt.setString(7, languageSelected);
            pstmt.setString(8, consent_status_general);
            pstmt.setString(9, consent_mechanism);
            pstmt.setString(10, ip_address);
            pstmt.setString(11, user_agent);
            pstmt.setString(12, data_point_consents.toJSONString());
            pstmt.executeUpdate();
            created = true;
        }catch(Exception e){
            e.printStackTrace();
        }

        if(created) {
            output.put("_added", true);
            output.put("consent_id",consentuuid.toString());
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
