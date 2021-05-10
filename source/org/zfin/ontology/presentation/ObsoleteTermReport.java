package org.zfin.ontology.presentation;

import org.zfin.ontology.ConsiderTerm;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.ReplacementTerm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic report for entities that use obsoleted terms.
 */
public abstract class ObsoleteTermReport {

    private List<ReplacementTerm> replacementTermList;
    private List<ConsiderTerm> considerTermList;
    private Set<GenericTerm> obsoletedTermList;

    public List<ReplacementTerm> getReplacementTermList() {
        return replacementTermList;
    }

    public void setReplacementTermList(List<ReplacementTerm> replacementTermList) {
        this.replacementTermList = replacementTermList;
    }

    public List<ConsiderTerm> getConsiderTermList() {
        return considerTermList;
    }

    public void setConsiderTermList(List<ConsiderTerm> considerTermList) {
        this.considerTermList = considerTermList;
    }

    public Set<GenericTerm> getObsoletedTermList() {
        return obsoletedTermList;
    }

    public void setObsoletedTermList(Set<GenericTerm> obsoletedTermList) {
        this.obsoletedTermList = obsoletedTermList;
    }

    public void addObsoletedTerm(GenericTerm term){
        if(obsoletedTermList == null)
            obsoletedTermList = new HashSet<GenericTerm>();
        obsoletedTermList.add(term);
    }
}