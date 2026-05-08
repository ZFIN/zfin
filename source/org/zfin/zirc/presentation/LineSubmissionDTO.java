package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.zirc.entity.LineSubmission;

/**
 * Wire format for the editable scalar fields of a {@link LineSubmission}.
 * Relationships (mutations, persons) are intentionally omitted — they have
 * dedicated UIs on the detail page.
 */
@Getter
@Setter
public class LineSubmissionDTO {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String previousNames;
    private Boolean featuresLinked;
    private String maternalBackground;
    private String paternalBackground;
    private Boolean backgroundChangeable;
    private String backgroundChangeConcerns;
    private String unreportedFeaturesDetails;
    private String additionalInfo;

    public static LineSubmissionDTO from(LineSubmission submission) {
        LineSubmissionDTO dto = new LineSubmissionDTO();
        dto.setZdbID(submission.getZdbID());
        dto.setName(submission.getName());
        dto.setAbbreviation(submission.getAbbreviation());
        dto.setPreviousNames(submission.getPreviousNames());
        dto.setFeaturesLinked(submission.getFeaturesLinked());
        dto.setMaternalBackground(submission.getMaternalBackground());
        dto.setPaternalBackground(submission.getPaternalBackground());
        dto.setBackgroundChangeable(submission.getBackgroundChangeable());
        dto.setBackgroundChangeConcerns(submission.getBackgroundChangeConcerns());
        dto.setUnreportedFeaturesDetails(submission.getUnreportedFeaturesDetails());
        dto.setAdditionalInfo(submission.getAdditionalInfo());
        return dto;
    }
}
