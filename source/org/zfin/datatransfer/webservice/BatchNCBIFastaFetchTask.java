package org.zfin.datatransfer.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.datatransfer.ServiceConnectionException;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        BatchNCBIFastaFetchTask task = new BatchNCBIFastaFetchTask(args);
        task.run();
    }

    public BatchNCBIFastaFetchTask(String[] args) {
        initAll();
        initInputs(args);
        initFiles();
    }

    public BatchNCBIFastaFetchTask(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;

        initAll();
        initInputs(new String[]{});
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
        Map<String, List<String>> batchTypes = splitBatchByDatabase(batch);

        List<String> nucleotideAccessions = batchTypes.get(NCBIEfetch.Type.NUCLEOTIDE.getVal());
        String nucleotideAccessionsString = String.join(",", nucleotideAccessions);

        List<String> proteinAccessions = batchTypes.get(NCBIEfetch.Type.POLYPEPTIDE.getVal());
        String proteinAccessionsString = String.join(",", proteinAccessions);

        if (nucleotideAccessions.size() + proteinAccessions.size() != batch.size()) {
            LOG.error("Batch size mismatch: " + batch.size() + " != " + (nucleotideAccessions.size() + proteinAccessions.size()));
        }

        LOG.debug("Fetching nucleotides: " + nucleotideAccessionsString);
        String nucleotideFasta = "";
        if (!nucleotideAccessions.isEmpty()) {
            nucleotideFasta = new NCBIRequest(NCBIRequest.Eutil.FETCH)
                    .with("db", NCBIEfetch.Type.NUCLEOTIDE.getVal())
                    .with("id", nucleotideAccessionsString)
                    .with("retmax", 5000)
                    .getFasta();
        }

        LOG.debug("Fetching proteins: " + proteinAccessionsString);
        String proteinFasta = "";
        if (!proteinAccessions.isEmpty()) {
            proteinFasta = new NCBIRequest(NCBIRequest.Eutil.FETCH)
                .with("db", NCBIEfetch.Type.POLYPEPTIDE.getVal())
                .with("id", proteinAccessionsString)
                .with("retmax", 5000)
                .getFasta();
        }

        return String.join("\n", List.of(nucleotideFasta, proteinFasta));
    }

    private Map<String, List<String>> splitBatchByDatabase(List<String> batch) {
        String accessions = String.join(",", batch);
        //hit the esearch endpoint to determine which database each accession belongs to
        try {
            String json = new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                    .with("db", NCBIEfetch.Type.NUCLEOTIDE.getVal())
                    .with("term", accessions)
                    .with("retmax", 5000)
                    .with("retmode", "json")
                    .fetchRawText();
            //parse out ".esearchresult.idlist" from response:
            List<String> nucleotideIdList = parseIdListFromJson(json);

            json = new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                    .with("db", NCBIEfetch.Type.POLYPEPTIDE.getVal())
                    .with("term", accessions)
                    .with("retmax", 5000)
                    .with("retmode", "json")
                    .fetchRawText();
            //parse out ".esearchresult.idlist" from response:
            List<String> proteinIdList = parseIdListFromJson(json);

            return Map.of(
                    NCBIEfetch.Type.NUCLEOTIDE.getVal(), nucleotideIdList,
                    NCBIEfetch.Type.POLYPEPTIDE.getVal(), proteinIdList
            );
        } catch (ServiceConnectionException e) {
            System.out.println("Error fetching accession lines: " + accessions);
            return new HashMap<>();
        }
    }

    private List<String> parseIdListFromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
            JsonNode idListNode = root.path("esearchresult").path("idlist");
            List<String> idList = new ArrayList<>();
            if (idListNode.isArray()) {
                for (JsonNode idNode : idListNode) {
                    idList.add(idNode.asText());
                }
            }
            return idList;
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private List<List<String>> partitionInputs(List<String> accessionLines) {
        return ListUtils.partition(accessionLines, BATCH_SIZE);
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

    private void initInputs(String[] args) {
        if (inputFilename == null) {
            inputFilename = System.getProperty("ncbiLoadInput");
        }

        if (outputFilename == null) {
            outputFilename = System.getProperty("ncbiLoadOutput");
        }

        if (null == inputFilename && null == outputFilename && args.length == 2) {
            inputFilename = args[0];
            outputFilename = args[1];
        }

        if (null == inputFilename || null == outputFilename) {
            LOG.error("Must provide system properties: ncbiLoadInput and ncbiLoadOutput, or the same as command line arguments 1 and 2");
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