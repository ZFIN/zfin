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
public class TranscriptUpdateValidator implements Validator {

    public boolean supports(Class aClass) {
        return TranscriptAttributeBean.class.equals(aClass);
    }

    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmpty(errors, "transcriptType", "not using lookup", "Must choose a type.");

        TranscriptAttributeBean TranscriptAttributeBean = (TranscriptAttributeBean) o;

        TranscriptType.Type transcriptType = TranscriptType.Type.getTranscriptType(TranscriptAttributeBean.getTranscriptType());
        TranscriptStatus.Status transcriptStatus = TranscriptStatus.Status.getStatus(TranscriptAttributeBean.getTranscriptStatus());
        List<TranscriptStatus.Status> transcriptStatuses = transcriptType.getStatusList(transcriptType);
        if (false == transcriptStatuses.contains(transcriptStatus)) {
            errors.rejectValue("transcriptStatus", "not using lookup", "Transcript status not allowed for type.  " +
                    "Allowed statuses: " +
                    transcriptStatuses.toString());
        }


    }
}
