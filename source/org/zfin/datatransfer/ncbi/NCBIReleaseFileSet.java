package org.zfin.datatransfer.ncbi;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Setter
@Getter
public class NCBIReleaseFileSet {

    public Integer releaseNumber;

    //ENUM for the file names: gene2accession.gz, RefSeqCatalog.gz, gene2vega.gz, zf_gene_info.gz, seq.fasta
    public enum FileName {
        GENE2ACCESSION("gene2accession.gz"),
        REFSEQ_CATALOG("RefSeqCatalog.gz"),
        GENE2VEGA("gene2vega.gz"),
        ZF_GENE_INFO("Danio_rerio.gene_info.gz");

        private final String fileName;

        FileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public File getFile(FileName fileName) {
        return null;
    }

    public static final String DEFAULT_URL_BASE = "https://ftp.ncbi.nlm.nih.gov/refseq/release/";

    private File gene2accession;
    private File refSeqCatalog;
    private File gene2vega;
    private File zfGeneInfo;

    public void setFile(FileName fileName, File file) {
        switch (fileName) {
            case GENE2ACCESSION -> setGene2accession(file);
            case REFSEQ_CATALOG -> setRefSeqCatalog(file);
            case GENE2VEGA -> setGene2vega(file);
            case ZF_GENE_INFO -> setZfGeneInfo(file);
        }
    }

    public void deleteAllFiles() {
        if (gene2accession != null) {
            gene2accession.delete();
        }
        if (refSeqCatalog != null) {
            refSeqCatalog.delete();
        }
        if (gene2vega != null) {
            gene2vega.delete();
        }
        if (zfGeneInfo != null) {
            zfGeneInfo.delete();
        }
    }

}
