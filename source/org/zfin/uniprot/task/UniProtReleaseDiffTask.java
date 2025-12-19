package org.zfin.uniprot.task;

import lombok.extern.log4j.Log4j2;
import org.biojava.bio.BioException;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.uniprot.UniProtFilterTask;
import org.zfin.uniprot.UniProtRoughTaxonFilter;
import org.zfin.uniprot.persistence.UniProtRelease;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.uniprot.UniProtTools.getArgOrEnvironmentVar;

/**
 * Combine filtering and diffing into one task.
 * Input files for release 1 can be a single file or multiple files. They can be gzipped or not.
 * Input files for release 2 can be a single file or multiple files. They can be gzipped or not.
 * Output file is a single report file that contains the JSON diff.
 *
 * Invoke this class with these environment variables:
 * - INPUT_FILESET_1 to point to the first input dat file(s). Can be a comma separated list of files that may be gzipped or not (also accepted as first argument to main).
 * - INPUT_FILESET_2 to point to the second input dat file(s). Can be a comma separated list of files that may be gzipped or not (also accepted as second argument to main).
 * - OUTPUT_FILE as the output json file name (also accepted as third argument to main).
 * - KEEP_TEMP_FILES_IN as the directory to keep the temporary files in. Set to "" to not preserve.  These temp files are:
 *   - filtered_set1.dat: The result of filtering and concatenating the first input fileset.
 *   - filtered_set2.dat: The result of filtering and concatenating the second input fileset.
 *
 * The OUTPUT_FILE is generated as a json blob.
 * There is also an html report that is generated for viewing the json blob.  It will be named OUTPUT_FILE.report.html.
 *
 */
@Log4j2
public class UniProtReleaseDiffTask extends AbstractScriptWrapper {
    private String inputFilenameSet1;
    private String inputFilenameSet2;
    private final String outputFilename;
    private final String release1;
    private final String release2;
    private Path tempDirectory;

    private final String keepTempFilesIn;

    public UniProtReleaseDiffTask(String inputFilenameSet1, String inputFilenameSet2, String release1, String release2, String outputFilename, String keepTempFiles) {
        this.inputFilenameSet1 = inputFilenameSet1;
        this.inputFilenameSet2 = inputFilenameSet2;
        this.release1 = release1;
        this.release2 = release2;
        this.outputFilename = outputFilename;
        this.keepTempFilesIn = keepTempFiles;
    }

    public static void main(String[] args) {
        String set1 = getArgOrEnvironmentVar(args, 0, "INPUT_FILESET_1", "");
        String set2 = getArgOrEnvironmentVar(args, 1, "INPUT_FILESET_2", "");
        String release1 = System.getenv("DB_RELEASE_1");
        String release2 = System.getenv("DB_RELEASE_2");

        String output = getArgOrEnvironmentVar(args, 2, "OUTPUT_FILE");
        String keepTempFiles = getArgOrEnvironmentVar(args, 3, "KEEP_TEMP_FILES_IN");
        UniProtReleaseDiffTask task = new UniProtReleaseDiffTask(set1, set2, release1, release2, output, keepTempFiles);

        try {
            task.runTask();
        } catch (Exception e) {
            System.err.println("Exception Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        HibernateUtil.closeSession();
        System.exit(0);
    }

    private void printUsageAndDie() {
        System.err.println("Usage: UniProtReleaseDiffTask <input fileset 1> <input fileset 2> <output file> <keep temp files in>");
        System.err.println("       alternatively, the arguments can be specified as environment variables:");
        System.err.println("       INPUT_FILESET_1, INPUT_FILESET_2, OUTPUT_FILE, KEEP_TEMP_FILES_IN");
        System.err.println("       The input filesets can be a single file or a comma separated list of files.");
        System.err.println("       The output file is the name of the report file to be generated.");
        System.err.println("       The KEEP_TEMP_FILES_IN is the directory to keep the temporary files in. Set to \"__DELETE__\" to not preserve.");
        System.err.println("       ");
        System.err.println("       Instead of INPUT_FILESET_1, you can also specify DB_RELEASE_1 as an environment variable and set it to the release number.");
        System.err.println("       Instead of INPUT_FILESET_2, you can also specify DB_RELEASE_2 as an environment variable and set it to the release number.");
        System.err.println("       ");
        System.err.println("       Existing releases for DB_RELEASE_1 and DB_RELEASE_2 are:");

        List<UniProtRelease> allReleases = getInfrastructureRepository().getAllUniProtReleases();
        allReleases.sort(Comparator.comparing(UniProtRelease::getUpr_id));
        allReleases.forEach(release -> System.err.println("       " + release.getUpr_id() + ": " + release.getPath() + " (run command with DB_RELEASE_1=" + release.getUpr_id() + " for example)"));

        System.exit(1);
    }


    public void runTask() throws IOException, BioException, SQLException {
        initAll();
        initWorkspace();

        //handle input file set 1
        //filter the input files using a filter object (FilterTask) and concatenate to create a single filtered file
        Path filteredFile1 = combineAndFilterInputFileSet(inputFilenameSet1, tempDirectory.resolve("filtered_set1.dat"));
        System.out.println("Filtered file 1: " + filteredFile1.toAbsolutePath());

        //handle input file set 2 the same way
        Path filteredFile2 = combineAndFilterInputFileSet(inputFilenameSet2, tempDirectory.resolve("filtered_set2.dat"));
        System.out.println("Filtered file 2: " + filteredFile2.toAbsolutePath());

        //pass the 2 filtered files to the diff task (UniProtCompareTask) and get the result
        UniProtCompareTask compareTask = new UniProtCompareTask(
                filteredFile1.toString(),
                filteredFile2.toString(),
                outputFilename);

        compareTask.runTask();

        //cleanup
        if (keepTempFilesIn != null && !keepTempFilesIn.equals("") && !keepTempFilesIn.equals("__DELETE__")) {
            Path keepFilesPath = Path.of(keepTempFilesIn);
            if (!Files.exists(keepFilesPath)) {
                System.err.println("Keep files path does not exist: " + keepFilesPath.toAbsolutePath());
                System.err.println("Keeping temp files in: " + tempDirectory.toAbsolutePath());
            } else if (!Files.isDirectory(keepFilesPath)) {
                System.err.println("Keep files path is not a directory: " + keepFilesPath.toAbsolutePath());
                System.err.println("Keeping temp files in: " + tempDirectory.toAbsolutePath());
            } else {
                System.out.println("Moving temp files to: " + keepFilesPath.toAbsolutePath());
                Files.move(filteredFile1, keepFilesPath.resolve(filteredFile1.getFileName()));
                Files.move(filteredFile2, keepFilesPath.resolve(filteredFile2.getFileName()));
                Files.deleteIfExists(tempDirectory);
            }
        } else {
            Files.deleteIfExists(filteredFile1);
            Files.deleteIfExists(filteredFile2);
            Files.deleteIfExists(tempDirectory);
        }

        System.out.println("File diff written to " + outputFilename);
    }

    private void initWorkspace() {
        //create a temp workspace directory
        try {
            tempDirectory = Files.createTempDirectory("uniprot_diff_" + System.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //check arguments
        if (StringUtils.isEmpty(inputFilenameSet1) || StringUtils.isEmpty(inputFilenameSet2)) {
            //check release as backup
            if (StringUtils.isEmpty(release1) || StringUtils.isEmpty(release2)) {
                System.err.println("No input file sets specified.");
                printUsageAndDie();
            } else {
                //get the release filesets
                log.info("Searching for release by ID: " + release1 + " and " + release2 + ".");
                inputFilenameSet1 = getInfrastructureRepository().getUniProtReleaseByID(Long.valueOf(release1)).getLocalFile().getAbsolutePath();
                inputFilenameSet2 = getInfrastructureRepository().getUniProtReleaseByID(Long.valueOf(release2)).getLocalFile().getAbsolutePath();
                log.info("Input file set 1 based on " + release1 + ": " + inputFilenameSet1);
                log.info("Input file set 2 based on " + release2 + ": " + inputFilenameSet2);
            }
        }

        //still empty?
        if (StringUtils.isEmpty(inputFilenameSet1) || StringUtils.isEmpty(inputFilenameSet2)) {
            System.err.println("No input file sets found based on arguments.");
            printUsageAndDie();
        }

    }


    private static List<File> getInputFiles(String filePath) {
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

    public static Path combineAndFilterInputPathSet(List<Path> inputFiles, Path outputFile) throws IOException {
        return combineAndFilterInputFileSet(inputFiles.stream().map(Path::toFile).toList(), outputFile);
    }

    public static Path combineAndFilterInputFileSet(List<File> inputFiles, Path outputFile) throws IOException {
        //for each of the input files, check if they are gzipped or not and create appropriate readers
        List<BufferedReader> inputFileReaderList = inputFiles.stream().map(UniProtReleaseDiffTask::getReaderForFile).toList();

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile.toFile());

        try {
            for(BufferedReader inputReader : inputFileReaderList) {
                UniProtFilterTask filterTask = new UniProtFilterTask(inputReader, fileOutputStream);
                filterTask.runTask();
            }
        } catch (Exception e) {
            System.err.println("Exception Error while running task: " + e.getMessage());
            log.error("Exception Error while running task: " + e.getMessage(), e);
            throw new IOException(e);
        }

        return outputFile;
    }

    public static Path combineAndFilterInputFileSet(String inputFileNames, Path outputFile) throws IOException {
        //create a set of input files from the input file set 1 based on comma separated list
        log.info("Filtering file(s): " + String.join(",", inputFileNames));

        List<File> inputFiles = getInputFiles(inputFileNames);
        return combineAndFilterInputFileSet(inputFiles, outputFile);
    }

    private static BufferedReader getReaderForFile(File file) {
        try {
            InputStream fileStream = new FileInputStream(file);
            InputStream input;

            if (file.getName().endsWith(".gz")) {
                input = new GZIPInputStream(fileStream);
            } else {
                input = fileStream;
            }

            return (new UniProtRoughTaxonFilter(new InputStreamReader(input))).getFilteredReader();
        } catch (IOException e) {
            System.err.println("Error while opening file: " + file.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }


}
