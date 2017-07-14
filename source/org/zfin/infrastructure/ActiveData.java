package org.zfin.infrastructure;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.delete.*;
import org.zfin.mapping.Panel;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.repository.RepositoryFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Entity
@Table(name = "zdb_active_data")
public class ActiveData implements ZdbID {

    @Id
    @Column(name = "zactvd_zdb_id")
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

        if (!id.startsWith(ActiveSource.ZDB)) {
            return false;
        }

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

        if (!id.startsWith(ActiveSource.ZDB)) {
            throw new InvalidZdbID(id);
        }

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

        if (!id.startsWith(ActiveSource.ZDB)) {
            return null;
        }

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

    public static boolean isMarker(Type type) {
        if (type == null) {
            return false;
        }
        return type.equals(Type.ATB) ||
                type.equals(Type.BAC) ||
                type.equals(Type.BAC_END) ||
                type.equals(Type.CDNA) ||
                type.equals(Type.CRISPR) ||
                type.equals(Type.EFG) ||
                type.equals(Type.EST) ||
                type.equals(Type.ETCONSTRCT) ||
                type.equals(Type.FOSMID) ||
                type.equals(Type.GENE) ||
                type.equals(Type.GENEP) ||
                type.equals(Type.GTCONSTRCT) ||
                type.equals(Type.MRPHLNO) ||
                type.equals(Type.PAC) ||
                type.equals(Type.RAPD) ||
                type.equals(Type.EREGION) ||
                type.equals(Type.SNP) ||
                type.equals(Type.SSLP) ||
                type.equals(Type.STS) ||
                type.equals(Type.TALEN) ||
                type.equals(Type.TGCONSTRCT) ||
                type.equals(Type.TSCRIPT);
    }

    public static boolean isGeneOrGeneP(String ID) {
        Type type = getType(ID);
        if (type.equals(Type.GENE) || type.equals(Type.GENEP)) {
            return true;
        }
        return false;
    }

    public static boolean isInGroupGenedom(String ID) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(ID);
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_NTR)) {
            return true;
        }
        return false;
    }

    public enum Type {
        ALT(DeleteFeatureRule.class, Feature.class),
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
        CNE,
        CRISPR(DeleteSTRRule.class),
        CUR,
        CV,
        DAT,
        DALIAS(null, DataAlias.class),
        DBLINK,
        DNOTE,
        EFG(DeleteEFGRule.class),
        EST,
        ETCONSTRCT(DeleteConstructRule.class),
        EXP,
        EXPUNIT,
        EXPCOND,
        EXTNOTE,
        FDBCONT,
        FDMD,
        FHIST,
        FISH(DeleteFishRule.class, Fish.class),
        FIG,
        FMREL,
        FOSMID,
        FPMD,
        FTMD,
        GENE,
        GENEP,
        GENOX,
        GENO(DeleteGenotypeRule.class, Genotype.class),
        GENOFEAT,
        GOTERM,
        GTCONSTRCT(DeleteConstructRule.class),
        IMAGE,
        IMAGEP,
        INFGRP,
        LINK,
        LOCUS,
        MAPDEL,
        MREL,
        CMREL,
        MRKRGOEV,
        MRKRSEQ,
        MRPHLNO(DeleteSTRRule.class),
        NOMEN(null, MarkerHistory.class),
        ORTHO,
        PAC,
        PAC_END,
        PNOTE,
        PROBELI,
        PTCONSTRCT(DeleteConstructRule.class),
        RAPD,
        REFCROSS(null, Panel.class),
        EREGION(DeleteRegionRule.class),
        RUN,
        SALIAS,
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
        ZYG,
        MIRNAG,
        SNORNAG,
        RRNAG,
        LNCRNAG,
        LINCRNAG,
        PIRNAG,
        TRNAG,
        SRPRNAG,
        SCRNAG,
        NCRNAG,
        NCCR,
        TLNRR,
        BR,
        BINDSITE,
        LIGANDBS,
        TFBS,
        EBS,
        NCBS,
        EMR,
        HMR,
        MDNAB,
        RR,
        TRR,
        PROMOTER,
        ENHANCER,
        LCR,
        NUCMO,
        DNAMO,
        RNAMO,
        PROTBS;


        private Class<? extends DeleteEntityRule> ruleClass;
        private static String allValues;
        private Class<? extends EntityZdbID> entity;

        Type() {
        }


        Type(Class<? extends DeleteEntityRule> deleteEntityRuleClass) {
            this.ruleClass = deleteEntityRuleClass;
        }

        Type(Class<? extends DeleteEntityRule> deleteEntityRuleClass, Class<? extends EntityZdbID> entity) {
            this.ruleClass = deleteEntityRuleClass;
            this.entity = entity;
        }

        public static String getValues() {
            if (allValues != null) {
                return allValues;
            }
            StringBuilder sb = new StringBuilder("[");
            int size = values().length;
            int index = 0;
            for (Type type : values()) {
                sb.append(type.name());
                index++;
                if (index < size) {
                    sb.append(",");
                }
            }
            sb.append("]");
            allValues = sb.toString();
            return allValues;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type)) {
                    return t;
                }
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

        public EntityZdbID getEntity(String zdbID) {
            return getInfrastructureRepository().getEntityByID(this.entity, zdbID);
        }

    }
}
