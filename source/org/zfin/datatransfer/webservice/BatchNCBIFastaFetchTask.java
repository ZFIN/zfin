package org.zfin.datatransfer.webservice;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Batch fetch to pull in FASTA files from NCBI
 *
 */
@Log4j2
public class BatchNCBIFastaFetchTask extends AbstractScriptWrapper {

    private static final int BATCH_SIZE = 168;
    @Setter
    private String inputFilename;
    @Setter
    private String outputFilename;
    private BufferedWriter writer;
    private BufferedReader reader;

    public static void main(String[] args) {
        BatchNCBIFastaFetchTask task = new BatchNCBIFastaFetchTask();
        task.run();
    }

    public BatchNCBIFastaFetchTask() {
        initAll();
        initInputs();
        initFiles();
    }

    public BatchNCBIFastaFetchTask(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;

        initAll();
        initInputs();
        initFiles();
    }

    public void run() {
        List<String> accessionLines = getInputLines();
        List<List<String>> batchesOfAccessions = partitionInputs(accessionLines);
        fetchAndWriteFastas(batchesOfAccessions);
        closeFiles();
    }

    private void fetchAndWriteFastas(List<List<String>> batchesOfAccessions) {
        for(List<String> batch : batchesOfAccessions) {
            try {
                String fastaResult = fetchFasta(batch);
                writer.write(fastaResult + "\n");
            } catch (IOException e) {
                LOG.error("Error fetching batch ");
                System.exit(4);
            }
            try {
                Thread.sleep(350); //rate limit
            } catch (InterruptedException e) {
                LOG.error("Thread exception");
                System.exit(5);
            }
        }
    }

    private String fetchFasta(List<String> batch) throws IOException {
        NCBIEfetch.Type type = NCBIEfetch.Type.NUCLEOTIDE;
        String accession = String.join(",", batch);
        LOG.debug("Fetching: " + accession);
        return new NCBIRequest(NCBIRequest.Eutil.FETCH)
                .with("db", type.getVal())
                .with("id", accession)
                .with("retmax", 5000)
                .getFasta();
    }

    private List<List<String>> partitionInputs(List<String> accessionLines) {
        int numBatches = (int)Math.ceil( ((double)accessionLines.size()) / ((double)BATCH_SIZE));
        List<List<String>> resultSet = new ArrayList<>();

        int count = 0;
        for(int i = 0; i < numBatches; i++) {
            List<String> batch = new ArrayList<>();
            for(int j = 0; j < BATCH_SIZE && count < accessionLines.size(); j++) {
                batch.add(accessionLines.get(count));
                count++;
            }
            resultSet.add(batch);
        }

        return resultSet;
    }

    public List<String> getInputLines() {
        List<String> lines = new ArrayList<>();
        try {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            LOG.error("Error reading file from ENV[NCBI_LOAD_INPUT]: " + inputFilename);
            System.exit(2);
        }
        return lines;
    }

    public String toString() {
        return "BatchNCBIFastaFetchTask [inputFilename=" + inputFilename + "] [outputFilename=" + outputFilename + "]";
    }

    private void initFiles() {
        writer = getBufferedWriter();
        try {
            reader = new BufferedReader(new FileReader(inputFilename));
        } catch (FileNotFoundException e) {
            LOG.error("Error reading file from ENV[NCBI_LOAD_INPUT]: " + inputFilename);
            System.exit(2);
        }
    }

    private BufferedWriter getBufferedWriter() {
        try {
            return new BufferedWriter(new FileWriter(outputFilename, true));
        } catch (IOException e) {
            LOG.error("Could not write to " + outputFilename);
            System.exit(3);
        }
        return null;
    }

    private void initInputs() {
        if (inputFilename == null) {
            inputFilename = System.getProperty("ncbiLoadInput");
        }

        if (outputFilename == null) {
            outputFilename = System.getProperty("ncbiLoadOutput");
        }

        if (null == inputFilename || null == outputFilename) {
            LOG.error("Must provide system properties: ncbiLoadInput and ncbiLoadOutput");
            System.exit(1);
        }

        if (!new File(inputFilename).exists()) {
            LOG.error("Input file " + inputFilename + " does not exist");
            System.exit(1);
        }
    }

    private void closeFiles() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            LOG.error("Couldn't close files");
            System.exit(6);
        }
    }

}