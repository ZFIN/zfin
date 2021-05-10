package org.zfin.anatomy.presentation;

import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

import java.util.Collections;
import java.util.List;

/**
 * Please provide JavaDoc info!!!
 */
public class RelationshipPresentation {

    String type;
    List<GenericTerm> terms;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        //this.type = type.replaceAll(" ", "&nbsp;");
    }

    public List<GenericTerm> getTerms() {
        return terms;
    }

    public void setTerms(List<GenericTerm> terms) {
        this.terms = terms;
        Collections.sort(terms);

    }
}
