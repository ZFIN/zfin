package org.zfin.wiki;

/**
 * Allowable wiki spaces.
 */
public enum Space {

    ANTIBODY("AB"),
    PROTOCOL("prot"),
    SANDBOX("sand"),
    NEWS("news"),
    MEETINGS("meetings"),
    JOBS("jobs"),
    ;

    private final String value ;

    Space(String value){
        this.value = value ;
    }

    public String getValue(){
        return value ;
    }
}
