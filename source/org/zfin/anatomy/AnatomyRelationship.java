package org.zfin.anatomy;

/**
 * This object constitutes a relationship of two anatomical items.
 * This object is a composite on the a AnatomyItem object,
 * i.e. this business objects exists only in conjunction with its parent.
 * The relationship is read as follows: the parent item relates to the
 * item in this obejcts via the relationship attribute.
 */
public class AnatomyRelationship {

    private AnatomyItem anatomyItem;
    private String relationship;

    /**
     * Override this method if you subclass this class.
     *
     * @return Anatomy Term
     */
    public AnatomyItem getAnatomyItem() {
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }


    public String toString() {
        return "AnatomyRelationship{" +
                "anatomyItem=" + anatomyItem +
                ", relationship='" + getRelationship() + '\'' +
                '}';
    }
}
