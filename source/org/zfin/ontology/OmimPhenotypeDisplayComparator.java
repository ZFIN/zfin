package org.zfin.ontology;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;

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


       String humanAbbrev1  = display1.getOrthology().getAbbreviation();
        String humanAbbrev2 = display2.getOrthology().getAbbreviation();


        int result = humanAbbrev1.compareTo(humanAbbrev2);
        return result;

    }
}