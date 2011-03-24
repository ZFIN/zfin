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

    public static boolean validateActiveData(String id) {
        if (id == null) {
            return false;
        }

        if (!id.startsWith(ActiveSource.ZDB))
            return false;

        Type type = null;
        for (Type zdbType : Type.values()) {
            if (id.contains(zdbType.name())) {
                type = zdbType;
            }
        }
        return type != null;
    }

    public static boolean isValidActiveData(String id, Type type) {
        if (type == null || id == null) {
            return false;
        }
        return id.contains(type.name());
    }

    public static Type validateID(String id) {
        if (id == null) {
            throw new InvalidZdbID();
        }

        if (!id.startsWith(ActiveSource.ZDB))
            throw new InvalidZdbID(id);

        Type type = null;
        for (Type zdbType : Type.values()) {
            if (id.contains(zdbType.name())) {
                type = zdbType;
            }
        }

        if (type == null) {
            throw new InvalidZdbID(id, Type.getValues());
        }
        return type;
    }

    public Type validateID() {
        return validateID(zdbID);
    }

    public static Type getTypeForId(String ID) {
        Type type = null;
        try {
            type = Type.getType(ID);
        } catch (Exception e) {
            //ignore
        }
        return type;
    }

    public enum Type {
        ALT,
        ANAT,
        ANATCMK,
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
        ETCONSTRCT,
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
        GTCONSTRCT,
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
        PTCONSTRCT,
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
        TGCONSTRCT,
        TSCRIPT,
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
