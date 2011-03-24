package org.zfin.ontology;

import org.zfin.anatomy.presentation.RelationshipSorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This data structure is used for providing anatomy/term relationship information
 * on the jsp.
 */
public class RelationshipPresentation implements Comparable<RelationshipPresentation> {

    String type;
    List<Term> items;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Term> getItems() {
        return items;
    }

    public void setItems(List<Term> items) {
        this.items = items;
    }

    public void addTerm(Term term){
        if(items == null)
            items = new ArrayList<Term>();
        items.add(term);
        Collections.sort(items);
    }
    @Override
    public int compareTo(RelationshipPresentation o) {
        Comparator<String> comp = new RelationshipSorting();
        return comp.compare(getType(), o.getType());
    }
}
