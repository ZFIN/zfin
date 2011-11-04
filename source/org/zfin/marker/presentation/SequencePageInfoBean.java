package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

/**
 */
public class SequencePageInfoBean {

    private Collection<DBLink> dbLinks;
    private Collection<MarkerDBLink> firstRelatedMarkerDBLink ;
    private Collection<MarkerDBLink> secondRelatedMarkerDBLink ;
    private Marker marker;


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

//    public Collection<MarkerDBLink> getFirstRelatedMarkerDBLink() {
//        return firstRelatedMarkerDBLink;
//    }

    public List<MarkerDBLink> getFirstRelatedMarkerDBLink() {
        List<MarkerDBLink> dblinks = new ArrayList<MarkerDBLink>(firstRelatedMarkerDBLink);
        Collections.sort(dblinks,new DbLinkMarkerSortComparator());
        return dblinks ;
    }

    public void setFirstRelatedMarkerDBLink(Collection<MarkerDBLink> firstRelatedMarkerDBLink) {
        this.firstRelatedMarkerDBLink = firstRelatedMarkerDBLink;
    }

    public List<MarkerDBLink> getSecondRelatedMarkerDBLink() {
        List<MarkerDBLink> dbLinks = new ArrayList<MarkerDBLink>(secondRelatedMarkerDBLink);
        Collections.sort(dbLinks,new DbLinkMarkerSortComparator());
        return dbLinks;
    }

    public void setSecondRelatedMarkerDBLink(Collection<MarkerDBLink> secondRelatedMarkerDBLink) {
        this.secondRelatedMarkerDBLink = secondRelatedMarkerDBLink;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }


}
