package org.zfin.publication;

import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.presentation.PublicationService;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * This script finds empty publication files.
 * It prints the filename, publication ZDB ID, and filetype of each empty file.
 */
public class FindEmptyPublicationFilesTask extends AbstractScriptWrapper {
    public static void main(String[] args) {
        FindEmptyPublicationFilesTask task = new FindEmptyPublicationFilesTask();
        task.runTask();
    }

    private void runTask() {
        initAll();
        PublicationService pubService = new PublicationService();
        List<PublicationFile> files = getPublicationRepository().getAllPublicationFiles();
        List<PublicationFile> emptyFiles = files.stream().filter(file -> pubService.getPublicationFileSizeOnDisk(file) == 0).toList();
        System.out.println("Filename,Publication ZDB ID,Filetype");
        for (PublicationFile file : emptyFiles) {
            System.out.println(file.getFileName() + "," + file.getPublication().getZdbID() + "," + file.getType().getName());
        }
        System.out.println("Found " + emptyFiles.size() + " empty publication files");
        if(emptyFiles.size() > 0) {
            System.exit(1);
        }
    }
}
