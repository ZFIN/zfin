package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MutationDetailControlledVocabularyTermDTO implements IsSerializable {

    private TermDTO term;
    private String displayName;
    private String abbreviation;
    private Integer order;

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public TermDTO getTerm() {
        return term;
    }

    public void setTerm(TermDTO term) {
        this.term = term;
    }
}
