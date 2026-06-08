package org.zfin.zirc.entity;

/**
 * Roles a {@link org.zfin.profile.Person} can hold on a {@link LineSubmission}.
 *
 * <p>Stored as the lowercased enum name in {@code line_submission_person.lsp_role},
 * gated by a CHECK constraint (see ZFIN-10325 migration). The DB also enforces
 * a {@code UNIQUE (submission, person)} so a person holds at most one role
 * per submission — keep that invariant in mind when adding new values here.
 *
 * <p>Admin users are intentionally not in this enum. The current ZIRC
 * curator policy ships a hard-coded admin list in the React detail page;
 * if admins ever need to be tagged per-submission, add a value here AND
 * extend the {@code lsp_role_chk} constraint in a follow-up migration.
 */
public enum LineSubmissionRole {
    SUBMITTER,
    PI;

    /** Lowercased wire form used in JSON and the {@code lsp_role} column. */
    public String wire() {
        return name().toLowerCase();
    }

    /** Parse a wire-form role string back to the enum; null/unknown → null. */
    public static LineSubmissionRole fromWire(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "submitter" -> SUBMITTER;
            case "pi"        -> PI;
            default          -> null;
        };
    }
}
