package org.zfin.uniprot;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.biojava.bio.BioException;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.uniprot.datfiles.DatFileReader;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.*;
import org.zfin.uniprot.interpro.*;
import org.zfin.uniprot.secondary.*;
import org.zfin.uniprot.secondary.handlers.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.ForeignDB.AvailableName.INTERPRO;
import static org.zfin.sequence.ForeignDB.AvailableName.UNIPROTKB;
import static org.zfin.uniprot.UniProtFilterTask.readAllZebrafishEntriesFromSourceIntoRecords;
import static org.zfin.uniprot.secondary.SecondaryLoadContext.*;

public class ProteinDomainInfoTest extends AbstractDatabaseTest {
    private static final String LEGACY_SP_DIRECTORY = "server_apps/data_transfer/SWISS-PROT/";
    private static final String PERLBIN = "/Users/ryan/perl5/perlbrew/perls/perl-5.32.0/bin/perl";
    private static final String PERL5LIB = "/Users/ryan/perl5/lib/perl5";
    private static final String BASHBIN = "/usr/local/bin/bash";
    private static final String PREZFIN = "/tmp/pre_zfin.dat.2023-11";


    /**
     * Test that we correctly handle the case where an existing protein and gene association
     * should be changed because the interpro2go file has changed
     *
     * @throws IOException
     */
    @Test
    public void testAddNewInterpro() throws IOException, BioException {
        //pull in the test data from the pre_zfin.dat file with only a single uniprot record
        BufferedReader inputFileReader = new BufferedReader(new FileReader(
                ZfinPropertiesEnum.SOURCEROOT.value() + "/test/uniprot-data/mgte/pre_zfin.Q90ZE4.dat"));
        UniprotReleaseRecords entries = readAllZebrafishEntriesFromSourceIntoRecords(inputFileReader);

        //set up the pipeline
        SecondaryTermLoadPipeline pipeline = new SecondaryTermLoadPipeline();
        pipeline.setUniprotRecords(entries);
        SecondaryLoadContext context = new SecondaryLoadContext();

        SequenceRepository sr = getSequenceRepository();

        //set existing uniprot records as a single entry (normally would be the entire set of uniprot records in our DB ~50k)
        DBLinkSlimDTO uniprotDBLink = DBLinkSlimDTO.builder()
            .accession("Q90ZE4")
            .dataZdbID("ZDB-GENE-000330-9")
            .markerAbbreviation("psen2")
            .dbName(UNIPROTKB.toString())
            .publicationIDs(List.of("ZDB-PUB-230615-71"))
            .build();
        context.setUniprotDbLinksByList(List.of(uniprotDBLink));

        //set existing interpro records as a single entry (normally would be the entire set of interpro records in our DB ~80k)
        DBLinkSlimDTO interproDBLink = DBLinkSlimDTO.builder()
            .accession("IPR001108")
            .dataZdbID("ZDB-GENE-000330-9")
            .markerAbbreviation("psen2")
            .dbName(INTERPRO.toString())
            .publicationIDs(List.of("ZDB-PUB-230615-71"))
            .build();
        context.setInterproDbLinksByList(List.of(interproDBLink));

        pipeline.setContext(context);
        pipeline.addHandler(new AddNewDBLinksFromUniProtsActionCreator(INTERPRO), AddNewDBLinksFromUniProtsActionProcessor.class);

        //run the pipeline
        List<SecondaryTermLoadAction> results = pipeline.createActions();

        //the test fixture pre_zfin.Q90ZE4.dat has 3 interpros:
        //        DR   InterPro; IPR001108; Peptidase_A22A.
        //        DR   InterPro; IPR006639; Preselin/SPP.
        //        DR   InterPro; IPR042524; Presenilin_C.
        //since our test is simulating a case where the existing DB has IPR001108, we should expect 2 new interpros to be added
        assertEquals(2, results.size());
        assertEquals("IPR006639", results.get(0).getAccession());
        assertEquals("IPR042524", results.get(1).getAccession());
    }

    @Test
    public void testInterpro2goFileHasChanged() throws IOException, BioException {
        //pull in the test data from the pre_zfin.dat file with only a single uniprot record
        BufferedReader inputFileReader = new BufferedReader(new FileReader(
                ZfinPropertiesEnum.SOURCEROOT.value() + "/test/uniprot-data/mgte/pre_zfin.dat.2023-11"));
        UniprotReleaseRecords entries = readAllZebrafishEntriesFromSourceIntoRecords(inputFileReader);

        //set up the pipeline
        SecondaryTermLoadPipeline pipeline = new SecondaryTermLoadPipeline();
        pipeline.setUniprotRecords(entries);
        SecondaryLoadContext context = new SecondaryLoadContext();

        //initialize to represent the existing data in the DB
        context.initializeUniprotDBLinksFromDatabase();
        context.initializeInterproDBLinksFromDatabase();
        context.initializeMarkerGoTermEvidenceFromDatabase();

        pipeline.setContext(context);

        //pull in the interpro2go file for translation
        String ipToGoFile = ZfinPropertiesEnum.SOURCEROOT.value() + "/test/uniprot-data/mgte/interpro2go";
        List<SecondaryTerm2GoTerm> ipToGoRecords =
                SecondaryTerm2GoTermTranslator.convertTranslationFileToUnloadFile(ipToGoFile, SecondaryTerm2GoTermTranslator.SecondaryTermType.InterPro);

        pipeline.addHandler(new MarkerGoTermEvidenceActionCreator(INTERPRO, ipToGoRecords), MarkerGoTermEvidenceActionProcessor.class);


        //run the pipeline
        List<SecondaryTermLoadAction> results = pipeline.createActions();

        //the test fixture pre_zfin.Q90ZE4.dat has 3 interpros:
        //        DR   InterPro; IPR001108; Peptidase_A22A.
        //        DR   InterPro; IPR006639; Preselin/SPP.
        //        DR   InterPro; IPR042524; Presenilin_C.
        //since our test is simulating a case where the existing DB has IPR001108, we should expect 2 new interpros to be added
        assertEquals(2, results.size());
        assertEquals("IPR006639", results.get(0).getAccession());
        assertEquals("IPR042524", results.get(1).getAccession());
    }

    @Test
    @Ignore("This test is too specific to a particular desktop environment and takes too long to run")
    public void compareToLegacyOutputDomainText() throws IOException, BioException {
//        int exitValue = executeBashCommand(PERLBIN + " protein_domain_info_load_for_testing.pl");
//        assertTrue(exitValue == 0);

        List<InterProProteinDTO> existingDbRecords = fetchExistingInterproDomainRecords();
        assertTrue(existingDbRecords.size() > 0);

        List<InterProProteinDTO> downloadedEntries = EntryListTranslator.parseFile(new File(getWorkingDir() + "entry.list"));
        InterproDomainActionCreator handler = new InterproDomainActionCreator(downloadedEntries);

        UniprotReleaseRecords uniprotRecords = new UniprotReleaseRecords();
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = new SecondaryLoadContext();
        context.setExistingInterproDomainRecords(existingDbRecords);
        handler.createActions(uniprotRecords, actions, context);

        //open file for writing
//        BufferedWriter writer = new FileWriter("/tmp/domain.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/domain.txt"));
        for(SecondaryTermLoadAction action: actions){
            Map<String, String> map = action.getRelatedEntityFields();
            InterProProteinDTO entry = InterProProteinDTO.fromMap(map);
            writer.write(entry.accession() + "|" + entry.name() + "|" + entry.type() + "\n");
        }
        writer.close();

//        SecondaryTermLoadService.processActions(actions, null);

        //check that we have successfully recreated the output of domain.txt:
        int exitValue = executeBashCommand("diff " + getWorkingDir() + "/domain.txt /tmp/domain.txt");
        assertTrue(exitValue == 0);


    }

    @Test
    @Ignore("This test is too specific to a particular desktop environment and takes too long to run")
    public void compareToLegacyOutputProteinText() throws IOException, BioException {
        int exitValue = executeBashCommand(PERLBIN + " protein_domain_info_load_for_testing.pl");
        assertTrue(exitValue == 0);

        UniprotReleaseRecords uniprotRecords = DatFileReader.getRecordsFromFile(PREZFIN);
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = SecondaryLoadContext.createFromDBConnection();
        context.setExistingProteinRecords(fetchExistingProteinRecords());
        InterproProteinActionCreator handler = new InterproProteinActionCreator();
        handler.createActions(uniprotRecords, actions, context);
        assertTrue(actions.size() > 0);

        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/protein.txt"));
        for(SecondaryTermLoadAction action: actions){
            Map<String, String> map = action.getRelatedEntityFields();
            ProteinDTO entry = new ProteinDTO(action.getAccession(), action.getLength());
            writer.write(entry.accession() + "|ZDB-FDBCONT-040412-47|" + entry.length() + "\n");
        }
        writer.close();

        //sort them
        executeBashCommand("sort " + getWorkingDir() + "/protein.txt -o " + getWorkingDir() + "/protein.txt");
        executeBashCommand("sort /tmp/protein.txt -o /tmp/protein.txt");

        exitValue = executeBashCommand("diff " + getWorkingDir() + "/protein.txt /tmp/protein.txt");
        assertTrue(exitValue == 0);
    }

    @Test
    @Ignore("This test is too specific to a particular desktop environment and takes too long to run")
    public void compareToLegacyOutputMarkerToProteinText() throws IOException, BioException {
//        int exitValue = executeBashCommand(PERLBIN + " protein_domain_info_load_for_testing.pl");
//        assertTrue(exitValue == 0);

        UniprotReleaseRecords uniprotRecords = DatFileReader.getRecordsFromFile(PREZFIN);
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = SecondaryLoadContext.createFromDBConnection();
        context.setExistingMarkerToProteinRecords(fetchExistingMarkerToProteinRecords());
        InterproMarkerToProteinActionCreator handler = new InterproMarkerToProteinActionCreator();
        handler.createActions(uniprotRecords, actions, context);
        assertTrue(actions.size() > 0);

        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/zfinprotein.txt"));
        for(SecondaryTermLoadAction action: actions){
            assertEquals(SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN, action.getSubType());
            MarkerToProteinDTO entry = new MarkerToProteinDTO(action.getGeneZdbID(), action.getAccession());
            writer.write(entry.markerZdbID() + "|" + entry.accession() + "\n");
        }
        writer.close();

        //sort them
        executeBashCommand("sort " + getWorkingDir() + "/zfinprotein.txt -o " + getWorkingDir() + "/zfinprotein.txt");
        executeBashCommand("sort /tmp/zfinprotein.txt -o /tmp/zfinprotein.txt");

        int exitValue = executeBashCommand("diff " + getWorkingDir() + "/zfinprotein.txt /tmp/zfinprotein.txt");
        assertTrue(exitValue == 0);
    }

    @Test
    @Ignore("This test is too specific to a particular desktop environment and takes too long to run")
    public void compareToLegacyOutputProteinToInterproText() throws IOException, BioException {
//        int exitValue = executeBashCommand(PERLBIN + " protein_domain_info_load_for_testing.pl");
//        assertTrue(exitValue == 0);

        UniprotReleaseRecords uniprotRecords = DatFileReader.getRecordsFromFile(PREZFIN);
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = SecondaryLoadContext.createFromDBConnection();
        context.setExistingProteinToInterproRecords(fetchExistingProteinToInterproRecords());
        ProteinToInterproActionCreator handler = new ProteinToInterproActionCreator();
        handler.createActions(uniprotRecords, actions, context);
        assertTrue(actions.size() > 0);

        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/unipro2interpro.txt"));
        for(SecondaryTermLoadAction action: actions){
            assertEquals(SecondaryTermLoadAction.SubType.PROTEIN_TO_INTERPRO, action.getSubType());
            ProteinToInterproDTO entry = ProteinToInterproDTO.fromMap(action.getRelatedEntityFields());
            writer.write(entry.uniprot() + "|" + entry.interpro() + "\n");
        }
        writer.close();

        //sort them
        executeBashCommand("sort " + getWorkingDir() + "/unipro2interpro.txt -o " + getWorkingDir() + "/unipro2interpro.txt");
        executeBashCommand("sort /tmp/unipro2interpro.txt -o /tmp/unipro2interpro.txt");

        int exitValue = executeBashCommand("diff " + getWorkingDir() + "/unipro2interpro.txt /tmp/unipro2interpro.txt");
        assertTrue(exitValue == 0);
    }
    
    @Test
    @Ignore("This test is too specific to a particular desktop environment and takes too long to run")
    public void compareToLegacyOutputProteinToPDBText() throws IOException, BioException {
//        int exitValue = executeBashCommand(PERLBIN + " protein_domain_info_load_for_testing.pl");
//        assertTrue(exitValue == 0);

        UniprotReleaseRecords uniprotRecords = DatFileReader.getRecordsFromFile(PREZFIN);
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = SecondaryLoadContext.createFromDBConnection();
        context.setExistingPdbRecords(fetchExistingPdbRecords());
        PDBActionCreator handler = new PDBActionCreator();
        handler.createActions(uniprotRecords, actions, context);
        assertTrue(actions.size() > 0);

        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/unipro2pdb.txt"));
        for(SecondaryTermLoadAction action: actions){
            assertEquals(SecondaryTermLoadAction.SubType.PDB, action.getSubType());
            PdbDTO entry = PdbDTO.fromMap(action.getRelatedEntityFields());
            writer.write(entry.uniprot() + "|" + entry.pdb() + "\n");
        }
        writer.close();

        //sort them
        executeBashCommand("sort " + getWorkingDir() + "/unipro2pdb.txt -o " + getWorkingDir() + "/unipro2pdb.txt");
        executeBashCommand("sort /tmp/unipro2pdb.txt -o /tmp/unipro2pdb.txt");

        int exitValue = executeBashCommand("diff " + getWorkingDir() + "/unipro2pdb.txt /tmp/unipro2pdb.txt");
        assertTrue(exitValue == 0);
    }

    @NotNull
    private String getWorkingDir() {
        String workingDir = ZfinPropertiesEnum.SOURCEROOT.value() + "/" + LEGACY_SP_DIRECTORY;
        return workingDir;
    }

    private int executeBashCommand(String command) throws IOException {
        String workingDir = getWorkingDir();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workingDir));
        Map<String, String> env = new HashMap<>();
        env.put("PGHOST", ZfinPropertiesEnum.PGHOST.value());
        env.put("DB_NAME", ZfinPropertiesEnum.DB_NAME.value());
        env.put("PERL5LIB", PERL5LIB);
        PumpStreamHandler streamHandler = new PumpStreamHandler(System.out, System.err);
        executor.setStreamHandler(streamHandler);
        CommandLine cmdLine = new CommandLine(BASHBIN);
        cmdLine.addArguments(new String[]{"-lc", command}, false);
        int exitValue = executor.execute(cmdLine, env);
        return exitValue;
    }

}
