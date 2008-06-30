package org.zfin.mutant;

import java.util.Date;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 */
public class Feature {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String abbreviationOrder;
    private Type type;
    private String comments;
    private Zygosity maleZygosity;
    private Zygosity femaleZygosity;
    private Date dateEntered;
    private Set<FeatureMarkerRelationship> featureMarkerRelations;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }


    public enum Type {
        DEFICIENCY,
	    INSERTION,
	    INVERSION,
	    POINT_MUTATION,
	    SEQUENCE_VARIANT,
	    TRANSGENIC_INSERTION,
	    TRANSLOC,
	    UNSPECIFIED,
	    COMPLEX_SUBSTITUTION;

        public String toString(){
            return name() ; 
        }
    }

    public Set<FeatureMarkerRelationship> getFeatureMarkerRelations() {
        return featureMarkerRelations;
    }

    public void setFeatureMarkerRelations(Set<FeatureMarkerRelationship> featureMarkerRelations) {
        this.featureMarkerRelations = featureMarkerRelations;
    }
}
