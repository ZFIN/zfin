package org.zfin.nomenclature.repair;

import org.apache.commons.collections.ListUtils;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Script to generate a report of genotype names that have issues and potentially some fixes to run
 * against the database.
 *
 * Run with: gradle genotypeNamingIssues --args='-o out.csv'
 *
 */
public class GenotypeNamingIssues extends AbstractScriptWrapper {

    private BufferedWriter outputFileWriter;
    private static final String DEFAULT_OUTPUT_PATH = "GenotypeNamingIssuesReport.csv";

    public static void main(String[] args) throws IOException {
        GenotypeNamingIssues gni = new GenotypeNamingIssues();
        gni.generateErrorReportOfNamingIssues();
        System.exit(0);
    }

    public void generateErrorReportOfNamingIssues() {
        initAll();

        List<NamingIssuesReportRow> allReportRows = getSuspiciousGenotypes();
        LOG.info("Found " + allReportRows.size() + " potential name errors.");

        categorizeNamingIssues(allReportRows);
        generateFixes(allReportRows);
        applyFixes(allReportRows);
        outputReport(allReportRows);

        HibernateUtil.closeSession();

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
        for (NamingIssuesReportRow row : rows) {
            if (canBeTransposedIntoEqualNames(row.getDisplayName(), row.getComputedDisplayName())) {
                row.setIssueCategory(NamingIssuesReportRow.IssueCategory.TRANSPOSED);
            }
        }
    }

    public void categorizeAlphabeticalTranspositions(List<NamingIssuesReportRow> rows) {
        for (NamingIssuesReportRow row : rows) {
            if (namesDifferOnlyByAlphabetization(row.getDisplayName(), row.getComputedDisplayName())) {
                row.setIssueCategory(NamingIssuesReportRow.IssueCategory.ALPHABETICAL);
            }
        }
    }

    private void generateFixes(List<NamingIssuesReportRow> rows) {
        for (NamingIssuesReportRow row : rows) {
            if (row.getIssueCategory() != NamingIssuesReportRow.IssueCategory.UNKNOWN) {
                String sql = String.format("UPDATE genotype SET geno_display_name = '%s' where geno_display_name = '%s' and geno_zdb_id = '%s';",
                        row.getComputedDisplayName(), row.getDisplayName(), row.getId());
                row.setSqlFix(sql);
            } else {
                row.setSqlFix("");
            }
        }
    }

    private void applyFixes(List<NamingIssuesReportRow> reportRows) {
        Transaction tx;
        Query query;
        String forceApplyFixes = System.getenv("FORCE_APPLY_FIXES");

        if ("true".equals(forceApplyFixes)) {
            LOG.info("APPLYING FIXES");

            for (NamingIssuesReportRow row : reportRows) {
                tx = HibernateUtil.createTransaction();
                if (row.getIssueCategory() != NamingIssuesReportRow.IssueCategory.UNKNOWN) {
                    LOG.info("Executing SQL: " + row.getSqlFix());
                    query = HibernateUtil.currentSession().createSQLQuery(row.getSqlFix());
                    query.executeUpdate();
                }
                tx.commit();
            }

        } else {
            LOG.info("NOT APPLYING FIXES");
        }
    }

    private void outputReport(List<NamingIssuesReportRow> reportRows) {
        BufferedWriter outputWriter = null;
        String outputPath = System.getProperty("reportFile", DEFAULT_OUTPUT_PATH);
        if (outputPath.equals("")) {
            //Seems we get empty string if property isn't specified. Maybe due to gradle configuration in console.gradle?
            outputPath = DEFAULT_OUTPUT_PATH;
        }
        
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputPath));
            outputWriter.write("\"ID\",\"Display Name\",\"Computed Display Name\",\"Issue Category\",\"SQL Fix\"\n");
            for (NamingIssuesReportRow row : reportRows) {
                outputWriter.write("\"https://zfin.org/" + row.getId() + "\",\"" + row.getDisplayName() + "\",\"" + row.getComputedDisplayName() + "\"," + "\"" + row.getIssueCategory().value + "\",\"" + row.getSqlFix() + "\"\n");
            }
        } catch (IOException ioe) {
            LOG.error("Error writing to file: " + outputPath);
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canBeTransposedIntoEqualNames(String displayName, String computedDisplayName) {
        try {
            Set<String> displayNameParts = Set.of(displayName.split(" ?; ?"));
            Set<String> computedNameParts = Set.of(computedDisplayName.split(" ?; ?"));
            return displayNameParts.equals(computedNameParts);
        } catch (java.lang.IllegalArgumentException iae) {
            //likely a name with duplicate parts
            LOG.warn("WARNING: " + displayName + " has issues -- duplicate parts?");
            return false;
        }
    }

    /**
     * Given a display name for a genotype and the database function's recommended name (aka computed name),
     * check if the only difference is that when you resort the original display name, it matches the computed
     * name.
     *
     */
    private boolean namesDifferOnlyByAlphabetization(String displayName, String computedDisplayName) {
        List<String> displayNameParts = List.of(displayName.split(" ?; ?"));
        List<String> computedNameParts = List.of(computedDisplayName.split(" ?; ?"));

        List<GenotypeFeatureName> sortedDisplayNameFeatures = displayNameParts
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .filter(Objects::nonNull)
                .sorted(new GenotypeFeatureNameComparator()) //only sort the original display name
                .collect(Collectors.toList());

        List<GenotypeFeatureName> computedDisplayNameFeatures = computedNameParts
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ListUtils.isEqualList(sortedDisplayNameFeatures, computedDisplayNameFeatures);
    }

}


