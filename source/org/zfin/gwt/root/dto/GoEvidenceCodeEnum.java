package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Note: NAS is only for display of existing evidence (case 5664).
 * Note: TAS is only for filtering out imported evidence (case 6447).
 */
public enum GoEvidenceCodeEnum implements IsSerializable {
    IDA, IPI, IGI, IMP, IEP, IC, ISS, IEA, ND, NAS, TAS, IBA, IBD, IKR, IMR, IRD;

    public static final int CARDINALITY_ANY = -1;
    public static final int CARDINALITY_ONE_OR_MORE = -2;


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
                return new InferenceCategory[]{InferenceCategory.ZFIN_GENE, InferenceCategory.REFSEQ,
                        InferenceCategory.GENBANK, InferenceCategory.UNIPROTKB,
                        InferenceCategory.GENPEPT,  InferenceCategory.INTERPRO,
                        InferenceCategory.PANTHER
                };
            default:
                return InferenceCategory.values();
        }
    }

    public InferenceCategory[] getInferenceCategories(String pubZdbID) {
        GoDefaultPublication goDefaultPublication = GoDefaultPublication.getPubForZdbID(pubZdbID);
        if (goDefaultPublication != null && goDefaultPublication.inferenceCategory() != null) {
            return new InferenceCategory[]{goDefaultPublication.inferenceCategory()};
        } else {
            return getInferenceCategories();
        }
//        return (goDefaultPublication != null ? new InferenceCategory[]{goDefaultPublication.inferenceCategory()} : getInferenceCategories());
    }


    public int getInferenceCategoryCardinality() {
        switch (this) {
            case NAS:
                return 0;
            case IC:
                return CARDINALITY_ONE_OR_MORE;
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
        if (pubZdbID.equals(GoDefaultPublication.EC.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.IEA};
        } else if (pubZdbID.equals(GoDefaultPublication.SPKW.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.IEA};
        } else if (pubZdbID.equals(GoDefaultPublication.INTERPRO.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.IEA};
        } else if (pubZdbID.equals(GoDefaultPublication.ROOT.zdbID())) {
            return new GoEvidenceCodeEnum[]{GoEvidenceCodeEnum.ND};
        } else {
            List<GoEvidenceCodeEnum> goEvidenceCodeEnumList = new ArrayList<GoEvidenceCodeEnum>();
            int counter = 0;
            // add all but the other two
            for (GoEvidenceCodeEnum goEvidenceCodeEnum : GoEvidenceCodeEnum.values()) {
                switch (goEvidenceCodeEnum) {
                    // these ones are filtered out
                    case IEA:
                    case ND:
                    case TAS:
                    case NAS:
                    case IBA:
                    case IBD:
                    case IKR:
                    case IMR:
                    case IRD:
                        break;
                    // add all remaining
                    default:
                        goEvidenceCodeEnumList.add(goEvidenceCodeEnum);
                        break;
                }
            }
            return goEvidenceCodeEnumList.toArray(new GoEvidenceCodeEnum[goEvidenceCodeEnumList.size()]);
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
