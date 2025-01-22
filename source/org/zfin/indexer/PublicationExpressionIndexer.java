package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.Figure;
import org.zfin.figure.presentation.ExpressionTableRow;

import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

@Log4j2
public class PublicationExpressionIndexer extends UiIndexer<ExpressionTableRow> {

    public PublicationExpressionIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<ExpressionTableRow> inputOutput() {
        List<ExpressionResult2> expressionResults = getExpressionRepository().getAllExpressionResults();
        List<ExpressionTableRow> resultList = new ArrayList<>();
        expressionResults.forEach(expressionResult -> {
            Figure figure = expressionResult.getExpressionFigureStage().getFigure();
            ExpressionTableRow row = new ExpressionTableRow(expressionResult.getExpressionFigureStage(),expressionResult);
            row.setFigure(figure);
            row.setPublication(expressionResult.getExpressionFigureStage().getExpressionExperiment().getPublication());
            resultList.add(row);
        });
        return resultList;
    }

    @Override
    protected void cleanUiTables() {
        String expressionPub = ExpressionTableRow.class.getAnnotation(Table.class).name();
        cleanoutTable(expressionPub);
    }

}
