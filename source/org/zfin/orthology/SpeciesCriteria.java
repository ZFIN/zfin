package org.zfin.orthology;

/**
 * User: giles
 * Date: Aug 8, 2006
 * Time: 11:59:19 AM
 */

/**
 * High level business criteria object created by the OrthologyCriteriaService class which holds
 * all of the form input for a given species.  This object is passed on to the repository by the
 * controller to form the hql query.
 */
public class SpeciesCriteria {
    private String name;
    private GeneSymbolCriteria symbol;
    private ChromosomeCriteria chromosome;
    private PositionCriteria position;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeneSymbolCriteria getSymbol() {
        return symbol;
    }

    public void setSymbol(GeneSymbolCriteria symbol) {
        this.symbol = symbol;
    }

    public ChromosomeCriteria getChromosome() {
        return chromosome;
    }

    public void setChromosome(ChromosomeCriteria chromosome) {
        this.chromosome = chromosome;
    }

    public PositionCriteria getPosition() {
        return position;
    }

    public void setPosition(PositionCriteria position) {
        this.position = position;
    }
}
