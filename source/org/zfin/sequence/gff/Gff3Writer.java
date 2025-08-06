package org.zfin.sequence.gff;

import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.load.LoadAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

@Log4j2
public class Gff3Writer {

    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";

    Gff3NcbiDAO dao = new Gff3NcbiDAO();
    public Set<LoadAction> actions = new HashSet<>();

    public static void main(String[] args) {
        init();
        Gff3Writer processor = new Gff3Writer();
        processor.start();
    }

    private ReportBuilder.SummaryTable summaryTable;

    public void start() {
        try {
            ReportBuilder builder = prepareReports();
            createZfinGeneFile();
            createRefSeqFile();
            createReport(builder);
        } catch (IOException e) {
            log.error("Error processing GFF3 file", e);
        }
    }

    private ReportBuilder prepareReports() {
        ReportBuilder builder = new ReportBuilder();
        builder.setTitle("GFF3 Generation Report");
        summaryTable = builder.addSummaryTable("GFF Feature Records");
        summaryTable.setHeaders(List.of("GFF Feature", "Number of Records exported"));
        return builder;
    }

    private void createReport(ReportBuilder builder) {
        ObjectNode report = builder.build();
        try {
            String jsonString = builder.getJsonString(report);

            // Write to file
            FileUtils.writeStringToFile(new File(".", "gff3_ncbi_report.json"), jsonString, StandardCharsets.UTF_8);
            writeOutputReportFile(jsonString);

        } catch (IOException e) {
            log.error("JSON reporting failed: " + e.getMessage(), e);
        }
    }

    private void createZfinGeneFile() throws IOException {
        List<Gff3Ncbi> filteredResults = getZfinGeneRecords();
        writeGff3File("zfin_genes.grcz12.gff3", filteredResults, false);
        //upsertSequenceFeatureChromosomeRecords(filteredResults);
    }

    private List<Gff3Ncbi> getZfinGeneRecords() {
        HibernateUtil.createTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put("feature", "gene");

        Pagination pagination = new Pagination();
        pagination.setLimit(0);
        pagination.setPage(0);
        Long totalRecords = dao.findByParams(pagination, params).getTotalResults();
        summaryTable.addSummaryRow(List.of("ZFIN genes", String.valueOf(totalRecords)));
        System.out.println("Total NCBI Gene records found: " + totalRecords);
        pagination.setLimit(400000);
        pagination.setPage(0);
        List<Gff3Ncbi> results = dao.findRecordsByFeature("gene");
        List<String> ncbiGeneIDs = results.stream().map(Gff3Ncbi::getGeneID).toList();
        // remove records without ZFIN gene association
        // retrieve all MarkerDBLinks for zfin genes with genbank accession
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        System.out.println("Total GenBank Records in ZFIN found: " + genbankList.size());
        Set<String> genBankIDsInZFIN = genbankList.stream()
            .map(DBLink::getAccessionNumber)
            .collect(Collectors.toSet());

        // filter out those records that have a ZFIN Gene ID
        List<Gff3Ncbi> filteredResults = results.stream()
            .filter(record -> record.getGeneZdbID() != null)
            .toList();
        HibernateUtil.rollbackTransaction();
        return filteredResults;
    }

    private void createRefSeqFile() throws IOException {
        String fileName = "zfin_refseq.grcz12.gff3";
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        List<String> chromosomeList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "MT");

        AtomicInteger totalRecords = new AtomicInteger();
        chromosomeList.forEach(chromosome -> {
            List<Gff3Ncbi> results = dao.findRecordsBySource(chromosome, List.of("BestRefSeq", "BestRefSeq,Gnomon", "Gnomon"));
            writeToGff3File(fileName, results);
            totalRecords.addAndGet(results.size());
            System.out.println("Total records for chromosome: " + chromosome + ": " + results.size());
        });
        summaryTable.addSummaryRow(List.of("ZFIN RefSeq Accessions", String.valueOf(totalRecords)));
        System.out.println("Total records BestRefseq: " + totalRecords);

    }

    private void writeToGff3File(String s, List<Gff3Ncbi> results) {
        writeGff3File(s, results, true);
    }

    public static void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    public void writeGff3File(String gff3FilePath, List<Gff3Ncbi> records, boolean addToFile) {
        File file = new File(gff3FilePath);
        if (file.exists() && !addToFile) {
            file.delete();
        }
        try {
            FileWriter outputfile = new FileWriter(file, addToFile);
            CSVWriter writer = new CSVWriter(outputfile, '\t', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            String[] header = {"##gff-version 3"};
            writer.writeNext(header);
            List<String[]> collect = records.stream().map(record -> {
                String[] line = new String[9];
                line[0] = record.getChromosome();
                line[1] = record.getSource();
                line[2] = record.getFeature();
                line[3] = String.valueOf(record.getStart());
                line[4] = String.valueOf(record.getEnd());
                line[5] = record.getScore();
                line[6] = record.getStrand();
                line[7] = String.valueOf(record.getFrame());
                line[8] = record.getAttributes();
                return line;
            }).collect(Collectors.toList());
            writer.writeAll(collect);
            try {
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateAttributeColumn(Set<Gff3NcbiAttributePair> attributePairs) {
        TreeSet<Gff3NcbiAttributePair> sortedPairs = new TreeSet<>(attributePairs);
        return sortedPairs.stream().filter(pair -> persistKeySet.contains(pair.getKey())).map(pair -> pair.getKey() + "=" + pair.getValue()).collect(Collectors.joining(";"));
    }

    private String generateAttributes(Map<String, List<String>> attributes) {
        return attributes.entrySet().stream().map((entry) -> {
            Gff3NcbiAttributePair pair = new Gff3NcbiAttributePair();
            pair.setKey(entry.getKey());
            pair.setValue(entry.getValue().stream().map(String::trim).collect(Collectors.joining(",")));
            return pair;
        }).collect(Collectors.toSet()).stream().map(pair -> pair.getKey() + "=" + pair.getValue()).collect(Collectors.joining(";"));
    }

    private static Set<String> persistKeySet = new LinkedHashSet<>();

    static {
        persistKeySet.add("ID");
        persistKeySet.add("Dbxref");
        persistKeySet.add("gene_id");
        persistKeySet.add("gene");
        persistKeySet.add("gene_name");
        persistKeySet.add("transcript");
        persistKeySet.add("Parent");
    }

    private Set<Gff3NcbiAttributePair> generateAttributePairs(Map<String, List<String>> attributes) {
        return attributes.entrySet().stream().filter(entry -> persistKeySet.contains(entry.getKey())).map((entry) -> {
            Gff3NcbiAttributePair pair = new Gff3NcbiAttributePair();
            pair.setKey(entry.getKey());
            pair.setValue(entry.getValue().stream().map(String::trim).collect(Collectors.joining(",")));
            return pair;
        }).collect(Collectors.toSet());
    }

    static Map<String, String> chromoMap = new HashMap<>();

    static {
        chromoMap.put("NC_133176.1", "1");
        chromoMap.put("NC_133177.1", "2");
        chromoMap.put("NC_133178.1", "3");
        chromoMap.put("NC_133179.1", "4");
        chromoMap.put("NC_133180.1", "5");
        chromoMap.put("NC_133181.1", "6");
        chromoMap.put("NC_133182.1", "7");
        chromoMap.put("NC_133183.1", "8");
        chromoMap.put("NC_133184.1", "9");
        chromoMap.put("NC_133185.1", "10");
        chromoMap.put("NC_133186.1", "11");
        chromoMap.put("NC_133187.1", "12");
        chromoMap.put("NC_133188.1", "13");
        chromoMap.put("NC_133189.1", "14");
        chromoMap.put("NC_133190.1", "15");
        chromoMap.put("NC_133191.1", "16");
        chromoMap.put("NC_133192.1", "17");
        chromoMap.put("NC_133193.1", "18");
        chromoMap.put("NC_133194.1", "19");
        chromoMap.put("NC_133195.1", "20");
        chromoMap.put("NC_133196.1", "21");
        chromoMap.put("NC_133197.1", "22");
        chromoMap.put("NC_133198.1", "23");
        chromoMap.put("NC_133199.1", "24");
        chromoMap.put("NC_133200.1", "25");
        chromoMap.put("NC_002333.2", "MT");

    }

    private void writeOutputReportFile(String jsonString) {
        String sourceRoot = ZfinPropertiesEnum.SOURCEROOT.value();
        if (sourceRoot == null) {
            sourceRoot = System.getenv("SOURCEROOT");
        }
        File reportFile = new File(".", "gff3_ncbi_report.html");
        try {
            String template = "./home/uniprot/zfin-report-template.html";
            String templateContents = FileUtils.readFileToString(new File(template));
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonString);
            FileUtils.writeStringToFile(reportFile, filledTemplate);
        } catch (IOException e) {
            System.out.println("ERROR: Could not write report file: " + e.getMessage());
        }
    }

    private void upsertSequenceFeatureChromosomeRecords(List<Gff3Ncbi> filteredResults) {
        HibernateUtil.createTransaction();
        try {

            List<MarkerGenomeLocation> locationList = getSequenceRepository().getAllGenomeLocations(GenomeLocation.Source.NCBI_LOADER);
            Map<String, MarkerGenomeLocation> geneIDMap = locationList.stream()
                .collect(Collectors.toMap(MarkerGenomeLocation::getAccessionNumber, location -> location, (a, b) -> a, LinkedHashMap::new));
            filteredResults.forEach(gff3Ncbi -> {
                String geneID = gff3Ncbi.getGeneID();
                MarkerGenomeLocation genomeLocation;
                if (geneIDMap.containsKey(geneID)) {
                    genomeLocation = geneIDMap.get(geneID);
                } else {
                    genomeLocation = new MarkerGenomeLocation();
                }
                genomeLocation.setAccessionNumber(geneID);
                genomeLocation.setMarker(getMarkerRepository().getMarker(gff3Ncbi.getGeneZdbID()));
                genomeLocation.setAssembly("GRCz12tu");
                genomeLocation.setChromosome(gff3Ncbi.getChromosome());
                genomeLocation.setStart(gff3Ncbi.getStart());
                genomeLocation.setEnd(gff3Ncbi.getEnd());
                genomeLocation.setSource(GenomeLocation.Source.NCBI_LOADER);
                getSequenceRepository().saveOrUpdateGenomeLocation(genomeLocation);
            });
        } catch (Exception e) {
            log.log(Level.ERROR, "Error saving genome location: ", e);
        }
        HibernateUtil.flushAndCommitCurrentSession();

        String n = "chr";
    }

}


