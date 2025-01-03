package org.zfin.ontology.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.presentation.ExpressionStatementPresentation;

/**
 * Display the full list of post composed terms and their hyperlink.
 */
public class ExpressionResultPresentation extends ExpressionStatementPresentation {

    private static final Logger logger = LogManager.getLogger(ExpressionResultPresentation.class);

    /**
     * Create hyperlink to term detail page.
     *
     * @param expressionResult  ExpressionResult
     * @param suppressPopupLink hide the popup link icon
     * @return hyperlink hyperlink
     */
    public static String getLink(ExpressionResult2 expressionResult, boolean suppressPopupLink, boolean curationLink) {
        if (expressionResult == null)
            return null;
        ExpressionStatement expressionStatement = new ExpressionStatement();
        expressionStatement.setEntity(expressionResult.getEntity());
        expressionStatement.setExpressionFound(expressionResult.isExpressionFound());

        return getLink(expressionStatement, suppressPopupLink);
    }

}
