package org.zfin.sequence;

//import org.apache.log4j.Logger;

import org.zfin.feature.Feature;

import java.io.Serializable;

public class FeatureDBLink extends DBLink implements Comparable<FeatureDBLink>, Serializable {


   // Logger logger = Logger.getLogger(FeatureDBLink.class);

    private Feature feature;
//    private Accession referencingAccession ;


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean equals(Object o) {
        if (o instanceof FeatureDBLink) {
            FeatureDBLink dbLink = (FeatureDBLink) o;
//            if( getZdbID()!=null && dbLink.getZdbID().equals(getZdbID()) ){
//                return true ;
//            }

            if (dbLink.getFeature().getZdbID().equals(dbLink.getFeature().getZdbID())
                    &&
                    dbLink.getAccessionNumber().equals(dbLink.getAccessionNumber())
                    &&
                    dbLink.getReferenceDatabase().equals(dbLink.getReferenceDatabase())
                    ) {
                return true;
            }
        }
        return false;
    }


    public int hashCode() {
        int result = 1;
//        result += (getZdbID() != null ? getZdbID().hashCode() : 0) * 29;
        result += (getFeature() != null ? getFeature().hashCode() : 0) * 13;
        result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
        result += (getReferenceDatabase() != null ? getReferenceDatabase().getZdbID().hashCode() : 0) * 17;
        return result;
    }


    public String toString() {
        String returnString = "";
        returnString += getZdbID() + "\n";
        returnString += getAccessionNumber() + "\n";
        returnString += getLength() + "\n";
        returnString += getReferenceDatabase().getZdbID() + "\n";
        returnString += getFeature().getZdbID() + "\n";
        returnString += getFeature().getName() + "\n";
        return returnString;
    }

    /**
     * Sort by accessionNBumber, reference DB id, and finally feature name
     *
     * @param featureDBLink featureDBLink to compare to.
     * @return Returns java comparison
     */
    public int compareTo(FeatureDBLink featureDBLink) {

        int accCompare = getAccessionNumber().compareTo(featureDBLink.getAccessionNumber());
        if (accCompare != 0) {
            return accCompare;
        }

        int refDBCompare = getReferenceDatabase().getZdbID().compareTo(featureDBLink.getReferenceDatabase().getZdbID());
        if (refDBCompare != 0) {
            return refDBCompare;
        }

        int featureCompare = getFeature().getZdbID().compareTo(featureDBLink.getFeature().getZdbID());
        if (featureCompare != 0) {
            return featureCompare;
        }

        return 0;
    }

}
