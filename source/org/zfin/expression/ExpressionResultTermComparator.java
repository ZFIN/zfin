package org.zfin.expression;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 */
public class ExpressionResultTermComparator implements Comparator<ExpressionResult2> {

    public int compare(ExpressionResult2 xpRslt1, ExpressionResult2 xpRslt2) {
        if (xpRslt1 == null && xpRslt2 == null)
            return 0;
        else if (xpRslt1 == null)
            return -1;
        else if (xpRslt2 == null)
            return 1;

        return xpRslt1.getSuperTerm().compareTo(xpRslt2.getSuperTerm());
    }
}