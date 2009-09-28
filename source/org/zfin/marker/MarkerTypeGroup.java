package org.zfin.marker;

import java.util.Set;

/**
 * This class maps to the marker_type_group table.
 */
public class MarkerTypeGroup {

    private String name ;
    private String comment;
    private Set<String> typeStrings ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<String> getTypeStrings() {
        return typeStrings;
    }

    public void setTypeStrings(Set<String> typeStrings) {
        this.typeStrings = typeStrings;
    }
}
