package org.zfin.nomenclature;

import org.apache.commons.lang3.StringUtils;

public class LineInfo implements EmptyTestable {

    private String geneName;
    private String geneSymbol;
    private String designation;
    private String protocol;
    private String mutationType;

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

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(geneName) &&
                StringUtils.isEmpty(geneSymbol) &&
                StringUtils.isEmpty(designation) &&
                StringUtils.isEmpty(protocol) &&
                StringUtils.isEmpty(mutationType);
    }
}
