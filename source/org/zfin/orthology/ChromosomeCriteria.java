package org.zfin.orthology;

import org.zfin.util.FilterType;

import java.util.List;

/**
 * Business criteria object for chromosome input created by the OrthologyCriteriaService class
 * which is passed on to the repository by the controller.
 */
public class ChromosomeCriteria {
    private FilterType type;
    private List<Integer> chromosomes;
    // this collection uses strings as chromosomes can be called 'X' and others
    private List<String> chromosomesNames;
    private int min;
    private int max;

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public List<Integer> getChromosomes() {
        return chromosomes;
    }

    public void setChromosomes(List<Integer> chromosomes) {
        this.chromosomes = chromosomes;
    }

    public List<String> getChromosomesNames() {
        return chromosomesNames;
    }

    public void setChromosomesNames(List<String> chromosomesNames) {
        this.chromosomesNames = chromosomesNames;
    }

    /**
     * Retrieve the one chromosome. Check with the
     * hasOneChromosome() method if there is one otheriwse
     * this method will throw a RuntimeException.
     */
    public int getSingleChromosome() {
        return chromosomes.get(0);
    }

    /**
     * Check if exactly one chromosome is defined.
     * call this method before using getSingleChromosome().
     */
    public boolean hasOneChromosome() {
        return !(chromosomes == null || chromosomes.size() != 1);
    }

    public boolean hasExactlyOneChromosomeName() {
        return !(chromosomesNames == null || chromosomesNames.size() != 1);
    }


    /**
     * Check if there is at least one chromosome number given or a maximum number defined.
     */
    public boolean hasAtLeastOneChromosome() {
        return (chromosomes != null && chromosomes.size() > 0) || max > 0;
    }

    public boolean hasChromosomeNames(){
        return (chromosomesNames != null && chromosomesNames.size() > 0) || max > 0;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
