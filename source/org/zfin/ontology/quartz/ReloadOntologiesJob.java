package org.zfin.ontology.quartz;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.ontology.OntologyManager;

/**
 * Re-load the ontologies into memory.
 */
public class ReloadOntologiesJob implements Job {

    private static final Logger LOG = Logger.getLogger(ReloadOntologiesJob.class);
    private static final double MILLISECONDS = 1000.0;

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        try {
            OntologyManager ontologyManager = OntologyManager.getInstance();
            ontologyManager.reLoadOntologies();
        } catch (Exception e) {
            LOG.error("Error while reloading the ontologies into Java Memory", e);
        }
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS;
        LOG.info("Finished re-loading all ontologies in Quartz Job: Took " + loadingTimeInSeconds + " seconds.");
    }
}
