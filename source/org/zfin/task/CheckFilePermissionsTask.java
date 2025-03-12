package org.zfin.task;

import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test if the expected directories are writeable. Must run this task as the same user
 * that should have those write permissions as it creates a temp file in the directory
 * and then deletes it.
 *
 * Use the VERBOSE=true environment variable to get detailed output.
 *
 */
public class CheckFilePermissionsTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        CheckFilePermissionsTask checkFilePermissions = new CheckFilePermissionsTask();
        checkFilePermissions.runTask();
    }

    private void runTask() {
        initAll();
        List<Path> pathsToTest = List.of(
                Paths.get(ZfinPropertiesEnum.LOADUP_FULL_PATH.value()),
                Paths.get(ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH.value()),
                Paths.get(ZfinPropertiesEnum.TARGETROOT.value(), "server_apps/data_transfer/PUBMED"), //authors file (Load-Complete-Author-Names_d)
                Paths.get(ZfinPropertiesEnum.DATABASE_UNLOAD_DIRECTORY.value(), "..", ZfinPropertiesEnum.INSTANCE.value())
        );
        int numberOfErrors = 0;

        System.out.println("Checking permissions for " + pathsToTest.size() + " directories");
        System.out.println("Use the VERBOSE=true environment variable to get detailed output.");

        for (Path path : pathsToTest) {
            boolean success = checkDirectoryWritePermissions(path);
            if (!success) {
                numberOfErrors++;
            }
        }
        System.exit(numberOfErrors);
    }

    private boolean checkDirectoryWritePermissions(Path path) {
        File dir = path.toFile();
        try {
            FileUtil.assertPathWritePermissions(dir);
            System.out.println("Good : " + dir.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("Bad  : " + dir.getAbsolutePath());
            if (!dir.exists()) {
                System.out.printf(" Directory %s does not exist\n", path);
            }
            if (System.getenv("VERBOSE") != null) {
                System.out.println("Caught Error");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }
}
