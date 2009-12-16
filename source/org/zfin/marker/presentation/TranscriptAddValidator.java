package org.zfin.marker.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.TranscriptType;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 */
public class TranscriptAddValidator implements Validator {

    public boolean supports(Class aClass) {
        return TranscriptAddBean.class.equals(aClass);
    }

    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "name", "not using lookup", "Name may not be empty.");
        ValidationUtils.rejectIfEmpty(errors, "chosenType", "not using lookup", "Must choose a type.");

        TranscriptAddBean transcriptAddBean = (TranscriptAddBean) o;
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(transcriptAddBean.getName());
        if (marker != null) {
            Object[] args = new Object[1];
            args[0] = transcriptAddBean.getName();
            errors.rejectValue("name", "not using lookup", args, "Marker with name [{0}] already exists.");
        }

        TranscriptType.Type transcriptType = TranscriptType.Type.getTranscriptType(transcriptAddBean.getChosenType());
        TranscriptStatus.Status transcriptStatus = TranscriptStatus.Status.getStatus(transcriptAddBean.getChosenStatus());
        List<TranscriptStatus.Status> transcriptStatuses = transcriptType.getStatusList(transcriptType);
        if (false == transcriptStatuses.contains(transcriptStatus)) {
            errors.rejectValue("chosenStatus", "not using lookup", "Transcript status not allowed for type.  " +
                    "Allowed statuses: " +
                    transcriptStatuses.toString());
        }


    }
}
