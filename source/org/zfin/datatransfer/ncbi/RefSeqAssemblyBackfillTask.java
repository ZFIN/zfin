package org.zfin.datatransfer.ncbi;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.zfin.datatransfer.ncbi.NCBIReleaseFileSet.FileName.GENE2ACCESSION;
import static org.zfin.util.DateUtil.nowToString;

/**
 * Runs {@link RefSeqAssemblyReconciler} on demand (ZFIN-10510): makes the RefSeq {@code db_link}
 * rows' {@code db_link_assembly} links match NCBI's gene2accession, adding and removing links.
 *
 * <p>{@link NCBIDirectPort} already does this after every weekly load. Run this to catch up a
 * database without waiting for a load, e.g. once when ZFIN-10510 is first deployed.
 *
 * <p>Usage: {@code gradle refSeqAssemblyBackfill [--args="/path/to/gene2accession.gz"]}. Given a
 * path, it reads that gzipped gene2accession file (NCBI's column layout and header; full or
 * zebrafish-filtered) and downloads nothing. Without one, it downloads only NCBI's current
 * gene2accession.gz into {@code NCBI_DOWNLOAD_DIRECTORY} (default
 * {@code NCBI_RELEASE_ARCHIVE_DIR/<today>}); that directory should be new or empty, since a
 * previously filtered download there gets resumed onto and corrupted.
 */
@Log4j2
public class RefSeqAssemblyBackfillTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        new RefSeqAssemblyBackfillTask().initAll();
        log.info("Starting RefSeq Assembly Backfill Task (ZFIN-10510)");

        List<Gene2AccessionDTO> gene2AccessionDTOs;
        if (args.length > 0) {
            File gene2AccessionFile = new File(args[0]);
            if (!gene2AccessionFile.isFile()) {
                throw new IllegalArgumentException("gene2accession file not found: " + gene2AccessionFile);
            }
            log.info("Reading gene2accession from " + gene2AccessionFile);
            gene2AccessionDTOs = new NCBIReleaseFileReader().readGene2AccessionFile(gene2AccessionFile);
        } else {
            File gene2AccessionFile = new NCBIReleaseFetcher().downloadReleaseFile(
                    GENE2ACCESSION, new File(getDownloadDirectory(), GENE2ACCESSION.getFileName()), null);
            gene2AccessionDTOs = new NCBIReleaseFileReader().readGene2AccessionFile(gene2AccessionFile);
        }

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        RefSeqAssemblyReconciler.Changes changes;
        try {
            changes = RefSeqAssemblyReconciler.reconcile(
                    RefSeqAssemblyReconciler.buildAccessionAssemblyMap(gene2AccessionDTOs, NcbiAssemblyResolver.fromDatabase()),
                    session);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }

        log.info("Finished RefSeq Assembly Backfill Task. Links added: " + changes.toAdd().size()
                + ", removed: " + changes.toRemove().size());
    }

    private static File getDownloadDirectory() {
        File downloadDirectory = new File(NCBILoadTask.NCBI_DOWNLOAD_DIRECTORY_BASE, nowToString("yyyy-MM-dd"));
        if (System.getenv("NCBI_DOWNLOAD_DIRECTORY") != null) {
            downloadDirectory = new File(System.getenv("NCBI_DOWNLOAD_DIRECTORY"));
        }
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }
        return downloadDirectory;
    }
}
