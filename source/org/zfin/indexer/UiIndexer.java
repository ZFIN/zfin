package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Duration;
import org.zfin.framework.dao.IndexerInfoDAO;
import org.zfin.framework.dao.IndexerRunDAO;
import org.zfin.framework.dao.IndexerTaskDAO;
import org.zfin.properties.ZfinProperties;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateSessionCreator.BATCH_SIZE;
import static org.zfin.repository.RepositoryFactory.getDiseasePageRepository;

@Log4j2
public abstract class UiIndexer<Entity> extends Thread {

    protected UiIndexerConfig indexerConfig;

    protected IndexerRun indexerRun;

    public UiIndexer(UiIndexerConfig indexerConfig) {
        this.indexerConfig = indexerConfig;
        indexerInfo = new IndexerInfo();
        indexerInfo.setName(indexerConfig.getTypeName());
        init();
    }

    public static void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    private String getLogPrefix() {
        return "[" + indexerConfig.getTypeName() + "]";
    }

    private final IndexerInfo indexerInfo;
    private final IndexerInfoDAO infoDAO = new IndexerInfoDAO();
    private final IndexerTaskDAO taskDAO = new IndexerTaskDAO();

    public void runIndex() {
        try {
            IndexerHelper helper = new IndexerHelper();
            helper.startProcess(getLogPrefix() + " 0. Start... ");
            indexerInfo.setIndexerRun(indexerRun);
            logIndexerInfo(infoDAO, true);
            int numberOfRecords = index();
            indexerInfo.setCount(numberOfRecords);
            logIndexerInfo(infoDAO, false);
            helper.addQuickReport(getLogPrefix() + " 0. End");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    private IndexerTask indexerTaskInputOutput;
    private IndexerTask indexerTaskDelete;
    private IndexerTask indexerTaskSave;

    protected Integer index() {
        logIndexerTask(getIndexerTask(IndexerTask.Type.INPUT_OUTPUT), true);
        List<Entity> output = createInputOutput();
        logIndexerTask(getIndexerTask(IndexerTask.Type.INPUT_OUTPUT), false);

        logIndexerTask(getIndexerTask(IndexerTask.Type.DELETE), true);
        cleanUiTables();
        logIndexerTask(getIndexerTask(IndexerTask.Type.DELETE), false);

        logIndexerTask(getIndexerTask(IndexerTask.Type.SAVE), true);
        persistOutput(output);
        logIndexerTask(getIndexerTask(IndexerTask.Type.SAVE), false);
        return output.size();
    }

    private IndexerTask getIndexerTask(IndexerTask.Type type) {
        switch (type) {
            case INPUT_OUTPUT -> {
                if (indexerTaskInputOutput == null) {
                    indexerTaskInputOutput = new IndexerTask();
                    indexerTaskInputOutput.setIndexerInfo(indexerInfo);
                    indexerTaskInputOutput.setName(IndexerTask.Type.INPUT_OUTPUT.name());
                }
                return indexerTaskInputOutput;
            }
            case DELETE -> {
                if (indexerTaskDelete == null) {
                    indexerTaskDelete = new IndexerTask();
                    indexerTaskDelete.setIndexerInfo(indexerInfo);
                    indexerTaskDelete.setName(IndexerTask.Type.DELETE.name());
                }
                return indexerTaskDelete;
            }
            case SAVE -> {
                if (indexerTaskSave == null) {
                    indexerTaskSave = new IndexerTask();
                    indexerTaskSave.setIndexerInfo(indexerInfo);
                    indexerTaskSave.setName(IndexerTask.Type.SAVE.name());
                }
                return indexerTaskSave;
            }
        }
        throw new RuntimeException("No indexer task found for " + type);
    }


    protected abstract List<Entity> inputOutput();

    protected List<Entity> createInputOutput() {
        IndexerHelper helper = new IndexerHelper();
        helper.startProcess(getLogPrefix() + " 1. Start Input / Output... ");
        HibernateUtil.createTransaction();
        List<Entity> outputList = inputOutput();
        HibernateUtil.flushAndCommitCurrentSession();
        helper.addQuickReport(getLogPrefix() + " 1. Finish Input / Output... ", outputList.size());
        return outputList;
    }

    protected void persistOutput(List<Entity> resultList) {
        IndexerHelper helper = new IndexerHelper();
        helper.startProcess(getLogPrefix() + " 2. Start Persistence... ");
        saveRecords(createBatch(resultList));
        helper.addQuickReport(getLogPrefix() + " 2. Finish Persistence... ");
    }

    private Collection<List<Entity>> createBatch(List<Entity> resultList) {
        IndexerHelper helper = new IndexerHelper();
        helper.startProcess();
        int numberOfBatches = resultList.size() / BATCH_SIZE + 1;
        AtomicInteger counter = new AtomicInteger();
        Collection<List<Entity>> batchedList = resultList.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numberOfBatches)).values();
        helper.addQuickReport(getLogPrefix() + " Create batches: " + batchedList.size());
        return batchedList;
    }

    protected abstract void cleanUiTables();

    protected void saveRecords(Collection<List<Entity>> batchedList) {
        if (this.getClass().equals(UiIndexerConfig.PublicationExpressionIndexer.getIndexClazz()) ||
            this.getClass().equals(UiIndexerConfig.TermPhenotypeIndexer.getIndexClazz()) ||
            this.getClass().equals(UiIndexerConfig.ChebiPhenotypeIndexer.getIndexClazz()) ||
            this.getClass().equals(UiIndexerConfig.GenesInvolvedIndexer.getIndexClazz())) {
            saveWithStatefulSession(batchedList);
        } else {
            saveWithStatelessSession(batchedList);
        }
    }

    private void saveWithStatefulSession(Collection<List<Entity>> batchedList) {
        HibernateUtil.createTransaction();
        batchedList.forEach(batch -> {
            for (Entity entity : batch) {
                HibernateUtil.currentSession().save(entity);
            }
        });
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void saveWithStatelessSession(Collection<List<Entity>> batchedList) {
        HibernateUtil.createStatelessTransaction();
        batchedList.forEach(batch -> {
            for (Entity entity : batch) {
                insertEntityGraph(HibernateUtil.currentStatelessSession(), entity);
            }
        });
        HibernateUtil.flushAndCommitCurrentStatelessSession();
    }

    protected void insertEntityGraph(StatelessSession session, Entity entity) {
        session.insert(entity);
    }

    protected void cleanoutTable(String schema, String... tables) {
        List<String> tableNames = Arrays.stream(tables).map(String::toLowerCase).map(s -> schema + "." + s).toList();
        log.info(getLogPrefix() + " Cleaning out tables: ");
        tableNames.forEach(table -> {
            HibernateUtil.createTransaction();
            getDiseasePageRepository().deleteUiTables(table);
            HibernateUtil.flushAndCommitCurrentSession();
        });
    }

    public String calculateRequestDuration(LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = new Duration(startTime, endTime);
        return duration.toString();
    }

    public static void main(String[] args) throws NoSuchFieldException {

        UiIndexer.init();
        List<String> argumentSet = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            argumentSet.add(args[i]);
            log.info("Args[" + i + "]: " + args[i]);
        }

        Map<String, UiIndexer<?>> indexers = new LinkedHashMap<>();
        for (UiIndexerConfig uic : UiIndexerConfig.getAllIndexerSorted()) {
            try {
                UiIndexer<?> i = (UiIndexer<?>) uic.getIndexClazz().getDeclaredConstructor(UiIndexerConfig.class).newInstance(uic);
                indexers.put(uic.getTypeName(), i);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }

        IndexerRunDAO indexerRunDAO = new IndexerRunDAO();
        IndexerRun indexerRun = new IndexerRun();
        logRunIndexer(indexerRun, indexerRunDAO, true);
        boolean isThreadedExecution = false; //is this always false?
        for (String type : indexers.keySet()) {
            if (argumentSet.size() == 0 || argumentSet.contains(type)) {
                if (isThreadedExecution) {
                    indexers.get(type).start();
                } else {
                    UiIndexer<?> uiIndexer = indexers.get(type);
                    uiIndexer.indexerRun = indexerRun;
                    uiIndexer.runIndex();
                }
            } else {
                log.info("Not Starting indexer: " + type);
            }
        }
        logRunIndexer(indexerRun, indexerRunDAO, false);

        log.info("Finished UI Indexing");
    }

    private static void logRunIndexer(IndexerRun indexerRun, IndexerRunDAO indexerRunDAO, Boolean isStart) {
        if (isStart) {
            indexerRun.setStartDate(LocalDateTime.now());
        } else {
            indexerRun.setEndDate(LocalDateTime.now());
            indexerRun.setDuration(java.time.Duration.between(indexerRun.getStartDate(), indexerRun.getEndDate()).toSeconds());
        }
        HibernateUtil.createTransaction();
        indexerRunDAO.entityManager = HibernateUtil.currentSession();
        indexerRunDAO.persist(indexerRun);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void logIndexerInfo(IndexerInfoDAO indexerInfoDAO, Boolean isStart) {
        if (isStart) {
            indexerInfo.setStartDate(LocalDateTime.now());
        } else {
            indexerInfo.setDuration(java.time.Duration.between(indexerInfo.getStartDate(), LocalDateTime.now()).toSeconds());
        }
        HibernateUtil.createTransaction();
        indexerInfoDAO.entityManager = HibernateUtil.currentSession();
        indexerInfoDAO.persist(indexerInfo);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void logIndexerTask(IndexerTask indexerTask, Boolean isStart) {
        if (isStart) {
            indexerTask.setStartDate(LocalDateTime.now());
        } else {
            indexerTask.setDuration(java.time.Duration.between(indexerTask.getStartDate(), LocalDateTime.now()).toSeconds());
        }
        HibernateUtil.createTransaction();
        taskDAO.entityManager = HibernateUtil.currentSession();
        taskDAO.persist(indexerTask);
        HibernateUtil.flushAndCommitCurrentSession();
    }


}
