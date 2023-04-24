package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.expression.ExpressionResult;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.framework.HibernateUtil;

import javax.persistence.Table;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

@Log4j2
public class PublicationExpressionIndexer extends UiIndexer<ExpressionResult> {

    public PublicationExpressionIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected void index() {
        List<ExpressionResult> records = retrieveRecords();
        cleanUiTables();
        saveRecords(records);
    }

    @Override
    protected List<ExpressionResult> retrieveRecords() {
        indexerHelper = new IndexerHelper();
        startTransaction("Start retrieving publication expression...");
        List<ExpressionResult> expressionResults = getExpressionRepository().getAllExpressionResults();
        commitTransaction("Finished retrieving publication expression: ", expressionResults.size());
        return expressionResults;
    }

    @Override
    protected void cleanUiTables() {
        String expressionPub = ExpressionTableRow.class.getAnnotation(Table.class).name();
        cleanoutTable(expressionPub);
    }

    @Override
    protected void saveRecords(List<ExpressionResult> expressionResults) {
        indexerHelper = new IndexerHelper();
        startTransaction("Start saving publication expression...");
        int numberOfBatches = expressionResults.size() / BATCH_SIZE + 1;
        log.info("Number of batches: " + numberOfBatches);
        AtomicInteger counter = new AtomicInteger();
        Collection<List<ExpressionResult>> batchedList = expressionResults.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numberOfBatches)).values();
        batchedList.forEach(batch -> {
            batch.forEach(expressionResult -> expressionResult.getFigures().forEach(figure -> {
                ExpressionTableRow row = new ExpressionTableRow(expressionResult);
                row.setFigure(figure);
                row.setPublication(expressionResult.getExpressionExperiment().getPublication());
                HibernateUtil.currentSession().save(row);
            }));
            HibernateUtil.currentSession().flush();
        });
        commitTransaction("Finished saving publication expression: ", expressionResults.size());
    }


}
