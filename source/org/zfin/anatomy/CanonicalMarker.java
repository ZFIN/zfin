package org.zfin.anatomy;

import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.Set;
import java.util.Iterator;

/**
 * Canonical Marker?????.
 */
public class CanonicalMarker {

    private String zdbID;
    private AnatomyItem item;
    private Marker gene;
    private String aoZdbID;
    private String geneID;
    private Set<Publication> publications;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public AnatomyItem getItem() {
        return item;
    }

    public void setItem(AnatomyItem item) {
        this.item = item;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public String getAoZdbID() {
        return aoZdbID;
    }

    public void setAoZdbID(String aoZdbID) {
        this.aoZdbID = aoZdbID;
    }

    public String getGeneID() {
        return geneID;
    }

    public void setGeneID(String geneID) {
        this.geneID = geneID;
    }

    public Publication getPublication() {
        Iterator iter = publications.iterator();
        return (Publication) iter.next();
    }
}
