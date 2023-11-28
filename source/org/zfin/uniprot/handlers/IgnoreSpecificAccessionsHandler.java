package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * This handler will ignore any accessions that are not explicitly whitelisted via the given file.
 * This is useful for testing, or troubleshooting
 * The file should be a list of accessions, one per line.
 * The file should be specified via the environment variable ACCESSIONS_INCLUSIONS_FILE.
 */
@Log4j2
public class IgnoreSpecificAccessionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        String inclusionsFilename = System.getenv("ACCESSIONS_INCLUSIONS_FILE");
        if (inclusionsFilename == null) {
            return;
        }

        File inclusionsFile = new File(inclusionsFilename);
        if (!inclusionsFile.exists()) {
            log.error("Could not find file " + inclusionsFilename + " to use as a list of accessions to include. Skipping.");
            return;
        }


        List<String> accessionsToInclude;
        try {
            accessionsToInclude = Files.readAllLines(inclusionsFile.toPath());
        } catch (IOException e) {
            log.error("Could not read file " + inclusionsFilename + " to use as a list of accessions to include. Skipping.");
            return;
        }

        if (accessionsToInclude.isEmpty()) {
            log.error("File " + inclusionsFilename + " to use as a list of accessions to include was empty. Skipping.");
            return;
        }

        log.info("Will only include accessions from : " + inclusionsFilename + " (" + accessionsToInclude.size() + " accessions)");
        Iterator<Map.Entry<String, RichSequenceAdapter>> iter = uniProtRecords.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, RichSequenceAdapter> entry = iter.next();
            String accession = entry.getKey();
            if (!accessionsToInclude.contains(accession)) {
                log.debug("Removing accession " + accession + " from load file because it is not in the list of accessions to include.");
                iter.remove();
            }
        }

        log.debug("After filtering, there are " + uniProtRecords.size() + " accessions in the load file.");
    }
}
