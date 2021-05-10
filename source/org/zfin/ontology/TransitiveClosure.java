package org.zfin.ontology;

import org.apache.commons.lang.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Maps to the table that contains the information of all children of a given term.
 * Allows to search for 'including substructure'
 */
@Entity
@Table(name = "all_term_contains")
public class TransitiveClosure implements Serializable, Comparable<TransitiveClosure> {

    @Id
    @ManyToOne
    @JoinColumn(name = "alltermcon_container_zdb_id")
    private GenericTerm root;
    @Id
    @ManyToOne
    @JoinColumn(name = "alltermcon_contained_zdb_id")
    private GenericTerm child;
    // this is the minimum number of terms you have to traverse to connect the root term with the child. 
    @Column(name = "alltermcon_min_contain_distance")
    private int distance;

    public GenericTerm getRoot() {
        return root;
    }

    public void setRoot(GenericTerm root) {
        this.root = root;
    }

    public GenericTerm getChild() {
        return child;
    }

    public void setChild(GenericTerm child) {
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
