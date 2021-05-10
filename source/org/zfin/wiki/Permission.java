package org.zfin.wiki;

/**
 * Wiki permissions.
 */
public enum Permission {
    EDIT("Edit"),
    VIEW("View"),
    ;

    private final String value ;

    Permission(String value){
        this.value = value ;
    }

    public String getValue(){
        return value ;
    }
}
