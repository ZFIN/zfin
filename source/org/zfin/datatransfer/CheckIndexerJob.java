package org.zfin.datatransfer;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.zfin.alliancegenome.JacksonObjectMapperFactoryZFIN;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.search.CheckIndexerRESTInterface;
import org.zfin.search.IndexerStatus;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.RestProxyFactory;

@Log4j2
public class CheckIndexerJob extends AbstractValidateDataReportTask {

    public CheckIndexerJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @SneakyThrows
    @Override
    public int execute() {

        while (true) {
            IndexerStatus status = getIndexStatus();

            if (status != null && status.getStatusMessages() != null) {
                if (status.getStatusMessages().getTimeElapsed() != null)
                    log.info("Elapsed Time: " + status.getStatusMessages().getTimeElapsed());
            }
            if (status == null || !status.getStatus().equals(IndexerStatus.Status.BUSY.name().toLowerCase())) {
                log.info("Total Time: " + status.getStatusMessages().getTimeTaken());
                System.exit(0);
            }
            Thread.sleep(60 * 1000);
        }
    }

    private static final ClientConfig config = new ClientConfig();

    static {
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
    }

    private IndexerStatus getIndexStatus() {
        String baseUrl = "http://localhost:" + ZfinPropertiesEnum.SOLR_PORT;
        log.debug(baseUrl);
        CheckIndexerRESTInterface api = RestProxyFactory.createProxy(CheckIndexerRESTInterface.class, baseUrl, config);
        IndexerStatus response = null;
        try {
            response = api.getIndexerStatus(ZfinPropertiesEnum.SOLR_CORE.value());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not check the Solr index: " + message);
        }
        return response;
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(log);
        System.out.println(args[0]);
        System.out.println(args[1]);
        CheckIndexerJob job = new CheckIndexerJob("CheckIndexer", args[0], args[1]);
        System.exit(job.execute());
    }
}
