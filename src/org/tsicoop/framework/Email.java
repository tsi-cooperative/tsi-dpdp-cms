package org.tsicoop.framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tsicoop.common.Constants;

import java.security.SecureRandom;

public class Email {

    public static String API_HOST = "https://api.zeptomail.com/v1.1/email";
    public static String AUTH_KEY = "Zoho-enczapikey wSsVR61/+xT0WK11zjb/ce0wmwlcBFnxR0l+3wSh4nWvHPnB9MdtkUzOBgeuGqVNGW9oEDpErLkrkB4AhDNYh4glzlBSCiiF9mqRe1U4J3x17qnvhDzDXGhVlxqLK4INww5om2lnG8gl+g==";

    public static void sendEmail(String apihost, String authorization, String email, String name, String subject, String content) throws Exception {
        HttpClient obj = new HttpClient();
        JSONObject test = new JSONObject();
        JSONObject fromOb = new JSONObject();
        fromOb.put("address","noreply@tsicoop.org");
        fromOb.put("name","TSI Coop");
        test.put("from",fromOb);
        JSONObject address = new JSONObject();
        address.put("address",email);
        address.put("name",name);
        JSONObject toAddress = new JSONObject();
        toAddress.put("email_address",address);
        JSONArray toArray = new JSONArray();
        toArray.add(toAddress);
        test.put("to",toArray);
        test.put("subject",subject);
        test.put("htmlbody",content);
        //System.out.println(test);
        JSONObject output = obj.sendPost(apihost,authorization,test);
        //System.out.println(output);
    }

    public static void sendOTP(String email, String otp){
        if(System.getenv("TSI_COOP_ENV") != null && System.getenv("TSI_COOP_ENV").equalsIgnoreCase(Constants.PRODUCTION_ENVT)) {
            String subject = "Your Login OTP";
            StringBuffer buff = new StringBuffer();
            buff.append("<p>The OTP for logging into your TSI Coop account is "+otp+". It is valid for 5 minutes.</p>");
            buff.append("<p>Please do not share this OTP with anyone.</p>");
            buff.append("<p>Warm Regards<br/>TSI Coop Team</p>");
            String content = buff.toString();
            try {
                new Email().sendEmail(API_HOST,
                        AUTH_KEY,
                        email,
                        "",
                        subject,
                        content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String generate4DigitOTP() {
        String otpS = null;
        if(System.getenv("TSI_COOP_ENV") != null && System.getenv("TSI_COOP_ENV").equalsIgnoreCase(Constants.PRODUCTION_ENVT)) {
            SecureRandom random = new SecureRandom();
            int otp = 1000 + random.nextInt(9000); // Generates a number between 1000 (inclusive) and 9999 (inclusive)
            otpS = String.valueOf(otp);
        }else{
            otpS = "1234";
        }
        return otpS;
    }

    public static void main(String[] args){
        String apihost = "https://api.zeptomail.com/v1.1/email";
        String authorization = "Zoho-enczapikey wSsVR61/+xT0WK11zjb/ce0wmwlcBFnxR0l+3wSh4nWvHPnB9MdtkUzOBgeuGqVNGW9oEDpErLkrkB4AhDNYh4glzlBSCiiF9mqRe1U4J3x17qnvhDzDXGhVlxqLK4INww5om2lnG8gl+g==";
        String email = "satish@tsiconsulting.in";
        String name = "Satish";
        String subject = "Your Login Password";
        String content = "<div><b> Test email sent successfully. </b></div>";
        try{
        new Email().sendEmail(  apihost,
                                authorization,
                                email,
                                name,
                                subject,
                                content);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
