package org.zfin.sequence.gff;

import org.zfin.framework.HibernateUtil;
import org.zfin.indexer.IndexerHelper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Gff3NcbiService {
    Gff3NcbiDAO gff3NcbiDAO = new Gff3NcbiDAO();
    static final int BATCH_SIZE = 2000;


    public void saveAll(List<Gff3Ncbi> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        createBatch(records).forEach(this::persistBatch);
    }

    int index = 0;

    private void persistBatch(List<Gff3Ncbi> records) {
        HibernateUtil.createTransaction();
        try {
            System.out.println("Insert batch number: " + index++ + " with size: " + records.size() + " records.");
            for (Gff3Ncbi record : records) {
                Set<Gff3NcbiAttributePair> attributePairs = record.getAttributePairs();
                record.setAttributePairs(attributePairs);
                // Set the bidirectional relationship before persisting
                if (attributePairs != null) {
                    attributePairs.forEach(attributePair -> attributePair.setGff3Ncbi(record));
                }
                // Let Hibernate handle the cascade - only persist the parent
                gff3NcbiDAO.persist(record);
            }
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException("Error saving Gff3Ncbi records", e);
        }
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().clear();
    }

    public static Collection<List<Gff3Ncbi>> createBatch(List<Gff3Ncbi> resultList) {
        IndexerHelper helper = new IndexerHelper();
        helper.startProcess();
        int numberOfBatches = resultList.size() / BATCH_SIZE + 1;
        AtomicInteger counter = new AtomicInteger();
        Collection<List<Gff3Ncbi>> batchedList = resultList.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numberOfBatches)).values();
        helper.addQuickReport(" Batches of size: " + batchedList.size());
        System.out.println("Number of batches: " + numberOfBatches + " of size " + BATCH_SIZE);
        return batchedList;
    }

}

