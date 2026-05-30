package org.zfin.zirc.service;

import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.zirc.entity.AuditEntry;

import java.util.List;

/**
 * Read-only lookup over {@code zirc.audit} for the FieldHistory popup.
 * Mirrors the legacy detail page's field-history queries but goes through
 * Hibernate and lives on the new schema-driven recId convention.
 *
 * <p>Writes go through {@code ZircSubmissionService.writeAudit(...)};
 * this service never inserts.
 */
@Service
public class ZircAuditQueryService {

    /**
     * Audit rows matching one (recId, fieldName) lookup. recId is parsed
     * into {@code (entityKind, entityId)} via {@link #parseRecId}; the
     * leaf-field name is matched against either an exact path or any
     * nested path ending in {@code /fieldName}, so the lookup works for
     * flat scopes (lesion's {@code /lesionSizeBp}) and for the submission's
     * nested object schemas (acceptance' {@code /acceptance/reasons}).
     */
    public List<AuditEntry> listField(String recId, String fieldName) {
        EntityRef ref = parseRecId(recId);
        if (ref == null) return List.of();
        return HibernateUtil.currentSession()
                .createQuery(
                    "from ZircAuditEntry "
                  + "where entityKind = :kind "
                  + "  and entityId   = :id "
                  + "  and (path = :pathExact or path like :pathSuffix) "
                  + "order by at asc",
                    AuditEntry.class)
                .setParameter("kind", ref.kind)
                .setParameter("id",   ref.id)
                .setParameter("pathExact",  "/" + fieldName)
                .setParameter("pathSuffix", "%/" + fieldName)
                .list();
    }

    /**
     * Section-scope history: every audit row for the entity (path-agnostic).
     * The legacy detail page bucketed these by section per-field on the
     * server; here we let the client filter if it cares about section
     * boundaries. Empty list when recId can't be parsed.
     */
    public List<AuditEntry> listSection(String recId, String sectionName) {
        EntityRef ref = parseRecId(recId);
        if (ref == null) return List.of();
        return HibernateUtil.currentSession()
                .createQuery(
                    "from ZircAuditEntry "
                  + "where entityKind = :kind and entityId = :id "
                  + "order by at asc",
                    AuditEntry.class)
                .setParameter("kind", ref.kind)
                .setParameter("id",   ref.id)
                .list();
    }

    private record EntityRef(String kind, String id) {}

    /**
     * Convention used end-to-end in the React detail page:
     * <ul>
     *   <li>{@code ZDB-ZLSUB-*}  → kind="submission",  id=zdbID</li>
     *   <li>{@code ZIRC-MUT-*}    → kind="mutation",   id=numeric</li>
     *   <li>{@code ZIRC-GENE-*}   → kind="gene",       id=numeric</li>
     *   <li>{@code ZIRC-LESION-*} → kind="lesion",     id=numeric</li>
     *   <li>{@code ZIRC-GA-*}     → kind="assay",      id=numeric</li>
     *   <li>{@code ZIRC-PHEN-*}   → kind="phenotype",  id=numeric</li>
     * </ul>
     * Unknown shapes return {@code null} and the calling endpoint emits []
     * rather than 400 — a stale icon click shouldn't error the page out.
     */
    private static EntityRef parseRecId(String recId) {
        if (recId == null || recId.isBlank()) return null;
        if (recId.startsWith("ZIRC-MUT-"))    return new EntityRef("mutation",  recId.substring("ZIRC-MUT-".length()));
        if (recId.startsWith("ZIRC-GENE-"))   return new EntityRef("gene",      recId.substring("ZIRC-GENE-".length()));
        if (recId.startsWith("ZIRC-LESION-")) return new EntityRef("lesion",    recId.substring("ZIRC-LESION-".length()));
        if (recId.startsWith("ZIRC-GA-"))     return new EntityRef("assay",     recId.substring("ZIRC-GA-".length()));
        if (recId.startsWith("ZIRC-PHEN-"))   return new EntityRef("phenotype", recId.substring("ZIRC-PHEN-".length()));
        // Anything else (e.g. ZDB-ZLSUB-...) is treated as a submission id.
        return new EntityRef("submission", recId);
    }
}
