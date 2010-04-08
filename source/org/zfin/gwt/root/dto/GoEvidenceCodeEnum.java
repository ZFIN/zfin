package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public enum GoEvidenceCodeEnum implements IsSerializable  {
    IDA, IPI, IGI, IMP, IEP, IC, ISS, IEA, ND;

    public static final int CARDINALITY_ANY=-1 ;


    public InferenceCategory[] getInferenceCategories() {
        switch (this) {
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
                return new InferenceCategory[]{InferenceCategory.ZFIN_GENE, InferenceCategory.ZFIN_MRPH_GENO, InferenceCategory.REFSEQ,
                        InferenceCategory.GENBANK};
            case IPI:
                return new InferenceCategory[]{InferenceCategory.ZFIN_GENE, InferenceCategory.REFSEQ,
                        InferenceCategory.GENPEPT, InferenceCategory.UNIPROTKB};
            case IMP:
                return new InferenceCategory[]{InferenceCategory.ZFIN_MRPH_GENO};
            case ND:
                return new InferenceCategory[]{};
            case ISS:
                return new InferenceCategory[]{InferenceCategory.ZFIN_GENE,InferenceCategory.REFSEQ, InferenceCategory.GENBANK,
                        InferenceCategory.UNIPROTKB, InferenceCategory.GENPEPT, InferenceCategory.INTERPRO
                };
            default:
                return InferenceCategory.values();
        }
    }

    public InferenceCategory[] getInferenceCategories(String pubZdbID) {
        switch (this) {
            case IEA:
                if(pubZdbID.equals(GoCurationDefaultPublications.EC.zdbID())){
                    return new InferenceCategory[]{InferenceCategory.EC};
                }
                else
                if(pubZdbID.equals(GoCurationDefaultPublications.SPKW.zdbID())){
                    return new InferenceCategory[]{InferenceCategory.SP_KW};
                }
                else
                if(pubZdbID.equals(GoCurationDefaultPublications.INTERPRO.zdbID())){
                    return new InferenceCategory[]{InferenceCategory.INTERPRO};
                }
                else{
                    // just return them all instead and report validation issues
                    return getInferenceCategories();
                }
            default:
                return getInferenceCategories();
        }
    }


    public int getInferenceCategoryCardinality(){
        switch (this) {
            case IC: return 1;
            case IDA: return 0;
            case IEA: return 1 ;
            case IEP: return 0 ;
            case IGI: return CARDINALITY_ANY;
            case IPI: return CARDINALITY_ANY;
            case IMP: return CARDINALITY_ANY;
            case ND: return 0 ;
            case ISS: return CARDINALITY_ANY;
            default: return CARDINALITY_ANY;
        }
    }


}
