package org.zfin.database;

import org.zfin.framework.ZfinSimpleTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience class to create a btsContains where clause
 */
public class BtsContainsService {

    private List<String> btsClauseList = new ArrayList<String>();
    private String btsTableColumn;

    public BtsContainsService(String btsTableColumn) {
        this.btsTableColumn = btsTableColumn;
    }

    public void addBtsExpandedValueList(String columnName, List<String> values) {
        addBtsValueList(columnName, values, true);
    }

    public void addBtsValueList(String columnName, List<String> values) {
        addBtsValueList(columnName, values, false);
    }

    private void addBtsValueList(String columnName, List<String> values, boolean useExpandedView) {
        if (values != null) {
            for (String value : values) {
                StringBuilder clauseItem = new StringBuilder(" ");
                clauseItem.append(columnName);
                clauseItem.append(":");
                if (useExpandedView)
                    clauseItem.append(btsExpand(btsEscape(value)));
                else
                    clauseItem.append(btsEscape(value));
                btsClauseList.add(clauseItem.toString());
            }
        }
    }

    public String getFullClause() {
        StringBuilder btsClause = new StringBuilder();
        for (String clause : btsClauseList) {
            if (!(btsClause.length() == 0))
                btsClause.append(" and ");
            btsClause.append(clause);
        }
        if (btsClause.length() > 0) {
            return "bts_contains(" + btsTableColumn + ", '" + btsClause.toString() + "', fas_all_score # real) ";
        }
        return null;
    }

    public String getFullOrClause() {
        StringBuilder btsClause = new StringBuilder();
        for (String clause : btsClauseList) {
            if (!(btsClause.length() == 0))
                btsClause.append(" or ");
            btsClause.append(clause);
        }
        if (btsClause.length() > 0) {
            return "bts_contains(" + btsTableColumn + ", '" + btsClause.toString() + "', fas_all_score # real) ";
        }
        return null;
    }

    public static String btsExpand(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(value);
        sb.append("^1000 or ");
        sb.append(value);
        sb.append("*)");
        return sb.toString();
    }

    public static String btsEscape(String value) {
        if (value == null)
            return null;

        return ZfinSimpleTokenizer.getSpecialCharacterReplacement(value.toLowerCase());
    }

}
