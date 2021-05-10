package org.zfin.nomenclature;

import org.apache.commons.lang3.StringUtils;

public class HomologyInfo implements EmptyTestable {

    private String species;
    private String geneSymbol;
    private String databaseID;

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getDatabaseID() {
        return databaseID;
    }

    public void setDatabaseID(String databaseID) {
        this.databaseID = databaseID;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(species) &&
                StringUtils.isEmpty(geneSymbol) &&
                StringUtils.isEmpty(databaseID);
    }
}
