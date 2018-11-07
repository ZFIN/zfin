package org.zfin.ontology;

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

        String humanAbbrev1 = display1.getOrthology().getNcbiOtherSpeciesGene().getAbbreviation();
        String humanAbbrev2 = display2.getOrthology().getNcbiOtherSpeciesGene().getAbbreviation();

        if (humanAbbrev1 != null && humanAbbrev2 != null) {
            return humanAbbrev1.compareTo(humanAbbrev2);
        }
        humanAbbrev1 = display1.getHumanGeneDetail().getGeneSymbol();
        humanAbbrev2 = display2.getHumanGeneDetail().getGeneSymbol();
        return humanAbbrev1.compareTo(humanAbbrev2);
    }
}