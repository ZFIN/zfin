package org.zfin.datatransfer.ncbi;

import lombok.Getter;
import lombok.Setter;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;
import org.zfin.datatransfer.ncbi.dto.Gene2VegaDTO;
import org.zfin.datatransfer.ncbi.dto.GeneInfoDTO;
import org.zfin.datatransfer.ncbi.dto.RefSeqCatalogDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
@Setter
public class NCBIReleaseFileReader {

    private NCBIReleaseFileSet fileset;

    public NCBIReleaseFileReader() {
    }

    public NCBIReleaseFileReader(NCBIReleaseFileSet fileset) {
        this.fileset = fileset;
    }

    public List<Gene2AccessionDTO> readGene2AccessionFile(File file) throws IOException {
        LoadFileReader<Gene2AccessionDTO> gene2AccessionReader = new LoadFileReader<>(Gene2AccessionDTO.class);
        return gene2AccessionReader.readFile(file);
    }

    public List<Gene2VegaDTO> readGene2VegaFile(File file) throws IOException {
        LoadFileReader<Gene2VegaDTO> gene2VegaReader = new LoadFileReader<>(Gene2VegaDTO.class);
        return gene2VegaReader.readFile(file);
    }

    public List<GeneInfoDTO> readGeneInfoFile(File file) throws IOException {
        LoadFileReader<GeneInfoDTO> geneInfoReader = new LoadFileReader<>(GeneInfoDTO.class);
        return geneInfoReader.readFile(file);
    }

    public List<RefSeqCatalogDTO> readRefSeqCatalogFile(File file) throws IOException {
        LoadFileReader<RefSeqCatalogDTO> refSeqCatalogFileReader = new LoadFileReader<>(RefSeqCatalogDTO.class);
        refSeqCatalogFileReader.setValidateHeaders(false);
        return refSeqCatalogFileReader.readFile(file);
    }

    public List<Gene2AccessionDTO> readGene2AccessionFile(NCBIReleaseFileSet releaseFiles) throws IOException {
        return this.readGene2AccessionFile(releaseFiles.getGene2accession());
    }

    public List<Gene2VegaDTO> readGene2VegaFile(NCBIReleaseFileSet fileSet) throws IOException {
        return this.readGene2VegaFile(fileSet.getGene2vega());
    }

    public List<GeneInfoDTO> readGeneInfoFile(NCBIReleaseFileSet fileSet) throws IOException {
        return this.readGeneInfoFile(fileSet.getZfGeneInfo());
    }

    public List<RefSeqCatalogDTO> readRefSeqCatalogFile(NCBIReleaseFileSet fileSet) throws IOException {
        return this.readRefSeqCatalogFile(fileSet.getRefSeqCatalog());
    }

    public List<Gene2AccessionDTO> readGene2AccessionFile() throws IOException {
        return this.readGene2AccessionFile(fileset.getGene2accession());
    }

    public List<Gene2VegaDTO> readGene2VegaFile() throws IOException {
        return this.readGene2VegaFile(fileset.getGene2vega());
    }

    public List<GeneInfoDTO> readGeneInfoFile() throws IOException {
        return this.readGeneInfoFile(fileset.getZfGeneInfo());
    }

    public List<RefSeqCatalogDTO> readRefSeqCatalogFile() throws IOException {
        return this.readRefSeqCatalogFile(fileset.getRefSeqCatalog());
    }
}
