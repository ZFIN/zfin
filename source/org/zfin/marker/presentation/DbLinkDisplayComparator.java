package org.zfin.marker.presentation;

import org.zfin.sequence.DBLink;

import java.util.Comparator;

/**
 */
public class DbLinkDisplayComparator implements Comparator<DBLink>{

    @Override
    public int compare(DBLink dbLink1, DBLink dbLink2) {
        int comparator ;
        comparator = dbLink1.getReferenceDatabase().getForeignDBDataType().getDisplayOrder()
                - dbLink2.getReferenceDatabase().getForeignDBDataType().getDisplayOrder();
        if (comparator != 0)  return comparator ;

        comparator = dbLink1.getReferenceDatabase().getForeignDB().getSignificance()
                - dbLink2.getReferenceDatabase().getForeignDB().getSignificance();
        if (comparator != 0)  return comparator ;
//
        if(dbLink2.getLength()!=null && dbLink1.getLength()!=null){
            comparator = dbLink2.getLength()
                    - dbLink1.getLength();
        }
        if(dbLink1==null && dbLink2 !=null){
            return 1 ;
        }
        if(dbLink1!=null && dbLink2 ==null){
            return -1 ;
        }
        if (comparator != 0)  return comparator ;

        // compare query, really?
        // not doing this,

        return  dbLink2.getAccessionNumberDisplay().compareTo(dbLink1.getAccessionNumberDisplay());
    }
}
