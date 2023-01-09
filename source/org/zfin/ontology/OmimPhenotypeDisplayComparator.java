package org.zfin.ontology;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;

/**
 */
public class OmimPhenotypeDisplayComparator implements Comparator<OmimPhenotypeDisplay> {

    public int compare(OmimPhenotypeDisplay display1, OmimPhenotypeDisplay display2) {
        if (display1 == null && display2 == null)
            return 0;
        else if (display1 == null)
            return -1;
        else if (display2 == null)
            return 1;

        if(CollectionUtils.isEmpty(display1.getZfinGene()) && CollectionUtils.isNotEmpty(display2.getZfinGene()))
            return 1;
        if(CollectionUtils.isNotEmpty(display1.getZfinGene()) && CollectionUtils.isEmpty(display2.getZfinGene()))
            return -1;
        if (display1.getHumanGeneDetail() != null && display2.getHumanGeneDetail() != null && display1.getHumanGeneDetail().getGeneSymbol() != null && display2.getHumanGeneDetail().getGeneSymbol() != null)
            return display1.getHumanGeneDetail().getGeneSymbol().compareTo(display2.getHumanGeneDetail().getGeneSymbol());
        else if (display1.getOrthology() != null && display2.getOrthology() != null && display1.getOrthology().getNcbiOtherSpeciesGene() != null && display2.getOrthology().getNcbiOtherSpeciesGene() != null)
            return display1.getOrthology().getNcbiOtherSpeciesGene().getAbbreviation().compareTo(display2.getOrthology().getNcbiOtherSpeciesGene().getAbbreviation());
        else
            return display1.getName().compareTo(display2.getName());
    }
}