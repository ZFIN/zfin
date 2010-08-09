package org.zfin.framework;

import org.apache.log4j.Logger;
import org.zfin.ontology.OntologyManager;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;

/**
 * Background Servlet that should not be called directly.
 * It loads all used ontologies into memory ({@link org.zfin.ontology.OntologyManager} class) upon
 * startup. Typically, this should be done from a serialized file to speed up versus re-loading them
 * from the TERM table. Syncing with the TERM table is done via a Quartz cron job run nightly during the week
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
        ReadOntologiesThread thread = new ReadOntologiesThread();
        thread.start();
        LOG.info("Ontology Manager Thread started: ");

    }

    private void reLoadFormDatabase() {
        try {
            OntologyManager.getInstance(OntologyManager.LoadingMode.DATABASE);
        } catch (Exception e) {
            LOG.error("Problem during re-loading ontologies from database", e);
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
                LOG.warn("Problem loading serialized file. Loading ontologies from database...",e);
                reLoadFormDatabase();
            }
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