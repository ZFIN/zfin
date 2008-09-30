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
}
