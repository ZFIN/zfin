package org.zfin.uniprot;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.biojava.bio.BioException;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.DatFileReader;
import org.zfin.uniprot.interpro.EntryListItemDTO;
import org.zfin.uniprot.interpro.EntryListTranslator;
import org.zfin.uniprot.interpro.ProteinDTO;
import org.zfin.uniprot.secondary.InterproDomainHandler;
import org.zfin.uniprot.secondary.InterproProteinHandler;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zfin.uniprot.secondary.SecondaryLoadContext.fetchExistingInterproDomainRecords;
import static org.zfin.uniprot.secondary.SecondaryLoadContext.fetchExistingProteinRecords;

public class ProteinDomainInfoTest extends AbstractDatabaseTest {
    private final String LEGACY_SP_DIRECTORY = "server_apps/data_transfer/SWISS-PROT/";
    private final String PERLBIN = "/Users/ryan/perl5/perlbrew/perls/perl-5.32.0/bin/perl";
    private final String PERL5LIB = "/Users/ryan/perl5/lib/perl5";

    private final String BASHBIN = "/usr/local/bin/bash";
    private final String PREZFIN = "/tmp/pre_zfin.dat.2023-11";



    @Test
    public void compareToLegacyOutputDomainText() throws IOException, BioException {
        DefaultExecutor executor = new DefaultExecutor();
        String workingDir = getWorkingDir();
        executor.setWorkingDirectory(new File(workingDir));

        Map<String, String> env = new HashMap<>();
        env.put("PGHOST", ZfinPropertiesEnum.PGHOST.value());
        env.put("DB_NAME", ZfinPropertiesEnum.DB_NAME.value());
        env.put("PERL5LIB", PERL5LIB);

        PumpStreamHandler streamHandler = new PumpStreamHandler(System.out, System.err);
        executor.setStreamHandler(streamHandler);

        CommandLine cmdLine = new CommandLine(BASHBIN);
        cmdLine.addArguments(new String[]{"-lc", "cd " + workingDir + " && " + PERLBIN + " protein_domain_info_load_for_testing.pl"}, false);
        int exitValue = executor.execute(cmdLine, env);

        assertFalse(executor.isFailure(exitValue));

        List<EntryListItemDTO> existingDbRecords = fetchExistingInterproDomainRecords();
        assertTrue(existingDbRecords.size() > 0);

        List<EntryListItemDTO> downloadedEntries = EntryListTranslator.parseFile(new File(workingDir + "entry.list"));
        InterproDomainHandler handler = new InterproDomainHandler(downloadedEntries);

        Map<String, RichSequenceAdapter> uniprotRecords = new HashMap<>();
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = new SecondaryLoadContext();
        context.setExistingInterproDomainRecords(existingDbRecords);
        handler.handle(uniprotRecords, actions, context);

        //open file for writing
//        BufferedWriter writer = new FileWriter("/tmp/domain.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/domain.txt"));
        for(SecondaryTermLoadAction action: actions){
            Map<String, String> map = action.getRelatedEntityFields();
            EntryListItemDTO entry = EntryListItemDTO.fromMap(map);
            writer.write(entry.accession() + "|" + entry.name() + "|" + entry.type() + "\n");
        }
        writer.close();

        //check that we have successfully recreated the output of domain.txt:
        CommandLine diffCmd = new CommandLine("diff");
        cmdLine.addArguments(new String[]{workingDir + "/domain.txt", "/tmp/domain.txt"}, false);
        exitValue = executor.execute(cmdLine, env);
        assertFalse(executor.isFailure(exitValue));

    }

    @Test
    public void compareToLegacyOutputProteinText() throws IOException, BioException {
        int exitValue = executeBashCommand(PERLBIN + " protein_domain_info_load_for_testing.pl");
        assertTrue(exitValue == 0);

        Map<String, RichSequenceAdapter> uniprotRecords = DatFileReader.getRecordsFromFile(PREZFIN);
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        SecondaryLoadContext context = SecondaryLoadContext.createFromDBConnection();
        context.setExistingProteinRecords(fetchExistingProteinRecords());
        InterproProteinHandler handler = new InterproProteinHandler();
        handler.handle(uniprotRecords, actions, context);
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
