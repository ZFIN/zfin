package org.zfin.orthology;

import org.zfin.orthology.repository.OrthologyHelper;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 */
public class OrthologyComparator implements Comparator<OrthologyHelper> {

    public int compare(OrthologyHelper o1, OrthologyHelper o2) {
        if(o1 == null && o2 == null)
            return 0;
        if(o1 == null)
            return -1;
        if(o2 == null)
            return 1;

        Species speciesOne = Species.getSpecies(o1.getSpecies());
        Species speciesTwo = Species.getSpecies(o2.getSpecies());

        return speciesOne.getIndex() - speciesTwo.getIndex();
    }
}
