package org.zfin.zirc.dto;

import org.zfin.profile.Person;
import org.zfin.zirc.entity.AuditEntry;

import java.text.SimpleDateFormat;

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
        return new AuditEntryDTO(
                a.getId(),
                a.getAt() == null
                        ? null
                        : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(a.getAt()),
                a.getActor(),
                name,
                a.getAction(),
                a.getPath(),
                a.getOldValue(),
                a.getNewValue());
    }
}
