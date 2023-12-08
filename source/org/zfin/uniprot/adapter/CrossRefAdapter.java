package org.zfin.uniprot.adapter;

import org.biojavax.CrossRef;
import org.biojavax.RankedCrossRef;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleCrossRef;

import java.util.Collection;
import java.util.Set;

public class CrossRefAdapter {
    public final CrossRef originalCrossRef;

    public CrossRefAdapter(CrossRef wrappedObject) {
        this.originalCrossRef = wrappedObject;
    }

    public static Collection<CrossRefAdapter> fromRankedCrossRefs(Set<RankedCrossRef> rankedCrossRefs) {
        return rankedCrossRefs.stream().map(rc -> new CrossRefAdapter(rc.getCrossRef())).toList();
    }

    public static CrossRefAdapter create(String dbname, String acc) {
        CrossRef crossRef = (CrossRef) RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{dbname,acc,Integer.valueOf(0)});
        return new CrossRefAdapter(crossRef);
    }

    public String getAccession() {
        return originalCrossRef.getAccession();
    }

    public String getDbname() {
        return originalCrossRef.getDbname();
    }
}
