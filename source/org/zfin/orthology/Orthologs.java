package org.zfin.orthology;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Highest level business object which holds all the information about each single orthologous gene record.
 * An ortholog record is the sum of all genes for given species that are orthologous to a
 * given gene of a Zebrafish (since we do not support orthology searches among non-zebrafish species).
 */
public class Orthologs implements Serializable {

    private List<OrthologySpecies> orthologSpecies;
    private String geneSymbol;
    private Set<EvidenceCode> distinctCodes;

    public void setOrthologSpecies(List<OrthologySpecies> orthologSpecies) {
        this.orthologSpecies = orthologSpecies;
    }

    public List getOrthologSpecies() {
        return orthologSpecies;
    }

    public void setGeneSymbol(String symbol) {
        this.geneSymbol = symbol;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public int getNumDistinctCodes() {
        return distinctCodes.size();
    }

    /**
     * Retrieve all distinct evidence codes used each orhtologous species.
     */
    public Set<EvidenceCode> getDistinctCodes() {
        if (this.distinctCodes != null) {
            return distinctCodes;
        } else {
            Set<EvidenceCode> distinctCodes = new TreeSet<EvidenceCode>();
            for (OrthologySpecies currentSpecies : orthologSpecies) {
                List<OrthologyItem> orthItems = currentSpecies.getItems();
                for (OrthologyItem currentOrthItem : orthItems) {
                    Evidence currentEvidence = currentOrthItem.getEvidence();
                    if (currentEvidence != null) {
                        Set<EvidenceCode> codes = currentEvidence.getCodes();
                        for (EvidenceCode currentCode : codes) {
                            distinctCodes.add(currentCode);
                        }
                    }
                }
            }
            this.distinctCodes = distinctCodes;
            return this.distinctCodes;
        }
    }


    public String toString() {
        return "Orthology{" +
                "orthologSpecies=" + orthologSpecies +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", distinctCodes=" + distinctCodes +
                '}';
    }
}
