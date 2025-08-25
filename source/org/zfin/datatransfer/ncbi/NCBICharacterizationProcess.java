package org.zfin.datatransfer.ncbi;

import org.apache.logging.log4j.core.util.FileUtils;
import org.zfin.datatransfer.util.CSVDiff;
import org.zfin.datatransfer.util.CSVToXLSXConverter;
import org.zfin.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.util.FileUtil.createZipArchive;

class NCBICharacterizationProcess {
    private final String beforeFilePath;
    private final String afterFilePath;

    public NCBICharacterizationProcess(String beforeFilePath, String afterFilePath) {
        this.beforeFilePath = beforeFilePath;
        this.afterFilePath = afterFilePath;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: NCBICharacterizationProcess <beforeFile> <afterFile>");
            System.err.println("Example: NCBICharacterizationProcess combined_before.csv combined_after.csv");
            System.err.println("Expect two CSV files that have already been combined with attribution data");
            System.err.println("When finished, details are saved to before_after.xlsx in the current directory");
            System.exit(1);
        }
        
        try {
            NCBICharacterizationProcess tester = new NCBICharacterizationProcess(args[0], args[1]);
            tester.run();
            System.out.println("Processing completed successfully.");
        } catch (IOException e) {
            System.err.println("Error processing CSV files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void run() throws IOException {
        validateInputFiles();

        File parentDir = tempDirName();
        FileUtils.mkdir(parentDir, true);

        System.out.println("Writing to " + parentDir);

        //break down changes into subsets using the provided before and after files
        String outputPrefix = new File(parentDir, "ncbi_compare_").toString();
        CSVDiff diff = new CSVDiff(outputPrefix,
                new String[]{"dblink_linked_recid","dblink_acc_num","dblink_fdbcont_zdb_id","recattrib_source_zdb_id"},
                new String[]{"dblink_info","dblink_zdb_id"});

        List<File> subsets = diff.process(beforeFilePath, afterFilePath);
        CSVToXLSXConverter converter = new CSVToXLSXConverter();
        converter.run(new File(new File(beforeFilePath).getParent(), "before_after.xlsx"),
                subsets,
                subsets.stream().map(
                        f -> f.getName().replace(".csv", "")
                                .replace("ncbi_compare_", ""))
                        .collect(Collectors.toList()),
                true
                );
//        createZipArchive(new File(new File(beforeFilePath).getParent(), "before_after.zip"), subsets);


        parentDir.delete();
    }

    private File tempDirName() {
        return new File("/tmp/ncbi_temp_" + DateUtil.nowToString("yyyy-MM-dd_HH-mm-ss"));
    }

    private void validateInputFiles() throws IllegalArgumentException {
        File beforeFile = new File(beforeFilePath);
        File afterFile = new File(afterFilePath);
        
        if (!beforeFile.exists()) {
            throw new IllegalArgumentException("Before file does not exist: " + beforeFilePath);
        }
        
        if (!afterFile.exists()) {
            throw new IllegalArgumentException("After file does not exist: " + afterFilePath);
        }
        
        if (!beforeFile.canRead()) {
            throw new IllegalArgumentException("Cannot read before file: " + beforeFilePath);
        }
        
        if (!afterFile.canRead()) {
            throw new IllegalArgumentException("Cannot read after file: " + afterFilePath);
        }
    }


}