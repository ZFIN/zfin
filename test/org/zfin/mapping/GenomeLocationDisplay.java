package org.zfin.mapping;

import java.util.HashSet;
import java.util.Set;

/**
 * presentation class for the genome location info.
 */
public class GenomeLocationDisplay extends GenomeLocation {

    public static final String DELIMITER = ", ";
    private Set<String> chromosomeList = new HashSet<>();

    public void addChromosomeNumber(String chromosome) {
        chromosomeList.add(chromosome);
    }

    public String getChromosomeNumber() {
        String builder = "";
        for (String number : chromosomeList) {
            builder += number;
            builder += DELIMITER;
        }
        return builder.substring(0, builder.lastIndexOf(DELIMITER));
    }

}
