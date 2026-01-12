package org.zfin.task;

import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import static org.zfin.datatransfer.service.DownloadService.isDownloadAlreadyProcessed;

/**
 * Task to check if a new load file is available for download.
 * Exits with code 0 if new file is available, 1 if not, and 2 on error.
 * Arguments:
 * 1st arg: Organization (eg. Noctua)
 * 2nd arg: Load file URL (eg. https://current.geneontology.org/products/upstream_and_raw_data/noctua_zfin.gpad.gz)
 *
 * If a new file is available, the task can be followed up with a download and load task.
 */
public class CheckNewLoadFileAvailableTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        //expect first arg to be load job url (eg. https://current.geneontology.org/products/upstream_and_raw_data/noctua_zfin.gpad.gz)
        if (args.length < 2) {
            System.out.println("Please provide Organization as first argument (eg. Noctua)");
            System.out.println("Please provide load file URL as second argument");
            System.out.println("eg. checkNewLoadFileAvailableTask Noctua https://current.geneontology.org/products/upstream_and_raw_data/noctua_zfin.gpad.gz");
            System.exit(3);
        }

        String organization = args[0];
        String url = args[1];

        CheckNewLoadFileAvailableTask checkNewLoadFileAvailableTask = new CheckNewLoadFileAvailableTask();
        checkNewLoadFileAvailableTask.runTask(GafOrganization.OrganizationEnum.getType(organization), url);
    }

    private void runTask(GafOrganization.OrganizationEnum orgEnum, String downloadUrl) {
        initAll();

        try {
            Date lastModified = DownloadService.getLastModifiedOnServer(new URL(downloadUrl));
            if (lastModified == null) {
                System.out.println("Could not determine Last-Modified date from server; assuming new file is available. Exiting with code 0.");
                System.exit(0);
            }
            boolean alreadyProcessed = isDownloadAlreadyProcessed(downloadUrl, orgEnum, lastModified);
            if (alreadyProcessed) {
                System.out.println("No new file available for download. Exiting with code 1.");
                System.exit(1);
            } else {
                System.out.println("New file available for download. Exiting with code 0.");
                System.exit(0);
            }
        } catch (IOException e) {
            System.err.println("Error checking for new load file: " + e.getMessage());
            System.exit(2);
            throw new RuntimeException(e);
        }

    }

}
