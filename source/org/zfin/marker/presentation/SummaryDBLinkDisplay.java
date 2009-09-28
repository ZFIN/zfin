package org.zfin.marker.presentation;

import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.DBLink;

import java.util.TreeSet;
import java.util.TreeMap;

public class SummaryDBLinkDisplay extends TreeMap<ReferenceDatabase, TreeSet<DBLink>> {

    public void addDBLink(DBLink dblink) {
        if (dblink != null) {
            ReferenceDatabase refdb = dblink.getReferenceDatabase();
            TreeSet<DBLink> dblinks ;

            //if we don't already have a TreeSet for this type, make one
            if (!this.containsKey(refdb)) {
                dblinks = new TreeSet<DBLink>();
                this.put(refdb,dblinks);
            } else {
                dblinks = this.get(refdb);
            }

            //now add
            dblinks.add(dblink);

        }
    }

}
