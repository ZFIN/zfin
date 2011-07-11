package org.zfin.marker.presentation;

import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

/**
 */
public class SequenceInfo  {

    private List<DBLink> dbLinks;
    private int numberDBLinks;
    private boolean isSorted = false ;
    private Comparator<DBLink> comparator = new DbLinkDisplayComparator();
    private boolean hasMoreLinks = false;

    public SequenceInfo() {
//        super(new LengthSorting());
    }

    public void addDBLinks(Collection<DBLink> links) {
        for (DBLink dbLink : links) {
            addDBLink(dbLink);
        }
    }

    public void addDBLinks(List<MarkerDBLink> links) {
        for (DBLink dbLink : links) {
            addDBLink(dbLink);
        }
        isSorted = false ;
    }

    public void addDBLink(DBLink dblink) {
        if(dbLinks == null){
            dbLinks = new ArrayList<DBLink>();
        }
        dbLinks.add(dblink);
        isSorted = false ;
    }

    public int getNumberDBLinks() {
        return numberDBLinks;
    }

    public void setNumberDBLinks(int numberDBLinks) {
        this.numberDBLinks = numberDBLinks;
    }

//    protected static class LengthSorting implements Comparator<DBLink> {
//        //todo: apply the sorting rules from the apg
//        //order:   fdbdt_display_order,fdb_db_significance ,dblink_length desc, fdb_db_name
//
//
//        public int compare(DBLink dblinkA, DBLink dblinkB) {
//
//
//            //todo: replace with fdbdt_display_order
//            ForeignDBDataType.DataType typeA = dblinkA.getReferenceDatabase().getForeignDBDataType().getDataType();
//            ForeignDBDataType.DataType typeB = dblinkB.getReferenceDatabase().getForeignDBDataType().getDataType();
//
//            if (!typeA.equals(typeB)) {
//                return typeA.compareTo(typeB);
//            }
//
//
//            //fdb_db_significance
//            Integer sigA = dblinkA.getReferenceDatabase().getForeignDB().getSignificance();
//            Integer sigB = dblinkB.getReferenceDatabase().getForeignDB().getSignificance();
//            if (!sigA.equals(sigB)) {
//                return sigA.compareTo(sigB);
//            }
//
//            //dblink_length desc
//            Integer lengthA = dblinkA.getLength();
//            Integer lengthB = dblinkB.getLength();
//            //reversed to make them descending
//            if (lengthA == null) lengthA = 0;
//            if (lengthB == null) lengthB = 0;
//            if (!lengthA.equals(lengthB))
//                return lengthB.compareTo(lengthA);
//
//            //fdb_db_name
//            String nameA = dblinkA.getReferenceDatabase().getForeignDB().getDbName().toString();
//            String nameB = dblinkB.getReferenceDatabase().getForeignDB().getDbName().toString();
//            if (!nameA.equals(nameB))
//                return nameA.compareTo(nameB);
//
//            return dblinkA.getZdbID().compareTo(dblinkB.getZdbID());
//        }
//
//    }

    public List<DBLink> getDbLinks() {
        if(dbLinks!=null && false==isSorted){
            Collections.sort(dbLinks,comparator);
            isSorted = true;
        }
        return dbLinks;
    }


    public void setDbLinks(List<DBLink> dbLinks) {
        this.dbLinks = dbLinks;
        Collections.sort(dbLinks,comparator);
        isSorted = true;
    }

    public boolean isHasMoreLinks() {
        return hasMoreLinks;
    }

    public void setHasMoreLinks(boolean hasMoreLinks) {
        this.hasMoreLinks = hasMoreLinks;
    }
}
