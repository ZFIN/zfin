package org.zfin.zirc.dto;

import org.zfin.profile.Person;
import org.zfin.zirc.entity.AuditEntry;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Wire shape for one audit-log row. Mirrors the shape FieldHistory expects:
 * timestamp + actor + old + new values, plus the JSON Pointer that was
 * patched (for cases where the same leaf name appears at multiple paths).
 *
 * Author is denormalized into {@code authorName} so the popup doesn't need
 * a second person fetch.
 */
public record AuditEntryDTO(
        Long id,
        String whenUpdated,
        String actor,
        String actorName,
        String action,
        // 'submission' | 'mutation' | 'gene' | 'lesion' | 'assay' | 'phenotype'.
        // Used client-side by ChangeHistoryPanel to bucket child-entity rows
        // under the form's "Mutations" section.
        String entityKind,
        String path,
        String oldValue,
        String newValue) {

    public static AuditEntryDTO of(AuditEntry a, Person actorPerson) {
        String name;
        if (actorPerson == null) {
            name = a.getActor();
        } else {
            String initial = (actorPerson.getFirstName() == null || actorPerson.getFirstName().isBlank())
                    ? "" : actorPerson.getFirstName().charAt(0) + ". ";
            name = initial + (actorPerson.getLastName() == null ? a.getActor() : actorPerson.getLastName());
        }
        // ISO-8601 with explicit offset so the React panel can format it
        // relative to the user's local day (Today / Yesterday / older).
        String whenIso = null;
        if (a.getAt() != null) {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            iso.setTimeZone(TimeZone.getDefault());
            whenIso = iso.format(a.getAt());
        }
        return new AuditEntryDTO(
                a.getId(),
                whenIso,
                a.getActor(),
                name,
                a.getAction(),
                a.getEntityKind(),
                a.getPath(),
                a.getOldValue(),
                a.getNewValue());
    }
}
