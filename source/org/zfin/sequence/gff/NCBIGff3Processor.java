package org.zfin.sequence.gff;

import com.fasterxml.jackson.databind.node.ObjectNode;
import htsjdk.tribble.gff.Gff3Feature;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static htsjdk.samtools.util.ftp.FTPClient.READ_TIMEOUT;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

@Log4j2
public class NCBIGff3Processor {

    public static final String GCF_049306965_1_GRCZ_12_TU_GENOMIC_GFF = "GCF_049306965.1_GRCz12tu_genomic.gff";

    static {
        Configurator.setLevel("htsjdk.tribble.gff.Gff3Codec", org.apache.logging.log4j.Level.ERROR);
    }

    private ReportBuilder.SummaryTable summaryTableLoad;
    private ReportBuilder.SummaryTable summaryTableFeatureHisto;
    private final Gff3NcbiDAO dao = new Gff3NcbiDAO();
    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";

    public static void main(String[] args) throws IOException {
        init();
        NCBIGff3Processor processor = new NCBIGff3Processor();
        processor.start();

    }

    private void start() throws IOException {
        ReportBuilder builder = prepareReports();
        String fileName = GCF_049306965_1_GRCZ_12_TU_GENOMIC_GFF;
        if ((new File(GCF_049306965_1_GRCZ_12_TU_GENOMIC_GFF + ".test")).exists()) {
            fileName = GCF_049306965_1_GRCZ_12_TU_GENOMIC_GFF + ".test";
        } else {
            downloadNcbiGff3File();
        }
        processEnsemblGff3(fileName);
        markZfinGeneRecords();
        createFeatureTypeHistogram();
        createReport(builder);
    }

    private void createFeatureTypeHistogram() {
        Map<String, Integer> histogram = dao.getFeatureTypeHistogram();
        Map<String, Integer> sortedMap = histogram.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue()).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        sortedMap.forEach((feature, count) -> summaryTableFeatureHisto.addSummaryRow(List.of(feature, String.valueOf(count))));
    }

    public static void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    public void processEnsemblGff3(String gff3FilePath) throws IOException {
        Gff3Reader reader = new Gff3Reader(gff3FilePath);

        // Get all gene features
        List<Gff3Feature> allRecords = reader.readAllFeatures();
        System.out.println("Total records: " + allRecords.size());
        summaryTableLoad.addSummaryRow(List.of("All records in File", String.valueOf(allRecords.size())));

        List<Gff3Ncbi> records = allRecords.stream()
            .map(feature -> {
                Gff3Ncbi ncbi = new Gff3Ncbi();
                ncbi.setChromosome(chromoMap.get(feature.getContig()));
                ncbi.setStart(feature.getStart());
                ncbi.setEnd(feature.getEnd());
                //ncbi.setAttributes(feature.);
                ncbi.setSource(feature.getSource() != null ? feature.getSource() : "unknown");
                ncbi.setFeature(feature.getType() != null ? feature.getType() : "unknown");
                ncbi.setScore(String.valueOf(feature.getScore()));
                ncbi.setFrame(String.valueOf(feature.getPhase()));
                ncbi.setStrand(feature.getStrand() != null ? feature.getStrand().name() : "unknown");
                ncbi.setAttributes(generateAttributes(feature.getAttributes()));
                ncbi.setAttributePairs(generateAttributePairs(feature.getAttributes()));
                return ncbi;
            })
            .toList();

        Gff3NcbiService service = new Gff3NcbiService();
        service.saveAll(records);
        reader.close();
    }

    private String generateAttributes(Map<String, List<String>> attributes) {
        return attributes.entrySet().stream()
            .map((entry) -> {
                Gff3NcbiAttributePair pair = new Gff3NcbiAttributePair();
                pair.setKey(entry.getKey());
                pair.setValue(entry.getValue().stream().map(String::trim).collect(Collectors.joining(",")));
                return pair;
            }).collect(Collectors.toSet()).stream().map(pair -> pair.getKey() + "=" + pair.getValue()).collect(Collectors.joining(";"));
    }

    private static final Set<String> persistKeySet = new HashSet<>();

    static {
        persistKeySet.add("gene_id");
        persistKeySet.add("gene_name");
        //persistKeySet.add("transcript");
        persistKeySet.add("Parent");
        persistKeySet.add("ID");
        persistKeySet.add("Dbxref");
    }

    private Set<Gff3NcbiAttributePair> generateAttributePairs(Map<String, List<String>> attributes) {

        return attributes.entrySet().stream()
            .filter(entry -> persistKeySet.contains(entry.getKey()))
            .map((entry) -> {
                Gff3NcbiAttributePair pair = new Gff3NcbiAttributePair();
                pair.setKey(entry.getKey());
                pair.setValue(entry.getValue().stream()
                    .map(String::trim).collect(Collectors.joining(",")));
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

    private void downloadNcbiGff3File() {
        String zippedFileName = "GCF_049306965.1_GRCz12tu_genomic.gff.gz";
        File file = new File(zippedFileName);
        if (file.exists()) {
            return;
        }

        String fileURL = "https://ftp.ncbi.nlm.nih.gov/genomes/refseq/vertebrate_other/Danio_rerio/all_assembly_versions/GCF_049306965.1_GRCz12tu/" + zippedFileName;

        try {
            FileUtils.copyURLToFile(
                new URL(fileURL),
                new File(zippedFileName),
                60000,
                READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileUtil.gunzipFile(zippedFileName);
    }

    private List<Gff3Ncbi> markZfinGeneRecords() {
        HibernateUtil.createTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put("feature", "gene");

        Pagination pagination = new Pagination();
        pagination.setLimit(0);
        pagination.setPage(0);
        Long totalRecords = dao.findByParams(pagination, params).getTotalResults();
        summaryTableLoad.addSummaryRow(List.of("Gene", String.valueOf(totalRecords)));
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

        // Gene IDs not matching a ZFIN record
        List<String> notFoundGeneIDs = (List<String>) CollectionUtils.removeAll(ncbiGeneIDs, new ArrayList<>(genBankIDsInZFIN));

        Map<String, String> genbankZFiNMap = genbankList.stream()
            .collect(Collectors.toMap(MarkerDBLink::getAccessionNumber, markerDBLink -> markerDBLink.getMarker().getZdbID(), (a, b) -> a, LinkedHashMap::new));
        summaryTableLoad.addSummaryRow(List.of(
            "ZFIN Gene Records matching NCBI Gene Records",
            String.valueOf(genbankZFiNMap.size())
        ));
        List<Gff3Ncbi> filteredResults = results.stream()
            .filter(record -> record.getAttributePairs().stream()
                .anyMatch(pair -> genBankIDsInZFIN.contains(pair.getGeneID())))
            .peek(gff3NcbiRecord -> {
                String geneID = gff3NcbiRecord.getGeneID();
                String zfinID = genbankZFiNMap.get(geneID);
                if (zfinID != null) {
                    Gff3NcbiAttributePair pair = new Gff3NcbiAttributePair();
                    pair.setKey("gene_id");
                    pair.setValue(zfinID);
                    pair.setGff3Ncbi(gff3NcbiRecord);
                    HibernateUtil.currentSession().persist(pair);
                    gff3NcbiRecord.getAttributePairs().add(pair);
                }
            })
            .toList();
        HibernateUtil.flushAndCommitCurrentSession();
        return filteredResults;
    }


    private ReportBuilder prepareReports() {
        ReportBuilder builder = new ReportBuilder();
        builder.setTitle("NCBI GFF3 Import Report");
        summaryTableLoad = builder.addSummaryTable("GFF Records");
        summaryTableLoad.setHeaders(List.of("Record Type", "Count"));
        summaryTableFeatureHisto = builder.addSummaryTable("Feature Histogram");
        summaryTableFeatureHisto.setHeaders(List.of("Feature Type", "Count"));
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

}


