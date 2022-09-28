package org.zfin.datatransfer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.Species;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.DataReportTask;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.*;
import org.zfin.util.FileUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.loadDir;
import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.webrootDirectory;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class LoadReferenceProteome extends AbstractValidateDataReportTask {

    static {
        options.addOption(loadDir);
        options.addOption(webrootDirectory);
        options.addOption(DataReportTask.jobNameOpt);
    }

    private LoadReferenceProteome(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] arguments) {
        initLogging();
        CommandLine commandLine = parseArguments(arguments, "load <>");
        String webrootDir = commandLine.getOptionValue(webrootDirectory.getOpt());
        String jobName = commandLine.getOptionValue(DataReportTask.jobNameOpt.getOpt());
        String loadingDir = commandLine.getOptionValue(loadDir.getOpt());
        String propertyFileName = getPropertyFileFromWebroot(webrootDir);

        LoadReferenceProteome load = new LoadReferenceProteome(jobName, propertyFileName, loadingDir);
        load.execute();
        System.exit(0);
    }


    public int execute() {
        initAll(propertyFilePath);
        runLoad();
        return 0;
    }

    private void runLoad() {
        try {
            HibernateUtil.createTransaction();
            List<String> accessionList = parseProteomeFile();
            dropProteome();
            persistProteome(accessionList);
            HibernateUtil.flushAndCommitCurrentSession();
            LOG.info("Committed load...");
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
        createReport();
    }

    // Returns list of accessions = reference proteins out of the fasta file
    private List<String> parseProteomeFile() throws Exception {
        String fileName = "UP000000437_7955.fasta";
        String zipFileName = fileName + ".gz";
        String url = "https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000437/" + zipFileName;
        File zipFile = new File(zipFileName);
        LOG.info("Downloding proteom zip file: " + zipFileName);
        FileUtils.copyURLToFile(
                new URL(url),
                zipFile,
                100000,
                100000);

        FileUtil.gunzipFile(zipFileName, fileName);

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            LOG.error("No load file found: " + fileName);
        }
        BufferedReader br = new BufferedReader(fileReader);
        SymbolTokenization symbolTokenization = RichSequence.IOTools.getProteinParser();
        SimpleNamespace ns = new SimpleNamespace("tr");
        RichSequenceIterator iterator = RichSequence.IOTools.readFasta(br, symbolTokenization, ns);
        List<String> ids = new ArrayList<>();
        LOG.info("Parsing fasta file " + fileName);
        while (iterator.hasNext()) {
            RichSequence richSequence = iterator.nextRichSequence();
            ids.add(richSequence.getAccession());
        }
        return ids;
    }

    private void dropProteome() {

        // drop existing proteome
        int numberDeleted = getSequenceRepository().deleteUnitProtProteome();
        LOG.info("Deleted Reference Proteome. Number of Records: " + numberDeleted);
    }

    private void persistProteome(List<String> ids) throws IOException {
        ReferenceDatabase uniProt = getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.UNIPROTKB, ForeignDBDataType.DataType.POLYPEPTIDE, ForeignDBDataType.SuperType.PROTEIN, Species.Type.ZEBRAFISH);
        List<ReferenceProtein> referenceProteins = new ArrayList<>();
        List<String> referenceProteinsNotFound = new ArrayList<>();
        ids.forEach(accession -> {
            List<MarkerDBLink> links = getSequenceRepository().getMarkerDBLinksForAccession(accession, uniProt);
            if (CollectionUtils.isNotEmpty(links)) {
                MarkerDBLink dblink = links.get(0);
                Marker gene = new Marker();
                gene.setZdbID(dblink.getDataZdbID());
                ReferenceProtein protein = new ReferenceProtein(dblink, gene);
                referenceProteins.add(protein);
            } else {
                referenceProteinsNotFound.add(accession);
            }
        });
        List<String> keys = new ArrayList<>();
        List<ReferenceProtein> referenceProteinsNormalized = new ArrayList<>();
        referenceProteins.forEach(referenceProtein -> {
            String key = referenceProtein.getUniprotAccession().getAccessionNumber() + ":" + referenceProtein.getGene().getZdbID();
            if (!keys.contains(key)) {
                referenceProteinsNormalized.add(referenceProtein);
                keys.add(key);
            }
        });

        LOG.info("Total reference proteins (unnormalized) found: " + referenceProteins.size());

        LOG.info("Total reference proteins not found: " + referenceProteinsNotFound.size());

        referenceProteinsNormalized.forEach(referenceProtein -> HibernateUtil.currentSession().save(referenceProtein));

        // store accessions not found into a separate file
        Path out = Paths.get("accessions-not-found.txt");
        Files.write(out, referenceProteinsNotFound, Charset.defaultCharset());
    }

    private void createReport() {
        //ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, loadDirectory, jobName, true);
//        createErrorReport(null, dataMap.get(NEW_UNIPROT_IDS_REPORT_NAME), reportConfiguration);
    }

    private static void initLogging() {
        initLog4J();
        Configurator.setLevel(LogManager.getLogger(DatabaseService.class).getName(), Level.INFO);
        Configurator.setLevel(LogManager.getLogger(AbstractScriptWrapper.class).getName(), Level.INFO);
        Configurator.setLevel(LogManager.getLogger(LoadReferenceProteome.class).getName(), Level.INFO);
    }
}


