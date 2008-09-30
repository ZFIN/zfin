package org.zfin.infrastructure;

/**
 */
public class ActiveData {
    private String zdbID;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        validateID(zdbID);
        this.zdbID = zdbID;
    }

    public static void validateID(String zdbID) {
        if (zdbID == null) {
            throw new InvalidZdbID();
        }

        if (!zdbID.startsWith(ActiveSource.ZDB))
            throw new InvalidZdbID(zdbID);

        boolean validType = false;
        for (Type type : Type.values()) {
            if (zdbID.contains(type.name()))
                validType = true;
        }

        if (!validType) {
            throw new InvalidZdbID(zdbID, Type.getValues());
        }

    }

    public enum Type {
        ALT,
        ANAT,
        ANATCMK,
        APATO,
        API,
        ATB,
        BAC,
        BAC_END,
        BHIT,
        BQRY,
        BRPT,
        CDNA,
        CDT,
        CHROMO,
        CND,
        CUR,
        DALIAS,
        DBLLINK,
        DNOTE,
        EFG,
        EST,
        ETCONST,
        EXP,
        EXPUNIT,
        EXCOND,
        EXTNOTE,
        FDBCONT,
        FHIST,
        FIG,
        FMREL,
        FOSMID,
        GENE,
        GENOFE,
        GOTERM,
        GTCONST,
        IMAGE,
        IMAGEP,
        INFGRP,
        LINK,
        MAPDEL,
        MREL,
        MRKRGOE,
        MRKRSEQ,
        MRPHLNO,
        NOMEN,
        OEVDISP,
        ORTHO,
        PAC,
        PNOTE,
        PROBELI,
        RAPD,
        REFCROS,
        REGION,
        RUN,
        SNP,
        SSLP,
        STAGE,
        STS,
        TEMP,
        TERM,
        TERMREL,
        TGCONST,
        URLREF,
        XPAT,
        XPAINF,
        XPATRES,
        ZYG;

        private static String allValues;

        public static String getValues() {
            if (allValues != null)
                return allValues;
            StringBuilder sb = new StringBuilder("[");
            int size = values().length;
            int index = 0;
            for (Type type : values()) {
                sb.append(type.name());
                index++;
                if (index < size)
                    sb.append(",");
            }
            sb.append("]");
            allValues = sb.toString();
            return allValues;
        }
    }
}
