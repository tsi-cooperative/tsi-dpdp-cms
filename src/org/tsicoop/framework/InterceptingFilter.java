package org.tsicoop.framework;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

public class InterceptingFilter implements Filter {

    private static final String URL_DELIMITER = "/";
    private static final String ADMIN_URI = "admin";
    private static final String APP_URI = "app";

    private static final HashMap<String, String> filterConfig = new HashMap<String, String>();
    @Override
    public void destroy() {
        // Any cleanup of resources
    }

    static {
        //log.info("Logger inits");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //System.out.println("Inside controller");
        String responseJson = "";
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String method = req.getMethod();
        String servletPath = req.getServletPath();
        String uri = req.getRequestURI();
        String classname = null;
        String operation = null;
        Properties apiRegistry = null;
        Properties config = null;
        boolean validrequest = true;
        boolean validheader = true;

        // set response header
        /*String origin = req.getHeader("Origin");
        if if (origin != null && (origin.contains("localhost"))) {
            res.setHeader("Access-Control-Allow-Origin", origin); // Echo back the origin
            res.setHeader("Access-Control-Allow-Credentials", "true"); // Allow credentials
        }*/
    /*    res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Echo back the origin
        res.setHeader("Access-Control-Allow-Credentials", "true"); // Allow credentials
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, Access-Control-Allow-Headers, access-control-allow-origin, access-control-allow-credentials, access-control-allow-methods");
        res.setHeader("Access-Control-Max-Age", "3600");*/
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");

        apiRegistry = SystemConfig.getProcessorConfig();
        config = SystemConfig.getAppConfig();

        if (apiRegistry.containsKey(servletPath.trim())) {
            StringTokenizer strTok = new StringTokenizer(servletPath, URL_DELIMITER);
 //           strTok.nextToken(); // skip api keyword
            String uriIdentifier = strTok.nextToken();
         /*   if (!(uriIdentifier.equalsIgnoreCase(ADMIN_URI)||uriIdentifier.equalsIgnoreCase(APP_URI))){
                res.sendError(400);
                return;
            }*/

            // Check
             try {
                 /*if(!servletPath.contains("api/app/login")
                         && !servletPath.contains("api/app/register")) {
                     validheader = InputProcessor.processHeader(req, res);
                 }*/
                 if(!validheader) {
                     res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                 }else{
                     InputProcessor.processInput(req, res);
                     operation = strTok.nextToken();
                     classname = apiRegistry.getProperty(servletPath.trim());
                     //System.out.println("operation:" + operation + " classname:" + classname);
                     if (classname == null || method == null) res.sendError(400);

                     //System.out.println("c:"+req.getParameter(Constants.NOTIF_PARAM));
                     // Check notification
                   /*  if(req.getParameter(Constants.NOTIF_PARAM)!=null){
                         String notifuuid = req.getParameter(Constants.NOTIF_PARAM);
                         new Notification().readNotification(notifuuid);
                     }*/

                     REST action = ((REST) Class.forName(classname).getConstructor().newInstance());
                     validrequest = action.validate(method, req, res);
                     //System.out.println("validrequest:" + validrequest);
                     if (validrequest) {
                         if (method.equalsIgnoreCase("GET")) {
                             res.setContentType("application/json");
                             action.get(req, res);
                         } else if (method.equalsIgnoreCase("POST")) {
                             res.setContentType("application/json");
                             action.post(req, res);
                         } else if (method.equalsIgnoreCase("PUT")) {
                             res.setContentType("application/json");
                             action.put(req, res);
                         } else if (method.equalsIgnoreCase("DELETE")) {
                             res.setContentType("application/json");
                             action.delete(req, res);
                         } else {
                             res.sendError(400);
                         }
                     }
                 }
            } catch (Exception e) {
                e.printStackTrace();
                res.sendError(400);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        SystemConfig.load(filterConfig.getServletContext());
        JSONSchemaValidator.createInstance(filterConfig.getServletContext());
        System.out.println("Loaded Masters");
        System.out.println("TSI DPDP CMS Service started in "+System.getenv("TSI_COOP_ENV")+" environment");
    }
}
