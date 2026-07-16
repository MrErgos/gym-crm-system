package io.github.mrergos.gymcrm;


import io.github.mrergos.gymcrm.config.Config;
import io.github.mrergos.gymcrm.logging.TransactionLoggingFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class GymCrmSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(GymCrmSystemApplication.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM System application...");

        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(8080);
            tomcat.getConnector();

            String contextPath = "";
            String docBasePath = new File(".").getAbsolutePath();

            Context tomcatCtx = tomcat.addContext(contextPath, docBasePath);

            context.register(Config.class);

            context.setServletContext(tomcatCtx.getServletContext());

            DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

            Tomcat.addServlet(tomcatCtx, "dispatcherServlet", dispatcherServlet);
            tomcatCtx.addServletMappingDecoded("/*", "dispatcherServlet");

            context.refresh();

            registerFilter(tomcatCtx, "characterEncodingFilter", context.getBean(CharacterEncodingFilter.class));
            registerFilter(tomcatCtx, "transactionLoggingFilter", context.getBean(TransactionLoggingFilter.class));

            tomcat.start();
            tomcat.getServer().await();

            log.info("Gym CRM application finished successfully.");
        } catch (LifecycleException e) {
            log.error("Start error.", e);
        }
    }

    private static void registerFilter(Context tomcatCtx, String name, Filter filter) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(name);
        filterDef.setFilter(filter);
        tomcatCtx.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(name);
        filterMap.addURLPatternDecoded("/*");
        filterMap.setDispatcher(DispatcherType.REQUEST.name());
        tomcatCtx.addFilterMap(filterMap);
    }
}
