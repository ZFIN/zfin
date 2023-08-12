package org.zfin.uniprot;

import lombok.extern.log4j.Log4j;
import org.biojava.bio.BioException;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Combine filtering and diffing into one task.
 * Input files for release 1 can be a single file or multiple files. They can be gzipped or not.
 * Input files for release 2 can be a single file or multiple files. They can be gzipped or not.
 * Output file is a single report file that contains the JSON diff.
 */
@Log4j
public class UniProtReleaseDiffTask extends AbstractScriptWrapper {
    private String inputFilenameSet1;
    private String inputFilenameSet2;
    private String outputFilename;
    private Path tempDirectory;

    public UniProtReleaseDiffTask(String inputFilenameSet1, String inputFilenameSet2, String outputFilename) {
        this.inputFilenameSet1 = inputFilenameSet1;
        this.inputFilenameSet2 = inputFilenameSet2;
        this.outputFilename = outputFilename;
    }

    public static void main(String[] args) {
        String set1 = getArgOrEnvironmentVar(args, 0, "INPUT_FILESET_1");
        String set2 = getArgOrEnvironmentVar(args, 1, "INPUT_FILESET_2");
        String output = getArgOrEnvironmentVar(args, 2, "OUTPUT_FILE");

        UniProtReleaseDiffTask task = new UniProtReleaseDiffTask(set1, set2, output);

        try {
            task.runTask();
        } catch (IOException e) {
            System.err.println("IOException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (BioException e) {
            System.err.println("BioException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (SQLException e) {
            System.err.println("SQLException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }

        HibernateUtil.closeSession();
        System.exit(0);
    }

    public void runTask() throws IOException, BioException, SQLException {
        initAll();
        initWorkspace();

        //handle input file set 1
        //filter the input files using a filter object (FilterTask) and concatenate to create a single filtered file
        Path filteredFile1 = combineAndFilterInputFileSet(inputFilenameSet1, "filtered_set1.dat");

        //handle input file set 2 the same way
        Path filteredFile2 = combineAndFilterInputFileSet(inputFilenameSet2, "filtered_set2.dat");

        //pass the 2 filtered files to the diff task (UniProtCompareTask) and get the result

        //cleanup

    }

    private void initWorkspace() {
        //create a temp workspace directory
        try {
            tempDirectory = Files.createTempDirectory("uniprot_diff_" + System.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private List<File> getInputFiles(String filePath) {
        List<File> fileList = new ArrayList<>();

        String[] filenames = filePath.split(",");
        for (String file : filenames) {
            Path path = Path.of(file.trim());
            if (!Files.exists(path)) {
                System.err.println("File does not exist: " + path.toAbsolutePath());
                System.exit(1);
            }
            LOG.debug("Input file: " + path.getFileName());
            fileList.add(path.toFile());
        }

        return fileList;
    }

    private Path combineAndFilterInputFileSet(String inputFileNames, String outputFilename) throws IOException {
        //create a set of input files from the input file set 1 based on comma separated list
        List<File> inputFiles = getInputFiles(inputFileNames);

        //for each of the input files, check if they are gzipped or not and create appropriate readers
        List<BufferedReader> inputFileReaderList = inputFiles.stream().map(file -> getReaderForFile(file)).toList();

        Path outputFilePath = tempDirectory.resolve(outputFilename);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath.toFile());

        try {
            for(File inputFile : inputFiles) {
                System.out.println("Filtering file: " + inputFile.getAbsolutePath());
                UniProtFilterTask filterTask = new UniProtFilterTask(new BufferedReader(new FileReader(inputFile)), fileOutputStream);
                filterTask.runTask();
            }
        } catch (Exception e) {
            System.err.println("Exception Error while running task: " + e.getMessage());
            System.exit(1);
        }

        return outputFilePath;
    }

    private BufferedReader getReaderForFile(File file) {
        try {
            InputStream fileStream = new FileInputStream(file);
            InputStream input;

            if (file.getName().endsWith(".gz")) {
                input = new GZIPInputStream(fileStream);
            } else {
                input = fileStream;
            }

            return new BufferedReader(new InputStreamReader(input));
        } catch (IOException e) {
            System.err.println("Error while opening file: " + file.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private static String getArgOrEnvironmentVar(String[] args, int index, String envVar) {
        if (args.length > index && args[index] != null) {
            return args[index];
        }

        String result = System.getenv(envVar);

        if (result == null) {
            System.err.println("Missing required argument: " + envVar + ". Please provide it as an environment variable or as argument: " + (index + 1) + ". ");
            System.exit(1);
        }

        return result;
    }

}
