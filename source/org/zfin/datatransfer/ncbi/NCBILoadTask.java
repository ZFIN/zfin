package org.zfin.datatransfer.ncbi;

import lombok.extern.log4j.Log4j2;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;
import org.zfin.datatransfer.ncbi.dto.Gene2VegaDTO;
import org.zfin.datatransfer.ncbi.dto.GeneInfoDTO;
import org.zfin.datatransfer.ncbi.dto.RefSeqCatalogDTO;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Batch fetch to pull in FASTA files from NCBI
 *
 */
@Log4j2
public class NCBILoadTask extends AbstractScriptWrapper {

    public static final String NCBI_DOWNLOAD_DIRECTORY = "/tmp/ncbi";

    public static void main(String[] args) throws IOException {
        NCBILoadTask task = new NCBILoadTask();
        task.run();
    }

    public NCBILoadTask() {
        initAll();
    }

    public void run() throws IOException {
        log.info("Starting NCBI Load Task");
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();
        NCBIReleaseFileReader reader = fetcher.downloadLatestReleaseFileSetReader(new File(NCBI_DOWNLOAD_DIRECTORY));
        List<Gene2AccessionDTO> gene2AccessionDTOs = reader.readGene2AccessionFile();
        List<Gene2VegaDTO> gene2VegaDTOs = reader.readGene2VegaFile();
        List<GeneInfoDTO> geneInfoDTOs = reader.readGeneInfoFile();
        List<RefSeqCatalogDTO> catalogDTOs = reader.readRefSeqCatalogFile();

        //... more to come
        log.info("Finished NCBI Load Task");
    }

}