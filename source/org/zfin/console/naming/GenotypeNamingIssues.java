package org.zfin.console.naming;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;


import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GenotypeNamingIssues extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        GenotypeNamingIssues gni = new GenotypeNamingIssues();
        gni.findNamingIssues();
        System.exit(0);
    }

    public void findNamingIssues() throws IOException {
        initAll();

        List<NamingIssuesReportRow> allSuspiciousGenotypes = getSuspiciousGenotypes();
        System.out.println("Found " + allSuspiciousGenotypes.size() + " potential name errors.");

        List<NamingIssuesReportRow> namesWithTranspositionErrors = getGenotypesWithTransposedNames(allSuspiciousGenotypes);
        System.out.println("Found " + namesWithTranspositionErrors.size() + " transposed name errors");
        System.out.println("Run these sql updates:");
        System.out.println("------------------------------");

        for(NamingIssuesReportRow row: namesWithTranspositionErrors) {
            System.out.println("UPDATE genotype SET geno_display_name = '" + row.getComputedDisplayName() + "' WHERE geno_zdb_id = '" + row.getId() + "' AND geno_display_name = '" + row.getDisplayName() + "';");
        }
    }

    public List<NamingIssuesReportRow> getGenotypesWithTransposedNames(List<NamingIssuesReportRow> rows) {
        List<NamingIssuesReportRow> returnList = new ArrayList<NamingIssuesReportRow>();

        Iterator<NamingIssuesReportRow> iter = rows.iterator();
        while (iter.hasNext()) {
            NamingIssuesReportRow row = iter.next();

            if (canBeTransposedIntoEqualNames(row.getDisplayName(), row.getComputedDisplayName()) ) {
                returnList.add(row);
                iter.remove();
            }
        }
        return returnList;
    }

    public boolean canBeTransposedIntoEqualNames(String displayName, String computedDisplayName) {
        try {
            Set<String> displayNameParts = Set.of(displayName.split(" ?; ?"));
            Set<String> computedNameParts = Set.of(computedDisplayName.split(" ?; ?"));
            return displayNameParts.equals(computedNameParts);
        } catch (java.lang.IllegalArgumentException iae) {
            //likely a name with duplicate parts
            return false;
        }
    }

    public List<NamingIssuesReportRow> getSuspiciousGenotypes() {
        String sql = "select geno_zdb_id, geno_display_name, get_genotype_display(geno_zdb_id) as computed_display_name \n" +
                " from genotype \n" +
                " where trim(get_genotype_display(geno_zdb_id)) != trim(geno_display_name) \n";

        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        return (List<NamingIssuesReportRow>) query
                .list()
                .stream()
                .map(row -> NamingIssuesReportRow.fromQueryResult(row))
                .collect(Collectors.toList());
    }


}


