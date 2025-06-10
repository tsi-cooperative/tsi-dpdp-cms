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

    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR" };

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
                    String ipaddress = getClientIpAddress(req);
                    output = recordConsent(input, ipaddress);
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

    protected JSONObject recordConsent(JSONObject input, String ipaddress){
        JSONObject output = new JSONObject();
        boolean created = false;
        DBQuery query = null;
        String anonUserId = (String) input.get("user_id");
        String fiduciaryId = (String) input.get("fiduciary_id");
        String policyId = (String) input.get("policy_id");
        String version = (String) input.get("policy_version");
        String principal_id = (String) input.get("principal_id");
        String jurisdiction = (String) input.get("jurisdiction");
        String languageSelected = (String) input.get("language_selected");
        String consent_status_general = (String) input.get("consent_status_general");
        String consent_mechanism = (String) input.get("consent_mechanism");
        String user_agent = (String) input.get("user_agent");
        JSONArray data_point_consents = (JSONArray) input.get("data_point_consents");
        UUID consentuuid = UUID.randomUUID();
        try {
            Connection conn = new PoolDB().getConnection();

            /**
             * Step 1: Deactivate previous active consent for this user and fiduciary
             */
            String usql = "update _consent set is_active_consent = FALSE, last_updated_at = NOW() WHERE user_id = ? and fiduciary_id = ? and is_active_consent = TRUE";
            PreparedStatement ustmt = conn.prepareStatement(usql);
            ustmt.setString(1, anonUserId);
            ustmt.setObject(2, UUID.fromString(fiduciaryId));
            ustmt.executeUpdate();

            /**
             * Step 2: Insert the NEW consent record
             */
            String sql = "INSERT INTO _consent (user_id,consent_id,policy_id,fiduciary_id,principal_id,policy_version,jurisdiction,language_selected,consent_status_general,consent_mechanism,ip_address,user_agent,data_point_consents) VALUES (?,?, ?, ?, ?, ?, ?,?, ?, ?, ?::inet, ?, ?::jsonb)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, anonUserId);
            pstmt.setObject(2, consentuuid);
            pstmt.setObject(3, UUID.fromString(policyId));
            pstmt.setObject(4, UUID.fromString(fiduciaryId));
            pstmt.setObject(5, null);
            pstmt.setString(6, version);
            pstmt.setString(7, jurisdiction);
            pstmt.setString(8, languageSelected);
            pstmt.setString(9, consent_status_general);
            pstmt.setString(10, consent_mechanism);
            pstmt.setString(11, ipaddress);
            pstmt.setString(12, user_agent);
            pstmt.setString(13, data_point_consents.toJSONString());
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

    private String getClientIpAddress(HttpServletRequest request) {
       String ip = null;
       for (String header : HEADERS_TO_TRY) {
            ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

       ip =  request.getRemoteAddr();
       if(ip.equalsIgnoreCase("[0:0:0:0:0:0:0:1]")){
           ip = "::1";
       }
       return ip;
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
