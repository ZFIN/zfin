package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

/**
 */
public class SequencePageInfoBean {

    private Collection<DBLink> dbLinks;
    private TreeMap<String, TreeSet<MarkerDBLink>> relatedMarkerDBLinks = new TreeMap<>();
    private Marker marker;
    private List<GeneProductsBean> geneProductsBean;


    public void addDBLinks(Collection<DBLink> links) {
        for (DBLink dbLink : links) {
            addDBLink(dbLink);
        }
    }

    public List<DBLink> getDbLinkList(){
        List<DBLink> dbLinkList = new ArrayList<DBLink>();
        if(dbLinks!=null){
            dbLinkList.addAll(dbLinks);
        }
        return dbLinkList;
    }

    public void addDBLink(DBLink dblink) {
        if(dbLinks == null){
            dbLinks = new TreeSet<DBLink>(new DbLinkDisplayComparator());
        }
        if (dblink != null) {
            dbLinks.add(dblink);
        }
    }

    public Collection<DBLink> getDbLinks() {
        return dbLinks;
    }

    public void setDbLinks(Collection<DBLink> dbLinks) {
        this.dbLinks = dbLinks;
    }

    public TreeMap<String, TreeSet<MarkerDBLink>> getRelatedMarkerDBLinks() {
        return relatedMarkerDBLinks;
    }

    public void addRelatedMarkerDBLink(String relationshipType, MarkerDBLink link) {
        if (!relatedMarkerDBLinks.containsKey(relationshipType)) {
            relatedMarkerDBLinks.put(relationshipType, new TreeSet<>(new DbLinkMarkerSortComparator()));
        }
        relatedMarkerDBLinks.get(relationshipType).add(link);
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }


    public List<GeneProductsBean> getGeneProductsBean() {
        return geneProductsBean;
    }

    public void setGeneProductsBean(List<GeneProductsBean> geneProductsBean) {
        this.geneProductsBean = geneProductsBean;
    }
}
