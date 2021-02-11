package org.zfin.marker.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.TranscriptType;
import org.zfin.repository.RepositoryFactory;

import java.awt.*;
import java.util.List;

/**
 */
public class TranscriptUpdateValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return TranscriptAttributeBean.class.isAssignableFrom(aClass);
    }

    @Override

    public void validate(Object o, Errors errors) {

        TranscriptAttributeBean TranscriptAttributeBean = (TranscriptAttributeBean) o;

        TranscriptType.Type transcriptType = TranscriptType.Type.getTranscriptType(TranscriptAttributeBean.getTranscriptType());
        TranscriptStatus.Status transcriptStatus = TranscriptStatus.Status.getStatus(TranscriptAttributeBean.getTranscriptStatus());
        List<TranscriptStatus.Status> transcriptStatuses = transcriptType.getStatusList(transcriptType);
        if (false == transcriptStatuses.contains(transcriptStatus)) {
            errors.rejectValue("transcriptStatus","transcriptStatus.  " +
                    "Allowed statuses: " +
                    transcriptStatuses.toString());
        }



    }
}
