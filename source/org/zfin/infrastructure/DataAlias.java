package org.zfin.infrastructure;

import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for alias records. You need to extend this class
 * to make use of the real function, e.g. MarkerAlias or AnatomySynonym.
 */
public class DataAlias implements Comparable, EntityAttribution, Serializable {

    protected String zdbID;
    protected String alias;
    protected DataAliasGroup.Group group;
    protected String dataZdbID;

    protected String aliasLowerCase;

    // this property is only useful when not only publications are desired
    public DataAliasGroup aliasGroup;

    public DataAliasGroup getAliasGroup() {
        return aliasGroup;
    }

    public void setAliasGroup(DataAliasGroup aliasGroup) {
        this.aliasGroup = aliasGroup;
    }

    protected Set<ActiveSource> sources;

    transient protected Set<PublicationAttribution> publications;

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

    public DataAliasGroup.Group getGroup() {
        return group;
    }

    public void setGroup(DataAliasGroup.Group group) {
        this.group = group;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
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

    public void addPublication(PublicationAttribution publicationAttribution) {
        if(this.publications==null){
            this.publications = new HashSet<PublicationAttribution>() ;
        }
        this.publications.add(publicationAttribution) ;
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


    public boolean hasPublication(PublicationAttribution publicationAttribution) {
        for(PublicationAttribution aPublicationAttribution: getPublications()){
            if(publicationAttribution.getPublication().equals(aPublicationAttribution.getPublication())){
                return true ;
            }
        }
        return false ;
    }
}
