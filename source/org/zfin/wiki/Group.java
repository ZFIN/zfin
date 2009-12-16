package org.zfin.wiki;

/**
 * Oft-used wiki groups.
 */
public enum Group {

    ZFIN_USERS("zfin-users"),
    ;

    private final String value ;

    Group(String value){
        this.value = value ;
    }

    public String getValue(){
        return value ;
    }
}
