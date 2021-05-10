package org.zfin.framework;

import org.hibernate.stat.Statistics;
import org.springframework.web.servlet.DispatcherServlet;
import org.zfin.ontology.RelationshipDisplayNames;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.blast.WebHostDatabaseStatisticsCache;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * Master Servlet that controls each request and response.
 * 1) Initialize Hibernate
 * 2) Initialize ZFIN properties
 * 3) Runs db dictionary consistency tests
 * <p/>
 * ToDo: the different initializations could be done in a better way by registering
 * plug-in classes whose init() methods all get called. Inherit from
 * PlugIn class.
 */
public class ZfinActionServlet extends DispatcherServlet {

    /**
     * This method is called the first time this servlet is instantiated.
     *
     * @param config ServletConfig
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String webRoot = getServletContext().getRealPath("/");
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(webRoot);
        initDatabase();
        if (Boolean.valueOf(ZfinPropertiesEnum.BLAST_CACHE_AT_STARTUP.value())) {
            initBlast();
        }
        initRelationshipDisplayNames();
    }

    private void initRelationshipDisplayNames() {
        new RelationshipDisplayNames();
    }

    private void initBlast() {
        Thread t = new Thread(() -> {
            WebHostDatabaseStatisticsCache.getInstance().cacheAll();
            HibernateUtil.closeSession();
        });
        t.start();
    }

    private void initDatabase() {
        // initialize Hibernate
        HibernateUtil.init();
        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
    }


}
