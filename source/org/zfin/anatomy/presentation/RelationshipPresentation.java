package org.zfin.anatomy.presentation;

import org.zfin.ontology.Term;

import java.util.Collections;
import java.util.List;

/**
 * Please provide JavaDoc info!!!
 */
public class RelationshipPresentation {

    String type;
    List<Term> terms;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        //this.type = type.replaceAll(" ", "&nbsp;");
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
        Collections.sort(terms);

    }
}
