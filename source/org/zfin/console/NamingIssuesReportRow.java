package org.zfin.console;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NamingIssuesReportRow {

    public String id;
    public String displayName;
    public String computedDisplayName;

    public NamingIssuesReportRow() {
    }

    public NamingIssuesReportRow(String id, String displayName, String computedDisplayName) {
        setId(id);
        setDisplayName(displayName);
        setComputedDisplayName(computedDisplayName);
    }

    public static NamingIssuesReportRow fromQueryResult(Object result) {
        Object[] typeCastResult = (Object[]) result;
        NamingIssuesReportRow row = new NamingIssuesReportRow();
        row.setId(typeCastResult[0].toString());
        row.setDisplayName(typeCastResult[1].toString());
        row.setComputedDisplayName(typeCastResult[2].toString());
        return row;
    }

}
