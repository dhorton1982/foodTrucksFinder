package com.hortonsoft.foodtruck;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;

public class Launcher {

    /**
     * The Jersey servlet name.
     */
    private static final String JERSEY_SERVLET_NAME = "jersey-container-servlet";

    /**
     * The entry point into the application.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new Launcher().start();
    }

    /**
     * Starts the embedded Tomcat server.
     * 
     * @throws Exception
     */
    void start() throws Exception {

        String port = System.getenv("PORT");
        if (port == null || port.isEmpty()) {
            port = "8080";
        }

        String contextPath = "";
        String appBase = ".";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.valueOf(port));
        tomcat.getHost().setAppBase(appBase);

        Context context = tomcat.addContext(contextPath, appBase);
        Tomcat.addServlet(context, JERSEY_SERVLET_NAME,
                new ServletContainer(new JerseyConfiguration()));
        context.addServletMappingDecoded("/api/*", JERSEY_SERVLET_NAME);

        tomcat.start();
        tomcat.getServer().await();
    }
}
