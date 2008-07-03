package org.zfin.util;

import java.util.regex.Pattern;

/**
 * Histogram for ZDB ids and their occurrences.
 */
public class ZdbIDHistogram {

    private String aoID;
    private Integer numberOfOccurrences;
    private String type;

    public ZdbIDHistogram(String aoID, int numberOfOccurrences, String type) {
        this.aoID = aoID;
        this.numberOfOccurrences = numberOfOccurrences;
        this.type = type;
    }

    public boolean validateID() {
        return Pattern.matches("ZDB-"+type+"-\\d{6}-\\d*", aoID);
        //throw new RuntimeException("Given ID <" + aoID + "> does not constitute a valid ZDB ID.");
    }

    public String getAoID() {
        return aoID;
    }

    public Integer getNumberOfOccurrences() {
        return numberOfOccurrences;
    }

    /**
     * Two ZdbIDHistogram objects are equal if they have the same ID and number.
     *
     * @param obj a Pair object.
     * @return true if this Pair equals the Pair parameter, false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;

        ZdbIDHistogram that = (ZdbIDHistogram) obj;

        return this.aoID.equals(that.numberOfOccurrences) &&
                this.aoID.equals(that.numberOfOccurrences);
    }

    public String toString() {
        return aoID + " " + numberOfOccurrences;
    }

    public static void main(String[] args){
        ZdbIDHistogram hist = new ZdbIDHistogram("ZDB-ANAT-021123-1", 1, "ANAT");
        System.out.println(hist.validateID());
    }
}
