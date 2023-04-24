package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Duration;
import org.zfin.properties.ZfinProperties;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getDiseasePageRepository;

@Log4j2
public abstract class UiIndexer<Entity> extends Thread {

    protected int BATCH_SIZE = 100;

    protected UiIndexerConfig indexerConfig;

    protected IndexerHelper indexerHelper;

    public UiIndexer(UiIndexerConfig indexerConfig) {
        this.indexerConfig = indexerConfig;
        init();
    }

    public void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    public void runIndex() {
        try {
            //display.startProcess(getClass().getSimpleName());
            index();
/*
            display.finishProcess();
            stats.printOutput();
*/
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.exit(-1);
        }
    }

    protected abstract void index();

    protected abstract List<Entity> retrieveRecords();

    protected abstract void cleanUiTables();

    protected abstract void saveRecords(List<Entity> entities);

    protected void cleanoutTable(String... tables) {
        startTransaction(null);
        log.info("Cleaning out table: " + String.join(",", tables));
        getDiseasePageRepository().deleteUiTables(tables);
        commitTransaction(null);
    }

    protected void commitTransaction(String message) {
        HibernateUtil.flushAndCommitCurrentSession();
        if (message != null)
            log.info(message);
    }

    protected void commitTransaction(String message, Integer numberOfRecords) {
        HibernateUtil.flushAndCommitCurrentSession();
        indexerHelper.addQuickReport(message, numberOfRecords);
    }

    protected void startTransaction(String message) {
        indexerHelper.startProcess(message);
        HibernateUtil.createTransaction();
    }

    public String calculateRequestDuration(LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = new Duration(startTime, endTime);
        return duration.toString();
    }

    public static void main(String[] args) throws NoSuchFieldException {
        HashMap<String, UiIndexer<?>> indexers = new HashMap<>();
        for (UiIndexerConfig uic : UiIndexerConfig.values()) {
            try {
                UiIndexer<?> i = (UiIndexer<?>) uic.getIndexClazz().getDeclaredConstructor(UiIndexerConfig.class).newInstance(uic);
                indexers.put(uic.getTypeName(), i);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
                System.exit(-1);
            }
        }

        boolean isThreadedExecution = false;
        for (String type : indexers.keySet()) {
            if (isThreadedExecution) {
                log.info("Starting in threaded mode for: " + type);
                indexers.get(type).start();
            } else {
                log.info("Starting indexer sequentially: " + type);
                indexers.get(type).runIndex();
            }
        }

        log.info("Finished UI Indexing");
    }


}
