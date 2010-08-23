package org.zfin.expression;

import org.zfin.expression.ExpressionResult;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 */
public class ExpressionResultTermComparator implements Comparator<ExpressionResult> {

    public int compare(ExpressionResult xpRslt1, ExpressionResult xpRslt2) {
        if (xpRslt1 == null && xpRslt2 == null)
            return 0;
        else if (xpRslt1 == null)
            return -1;
        else if (xpRslt2 == null)
            return 1;

        return xpRslt1.getSuperterm().compareTo(xpRslt2.getSuperterm());
    }
}