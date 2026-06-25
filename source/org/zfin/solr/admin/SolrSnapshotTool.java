package org.zfin.solr.admin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;

/**
 * CLI for Solr admin snapshot operations — {@code backup} and {@code restore}
 * via the replication handler. Java successor to the {@code solr-backup.groovy}
 * / {@code solr-restore.groovy} scripts; all the actual work lives in
 * {@link SolrAdminClient}, shared with {@link org.zfin.solr.indexer.SolrReindexOrchestrator}.
 *
 * <p>Invoked via gradle (which supplies the runtime classpath):
 * <pre>
 *   gradle solrBackup  -PsolrBackupLocation=&lt;dir&gt; -PsolrBackupName=&lt;stamp&gt;
 *   gradle solrRestore -PsolrBackupLocation=&lt;dir&gt; -PsolrIndex=&lt;name&gt;
 * </pre>
 *
 * <p>The gradle tasks pass the standard {@link AbstractValidateDataReportTask}
 * triple (properties path, data dir, job name), then {@code subcommand},
 * {@code location}, and {@code name} as {@code args[3..5]}. Endpoint
 * (SOLR/CORE) comes from env vars or {@link org.zfin.properties.ZfinPropertiesEnum}
 * via {@link SolrAdminClient#fromEnvOrProperties()}.
 */
public class SolrSnapshotTool extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(SolrSnapshotTool.class);

    private String subcommand;
    private String location;
    private String name;

    public SolrSnapshotTool(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        var job = new SolrSnapshotTool(args[2], args[0], args[1]);
        job.subcommand = args.length > 3 ? args[3].trim() : "";
        job.location   = args.length > 4 ? args[4].trim() : "";
        job.name       = args.length > 5 ? args[5].trim() : "";
        job.initDatabase(false);   // Solr-only tool — no Hibernate session needed
        System.exit(job.execute());
    }

    @Override
    public int execute() {
        if (location.isBlank() || name.isBlank()) {
            logger.error("usage: <backup|restore> <location> <name> "
                + "(got subcommand='{}', location='{}', name='{}')", subcommand, location, name);
            return 1;
        }
        try {
            SolrAdminClient solr = SolrAdminClient.fromEnvOrProperties();
            switch (subcommand) {
                case "backup"  -> solr.backup(location, name);
                case "restore" -> solr.restore(location, name);
                default -> {
                    logger.error("unknown subcommand '{}' (expected 'backup' or 'restore')", subcommand);
                    return 1;
                }
            }
            return 0;
        } catch (Exception e) {
            logger.error("{} failed", subcommand, e);
            return 1;
        }
    }
}
