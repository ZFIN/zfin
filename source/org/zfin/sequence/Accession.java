/**
 *  Class Accession.
 */
package org.zfin.sequence;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.orthology.Species;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A wrapper around the accession_bank table.
 */
public class Accession implements Comparable, Serializable {

    private Logger logger = Logger.getLogger(Accession.class);

    private Long ID;
    private String number;
    private String defline;
    private Integer length;
    private String abbreviation;
    private Set<LinkageGroup> linkageGroups;
    private ReferenceDatabase referenceDatabase;
    private Set<EntrezProtRelation> relatedEntrezAccessions;
//    private Set<Accession> relatedAccessions;
    private Set<DBLink> dbLinks;
    private Set<MarkerDBLink> blastableMarkerDBLinks;


    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDefline() {
        return defline;
    }

    public void setDefline(String defline) {
        this.defline = defline;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public Set<LinkageGroup> getLinkageGroups() {
        return linkageGroups;
    }

    public void setLinkageGroups(Set<LinkageGroup> linkageGroups) {
        this.linkageGroups = linkageGroups;
    }

    public String getURL() {
        return referenceDatabase.getBaseURL();
    }

//    public Accession getEntrezGeneAccession () {
//        for (Accession rc : relatedAccessions){
//            if (rc.getReferenceDatabase().getNumber().equals("Entrez"))
//                return rc;
//        }
//        return null;
//    }


    public Set<EntrezProtRelation> getRelatedEntrezAccessions() {
        return relatedEntrezAccessions;
    }

    public void setRelatedEntrezAccessions(Set<EntrezProtRelation> relatedEntrezAccessions) {
        this.relatedEntrezAccessions = relatedEntrezAccessions;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    //    todo: update to use get/setMarkerDBLinks

    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        for (DBLink link : getDbLinks()) {
            // for some reason this doesn't seem to always work here, so use a try catch block, as well
            if (link instanceof MarkerDBLink) {
                MarkerDBLink markerLink = (MarkerDBLink) link;
                markers.add(markerLink.getMarker());
            }
        }
        return markers;
    }

    public List<Marker> getBlastableMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        for (MarkerDBLink link : getBlastableMarkerDBLinks()) {
            if (link.getMarker() != null) {
                markers.add(link.getMarker());
            }
        }
        return markers;
    }

    public int compareTo(Object o) {
        if (o instanceof Accession) {
            Accession a = (Accession) o;
            return a.getNumber().compareTo(getNumber());
        } else {
            return 0;
        }
    }

    public Set<DBLink> getDbLinks() {

        if (this.dbLinks == null) {
            HibernateUtil.currentSession().flush();
            HibernateUtil.currentSession().refresh(this);
        }
        return this.dbLinks;
    }

    public void setDbLinks(Set<DBLink> dbLinks) {
        this.dbLinks = dbLinks;
    }


    public Set<MarkerDBLink> getBlastableMarkerDBLinks() {
        if (this.blastableMarkerDBLinks == null) {
            HibernateUtil.currentSession().flush();

            HibernateUtil.currentSession().refresh(this);
        }
        return this.blastableMarkerDBLinks;
    }

    public void setBlastableMarkerDBLinks(Set<MarkerDBLink> blastableMarkerDBLinks) {
        this.blastableMarkerDBLinks = blastableMarkerDBLinks;
    }

    public Species getOrganism() {
        if (CollectionUtils.isEmpty(relatedEntrezAccessions)) {
            return Species.ZEBRAFISH;
        }
        Species species = null;
        for (EntrezProtRelation entrezProtRelation : relatedEntrezAccessions) {
            Species entrezSpecies = entrezProtRelation.getOrganism();
            if (species == null) {
                species = entrezSpecies;
            } else {
                if (species != entrezSpecies) {
                    throw new RuntimeException("Same accessions for different species, not allowed!!");
                }
            }

        }
        return species;
    }

    public boolean equals(Object o) {
        if (o instanceof Accession) {
            Accession a = (Accession) o;
            if (a.getNumber().equals(getNumber())
                    &&
                    a.getReferenceDatabase().getForeignDB().getDbName().equals(getReferenceDatabase().getForeignDB().getDbName())
                    ) {
                return true;
            }
        }
        return false;
    }
}


