package org.zfin.infrastructure;

/**
 */
public class ActiveData implements ZdbID {
    private String zdbID;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
        validateID();
    }

    public Type validateID() {
        if (zdbID == null) {
            throw new InvalidZdbID();
        }

        if (!zdbID.startsWith(ActiveSource.ZDB))
            throw new InvalidZdbID(zdbID);

        Type type = null;
        for (Type zdbType : Type.values()) {
            if (zdbID.contains(zdbType.name())) {
                type = zdbType;
            }
        }

        if (type == null) {
            throw new InvalidZdbID(zdbID, Type.getValues());
        }
        return type;
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
        GENOX,
        GENO,
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

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No active Data type of string " + type + " found.");
        }

    }
}
