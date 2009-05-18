package org.zfin.expression;

/**
 * Domain object
 */
public class ExpressionAssay implements Comparable<ExpressionAssay> {

    private String name;
    private String comments;
    private int displayOrder;
    private String abbreviation;


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


    public int compareTo(ExpressionAssay anotherAssay) {
        if (anotherAssay == null)
            return +1;
        return displayOrder - anotherAssay.getDisplayOrder();
    }

    public static boolean isAntibodyAssay(String assayName) {
        Type assay = Type.getAsssay(assayName);
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

        public static Type getAsssay(String assayName) {
            for (Type t : values()) {
                if (t.getName().equals(assayName))
                    return t;
            }
            throw new RuntimeException("No Assay name " + assayName + " found.");
        }

    }
}
