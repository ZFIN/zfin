package org.zfin.console;

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

        categorizeNamingIssues(allSuspiciousGenotypes);

        outputReport(allSuspiciousGenotypes);
    }

    public List<NamingIssuesReportRow> getSuspiciousGenotypes() {
        String sql = NamingIssuesReportRow.reportSql();
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        return (List<NamingIssuesReportRow>) query
                .list()
                .stream()
                .map(NamingIssuesReportRow::fromQueryResult)
                .collect(Collectors.toList());
    }

    private void categorizeNamingIssues(List<NamingIssuesReportRow> allSuspiciousGenotypes) {
        categorizeGenotypesWithTransposedNames(allSuspiciousGenotypes);
        categorizeAlphabeticalTranspositions(allSuspiciousGenotypes);
    }

    public void categorizeGenotypesWithTransposedNames(List<NamingIssuesReportRow> rows) {
        Iterator<NamingIssuesReportRow> iter = rows.iterator();
        while (iter.hasNext()) {
            NamingIssuesReportRow row = iter.next();

            if (canBeTransposedIntoEqualNames(row.getDisplayName(), row.getComputedDisplayName()) ) {
                row.setIssueCategory("Transposed Names");
            }
        }
    }

    public void categorizeAlphabeticalTranspositions(List<NamingIssuesReportRow> rows) {
        Iterator<NamingIssuesReportRow> iter = rows.iterator();
        while (iter.hasNext()) {
            NamingIssuesReportRow row = iter.next();

            if (namesDifferOnlyByAlphabetization(row.getDisplayName(), row.getComputedDisplayName()) ) {
                row.setIssueCategory("Ordered Alphabetically");
            }
        }
    }

    private void outputReport(List<NamingIssuesReportRow> allSuspiciousGenotypes) {
        for(NamingIssuesReportRow row: allSuspiciousGenotypes) {
            // System.out.println("UPDATE genotype SET geno_display_name = '" + row.getComputedDisplayName() + "' WHERE geno_zdb_id = '" + row.getId() + "' AND geno_display_name = '" + row.getDisplayName() + "';");
            System.out.println("\"https://zfin.org/" + row.getId() + "\",\"" + row.getDisplayName() + "\",\"" + row.getComputedDisplayName() + "\"," + "\"" + row.getIssueCategory() + "\"");
        }
    }

    public boolean canBeTransposedIntoEqualNames(String displayName, String computedDisplayName) {
        try {
            Set<String> displayNameParts = Set.of(displayName.split(" ?; ?"));
            Set<String> computedNameParts = Set.of(computedDisplayName.split(" ?; ?"));
            return displayNameParts.equals(computedNameParts);
        } catch (java.lang.IllegalArgumentException iae) {
            //likely a name with duplicate parts
            System.out.println("WARNING: " + displayName + " has issues -- duplicate parts?");
            return false;
        }
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

        List<GenotypeFeatureName> sortedDisplayNameFeatures = displayNameParts
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .filter(elem -> elem != null)
                .sorted(new GenotypeFeatureNameComparator()) //only sort the original display name
                .collect(Collectors.toList());

        List<GenotypeFeatureName> computedDisplayNameFeatures = computedNameParts
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .filter(elem -> elem != null)
                .collect(Collectors.toList());

        return GenotypeFeatureNameComparator.listsEqual(sortedDisplayNameFeatures, computedDisplayNameFeatures);
    }

}


