package org.zfin.orthology;

/**
 * User: giles
 * Date: Jul 13, 2006
 * Time: 2:39:13 PM
 */

import java.util.List;
import java.io.Serializable;

/**
 * Business object which holds all the chromosome/gene symbol/position info for a single species.
 */
public class OrthologyItem  implements Serializable {
    private String symbol;
    private List<Chromosome> chromosomes;
    private List<AccessionItem> accessionItems;
    private Evidence evidence;

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setChromosomes(List<Chromosome> chromosomes) {
        this.chromosomes = chromosomes;
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void setAccessionItems(List<AccessionItem> accessionItems) {
        this.accessionItems = accessionItems;
    }

    public List<AccessionItem> getAccessionItems() {
        return accessionItems;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public Evidence getEvidence() {
        return evidence;
    }
}
