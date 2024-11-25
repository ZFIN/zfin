package org.zfin.datatransfer;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    //check every 60 seconds
    public static final int LOOP_TIME_IN_MILLISECONDS = 60 * 1000;

    //wait for maximum of 300 times through the loop (300 minutes if loop time is 60 seconds)
    public static final int MAX_LOOP_COUNT = 300;


    public CheckIndexerJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @SneakyThrows
    @Override
    public int execute() {

        for (int i = 0; i < MAX_LOOP_COUNT; i++) {
            IndexerStatus status = getIndexStatus();

            if (status == null) {
                log.info("Could not check the Solr index. Solr Indexer is not available.");
                System.exit(1);
            } else if (status.getStatusMessages() != null) {
                if (status.getStatusMessages().getTimeElapsed() != null) {
                    log.info("Elapsed Time: " + status.getStatusMessages().getTimeElapsed());
                } else {
                    log.info("Unexpected Behavior. Elapsed Time Missing. Status Object:");
                    log.info(new ObjectMapper().writeValueAsString(status));
                }
            } else {
                log.info("Unexpected Behavior. Status Object:");
                log.info(new ObjectMapper().writeValueAsString(status));
            }

            if (!status.getStatus().equals(IndexerStatus.Status.BUSY.name().toLowerCase())) {
                log.info("Total Time: " + status.getStatusMessages().getTimeTaken());
                System.exit(0);
            }

            Thread.sleep(LOOP_TIME_IN_MILLISECONDS);
        }
        log.info("Timeout while checking on solr indexer. Exiting.");
        System.exit(1);
        return 0;
    }

    private static final ClientConfig config = new ClientConfig();

    static {
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
    }

    private IndexerStatus getIndexStatus() {
        String baseUrl = "http://" + ZfinPropertiesEnum.SOLR_HOST + ":" + ZfinPropertiesEnum.SOLR_PORT;
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
