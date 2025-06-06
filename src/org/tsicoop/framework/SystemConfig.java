package org.tsicoop.framework;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;


public class SystemConfig {
    private static Properties appConfig;
    private static Properties schemaConfig;
    private static Properties processorConfig;

    private static JSONObject bre;

    public static void load(ServletContext ctx) {
        if (appConfig == null) {
            appConfig = new Properties();
            try {
                appConfig.load(ctx.getResourceAsStream("/WEB-INF/_config.tsi"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Loaded _config.tsi");
        }

       if (processorConfig == null) {
            processorConfig = new Properties();
            try {
                processorConfig.load(ctx.getResourceAsStream("/WEB-INF/_processor.tsi"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Loaded _processor.tsi");
        }
    }

    public static Properties getAppConfig() {
        return appConfig;
    }
    public static Properties getSchema() { return schemaConfig;}

    public static Properties getProcessorConfig(){
        return processorConfig;
    }

    public static JSONObject getBRE(){
        return bre;
    }
}
