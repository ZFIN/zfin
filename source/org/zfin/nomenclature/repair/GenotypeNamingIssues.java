package org.zfin.nomenclature.repair;

import org.apache.commons.collections.ListUtils;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
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
        categorizeManuallyApprovedChanges(allSuspiciousGenotypes);
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

    public void categorizeManuallyApprovedChanges(List<NamingIssuesReportRow> rows) {
        //see:https://docs.google.com/spreadsheets/d/1DW28aF66zDr6g9oqtFfV_Xwwseeb0DMP_uDz61BHsbI/edit
        Map<String, String> fixes = new HashMap<>();
        fixes.put("cct5<sup>tf212</sup>", "cct5<sup>tf212b</sup>");
        fixes.put("tud70/tud70", "Df(Chr12:dlx3b, dlx4b)tud70/tud70");
        fixes.put("tud70/tud70 ; s356tTg", "Df(Chr12:dlx3b, dlx4b)tud70/tud70; s356tTg");
        fixes.put("oz27/oz27", "Df(Chr13:six1a,six4a)oz27/oz27");
        fixes.put("oz5Tg/oz5Tg ; oz27/oz27 ; cz3327Tg/cz3327Tg", "Df(Chr13:six1a,six4a)oz27/oz27; cz3327Tg/cz3327Tg; oz5Tg/oz5Tg");
        fixes.put("oz16/oz16", "Df(Chr20:six1b,six4b)oz16/oz16");
        fixes.put("oz16/oz16 ; oz27/oz27", "Df(Chr20:six1b,six4b)oz16/oz16; Df(Chr13:six1a,six4a)oz27/oz27");
        fixes.put("oz5Tg/oz5Tg ; oz27/oz27 ; oz16/oz16 ; cz3327Tg/cz3327Tg", "Df(Chr20:six1b,six4b)oz16/oz16; Df(Chr13:six1a,six4a)oz27/oz27; cz3327Tg/cz3327Tg; oz5Tg/oz5Tg");
        fixes.put("oz16/oz16 ; oz5Tg", "Df(Chr20:six1b,six4b)oz16/oz16; oz5Tg");
        fixes.put("kcnk5a<sup>nk6aEt</sup>; nkuasgfp1aTg", "nk6aEt; nkuasgfp1aTg");
        fixes.put("plcg1<sup>t26480/</sup>; ptch1<sup>tj222/</sup>; ptch2<sup>hu1602/+</sup>", "plcg1<sup>t26480/+</sup>; ptch1<sup>tj222/+</sup>; ptch2<sup>hu1602/+</sup>");
        for (NamingIssuesReportRow row : rows) {
            if (fixes.containsKey(row.getDisplayName()) && fixes.get(row.getDisplayName()).equals(row.getComputedDisplayName())) {
                row.setIssueCategory(NamingIssuesReportRow.IssueCategory.MANUAL_FIX);
            }
        }
    }

    private void generateFixes(List<NamingIssuesReportRow> rows) {
        for (NamingIssuesReportRow row : rows) {
            if (row.getIssueCategory() != NamingIssuesReportRow.IssueCategory.UNKNOWN &&
                    StringUtils.isNotEmpty(row.getComputedDisplayName().strip())) {
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


