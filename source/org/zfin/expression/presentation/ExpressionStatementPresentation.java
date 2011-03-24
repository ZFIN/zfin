package org.zfin.expression.presentation;

import org.zfin.expression.ExpressionStatement;
import org.zfin.ontology.presentation.ExpressionResultPresentation;
import org.zfin.ontology.presentation.TermPresentation;

/**
 * Links and names for ExpressionStatement objects
 */
public class ExpressionStatementPresentation extends TermPresentation {


    public static String getLink(ExpressionStatement statement, boolean suppressPopupLink) {
        if (statement == null)
            return null;

        //if there's no "not" - just pull the entity out and use regular PostComposedEntity
        //presentation stuff..
        if (statement.isExpressionFound()) {
            return TermPresentation.getLink(statement.getEntity(), suppressPopupLink);
        } else {
            return getNotExpressedElement() + getLink(statement.getEntity(), suppressPopupLink);
        }

    }


    public static String getName(ExpressionStatement statement) {
        if (statement == null)
            return null;

        StringBuilder name = new StringBuilder(50);
        if (!statement.isExpressionFound())
            name.append(getNotExpressedElement());
        name.append(TermPresentation.getName(statement.getEntity()));
        return name.toString();

    }

    protected static String getNotExpressedElement() {
        String notExpressedTitle = "The gene was reported as NOT expressed in this structure.";
        StringBuilder notExpressedSpan = new StringBuilder(25 + notExpressedTitle.length());
        notExpressedSpan.append("<span title=\"");
        notExpressedSpan.append(notExpressedTitle);
        notExpressedSpan.append("\">");
        notExpressedSpan.append("(not)");
        notExpressedSpan.append("</span>&nbsp;");
        return notExpressedSpan.toString();
    }

}
