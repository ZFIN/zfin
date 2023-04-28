package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Duration;
import org.zfin.ontology.OmimPhenotypeDisplay;
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

    private String getLogPrefix() {
        return "[" + indexerConfig.getTypeName() + "]";
    }

    public void runIndex() {
        try {
            IndexerHelper helper = new IndexerHelper();
            helper.startProcess(getLogPrefix() + " 0. Start... ");
            index();
            helper.addQuickReport(getLogPrefix() + " 0. End");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.exit(-1);
        }
    }

    protected void index(){
        List<Entity> output = createInputOutput();
        cleanUiTables();
        persistOutput(output);
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
        helper.addQuickReport(getLogPrefix() + " Create batches: ");
        return batchedList;
    }

    protected abstract void cleanUiTables();

    protected void saveRecords(Collection<List<Entity>> batchedList){
        HibernateUtil.createTransaction();
        batchedList.forEach(batch -> {
            for (Entity entity : batch) {
                HibernateUtil.currentSession().save(entity);
            }
            HibernateUtil.currentSession().flush();
        });
        HibernateUtil.flushAndCommitCurrentSession();
    }

    protected void cleanoutTable(String... tables) {
        List<String> tableNames = Arrays.stream(tables).map(String::toLowerCase).toList();
        log.info(getLogPrefix()+" Cleaning out tables: " );
        tableNames.forEach(table -> {
            HibernateUtil.createTransaction();
            getDiseasePageRepository().deleteUiTables(table);
            HibernateUtil.flushAndCommitCurrentSession();
        });
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

        args = new String[6];
        args[0] = "ChebiPhenotype";
        args[1] = "GenesInvolved";
        args[2] = "FishModel";
        args[3] = "ChebiFishModel";
        args[4] = "PublicationExpression";
        args[5] = "TermPhenotype";
        Set<String> argumentSet = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            argumentSet.add(args[i]);
            log.info("Args[" + i + "]: " + args[i]);
        }

        HashMap<String, UiIndexer<?>> indexers = new HashMap<>();
        for (UiIndexerConfig uic : UiIndexerConfig.getAllIndexerSorted()) {
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
            if (argumentSet.size() == 0 || argumentSet.contains(type)) {
                if (isThreadedExecution) {
                    indexers.get(type).start();
                } else {
                    indexers.get(type).runIndex();
                }
            } else {
                log.info("Not Starting indexer: " + type);
            }
        }

        log.info("Finished UI Indexing");
    }


}
