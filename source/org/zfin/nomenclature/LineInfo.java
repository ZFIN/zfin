package org.zfin.nomenclature;

import org.apache.commons.lang3.StringUtils;

public class LineInfo implements EmptyTestable {

    private String background;
    private String geneName;
    private String geneSymbol;
    private String designation;
    private String protocol;
    private String mutationType;
    private String mutationDetails;
    private String sequence;

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getMutationDetails() {
        return mutationDetails;
    }

    public void setMutationDetails(String mutationDetails) {
        this.mutationDetails = mutationDetails;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(background) &&
                StringUtils.isEmpty(geneName) &&
                StringUtils.isEmpty(geneSymbol) &&
                StringUtils.isEmpty(designation) &&
                StringUtils.isEmpty(protocol) &&
                StringUtils.isEmpty(mutationType) &&
                StringUtils.isEmpty(mutationDetails) &&
                StringUtils.isEmpty(sequence);
    }
}
