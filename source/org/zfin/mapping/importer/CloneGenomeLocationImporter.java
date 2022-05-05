package org.zfin.mapping.importer;

import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getLinkageRepository;

public class CloneGenomeLocationImporter extends AbstractScriptWrapper {

    private String inputFilename;

    public static void main(String[] args) {
        CloneGenomeLocationImporter cgli = new CloneGenomeLocationImporter();
        cgli.runTask();
    }

    public void runTask() {
        initAll();
        getInputFilenameOrDie();
        List<AGPEntry> agpEntries = loadAgpEntriesFromFile();
        saveAgpEntriesToDatabase(agpEntries);

    }

    private void getInputFilenameOrDie() {
        if (System.getenv("AGP_FILE") != null) {
            inputFilename = System.getenv("AGP_FILE");
        } else {
            printUsage();
            System.exit(1);
        }
    }

    private List<AGPEntry> loadAgpEntriesFromFile() {
        List<AGPEntry> data = new ArrayList<>();
        try {
            System.out.println("Loading " + inputFilename);
            File inputFile = new File(inputFilename);

            BufferedReader input;
            input = new BufferedReader( new FileReader(inputFile));
            data = input.lines()
                        .map(AGPEntry::new)
                        .collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Error loading file");
            System.exit(1);
        }
        return data;
    }

    private void saveAgpEntriesToDatabase(List<AGPEntry> agpEntries) {
        Transaction transaction = HibernateUtil.createTransaction();
        for(AGPEntry entry : agpEntries) {
            getLinkageRepository().saveAGPEntry(entry);
        }
        transaction.commit();
    }

    public static void printUsage() {
        System.err.println("Call with AGP file environment variable AGP_FILE");
    }
}
