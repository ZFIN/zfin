package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

/**
 * Domain object
 */
public class ExpressionAssay implements Comparable<ExpressionAssay> {

    @JsonView(View.API.class)
    private String name;
    private String comments;
    private int displayOrder;
    @JsonView(View.API.class)
    private String abbreviation;
    private boolean immunogen;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public boolean isImmunogen() {
        return immunogen;
    }

    public void setImmunogen(boolean immunogen) {
        this.immunogen = immunogen;
    }

    public int compareTo(ExpressionAssay anotherAssay) {
        if (anotherAssay == null)
            return 1;
        return displayOrder - anotherAssay.getDisplayOrder();
    }

    public static boolean isAntibodyAssay(String assayName) {
        Type assay = Type.getAssay(assayName);
        return assay == Type.WESTERN_BLOT || assay == Type.IMMUNOHISTOCHEMISTRY || assay == Type.OTHER;
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

        public static Type getAssay(String assayName) {
            for (Type t : values()) {
                if (t.getName().equals(assayName))
                    return t;
            }
            throw new RuntimeException("No Assay name " + assayName + " found.");
        }

    }
}
