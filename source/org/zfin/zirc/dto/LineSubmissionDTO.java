package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.profile.Person;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.Mutation;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record LineSubmissionDTO(
        @NotNull String zdbID,
        String name,
        String abbreviation,
        String previousNames,
        Boolean singleAllelic,
        String maternalBackground,
        String paternalBackground,
        Boolean backgroundChangeable,
        String backgroundChangeConcerns,
        String unreportedFeaturesDetails,
        String husbandryInfo,
        String additionalInfo,
        String[] reasons,
        String reasonsOther,
        List<MutationDTO> mutations,
        List<LinkedFeatureDTO> linkedFeatures,
        boolean draft,
        String createdAt,
        String updatedAt,
        String submitterNames) {

    public static LineSubmissionDTO of(LineSubmission s) {
        List<MutationDTO> muts = s.getMutations() == null ? List.of() :
                s.getMutations().stream()
                        .sorted(Comparator.comparing(
                                Mutation::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(MutationDTO::of)
                        .toList();
        List<LinkedFeatureDTO> links = s.getLinkedFeatures() == null ? List.of() :
                s.getLinkedFeatures().stream()
                        .sorted(Comparator
                                .comparing((org.zfin.zirc.entity.LinkedFeature lf)
                                        -> lf.getMutationA() == null ? null : lf.getMutationA().getId(),
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(lf -> lf.getMutationB() == null ? null : lf.getMutationB().getId(),
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(LinkedFeatureDTO::of)
                        .toList();
        return new LineSubmissionDTO(
                s.getZdbID(),
                s.getName(),
                s.getAbbreviation(),
                s.getPreviousNames(),
                s.getSingleAllelic(),
                s.getMaternalBackground(),
                s.getPaternalBackground(),
                s.getBackgroundChangeable(),
                s.getBackgroundChangeConcerns(),
                s.getUnreportedFeaturesDetails(),
                s.getHusbandryInfo(),
                s.getAdditionalInfo(),
                s.getReasons(),
                s.getReasonsOther(),
                muts,
                links,
                Boolean.TRUE.equals(s.getIsDraft()),
                formatDate(s.getCreatedAt()),
                formatDate(s.getUpdatedAt()),
                formatSubmitters(s));
    }

    private static String formatDate(Date d) {
        return d == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }

    /** "F. Last, G. Other" in submission sort order; null when none. */
    private static String formatSubmitters(LineSubmission s) {
        if (s.getPersons() == null || s.getPersons().isEmpty()) return null;
        String joined = s.getPersons().stream()
                .sorted(Comparator.comparing(LineSubmissionPerson::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LineSubmissionPerson::getPerson)
                .filter(Objects::nonNull)
                .map(LineSubmissionDTO::personLabel)
                .collect(Collectors.joining(", "));
        return joined.isBlank() ? null : joined;
    }

    private static String personLabel(Person p) {
        String initial = (p.getFirstName() == null || p.getFirstName().isBlank())
                ? "" : p.getFirstName().charAt(0) + ". ";
        return initial + (p.getLastName() == null ? p.getZdbID() : p.getLastName());
    }
}
