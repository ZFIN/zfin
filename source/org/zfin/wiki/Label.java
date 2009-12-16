package org.zfin.wiki;

/**
 * Oft-used wiki labels.
 */
public enum Label {

    ZEBRAFISH_BOOK("zebrafish_book"),
    ZFIN_ANTIBODY_LABEL("zfin_antibody"),
    ;

    private final String value ;

    Label(String value){
        this.value = value ;
    }

    public String getValue(){
        return value ;
    }
}
