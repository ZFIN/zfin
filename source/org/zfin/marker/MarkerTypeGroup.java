package org.zfin.marker;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: nathandunn
 * Date: Sep 28, 2007
 * Time: 8:31:01 AM
 * To change this template use File | Settings | File Templates.
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
