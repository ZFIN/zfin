package org.zfin.console;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.util.LoggingUtil;


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
        System.out.println("------------------------------");

        List<NamingIssuesReportRow> furtherFiltered = filterListToOnlyThoseThatHaveBeenAlphabetized(namesWithTranspositionErrors);
        System.out.println("After filtering, found " + furtherFiltered.size() + " errors remaining that are a simple ordering issue");
        System.out.println("------------------------------");

        for(NamingIssuesReportRow row: furtherFiltered) {
            // System.out.println("UPDATE genotype SET geno_display_name = '" + row.getComputedDisplayName() + "' WHERE geno_zdb_id = '" + row.getId() + "' AND geno_display_name = '" + row.getDisplayName() + "';");
            System.out.println("\"https://zfin.org/" + row.getId() + "\",\"" + row.getDisplayName() + "\",\"" + row.getComputedDisplayName() + "\"");
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

    public List<NamingIssuesReportRow> filterListToOnlyThoseThatHaveBeenAlphabetized(List<NamingIssuesReportRow> rows) {
        List<NamingIssuesReportRow> returnList = new ArrayList<NamingIssuesReportRow>();

        Iterator<NamingIssuesReportRow> iter = rows.iterator();
        while (iter.hasNext()) {
            NamingIssuesReportRow row = iter.next();

            if (namesDifferOnlyByAlphabetization(row.getDisplayName(), row.getComputedDisplayName()) ) {
                returnList.add(row);
                iter.remove();
            }
        }
        return returnList;

    }

    /**
     * Given a display name for a genotype and the database function's recommended name (aka computed name),
     * check if the only difference is that when you resort the original display name, it matches the computed
     * name.
     *
     * @param displayName
     * @param computedDisplayName
     * @return
     */
    private boolean namesDifferOnlyByAlphabetization(String displayName, String computedDisplayName) {
        List<String> displayNameParts = List.of(displayName.split(" ?; ?"));
        List<String> computedNameParts = List.of(computedDisplayName.split(" ?; ?"));

        List<GenotypeFeatureNamePattern> sortedDisplayNameFeatures = displayNameParts
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .filter(elem -> elem != null)
                .sorted(new GenotypeFeatureNameComparator()) //only sort the original display name
                .collect(Collectors.toList());

        List<GenotypeFeatureNamePattern> computedDisplayNameFeatures = computedNameParts
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .filter(elem -> elem != null)
                .collect(Collectors.toList());

        return listsEqual(sortedDisplayNameFeatures, computedDisplayNameFeatures);
    }

    private boolean listsEqual(List<GenotypeFeatureNamePattern>list1, List<GenotypeFeatureNamePattern>list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for(int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
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


}


