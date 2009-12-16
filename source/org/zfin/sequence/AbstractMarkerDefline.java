package org.zfin.sequence;

import org.zfin.marker.Marker;
import org.zfin.sequence.blast.Database;

/**
 */
public abstract class AbstractMarkerDefline implements Defline {

    protected Database.Type type;

    protected abstract StringBuilder createSpecificInformation(DBLink dbLink, Marker marker, StringBuilder stringBuilder);

    protected abstract DBLink getDBLink();

    protected abstract Marker getMarker();

    /**
     * Builds the header string.  Have to split up because TranscriptDBLink doesn't inherit from MarkerDBLink
     *
     * @param dbLink        Dblink
     * @param marker        Marker attached to dblink
     * @param stringBuilder The current StringBuilder object.
     * @return
     */
    protected StringBuilder createHeader(DBLink dbLink, Marker marker, StringBuilder stringBuilder) {
        stringBuilder.append(">lcl").append("|");
        stringBuilder.append(dbLink.getAccessionNumber()).append("|");
        stringBuilder.append(marker.getZdbID());
        if (dbLink.getReferenceDatabase() != null && dbLink.getReferenceDatabase().getPrimaryBlastDatabase() != null) {
            stringBuilder.append(" ").append(dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getAbbrev());
            type = dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getType();
        }
        stringBuilder.append(" ").append(marker.getAbbreviation());
        return stringBuilder;
    }

    protected StringBuilder createLength(DBLink dbLink, StringBuilder stringBuilder) {
        stringBuilder.append(" ").append(dbLink.getLength()).append(" ");
        if (type == null) {
            stringBuilder.append(" length ");
        } else if (type == Database.Type.NUCLEOTIDE) {
            stringBuilder.append("bp");
        } else if (type == Database.Type.PROTEIN) {
            stringBuilder.append("aa");
        }
        return stringBuilder;
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        createHeader(getDBLink(), getMarker(), stringBuilder);
        createSpecificInformation(getDBLink(), getMarker(), stringBuilder);
        createLength(getDBLink(), stringBuilder);
        return stringBuilder.toString();
    }

    public String getAccession() {
        return getDBLink().getAccessionNumber();
    }

    @Override
    public boolean equals(Object o) {
        return this.toString().equals(o.toString());
    }
}
