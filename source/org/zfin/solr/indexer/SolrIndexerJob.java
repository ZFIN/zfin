package org.zfin.solr.indexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Driver for ad-hoc runs of the SolrJ-based replacements for DIH entities.
 * The migrated entities live in {@link IndexerRegistry}; this job runs
 * either all of them or a subset chosen via {@code -PsolrEntities=...}.
 *
 * <p>For nightly full reindexes — which still need DIH for the entities
 * not yet migrated — see {@link SolrReindexOrchestrator} instead.
 *
 * <p>The job runs through each requested indexer in order, then issues a
 * single hard commit at the end. Each indexer is responsible for its own
 * Hibernate-backed read of the source data and SolrJ-backed write of the
 * resulting documents; the driver owns the lifecycle and the final commit.
 *
 * <p>Invoke via the {@code solrIndex} gradle task. Optionally pass
 * {@code -PsolrEntities=lab,company} to limit the run to a subset.
 */
public class SolrIndexerJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(SolrIndexerJob.class);

    private String requestedEntities; // null = all

    public SolrIndexerJob(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        SolrIndexerJob job = new SolrIndexerJob(args[2], args[0], args[1]);
        // args[3] is optional: comma-separated entity-name list. The gradle
        // task passes the empty string when -PsolrEntities isn't provided,
        // which we treat as "all".
        if (args.length > 3 && !args[3].isBlank()) job.requestedEntities = args[3];
        job.initDatabase(true);
        System.exit(job.execute());
    }

    @Override
    public int execute() {
        setLoggerFile();
        clearReportDirectory();

        List<SolrIndexer> toRun = pickIndexers();
        if (toRun.isEmpty()) {
            logger.error("No indexers selected (requested='{}'). Available: {}",
                requestedEntities, IndexerRegistry.names());
            return 2;
        }

        SolrClient solr = null;
        try {
            solr = newSolrClient();
            for (SolrIndexer indexer : toRun) {
                logger.info("=== running indexer: {} ===", indexer.name());
                long t0 = System.currentTimeMillis();
                indexer.index(solr);
                logger.info("=== indexer {} done in {} ms ===",
                    indexer.name(), System.currentTimeMillis() - t0);
            }
            logger.info("committing");
            solr.commit();
            return 0;
        } catch (Exception e) {
            logger.error("indexer job failed", e);
            return 1;
        } finally {
            if (solr != null) {
                try { solr.close(); } catch (Exception ignored) { }
            }
            HibernateUtil.closeSession();
        }
    }

    private List<SolrIndexer> pickIndexers() {
        if (requestedEntities == null) return List.copyOf(IndexerRegistry.all());
        var picked = new ArrayList<SolrIndexer>();
        for (String name : Arrays.stream(requestedEntities.split(",")).map(String::trim).toList()) {
            SolrIndexer i = IndexerRegistry.get(name);
            if (i == null) {
                logger.warn("unknown indexer '{}' — skipping. known: {}", name, IndexerRegistry.names());
                continue;
            }
            picked.add(i);
        }
        return picked;
    }

    /**
     * Build the Solr URL the same way {@code SolrService} does — from the
     * SOLR_HOST / SOLR_PORT / SOLR_CONTEXT / SOLR_CORE properties — so we
     * end up talking to the same core the rest of the app reads from.
     * HttpSolrClient is deprecated in SolrJ 9 but {@code SolrService} still
     * uses it; align with that until the whole codebase moves to Http2.
     */
    private SolrClient newSolrClient() {
        String url = "http://"
            + ZfinPropertiesEnum.SOLR_HOST.value()  + ":"
            + ZfinPropertiesEnum.SOLR_PORT.value()  + "/"
            + ZfinPropertiesEnum.SOLR_CONTEXT.value() + "/"
            + ZfinPropertiesEnum.SOLR_CORE.value()  + "/";
        logger.info("Solr endpoint: {}", url);
        return new HttpSolrClient.Builder(url).build();
    }
}
