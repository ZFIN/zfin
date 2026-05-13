package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.Mutation;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Wire format for a {@link LineSubmission}. Mixes editable scalar fields
 * (name, backgrounds, reasons, …) with server-managed metadata (zdbID,
 * isDraft, createdAt / updatedAt / submittedAt / deletedAt). The
 * metadata is read-only on the client — saves only update the scalar
 * fields via {@code /save-field}, {@code /save-reasons},
 * {@code /save-linked-features}, etc. Relationships include the nested
 * mutations list (summary view for the parent edit page) and linked
 * features; persons live on the parent detail page UI.
 */
@Getter
@Setter
public class LineSubmissionDTO {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String previousNames;
    private String maternalBackground;
    private String paternalBackground;
    private Boolean backgroundChangeable;
    private String backgroundChangeConcerns;
    private String unreportedFeaturesDetails;
    private String additionalInfo;
    private Boolean singleAllelic;
    private String husbandryInfo;
    private String[] reasons;
    private String reasonsOther;
    private List<LinkedFeatureDTO> linkedFeatures;
    private List<MutationDTO> mutations;
    private Boolean isDraft;
    private Date deletedAt;
    private Date submittedAt;
    private Date createdAt;
    private Date updatedAt;

    public static LineSubmissionDTO from(LineSubmission submission) {
        LineSubmissionDTO dto = new LineSubmissionDTO();
        dto.setZdbID(submission.getZdbID());
        dto.setName(submission.getName());
        dto.setAbbreviation(submission.getAbbreviation());
        dto.setPreviousNames(submission.getPreviousNames());
        dto.setMaternalBackground(submission.getMaternalBackground());
        dto.setPaternalBackground(submission.getPaternalBackground());
        dto.setBackgroundChangeable(submission.getBackgroundChangeable());
        dto.setBackgroundChangeConcerns(submission.getBackgroundChangeConcerns());
        dto.setUnreportedFeaturesDetails(submission.getUnreportedFeaturesDetails());
        dto.setAdditionalInfo(submission.getAdditionalInfo());
        dto.setSingleAllelic(submission.getSingleAllelic());
        dto.setHusbandryInfo(submission.getHusbandryInfo());
        dto.setReasons(submission.getReasons());
        dto.setReasonsOther(submission.getReasonsOther());
        // Order by (mutationA.id, mutationB.id) — already the on-disk order
        // since the CHECK constraint enforces it.
        dto.setLinkedFeatures(
            submission.getLinkedFeatures().stream()
                .sorted(Comparator
                    .comparing((LinkedFeature lf) -> lf.getMutationA().getId())
                    .thenComparing(lf -> lf.getMutationB().getId()))
                .map(LinkedFeatureDTO::from)
                .toList());
        dto.setMutations(
            submission.getMutations().stream()
                .sorted(Comparator.comparing(Mutation::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(MutationDTO::from)
                .toList());
        dto.setIsDraft(submission.getIsDraft());
        dto.setDeletedAt(submission.getDeletedAt());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setCreatedAt(submission.getCreatedAt());
        dto.setUpdatedAt(submission.getUpdatedAt());
        return dto;
    }
}
