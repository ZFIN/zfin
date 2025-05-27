package org.zfin.datatransfer.ncbi;

import org.apache.logging.log4j.core.util.FileUtils;
import org.zfin.datatransfer.util.CSVDiff;
import org.zfin.datatransfer.util.CSVMerge;
import org.zfin.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.zfin.util.FileUtil.createZipArchive;

class NCBICharacterizationProcess {
    private final String path;

    public NCBICharacterizationProcess(String path) {
        this.path = path;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: NCBICharacterizationProcess <pathToNCBIBeforeAfterFiles>");
            System.err.println("Example: NCBICharacterizationProcess $TARGETROOT/server_apps/datatransfer/NCBIGENE");
            System.err.println("Expect the path to the directory containing the NCBI files to include before_db_link.csv, after_db_link.csv, before_recattrib.csv, and after_recattrib.csv");
            System.err.println("When finished, details are saved to before_after.zip in the provided path");
            System.exit(1);
        }
        NCBICharacterizationProcess tester = new NCBICharacterizationProcess(args[0]);
        tester.run();
    }

    private void run() throws IOException {
        assertNCBIFilesExist();

        File parentDir = tempDirName();
        FileUtils.mkdir(parentDir, true);

        System.out.println("Writing to " + parentDir);

        File beforeFile = new File(parentDir, "combined_before.csv");
        File afterFile = new File(parentDir, "combined_after.csv");

        //merge the db_link and recattrib table dumps into a single table dump that looks like db_link but with attributions
        CSVMerge merger1 = new CSVMerge(
                new File(path, "before_db_link.csv"),
                new File(path, "before_recattrib.csv"),
                "dblink_zdb_id",
                "recattrib_data_zdb_id",
                "recattrib_source_zdb_id",
                beforeFile
        );
        merger1.run();

        CSVMerge merger2 = new CSVMerge(
                new File(path, "after_db_link.csv"),
                new File(path, "after_recattrib.csv"),
                "dblink_zdb_id",
                "recattrib_data_zdb_id",
                "recattrib_source_zdb_id",
                afterFile
        );
        merger2.run();

        //now that we have the combined before and after tables (as csv files), break down changes into subsets
        String outputPrefix = new File(parentDir, "ncbi_compare_").toString();
        CSVDiff diff = new CSVDiff(outputPrefix,
                new String[]{"dblink_linked_recid","dblink_acc_num","dblink_fdbcont_zdb_id","recattrib_source_zdb_id"},
                new String[]{"dblink_info","dblink_zdb_id"});

        List<File> subsets = diff.process(beforeFile.getAbsolutePath(), afterFile.getAbsolutePath());
        createZipArchive(new File(path, "before_after.zip"), subsets);

        //cleanup:
        for(File file : subsets) {
            file.delete();
        }
        beforeFile.delete();
        afterFile.delete();
        parentDir.delete();
    }

    private File tempDirName() {
        return new File("/tmp/ncbi_temp_" + DateUtil.nowToString("yyyy-MM-dd_HH-mm-ss"));
    }

    private void assertNCBIFilesExist() {
        assertFileExists("before_db_link.csv");
        assertFileExists("after_db_link.csv");
        assertFileExists("before_recattrib.csv");
        assertFileExists("after_recattrib.csv");
    }

    private void assertFileExists(String filename) {
        if (!new File(path, filename).exists()) {
            System.err.println(filename + " does not exist");
            System.exit(1);
        }
    }


}