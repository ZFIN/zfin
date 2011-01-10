package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.*;

import java.util.Arrays;

/**
 * Error strings should be of the form:
 * Annotations with <EC> evidence must have [<exactly 1, 0>] item(s) of type(s) <types> ]
 * in the inferred from field [with pub XXX in the default pub field.]
 */
public class GoEvidenceValidator {

    public final static String PROTEIN_BINDING_OBO_ID = "GO:0005515" ;

    private static String generateErrorString(GoEvidenceDTO dto) {
        String pubName = null;

        GoCurationDefaultPublications pubEnum = GoCurationDefaultPublications.getPubForZdbID(dto.getPublicationZdbID());
        if (pubEnum != null) {
            switch (pubEnum) {
                case INTERPRO:
                case SPKW:
                case EC:
                case ROOT:
                    pubName = pubEnum.title();
                    break;
            }
        }

        return generateErrorString(dto, pubName);
    }

    private static String generateErrorString(GoEvidenceDTO dto, String pubName) {
        GoEvidenceCodeEnum evidenceCode = dto.getEvidenceCode();
        int cardinality = evidenceCode.getInferenceCategoryCardinality();
        InferenceCategory[] inferenceCategories = evidenceCode.getInferenceCategories(dto.getPublicationZdbID());
        String errorString = " Annotations with ";
        errorString += evidenceCode.name() + " evidence must ";
        switch (cardinality) {
            case 0:
                errorString += " have 0 items ";
                break;
            case 1:
                errorString += " have exactly 1 item ";
                break;
            default:
                errorString += " be ";
        }
//        errorString += " in the inferred from field " ;
        if (cardinality != 0 && inferenceCategories != null) {
            errorString += " of type(s) " + Arrays.asList(inferenceCategories) + " ";
        }
        errorString += " in the inference field";

        if (pubName != null) {
            errorString += " with " + pubName + " from the default pub field";
        }

        errorString += ".";


        return errorString;
    }

    public static boolean validate(HandlesError viewClickLabel, GoEvidenceDTO dto) {
        if (dto.getGoTerm() == null || dto.getGoTerm().getZdbID().isEmpty()) {
            viewClickLabel.setError("GO Term must be valid.");
            return false;
        }
        if (dto.getGoTerm().isObsolete()) {
            viewClickLabel.setError("GO Term must not be obsolete.");
            return false;
        }

        if(dto.getPublicationZdbID()==null){
            viewClickLabel.setError("Publication must be selected.");
            return false;
        }

        if(dto.getEvidenceCode()==null){
            viewClickLabel.setError("Evidence code must be selected.");
            return false;
        }

        if (false == validateInferenceCardinality(viewClickLabel, dto)) {
            return false;
        }
        if (false == validateInferences(viewClickLabel, dto)) {
            return false;
        }

        if (dto.getEvidenceCode().equals(GoEvidenceCodeEnum.ND) &&
                false == dto.getPublicationZdbID().equals(GoCurationDefaultPublications.ROOT.zdbID())) {
            viewClickLabel.setError("Annotations with evidence code ND must have default pub " + GoCurationDefaultPublications.ROOT.title() + ".");
            return false;
        } else
        if ((false == dto.getEvidenceCode().equals(GoEvidenceCodeEnum.ND) &&
                dto.getPublicationZdbID().equals(GoCurationDefaultPublications.ROOT.zdbID()))
                ) {
            viewClickLabel.setError("Annotations with default pub " + GoCurationDefaultPublications.ROOT.title() + " must have evidence code ND.");
            return false;
        } else
        if (dto.getEvidenceCode().equals(GoEvidenceCodeEnum.IEA)) {
            String inference = dto.getInferredFrom().iterator().next();
            if (dto.getPublicationZdbID().equals(GoCurationDefaultPublications.INTERPRO.zdbID())) {
                if (false == inference.startsWith(InferenceCategory.INTERPRO.prefix())) {
                    viewClickLabel.setError(generateErrorString(dto, GoCurationDefaultPublications.INTERPRO.title()));
                    return false;
                }
            } else if (dto.getPublicationZdbID().equals(GoCurationDefaultPublications.SPKW.zdbID())) {
                if (false == inference.startsWith(InferenceCategory.SP_KW.prefix())) {
                    viewClickLabel.setError(generateErrorString(dto, GoCurationDefaultPublications.SPKW.title()));
                    return false;
                }
            } else if (dto.getPublicationZdbID().equals(GoCurationDefaultPublications.EC.zdbID())) {
                if (false == inference.startsWith(InferenceCategory.EC.prefix())) {
                    viewClickLabel.setError(generateErrorString(dto, GoCurationDefaultPublications.EC.title()));
                    return false;
                }
            } else {
                viewClickLabel.setError("Pub must be InterPro2Go, SPKW2GO, or EC2GO Mapping from default pub list and have a matching annotation.");
                return false;
            }
        }
        // not an IEA EC, but having an IEA pub
        else
        if (dto.getPublicationZdbID().equals(GoCurationDefaultPublications.INTERPRO.zdbID())
                ||
                dto.getPublicationZdbID().equals(GoCurationDefaultPublications.SPKW.zdbID())
                ||
                dto.getPublicationZdbID().equals(GoCurationDefaultPublications.EC.zdbID())
                ) {
            viewClickLabel.setError("Evidence code should be IEA for this default pub: " + GoCurationDefaultPublications.getPubForZdbID(dto.getPublicationZdbID()).title());
            return false;
        }
        else
        // fogbugz 6292
        if ( (dto.getEvidenceCode().equals(GoEvidenceCodeEnum.IPI) && 
            false==dto.getGoTerm().getTermOboID().equals(PROTEIN_BINDING_OBO_ID))
            ||
            (dto.getGoTerm().getTermOboID().equals(PROTEIN_BINDING_OBO_ID) &&
            false==dto.getEvidenceCode().equals(GoEvidenceCodeEnum.IPI))
            ) {
                viewClickLabel.setError("Protein binding ("+PROTEIN_BINDING_OBO_ID+
                        ") may only be used with the evidence code IPI." );
                return false ;
         }
        else
        if (dto.getGoTerm().getTermOboID().equals(PROTEIN_BINDING_OBO_ID) &&
            dto.getEvidenceCode().equals(GoEvidenceCodeEnum.IPI)
            ) {
             if(dto.getFlag()== GoEvidenceQualifier.NOT){
                viewClickLabel.setError("Not flag not allowed for this evidence code" );
                return false ;
              }
         }
        // fogbugz 6292
        else
        if (dto.getEvidenceCode().equals(GoEvidenceCodeEnum.IEP)) {
            if(false==dto.getGoTerm().getOntology().getOntologyName().equals("biological_process")){
                viewClickLabel.setError(
                        "Only biological process terms allowed for this evidence code, not ["
                                + dto.getGoTerm().getOntology().getOntologyName() + "]"  );
                return false ;
            }
        }

        viewClickLabel.clearError();
        return true;
    }

    private static boolean validateInferences(HandlesError viewClickLabel, GoEvidenceDTO dto) {
        if (isValidInferences(dto)) {
            return true;
        } else {
            viewClickLabel.setError(generateErrorString(dto));
            return false;
        }
    }

    private static boolean validateInferenceCardinality(HandlesError viewClickLabel, GoEvidenceDTO dto) {
        if (isValidCardinality(dto)) {
            return true;
        } else {
            viewClickLabel.setError(generateErrorString(dto));
            return false;
        }
    }


    public static boolean isValidCardinality(GoEvidenceDTO dto) {
        int cardinality = dto.getEvidenceCode().getInferenceCategoryCardinality();
        if (cardinality == GoEvidenceCodeEnum.CARDINALITY_ANY) {
            return true;
        } else if (dto.getInferredFrom() != null) {
            return cardinality == dto.getInferredFrom().size();
        }
//        if(inferredFrom==null)
        else {
            // already handled ANY case above
            return cardinality == 0;
        }
    }

    public static boolean isInferenceValid(String inference, GoEvidenceDTO dto) {
        InferenceCategory[] inferenceCategories = dto.getEvidenceCode().getInferenceCategories(dto.getPublicationZdbID());

        boolean validInference = false;
        for (InferenceCategory inferenceCategory : inferenceCategories) {
            if (inference.matches(inferenceCategory.match())) {
                return true;
            }
        }
        return validInference;
    }

    public static boolean isValidInferences(GoEvidenceDTO dto) {

        for (String inference : dto.getInferredFrom()) {
            if (false == isInferenceValid(inference, dto)) return false;
        }

        return true;
    }
}
