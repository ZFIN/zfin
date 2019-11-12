package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZFINExpressionDTO extends BasicExpressionDTO {

    private String assayName;
    private List<CrossReferenceDTO> ensemblCrossReferences;
    private String publicationTitle;
    private String generatedGeneDescription;
    private String geneName;
    private String geneSymbol;

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }


    public String getPublicationTitle() {
        return publicationTitle;
    }

    public void setPublicationTitle(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }

    public String getGeneratedGeneDescription() {
        return generatedGeneDescription;
    }

    public void setGeneratedGeneDescription(String generatedGeneDescription) {
        this.generatedGeneDescription = generatedGeneDescription;
    }



    public List<CrossReferenceDTO> getEnsemblCrossReferences() {
        return ensemblCrossReferences;
    }

    public void setEnsemblCrossReferences(List<CrossReferenceDTO> ensemblCrossReferences) {
        this.ensemblCrossReferences = ensemblCrossReferences;
    }


    public String getAssayName() {
        return assayName;
    }

    public void setAssayName(String assayName) {
        this.assayName = assayName;
    }



}
