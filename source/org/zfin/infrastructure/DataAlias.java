package org.zfin.infrastructure;

import org.zfin.publication.Publication;

import java.util.Set;
import java.util.HashSet;

/**
 * Base class for alias records. You need to extend this class
 * to make use of the real function, e.g. MarkerAlias or AnatomySynonym.
 */
public class DataAlias implements Comparable {

    public enum Group {
        ALIAS("alias"),
        PLURAL("plural"),
        SECONDARY_ID("secondary id"),
        SEQUENCE_SIMILARITY("sequence similarity"),
        EXACT_ALIAS("exact alias"),
        RELATED_ALIAS("related alias"),
        RELATED_PLURAL("related plural"),
        EXACT_PLURAL("exact plural");

        private String value;

        private Group(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    protected String zdbID;
    protected String alias;
    protected Group group;
    protected String aliasLowerCase;

    // this property is only useful when not only publications are desired
    protected Set<ActiveSource> sources;

    protected Set<PublicationAttribution> publications;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getAliasLowerCase() {
        return aliasLowerCase;
    }

    public void setAliasLowerCase(String aliasLowerCase) {
        this.aliasLowerCase = aliasLowerCase;
    }

    public Set<ActiveSource> getSources() {
        return sources;
    }

    public void setSources(Set<ActiveSource> sources) {
        this.sources = sources;
    }

    public int getSourceCount() {
        if (sources == null)
            return 0;
        else
            return sources.size();
    }

    public Set<PublicationAttribution> getPublications() {
        if (publications == null)
          return new HashSet<PublicationAttribution>();
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            for (PublicationAttribution pubAttr : getPublications())
                return pubAttr.getPublication();
        }
        return null;
    }

    public int compareTo(Object otherAlias) {
        return aliasLowerCase.compareTo(((DataAlias) otherAlias).getAliasLowerCase());
    }
}
