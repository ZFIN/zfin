package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ZfinStringUtils;

public class StrDetailsValidator implements Validator {

    private static Logger log = Logger.getLogger(StrDetailsValidator.class);

    private static final MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return StrDetailsBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StrDetailsBean bean = (StrDetailsBean) o;
        Marker.Type type = Marker.Type.getType(bean.getType());

        if (type == null) {
            errors.reject("Invalid type specified");
            return;
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "str.name.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sequence1", "str.sequence.empty");
        if (type == Marker.Type.TALEN) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sequence2", "str.sequence.empty");
        }

        if (!errors.hasFieldErrors("name")) {
            Marker existing = markerRepository.getMarkerByName(bean.getName());
            if (existing != null && !existing.getZdbID().equals(bean.getZdbID())) {
                errors.rejectValue("name", "str.name.inuse");
            }
        }

        if (!errors.hasFieldErrors("sequence1") &&
                !ZfinStringUtils.isValidNucleotideSequence(bean.getSequence1(), type)) {
            errors.rejectValue("sequence1", "str.sequence.characters");
        }

        if (type == Marker.Type.TALEN &&
                !errors.hasFieldErrors("sequence2") &&
                !ZfinStringUtils.isValidNucleotideSequence(bean.getSequence2(), type)) {
            errors.rejectValue("sequence2", "str.sequence.characters");
        }

        if (!errors.hasFieldErrors("sequence1")) {
            SequenceTargetingReagent existing;
            if (type == Marker.Type.TALEN && !errors.hasFieldErrors("sequence2")) {
                existing = markerRepository.getSequenceTargetingReagentBySequence(type, bean.getSequence1(), bean.getSequence2());
            } else {
                existing = markerRepository.getSequenceTargetingReagentBySequence(type, bean.getSequence1());
            }
            if (existing != null && !existing.getZdbID().equals(bean.getZdbID())) {
                log.warn(existing.zdbID + " " + bean.getZdbID());
                Object[] args = new Object[]{existing.getName()};
                String defaultMessage = "Sequence is already used";
                errors.rejectValue("sequence1", "str.sequence.inuse", args, defaultMessage);
                errors.rejectValue("sequence2", "str.sequence.inuse", args, defaultMessage);
            }
        }
    }
}
