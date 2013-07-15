package org.zfin.marker.presentation;

import org.zfin.sequence.DBLink;

import java.util.Comparator;

public class DbLinkDisplayComparator implements Comparator<DBLink> {

    /**
     * Sort by:
     * 1. display of of foreign_db_data_type
     * 2. foreign_db significance
     * 3. length of db_link
     * 4. name of db_link accession number
     *
     * @param dbLink1 dbLink
     * @param dbLink2 dbLink
     * @return integer
     */
    @Override
    public int compare(DBLink dbLink1, DBLink dbLink2) {

        if (dbLink1 == null && dbLink2 == null)
            return 0;
        if (dbLink1 == null)
            return 1;
        if (dbLink2 == null)
            return -1;

        int comparator;
        comparator = dbLink1.getReferenceDatabase().getForeignDBDataType().getDisplayOrder()
                - dbLink2.getReferenceDatabase().getForeignDBDataType().getDisplayOrder();
        if (comparator != 0)
            return comparator;

        comparator = dbLink1.getReferenceDatabase().getForeignDB().getSignificance()
                - dbLink2.getReferenceDatabase().getForeignDB().getSignificance();
        if (comparator != 0)
            return comparator;

        if (dbLink2.getLength() == null && dbLink1.getLength() != null)
            return -1;
        if (dbLink1.getLength() == null && dbLink2.getLength() != null)
            return 1;

        if (dbLink1.getLength() != null && dbLink2.getLength() != null) {
            if (dbLink2.getLength() > dbLink1.getLength())
                return 1;
            if (dbLink1.getLength() > dbLink2.getLength())
                return -1;
        }
        return dbLink2.getAccessionNumberDisplay().compareTo(dbLink1.getAccessionNumberDisplay());
    }
}
