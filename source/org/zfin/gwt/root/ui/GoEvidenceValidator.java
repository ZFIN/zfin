package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.*;

import java.util.Arrays;
import java.util.Set;

/**
 * Error strings should be of the form:
 * Annotations with <EC> evidence must have [<exactly 1, 0>] item(s) of type(s) <types> ]
 * in the inferred from field [with pub XXX in the default pub field.]
 */
public class GoEvidenceValidator {

    public final static String PROTEIN_BINDING_OBO_ID = "GO:0005515";

    private static String generateErrorString(GoEvidenceDTO dto) {
        return generateErrorString(dto.getEvidenceCode(), dto.getPublicationZdbID());
    }

    public static String generateErrorString(GoEvidenceCodeEnum goEvidenceCodeEnum, String pubZdbID) {
        String pubName = null;

        GoDefaultPublication pubEnum = GoDefaultPublication.getPubForZdbID(pubZdbID);
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

        return generateErrorString(goEvidenceCodeEnum, pubZdbID, pubName);
    }

    private static String generateErrorString(GoEvidenceDTO dto, String pubName) {
        return generateErrorString(dto.getEvidenceCode(), dto.getPublicationZdbID(), pubName);
    }

    public static String generateErrorString(GoEvidenceCodeEnum evidenceCode, String pubZdbID, String pubName) {
        int cardinality = evidenceCode.getInferenceCategoryCardinality();
        InferenceCategory[] inferenceCategories = evidenceCode.getInferenceCategories(pubZdbID);
        String errorString = " Annotations with ";
        errorString += evidenceCode.name() + " evidence must ";
        switch (cardinality) {
            case 0:
                errorString += " have 0 items ";
                break;
            case 1:
                errorString += " have exactly 1 item ";
                break;
            case GoEvidenceCodeEnum.CARDINALITY_ONE_OR_MORE:
                errorString += " have 1 or more annotation ";
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

    public static void validate(GoEvidenceDTO dto) throws ValidationException {
        if (dto.getGoTerm() == null || dto.getGoTerm().getZdbID().isEmpty()) {
            throw new ValidationException("GO Term must be valid.");
        }
        if (dto.getGoTerm().isObsolete()) {
            throw new ValidationException("GO Term must not be obsolete.");
        }

        if (dto.getPublicationZdbID() == null) {
            throw new ValidationException("Publication must be selected.");
        }

        if (dto.getEvidenceCode() == null) {
            throw new ValidationException("Evidence code must be selected.");
        }

        validateInferenceCardinality(dto);
        validateInferences(dto);
        validateEvidenceVsPub(dto.getEvidenceCode(), dto.getPublicationZdbID(), dto.getFirstInference());
        validateProteinBinding(dto.getEvidenceCode(), dto.getInferredFrom(),
                dto.getGoTerm().getOboID(), dto.getGoTerm().getOntology().getOntologyName(),
                dto.getFlag());
    }


    public static void validateProteinBinding(GoEvidenceCodeEnum evidenceCode,
                                              Set<String> inferences,
                                              String termOboId,
                                              String ontologyName,
                                              GoEvidenceQualifier flag) throws ValidationException {
        // fogbugz 6292
        if (termOboId.equals(PROTEIN_BINDING_OBO_ID) && evidenceCode != GoEvidenceCodeEnum.IPI
                ) {
            throw new ValidationException("Protein binding (" + PROTEIN_BINDING_OBO_ID +
                    ") may only be used with the evidence code IPI.");
        } else if (termOboId.equals(PROTEIN_BINDING_OBO_ID) && evidenceCode == GoEvidenceCodeEnum.IPI) {
            if (flag == GoEvidenceQualifier.NOT) {
                throw new ValidationException("Not flag not allowed for this go term");
            }

            if (inferences == null || inferences.size() == 0) {
                throw new ValidationException("An inference is required for this go term");
            }
        }
        // fogbugz 6292
        else if (evidenceCode == GoEvidenceCodeEnum.IEP) {
            if (false == ontologyName.equals("biological_process")) {
                throw new ValidationException("Only biological process terms allowed for this evidence code, not ["
                        + ontologyName + "]");
            }
        }
    }

    public static void validateEvidenceVsPub(GoEvidenceCodeEnum evidenceCode, String publicationZdbID, String firstInference) throws ValidationException {

        if (evidenceCode.equals(GoEvidenceCodeEnum.ND) &&
                false == publicationZdbID.equals(GoDefaultPublication.ROOT.zdbID())) {
            throw new ValidationException("Annotations with evidence code ND must have default pub " + GoDefaultPublication.ROOT.title() + ".");
        } else if ((false == evidenceCode.equals(GoEvidenceCodeEnum.ND) &&
                publicationZdbID.equals(GoDefaultPublication.ROOT.zdbID()))
                ) {
            throw new ValidationException("Annotations with default pub " + GoDefaultPublication.ROOT.title() + " must have evidence code ND.");
        } else if (evidenceCode.equals(GoEvidenceCodeEnum.IEA)) {
            // if a go-ref pub, then
            String errorString = GoDefaultPublication.validateInference(publicationZdbID, InferenceCategory.getInferenceCategoryByValue(firstInference));
            if (errorString != null) {
                throw new ValidationException(errorString);
            }
        }
        // not an IEA EC, but having an IEA pub
        else if (GoDefaultPublication.isIeaPublication(publicationZdbID)) {
            throw new ValidationException("Evidence code should be IEA for this default pub: " + GoDefaultPublication.getPubForZdbID(publicationZdbID).title());
        }
    }

    private static void validateInferences(GoEvidenceDTO dto) throws ValidationException {
        if (!isValidInferences(dto)) {
            throw new ValidationException(generateErrorString(dto));
        }
    }

    private static void validateInferenceCardinality(GoEvidenceDTO dto) throws ValidationException {
        if (!isValidCardinality(dto.getEvidenceCode(), dto.getInferredFrom())) {
            throw new ValidationException(generateErrorString(dto));
        }
    }


    public static boolean isValidCardinality(GoEvidenceCodeEnum goEvidenceCodeEnum, Set<String> inferences) {
        int cardinality = goEvidenceCodeEnum.getInferenceCategoryCardinality();
        if (cardinality == GoEvidenceCodeEnum.CARDINALITY_ANY) {
            return true;
        } else if (inferences != null) {
            if (cardinality == GoEvidenceCodeEnum.CARDINALITY_ONE_OR_MORE) {
                return inferences.size() >= 1;
            } else {
                return cardinality == inferences.size();
            }
        }
//        if(inferredFrom==null)
        else {
            // already handled ANY case above
            return cardinality == 0;
        }
    }

    public static boolean isInferenceValid(String inference, GoEvidenceCodeEnum goEvidenceCodeEnum, String pubZdbId) {
        InferenceCategory[] inferenceCategories = goEvidenceCodeEnum.getInferenceCategories(pubZdbId);

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
            if (false == isInferenceValid(inference, dto.getEvidenceCode(), dto.getPublicationZdbID())) return false;
        }

        return true;
    }
}
