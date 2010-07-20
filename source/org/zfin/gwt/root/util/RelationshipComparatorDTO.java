package org.zfin.gwt.root.util;

/**
 * Comparator for relationship type strings. 
 */
public class RelationshipComparatorDTO extends CustomizableComparatorDTO {

    public RelationshipComparatorDTO() {
        super();
        addToBottom("Start Stage");
        addToBottom("End Stage");

        addToTop("develops from");
        addToTop("develops into");
        addToTop("is part of");
        addToTop("has parts");
        addToTop("is a type");
        addToTop("has subtype");
    }

}