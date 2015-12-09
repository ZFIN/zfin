package org.zfin.gwt.root.dto;

import java.io.Serializable;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class EapQualityTermDTO  implements Serializable {

    private TermDTO term;
    private String tag;
    private String nickName;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public TermDTO getTerm() {
        return term;
    }

    public void setTerm(TermDTO term) {
        this.term = term;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}