package org.zfin.ontology;

import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;

/**
 * Maps to the table that contains the information of all children of a given term.
 * Allows to search for 'including substructure'
 */
public class TransitiveClosure implements Serializable, Comparable<TransitiveClosure> {

    private Term root;
    private Term child;
    // this is the minimum number of terms you have to traverse to connect the root term with the child. 
    private int distance;

    public Term getRoot() {
        return root;
    }

    public void setRoot(Term root) {
        this.root = root;
    }

    public Term getChild() {
        return child;
    }

    public void setChild(Term child) {
        this.child = child;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransitiveClosure))
            return false;
        TransitiveClosure aoChildren = (TransitiveClosure) o;
        return
                ObjectUtils.equals(root, aoChildren.getRoot()) &&
                        ObjectUtils.equals(child, aoChildren.getChild());
    }

    @Override
    public int hashCode() {
        int hash = 37;
        if (root != null)
            hash = hash * root.hashCode();
        if (child != null)
            hash += hash * child.hashCode();
        return hash;
    }

    @Override
    public int compareTo(TransitiveClosure o) {
        if (o == null)
            return -1;
        if (getChild().getZdbID().equals(o.getChild().getZdbID()))
            return 0;
        if (getDistance() == o.getDistance())
            return getChild().getTermName().compareToIgnoreCase(o.getChild().getTermName());
        return getDistance() - o.getDistance();
    }
}
