package org.zfin.ontology.presentation;

import org.zfin.expression.ExpressionResult;
import org.zfin.ontology.Term;

/**
 * Display the full list of post composed terms and their hyperlink.
 */
public class ExpressionResultPresentation extends TermPresentation {

    private static final String uri = "ontology/term-detail?termID=";

    /**
     * Create hyperlink to term detail page.
     *
     * @param expressionResult ExpressionResult
     * @return hyperlink hyperlink
     */
    public static String getLink(ExpressionResult expressionResult) {
        if (expressionResult == null)
            return null;
        StringBuffer postComposedTermHyperlink = new StringBuffer(50);
        if (!expressionResult.isExpressionFound())
            postComposedTermHyperlink.append(getNotExpressedElement());
        postComposedTermHyperlink.append("<span class=\"postcomposedtermlink\">");
        postComposedTermHyperlink.append(getLink(expressionResult.getSuperterm()));
        if (expressionResult.getSubterm() != null) {
            postComposedTermHyperlink.append("&nbsp;");
            postComposedTermHyperlink.append(getLink(expressionResult.getSubterm()));
        }
        postComposedTermHyperlink.append("</span>");
        return postComposedTermHyperlink.toString();
    }

    private static String getNotExpressedElement() {
        String notExpressedTitle = "The gene was reported as NOT expressed in this structure.";
        StringBuffer notExpressedSpan = new StringBuffer(25 + notExpressedTitle.length());
        notExpressedSpan.append("<span title=\"");
        notExpressedSpan.append(notExpressedTitle);
        notExpressedSpan.append("\">");
        notExpressedSpan.append("(not)");
        notExpressedSpan.append("</span> ");
        return notExpressedSpan.toString();
    }

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @param term term
     * @param name name attribute in hyperlink
     * @return html for term link
     */
    public static String getLink(Term term, String name) {
        if (term == null || name == null)
            return null;
        return getTomcatLinkWithTitle(uri, term.getID(), term.getTermName(), name, term.getOntology().getCommonName());
    }

}
