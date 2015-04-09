package org.zfin.infrastructure;

import org.zfin.infrastructure.delete.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
        String[] components = id.split("-");
        for (Type zdbType : Type.values()) {
            if (components[1].equals(zdbType.name())) {
                type = zdbType;
            }
        }

        if (type == null) {
            throw new InvalidZdbID(id, Type.getValues());
        }
        return type;
    }

    public static Type getType(String id) {
        if (id == null) {
            return null;
        }

        if (!id.startsWith(ActiveSource.ZDB))
            return null;

        Type type = null;
        String[] components = id.split("-");
        for (Type zdbType : Type.values()) {
            if (components[1].equals(zdbType.name())) {
                type = zdbType;
            }
        }

        if (type == null) {
            return null;
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
        ALT(DeleteFeatureRule.class),
        ANAT,
        ANATCMK,
        API,
        ATB(DeleteAntibodyRule.class),
        BAC,
        BAC_END,
        BHIT,
        BQRY,
        BRPT,
        CDNA,
        CDT,
        CHROMO,
        CND,
        CRISPR(DeleteSTRRule.class),
        CUR,
        DALIAS,
        DBLINK,
        DNOTE,
        EFG(DeleteEFGRule.class),
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
        GENEP,
        GENOX,
        GENO(DeleteGenotypeRule.class),
        GENOFE,
        GOTERM,
        GTCONSTRCT,
        IMAGE,
        IMAGEP,
        INFGRP,
        LINK,
        MAPDEL,
        MREL,
        CMREL,
        MRKRGOEV,
        MRKRSEQ,
        MRPHLNO(DeleteSTRRule.class),
        NOMEN,
        OEVDISP,
        ORTHO,
        PAC,
        PAC_END,
        PNOTE,
        PROBELI,
        PTCONSTRCT,
        RAPD,
        REFCROS,
        REGION(DeleteRegionRule.class),
        RUN,
        SNP,
        SSLP,
        STAGE,
        STS,
        TALEN(DeleteSTRRule.class),
        TEMP,
        TERM,
        TERMREL,
        TGCONSTRCT(DeleteConstructRule.class),
        TSCRIPT,
        URLREF,
        XPAT,
        XPATINF,
        XPATRES,
        ZYG;

        private Class<? extends DeleteEntityRule> ruleClass;
        private static String allValues;

        Type() {
        }

        Type(Class<? extends DeleteEntityRule> deleteEntityRuleClass) {
            this.ruleClass = deleteEntityRuleClass;
        }

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

        public DeleteEntityRule getDeleteEntityRule(String zdbID) {
            DeleteEntityRule rule = null;
            try {
                Constructor constructor = ruleClass.getConstructor(String.class);
                rule = (DeleteEntityRule) constructor.newInstance(zdbID);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return rule;
        }
    }
}
