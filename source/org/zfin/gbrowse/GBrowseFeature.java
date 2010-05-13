package org.zfin.gbrowse;

import java.io.Serializable;
import java.util.Set;

/**
 * This class is a direct mapping of the Seqfeature::Store database
 * <p/>
 * The feature is the central object in that schema, somewhat like
 * how we use marker.
 * <p/>
 * DNA Clones, genes and transcripts are all features, but things
 * that we don't represent as markers are also features, like
 * chromosomes, exons and UTRs.
 */
public class GBrowseFeature implements Serializable, Comparable {
    private int id;
    private long start;
    private long end;
    private GBrowseType type;
    private GBrowseContig contig;
    private Set<GBrowseName> synonyms;
    private Set<GBrowseAttribute> attributes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public GBrowseContig getContig() {
        return contig;
    }

    public void setContig(GBrowseContig contig) {
        this.contig = contig;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Set<GBrowseName> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<GBrowseName> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<GBrowseAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<GBrowseAttribute> attributes) {
        this.attributes = attributes;
    }

    public GBrowseType getType() {
        return type;
    }

    public void setType(GBrowseType type) {
        this.type = type;
    }

    public String getName() {
        for (GBrowseName gbrowseName : getSynonyms()) {
            if (gbrowseName.getDisplayName()) return gbrowseName.getName();
        }
        return null;
    }


    public int compareTo(Object o) {
        GBrowseFeature other = (GBrowseFeature) o;
        if (other == null)
            return 1;
        return getStart().compareTo(other.getStart());
    }

    public String toString() {
        return getName() + ":" + getType().getTag();
    }

}

