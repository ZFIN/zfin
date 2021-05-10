package org.zfin.construct;

/**
 * Created by prita on 3/2/2015.
 */

import org.zfin.publication.Publication;



        import org.zfin.infrastructure.EntityAttribution;
        import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;

import java.util.HashSet;
        import java.util.Set;


/**
 * Class MarkerRelationship.
 */
public class ConstructRelationship implements EntityAttribution {

    public enum Type {


        CODING_SEQUENCE_OF("coding sequence of"),
        CONTAINS_REGION("contains region"),
        CONTAINS_POLYMORPHISM("contains polymorphism"),

        PROMOTER_OF("promoter of");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public String getValue() {
            return value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No ConstructRelationship type of string " + type + " found.");
        }
    }



    private Type type;

    private String zdbID;


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ConstructCuration getConstruct() {
        return construct;
    }

    public void setConstruct(ConstructCuration construct) {
        this.construct = construct;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }



    private ConstructCuration construct;
    private Marker marker;



    private Set<PublicationAttribution> publications;

    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID() {
        return zdbID;
    }

    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }



    public Set<PublicationAttribution> getPublications() {
        if (publications == null)
            return new HashSet<PublicationAttribution>();
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            return getPublications().iterator().next().getPublication();
        } else {
            return null;
        }
    }


}


