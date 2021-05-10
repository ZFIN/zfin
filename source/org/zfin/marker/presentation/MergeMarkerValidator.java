package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Marker;

/**
 * This class validates merges for different types of markers.
 */
public class MergeMarkerValidator implements Validator {

    @Override
    public boolean supports(Class clazz) {
        return MergeBean.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MergeBean mergeBean = (MergeBean) target;
        Marker markerToDelete = mergeBean.getMarkerToDelete();
        Marker markerToMergeInto = mergeBean.getMarkerToMergeInto();


        if (markerToDelete == null) {
            errors.reject("", "Marker to delete undefined.");
            return ;
        }
        if (markerToMergeInto == null) {
            errors.reject("", "Marker to merge into undefined.");
            return;
        }

        if (markerToDelete.isInTypeGroup(Marker.TypeGroup.ATB)
                &&
                markerToMergeInto.isInTypeGroup(Marker.TypeGroup.ATB)) {
            validateAntibodyMerge((Antibody) markerToDelete, (Antibody) markerToMergeInto, errors);
        } else {
            errors.reject("", "Merge of these markers types not supported.");
        }

    }

    public boolean isEqualOrUnspecified(String a, String b) {
        return (StringUtils.isEmpty(a) || StringUtils.isEmpty(b) || StringUtils.equals(a, b));
    }

    /**
     * Here, we just have to make sure that the antibodies are the same.
     *
     * @param antibodyToDelete    Antibody that gets deleted.
     * @param antibodyToMergeInto Antibody that data is added onto from the deleted antibody.
     * @param errors              Validation errors.
     */
    private void validateAntibodyMerge(Antibody antibodyToDelete, Antibody antibodyToMergeInto, Errors errors) {
        if (!isEqualOrUnspecified(antibodyToDelete.getClonalType(), antibodyToMergeInto.getClonalType())) {
            errors.reject("", "Clonal type must be the same to merge.");
        }
        if (!isEqualOrUnspecified(antibodyToDelete.getHeavyChainIsotype(), antibodyToMergeInto.getHeavyChainIsotype())) {
            errors.reject("", "Heavy Chain Isotypes must be the same to merge.");
        }
        if (!isEqualOrUnspecified(antibodyToDelete.getLightChainIsotype(), antibodyToMergeInto.getLightChainIsotype())) {
            errors.reject("", "Light Chain Isotypes must be the same to merge.");
        }
        if (!isEqualOrUnspecified(antibodyToDelete.getHostSpecies(), antibodyToMergeInto.getHostSpecies())) {
            errors.reject("", "Host Species must be the same to merge.");
        }
        if (!isEqualOrUnspecified(antibodyToDelete.getImmunogenSpecies(), antibodyToMergeInto.getImmunogenSpecies())) {
            errors.reject("", "Immunogen Species must be the same to merge.");
        }
    }

}
