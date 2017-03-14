package org.zfin.framework;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;
import org.zfin.ontology.OntologyDataManager;
import org.zfin.ontology.OntologyManager;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Background Servlet that should not be called directly.
 * It loads all used ontologies into memory ({@link org.zfin.ontology.OntologyManager} class) upon
 * startup. Typically, this should be done from a serialized file to speed up versus re-loading them
 * from the TERM table. Syncing with the TERM table is done via Jenkins jobs that run nightly during the week
 */
public class OntologyManagerServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(OntologyManagerServlet.class);

    /**
     * Initialize this servlet.
     * Re-load the ontologies from a serialized file if it exist,
     * otherwise, re-load from the TERM table (which takes much longer).
     */
    @Override
    public void init() throws ServletException {

        // Verify that we were not accessed using the invoker servlet
        String servletName = getServletConfig().getServletName();
        if (servletName == null)
            servletName = "";
        if (servletName.startsWith("org.apache.catalina.INVOKER."))
            throw new UnavailableException("Called through Invoker Servlet");
        if (true == Boolean.valueOf(ZfinPropertiesEnum.LOAD_ONTOLOGIES_AT_STARTUP.toString())) {
            ReadOntologiesThread thread = new ReadOntologiesThread();
            thread.start();
        }
        LOG.info("Ontology Manager Thread started: ");
        WatchOntologyDirectoryThread watchThread = new WatchOntologyDirectoryThread();
        watchThread.start();
        MonitorDbConnectionPoolThread monitor = new MonitorDbConnectionPoolThread();
        monitor.start();

    }

    private void reLoadFormDatabase() {
        try {
            OntologyManager.getInstance(OntologyManager.LoadingMode.DATABASE);
        } catch (Exception e) {
            LOG.error("Problem during re-loading ontologies from database", e);
        }
    }

    private class MonitorDbConnectionPoolThread extends Thread {

        /**
         * Reload the ontology cache: OntologyManager
         */
        public MonitorDbConnectionPoolThread() {
            super("Monitor DB Connection Pool Thread");
            setDaemon(true);
        }

        @Override
        public void run() {
            InitialContext ictx = null;
            try {
                ictx = new InitialContext();
                ComboPooledDataSource pds = (ComboPooledDataSource) ictx.lookup("java:comp/env/jdbc/zfin");

                File outputFile = new File(FileUtil.getTomcatDataTransferDirectory(), "db-connection-pool-monitor.txt");

                // write the connection pool info every 10 seconds into an output file.
                while (true) {
                    // re-create the output file in case it does not exist or was removed.
                    if (!outputFile.exists()) {
                        outputFile.createNewFile();
                        String header = "Date and Time, Max Pool Size, Min Pool Size, Total in Pool, Num in use, Num idle";
                        header += System.lineSeparator();
                        Files.write(Paths.get(outputFile.getAbsolutePath()), header.getBytes(), StandardOpenOption.APPEND);
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String outputString = dateFormat.format(date);
                    outputString += ", ";
                    outputString += pds.getMaxPoolSize();
                    outputString += ", ";
                    outputString += pds.getMinPoolSize();
                    outputString += ", ";
                    outputString += pds.getNumConnectionsDefaultUser();
                    outputString += ", ";
                    outputString += pds.getNumBusyConnectionsDefaultUser();
                    outputString += ", ";
                    outputString += pds.getNumIdleConnectionsDefaultUser();
                    outputString += System.lineSeparator();
                    Files.write(Paths.get(outputFile.getAbsolutePath()), outputString.getBytes(), StandardOpenOption.APPEND);
                    Thread.sleep(10000);
                }
            } catch (NamingException | IOException | SQLException e) {
                LOG.error(e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class WatchOntologyDirectoryThread extends Thread {

        /**
         * Reload the ontology cache: OntologyManager
         */
        public WatchOntologyDirectoryThread() {
            super("Watch Ontology Reload Thread");
            setDaemon(true);
        }

        @Override
        public void run() {
            Path dir = ZfinProperties.getOntologyReloadStatusDirectory();
            File file = dir.toFile();
            if (!file.exists())
                file.mkdir();
            try {
                new WatchOntologyRefresh(dir).processEvents();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Thread that does the ontology loading.
     */
    private class ReadOntologiesThread extends Thread {

        /**
         * Reload the ontology cache: OntologyManager
         */
        public ReadOntologiesThread() {
            super("ZFIN Ontology Reload Thread");
            setDaemon(true);
        }

        @Override
        public void run() {
            // check if serialized file exists.
            try {
                OntologyManager.getInstance(OntologyManager.LoadingMode.SERIALIZED_FILE);
            } catch (Exception e) {
                LOG.warn("Problem loading serialized file. Loading ontologies from database...", e);
                reLoadFormDatabase();
            }
            // initialize ontology data manager
            // has to happen after the ontology manager is fully loaded.
            OntologyDataManager.getInstance();
            HibernateUtil.closeSession();
        }
    }

    /**
     * Nothing needs to be done.
     * Whenever the ontologies are loaded through the database a new serialized file is produced at that time.
     * Updating the ontologies through the nightly job will also update the file image.
     */
    @Override
    public void destroy() {
        LOG.info("Ontology Servlet is decomissioned.");
    }

}