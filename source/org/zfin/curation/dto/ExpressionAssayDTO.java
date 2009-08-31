package org.zfin.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ExpressionAssayDTO implements IsSerializable {

    public static boolean isAntibodyAssay(String assayName) {
        return assayName != null &&
                (assayName.equals(Type.WESTERN_BLOT.getName()) ||
                 assayName.equals(Type.IMMUNOHISTOCHEMISTRY.getName()) ||
                 assayName.equals(Type.OTHER.getName()));
    }

    public enum Type {

        WESTERN_BLOT("Western blot"),
        IMMUNOHISTOCHEMISTRY("Immunohistochemistry"),
        OTHER("other"),
        CDNA_CLONES("cDNA clones");

        private String name;

        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Type getAsssay(String assayName) {
            for (Type t : values()) {
                if (t.getName().equals(assayName))
                    return t;
            }
            throw new RuntimeException("No Assay name " + assayName + " found.");
        }

    }

}
