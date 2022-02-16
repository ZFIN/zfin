package org.zfin.nomenclature.repair;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NamingIssuesReportRow {

    public String id;
    public String displayName;
    public String computedDisplayName;
    public IssueCategory issueCategory;
    public String sqlFix;

    public enum IssueCategory {
        UNKNOWN("Unknown"),
        ALPHABETICAL("Ordered Alphabetically"), //Computed names is same as display name if display name is ordered alphabetically (with Tg at the end)
        TRANSPOSED("Transposed Names"), //Computed names is same as display name, but the ordering is different
        MANUAL_FIX("Manual Fix"); //Manually approved for fixing to computed display name
        public String value;
        IssueCategory(String value) {
            this.value = value;
        }
    }

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
        row.setIssueCategory(IssueCategory.UNKNOWN);
        return row;
    }

    public static String reportSql() {
        return "select geno_zdb_id, geno_display_name, get_genotype_display(geno_zdb_id) as computed_display_name \n" +
                " from genotype \n" +
                " where trim(get_genotype_display(geno_zdb_id)) != trim(geno_display_name) \n";
    }

}
