package org.zfin.feature;

import java.util.Set;


public class FeatureTypeGroup {


    private String name;
    private String comment;
    private Set<String> typeStrings;

    public Set<String> getTypeStrings() {
        return typeStrings;
    }

    public void setTypeStrings(Set<String> typeStrings) {
        this.typeStrings = typeStrings;
    }

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


}
