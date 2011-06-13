package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.presentation.ExpressionStatementPresentation;

/**
 * Display the full list of post composed terms and their hyperlink.
 */
public class ExpressionResultPresentation extends ExpressionStatementPresentation {

    private static final Logger logger = Logger.getLogger(ExpressionResultPresentation.class);

    /**
     * Create hyperlink to term detail page.
     *
     * @param expressionResult  ExpressionResult
     * @param suppressPopupLink hide the popup link icon
     * @return hyperlink hyperlink
     */
    public static String getLink(ExpressionResult expressionResult, boolean suppressPopupLink, boolean curationLink) {
        if (expressionResult == null)
            return null;
        if (curationLink)
            return getCurationLink(expressionResult);
        ExpressionStatement expressionStatement = new ExpressionStatement();
        expressionStatement.setEntity(expressionResult.getEntity());
        expressionStatement.setExpressionFound(expressionResult.isExpressionFound());

        return getLink(expressionStatement, suppressPopupLink);
    }

    private static String getCurationLink(ExpressionResult expressionResult) {
        return getWebdriverLink(CURATION_URI+"&pubcur_c_tab=FX", expressionResult.getExpressionExperiment().getPublication().getZdbID(),
                "edit");
    }


    public static String getName(ExpressionResult expressionResult) {
        if (expressionResult == null)
            return null;

        ExpressionStatement expressionStatement = new ExpressionStatement();
        expressionStatement.setEntity(expressionResult.getEntity());
        expressionStatement.setExpressionFound(expressionResult.isExpressionFound());

        return getName(expressionStatement);
    }


}
