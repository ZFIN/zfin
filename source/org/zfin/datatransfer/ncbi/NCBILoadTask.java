package org.zfin.datatransfer.ncbi;

import lombok.extern.log4j.Log4j2;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;


/**
 * Batch fetch to pull in FASTA files from NCBI
 *
 */
@Log4j2
public class NCBILoadTask extends AbstractScriptWrapper {


    public static void main(String[] args) {
        NCBILoadTask task = new NCBILoadTask();
        task.run();
    }

    public NCBILoadTask() {
        initAll();
    }

    public void run() {
        log.info("Starting NCBI Load Task");
        Integer release = getReleaseNumber();
        //... more to come
        log.info("Finished NCBI Load Task");
    }

    private Integer getReleaseNumber() {
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();
        Integer release = fetcher.getCurrentRelease().orElseThrow(() -> new RuntimeException("Could not fetch release number"));
        log.info("Current release is: " + release);
        return release;
    }

}