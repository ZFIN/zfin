package org.zfin.publication;

import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.presentation.PublicationService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * This script finds empty publication files.
 * It prints the filename, publication ZDB ID, and filetype of each empty file.
 */
public class FindEmptyPublicationFilesTask extends AbstractScriptWrapper {
    public static void main(String[] args) throws IOException {
        FindEmptyPublicationFilesTask task = new FindEmptyPublicationFilesTask();
        task.runTask();
    }

    private void runTask() throws IOException {
        initAll();
        PublicationService pubService = new PublicationService();
        List<PublicationFile> files = getPublicationRepository().getAllPublicationFiles();
        List<PublicationFile> emptyFiles = files.stream().filter(file -> pubService.getPublicationFileSizeOnDisk(file) == 0).toList();
        String outputDir = ".";
        if (System.getenv("ARTIFACTS_DIR") != null) {
            outputDir = System.getenv("ARTIFACTS_DIR");
        }
        File outputReportFile = new File(outputDir, "empty_publication_files.csv");
        System.out.println("Writing report to " + outputReportFile.getAbsolutePath());

        try (BufferedWriter reportFileHandle = new BufferedWriter(new FileWriter(outputReportFile))) {
            System.out.println("Filename,Publication ZDB ID,Filetype");
            reportFileHandle.write("Filename,Publication ZDB ID,Filetype\n");
            for (PublicationFile file : emptyFiles) {
                System.out.println(file.getFileName() + "," + file.getPublication().getZdbID() + "," + file.getType().getName());
                reportFileHandle.write(file.getFileName() + "," + file.getPublication().getZdbID() + "," + file.getType().getName() + "\n");
            }
        }

        System.out.println("Found " + emptyFiles.size() + " empty publication files");
        if(emptyFiles.size() > 0) {
            System.exit(1);
        }
    }
}
