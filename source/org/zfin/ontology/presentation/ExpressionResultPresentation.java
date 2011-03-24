package org.zfin.ontology.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.presentation.ExpressionStatementPresentation;
import org.zfin.ontology.Term;

/**
 * Display the full list of post composed terms and their hyperlink.
 */
public class ExpressionResultPresentation extends ExpressionStatementPresentation {

    private static final Logger logger = Logger.getLogger(ExpressionResultPresentation.class);

    /**
     * Create hyperlink to term detail page.
     *
     * @param expressionResult ExpressionResult
     * @param suppressPopupLink hide the popup link icon
     * @return hyperlink hyperlink
     */
    public static String getLink(ExpressionResult expressionResult, boolean suppressPopupLink) {
        if (expressionResult == null)
            return null;

        ExpressionStatement expressionStatement = new ExpressionStatement();
        expressionStatement.setEntity(expressionResult.getEntity());
        expressionStatement.setExpressionFound(expressionResult.isExpressionFound());

        return getLink(expressionStatement, suppressPopupLink);
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
