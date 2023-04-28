package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.expression.ExpressionResult;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.framework.HibernateUtil;

import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

@Log4j2
public class PublicationExpressionIndexer extends UiIndexer<ExpressionTableRow> {

    public PublicationExpressionIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<ExpressionTableRow> inputOutput() {
        List<ExpressionResult> expressionResults = getExpressionRepository().getAllExpressionResults();
        List<ExpressionTableRow> resultList = new ArrayList<>();
        expressionResults.forEach(expressionResult -> expressionResult.getFigures().forEach(figure -> {
            ExpressionTableRow row = new ExpressionTableRow(expressionResult);
            row.setFigure(figure);
            row.setPublication(expressionResult.getExpressionExperiment().getPublication());
            resultList.add(row);
        }));
        return resultList;
    }

    @Override
    protected void cleanUiTables() {
        String expressionPub = ExpressionTableRow.class.getAnnotation(Table.class).name();
        cleanoutTable(expressionPub);
    }

}
