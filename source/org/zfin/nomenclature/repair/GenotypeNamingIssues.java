package org.zfin.nomenclature.repair;

import lombok.SneakyThrows;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.hibernate.Query;
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

    public static final Option outputFileName = OptionBuilder.withArgName("output").hasArg().isRequired().withDescription("the file to write csv report").create("o");

    private BufferedWriter outputFileWriter;

    static {
        options.addOption(outputFileName);
    }

    public GenotypeNamingIssues(BufferedWriter outputFileWriter) throws IOException {
        super();
        this.outputFileWriter = outputFileWriter;
    }

    public static void main(String[] args) throws IOException {
        CommandLine commandLine = parseArguments(args, "");
        String outputPath = commandLine.getOptionValue(outputFileName.getOpt());

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputPath));
        GenotypeNamingIssues gni = new GenotypeNamingIssues(outputWriter);
        gni.findNamingIssues();
        outputWriter.close();

        System.exit(0);
    }

    public void findNamingIssues() throws IOException {
        initAll();

        List<NamingIssuesReportRow> allSuspiciousGenotypes = getSuspiciousGenotypes();
        System.err.println("Found " + allSuspiciousGenotypes.size() + " potential name errors.");

        categorizeNamingIssues(allSuspiciousGenotypes);
        applyFixes(allSuspiciousGenotypes);

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
                row.setIssueCategory(NamingIssuesReportRow.IssueCategory.TRANSPOSED);
            }
        }
    }

    public void categorizeAlphabeticalTranspositions(List<NamingIssuesReportRow> rows) {
        Iterator<NamingIssuesReportRow> iter = rows.iterator();
        while (iter.hasNext()) {
            NamingIssuesReportRow row = iter.next();

            if (namesDifferOnlyByAlphabetization(row.getDisplayName(), row.getComputedDisplayName()) ) {
                row.setIssueCategory(NamingIssuesReportRow.IssueCategory.ALPHABETICAL);
            }
        }
    }

    private void applyFixes(List<NamingIssuesReportRow> rows) {
        Iterator<NamingIssuesReportRow> iter = rows.iterator();
        while (iter.hasNext()) {
            NamingIssuesReportRow row = iter.next();

            if (row.getIssueCategory() != NamingIssuesReportRow.IssueCategory.UNKNOWN) {
                String sql = String.format("UPDATE genotype SET geno_display_name = '%s' where geno_display_name = '%s' and geno_zdb_id = '%s';",
                                            row.getComputedDisplayName(), row.getDisplayName(), row.getId());
                row.setSqlFix(sql);
            } else {
                row.setSqlFix("");
            }
        }
    }

    private void outputReport(List<NamingIssuesReportRow> allSuspiciousGenotypes) {
        write("\"ID\",\"Display Name\",\"Computed Display Name\",\"Issue Category\",\"SQL Fix\"");
        for(NamingIssuesReportRow row: allSuspiciousGenotypes) {
            write("\"https://zfin.org/" + row.getId() + "\",\"" + row.getDisplayName() + "\",\"" + row.getComputedDisplayName() + "\"," + "\"" + row.getIssueCategory().value + "\",\"" + row.getSqlFix() + "\"");
        }
    }

    public boolean canBeTransposedIntoEqualNames(String displayName, String computedDisplayName) {
        try {
            Set<String> displayNameParts = Set.of(displayName.split(" ?; ?"));
            Set<String> computedNameParts = Set.of(computedDisplayName.split(" ?; ?"));
            return displayNameParts.equals(computedNameParts);
        } catch (java.lang.IllegalArgumentException iae) {
            //likely a name with duplicate parts
            System.err.println("WARNING: " + displayName + " has issues -- duplicate parts?");
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

    @SneakyThrows
    private void write(String line) {
        this.outputFileWriter.write(line + "\n");
    }

}


