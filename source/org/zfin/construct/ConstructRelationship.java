package org.zfin.construct;

/**
 * Created by prita on 3/2/2015.
 */

import lombok.Getter;
import lombok.Setter;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.Set;


/**
 * Class MarkerRelationship.
 */
@Setter
@Getter
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


    private ConstructCuration construct;
    private Marker marker;



    private Set<PublicationAttribution> publications;


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


