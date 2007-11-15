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
 */
public class AnatomyRelationshipOne extends AnatomyRelationship implements Serializable {

    private AnatomyItem anatomyItemOne;
    private AnatomyItem anatomyItemTwo;
    private String typeID;
    private AnatomyRelationshipTypePersistence type;

    @Override
    public AnatomyItem getAnatomyItem(){
        return anatomyItemTwo;
    }

    public AnatomyItem getAnatomyItemOne() {
        return anatomyItemOne;
    }

    public void setAnatomyItemOne(AnatomyItem anatomyItemOne) {
        this.anatomyItemOne = anatomyItemOne;
    }


    public AnatomyItem getAnatomyItemTwo() {
        return anatomyItemTwo;
    }

    public void setAnatomyItemTwo(AnatomyItem anatomyItemTwo) {
        this.anatomyItemTwo = anatomyItemTwo;
    }

    public AnatomyRelationshipTypePersistence getType() {
        return type;
    }

    public void setType(AnatomyRelationshipTypePersistence type) {
        this.type = type;
    }


    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }

    public String getRelationship() {
        return type.getRelationOneToTwo();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof AnatomyRelationshipOne))
            return false;

        AnatomyRelationshipOne itemOne = (AnatomyRelationshipOne) o;

        return EqualsUtil.areEqual(this.anatomyItemOne, itemOne.getAnatomyItemOne()) &&
                EqualsUtil.areEqual(this.type, itemOne.getType());
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 31 * hash + (null == anatomyItemOne ? 0 : anatomyItemOne.hashCode());
        hash = 31 * hash + (null == type ? 0 : type.hashCode());
        return hash;
    }
}
