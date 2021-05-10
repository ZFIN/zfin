package org.zfin.mapping;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;

/**
 * Entity for linkage members. They can contain any combination of marker abd feature,
 * i.e. there are 4 combinations (until we combine feature and marker in a single table).
 * <p/>
 * Note: The comparable interface is meant for the case where for a given marker/feature
 * a list of linkageMembers are retrieved. The first entity then is the marker/feature
 * in question and the second entity contains the linked (paired) members (marker or feature).
 */
abstract public class LinkageMember implements Cloneable, Comparable<LinkageMember> {

    protected long id;
    protected Double lod;
    protected Double distance;
    protected String metric;
    protected String markerOneZdbId;
    protected String markerTwoZdbId;
    protected Linkage linkage;

    // Ensure to set these variables by the sub classes
    // attributes of the second entity
    protected EntityZdbID entityOne;
    protected EntityZdbID entityTwo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Double getLod() {
        return lod;
    }

    public void setLod(Double lod) {
        this.lod = lod;
    }

    public Linkage getLinkage() {
        return linkage;
    }

    public void setLinkage(Linkage linkage) {
        this.linkage = linkage;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getMarkerOneZdbId() {
        return markerOneZdbId;
    }

    public void setMarkerOneZdbId(String markerOneZdbId) {
        this.markerOneZdbId = markerOneZdbId;
    }

    public String getMarkerTwoZdbId() {
        return markerTwoZdbId;
    }

    public void setMarkerTwoZdbId(String markerTwoZdbId) {
        this.markerTwoZdbId = markerTwoZdbId;
    }

    public EntityZdbID getEntityOne() {
        return entityOne;
    }

    public EntityZdbID getEntityTwo() {
        return entityTwo;
    }

    /**
     * Returns the second marker / feature, named as linked member
     *
     * @return
     */
    abstract public ZdbID getLinkedMember();

    public abstract LinkageMember getInverseMember();

    @Override
    public int compareTo(LinkageMember o) {
        int publicationComparison = linkage.getPublication().getAuthors().compareTo(o.getLinkage().getPublication().getAuthors());
        if (publicationComparison != 0)
            return publicationComparison;
        int chromosomeComparison = linkage.getChromosome().compareTo(o.getLinkage().getChromosome());
        if (chromosomeComparison != 0)
            return chromosomeComparison;
        if (!entityOne.getEntityType().equals(o.entityOne.getEntityType())) {
            if (entityOne.getEntityType().equals(Feature.MUTANT))
                return 1;
            else if (o.entityOne.getEntityType().equals(Feature.MUTANT))
                return -1;
            else
                return entityOne.getEntityType().compareTo(o.entityOne.getEntityType());
        }
        return entityOne.getAbbreviationOrder().compareToIgnoreCase(o.entityOne.getAbbreviationOrder());
    }
}
