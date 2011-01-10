package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/** Note that NAS is only for display of existing evidence (case 5664).  
 */
public enum GoEvidenceCodeEnum implements IsSerializable {
    IDA, IPI, IGI, IMP, IEP, IC, ISS, IEA, ND, NAS;

    public static final int CARDINALITY_ANY = -1;


    public InferenceCategory[] getInferenceCategories() {
        switch (this) {
            case NAS: // has no inferences
                return new InferenceCategory[]{};
            case IC:
                return new InferenceCategory[]{InferenceCategory.GO};
            case IDA:
                return new InferenceCategory[]{};
            case IEA:
                return new InferenceCategory[]{InferenceCategory.SP_KW, InferenceCategory.EC,
                        InferenceCategory.INTERPRO};
            case IEP:
                return new InferenceCategory[]{};
            case IGI:
                return new InferenceCategory[]{InferenceCategory.ZFIN_MRPH_GENO, InferenceCategory.ZFIN_GENE, InferenceCategory.REFSEQ,
                        InferenceCategory.GENBANK};
            case IPI:
                return new InferenceCategory[]{InferenceCategory.ZFIN_GENE, InferenceCategory.REFSEQ,
                        InferenceCategory.GENPEPT, InferenceCategory.UNIPROTKB};
            case IMP:
                return new InferenceCategory[]{InferenceCategory.ZFIN_MRPH_GENO};
            case ND:
                return new InferenceCategory[]{};
            case ISS:
                return new InferenceCategory[]{InferenceCategory.ZFIN_GENE, InferenceCategory.REFSEQ, InferenceCategory.GENBANK,
                        InferenceCategory.UNIPROTKB, InferenceCategory.GENPEPT, InferenceCategory.INTERPRO
                };
            default:
                return InferenceCategory.values();
        }
    }

    public InferenceCategory[] getInferenceCategories(String pubZdbID) {
        switch (this) {
            case IEA:
                if (pubZdbID.equals(GoCurationDefaultPublications.EC.zdbID())) {
                    return new InferenceCategory[]{InferenceCategory.EC};
                } else if (pubZdbID.equals(GoCurationDefaultPublications.SPKW.zdbID())) {
                    return new InferenceCategory[]{InferenceCategory.SP_KW};
                } else if (pubZdbID.equals(GoCurationDefaultPublications.INTERPRO.zdbID())) {
                    return new InferenceCategory[]{InferenceCategory.INTERPRO};
                } else {
                    // just return them all instead and report validation issues
                    return getInferenceCategories();
                }
            default:
                return getInferenceCategories();
        }
    }


    public int getInferenceCategoryCardinality() {
        switch (this) {
            case NAS:
                return 0;
            case IC:
                return 1;
            case IDA:
                return 0;
            case IEA:
                return 1;
            case IEP:
                return 0;
            case IGI:
                return CARDINALITY_ANY;
            case IPI:
                return 1;
            case IMP:
                return CARDINALITY_ANY;
            case ND:
                return 0;
            case ISS:
                return CARDINALITY_ANY;
            default:
                return CARDINALITY_ANY;
        }
    }


    public static GoEvidenceCodeEnum[] getCodeEnumForPub(String pubZdbID) {
        if (pubZdbID.equals(GoCurationDefaultPublications.EC.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.IEA};
        } else if (pubZdbID.equals(GoCurationDefaultPublications.SPKW.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.IEA};
        } else if (pubZdbID.equals(GoCurationDefaultPublications.INTERPRO.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.IEA};
        } else if (pubZdbID.equals(GoCurationDefaultPublications.ROOT.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.ND};
        } else {
            GoEvidenceCodeEnum[] returnEvidences = new GoEvidenceCodeEnum[GoEvidenceCodeEnum.values().length - 3];
            int counter = 0;
            // add all but the other two
            for (GoEvidenceCodeEnum goEvidenceCodeEnum : GoEvidenceCodeEnum.values()) {
                switch (goEvidenceCodeEnum) {
                    case IEA:
                    case ND:
                    case NAS:
                        break;
                    default:
                        returnEvidences[counter++] = goEvidenceCodeEnum;
                        break;
                }
            }
            return returnEvidences;
        }
    }

    public static GoEvidenceCodeEnum getType(String type) {
        for (GoEvidenceCodeEnum t : values()) {
            if (t.toString().equals(type.trim()))
                return t;
        }
        throw new RuntimeException("No GoEvidenceCodeEnum named " + type + " found.");
    }
}
