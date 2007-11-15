package org.zfin.anatomy.repository;

import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.util.EqualsUtil;

import java.io.Serializable;

/**
 * This object constitutes a relationship of two anatomical items.
 * This object is a composite on the a AnatomyItem object,
 * i.e. this business objects exists only in conjunction with its parent.
 * The relationship is read as follows: the parent item relates to the
 * item in this obejcts via the relationship attribute.
 * <p/>
 * Convenience class to allow retrieving relationships that are stored in two places
 * in the database.
 */
public class AnatomyRelationshipTwo extends AnatomyRelationship implements Serializable {

    private AnatomyItem anatomyItemTwo;
    private AnatomyItem anatomyItemOne;
    private AnatomyRelationshipTypePersistence type;

    @Override
    public AnatomyItem getAnatomyItem(){
        return anatomyItemOne;
    }

    public AnatomyItem getAnatomyItemTwo() {
        return anatomyItemTwo;
    }

    public void setAnatomyItemTwo(AnatomyItem anatomyItemTwo) {
        this.anatomyItemTwo = anatomyItemTwo;
    }


    public AnatomyItem getAnatomyItemOne() {
        return anatomyItemOne;
    }

    public void setAnatomyItemOne(AnatomyItem anatomyItemOne) {
        this.anatomyItemOne = anatomyItemOne;
    }

    public AnatomyRelationshipTypePersistence getType() {
        return type;
    }

    public void setType(AnatomyRelationshipTypePersistence type) {
        this.type = type;
    }

    public String getRelationship() {
        return type.getRelationTwoToOne();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof AnatomyRelationshipTwo))
            return false;

        AnatomyRelationshipTwo itemOne = (AnatomyRelationshipTwo) o;

        return EqualsUtil.areEqual(this.anatomyItemTwo, itemOne.getAnatomyItemTwo()) &&
                EqualsUtil.areEqual(this.type, itemOne.getType());
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 31 * hash + (null == anatomyItemTwo ? 0 : anatomyItemTwo.hashCode());
        hash = 31 * hash + (null == type ? 0 : type.hashCode());
        return hash;
    }

}
