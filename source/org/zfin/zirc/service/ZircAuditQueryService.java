package org.zfin.zirc.service;

import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.zirc.api.ZircAssayFormSchema;
import org.zfin.zirc.api.ZircFormSchema;
import org.zfin.zirc.api.ZircGeneFormSchema;
import org.zfin.zirc.api.ZircLesionFormSchema;
import org.zfin.zirc.api.ZircMutationFormSchema;
import org.zfin.zirc.api.ZircPhenotypeFormSchema;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.entity.AuditEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        String hql = """
                from ZircAuditEntry
                where entityKind = :kind
                  and entityId   = :id
                  and (path = :pathExact or path like :pathSuffix)
                order by at asc
                """;
        return HibernateUtil.currentSession()
                .createQuery(hql, AuditEntry.class)
                .setParameter("kind", ref.kind)
                .setParameter("id",   ref.id)
                .setParameter("pathExact",  "/" + fieldName)
                .setParameter("pathSuffix", "%/" + fieldName)
                .list();
    }

    /**
     * Section-scope history. Returns the audit rows that visually live in
     * the named uiSchema section of the entity referenced by {@code recId}:
     *
     * <ul>
     *   <li>For a field-only section ({@code "General"}, {@code "Background"}, …)
     *       — rows on the parent entity whose path's deepest segment is one
     *       of the section's controls. Nested object paths (e.g.
     *       {@code /acceptance/reasons}) match too via the leaf segment.</li>
     *   <li>For a container section ({@code "Genes"}, {@code "Lesions"},
     *       {@code "Genotyping Assays"}, {@code "Phenotypes"},
     *       {@code "Mutations"}) — also the add/delete rows on the parent
     *       ({@code create-gene} / {@code delete-gene}, etc.) plus every
     *       audit row on the children of the right entity kind that belong
     *       to this parent.</li>
     * </ul>
     *
     * <p>Empty list when {@code recId} can't be parsed, the section name is
     * blank, or no uiSchema describes the parent kind.
     */
    public List<AuditEntry> listSection(String recId, String sectionName) {
        EntityRef ref = parseRecId(recId);
        if (ref == null || sectionName == null || sectionName.isBlank()) return List.of();

        UiSchemaElement uiSchema = uiSchemaForKind(ref.kind);
        if (uiSchema == null) return List.of();

        Set<String> fields = new HashSet<>(SchemaSections.groupsToFields(uiSchema)
                .getOrDefault(sectionName, List.of()));
        String childKind = containerChildKindFor(ref.kind, sectionName);
        String createAction = childKind == null ? null : "create-" + childKind;
        String deleteAction = childKind == null ? null : "delete-" + childKind;

        Session session = HibernateUtil.currentSession();
        List<AuditEntry> parentRows = session.createQuery("""
                        from ZircAuditEntry
                        where entityKind = :kind and entityId = :id
                        """, AuditEntry.class)
                .setParameter("kind", ref.kind)
                .setParameter("id",   ref.id)
                .list();

        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : parentRows) {
            String path = e.getPath();
            String leaf = leafOf(path);
            if (leaf != null && fields.contains(leaf)) {
                result.add(e);
                continue;
            }
            if (childKind != null
                    && (createAction.equals(e.getAction()) || deleteAction.equals(e.getAction()))) {
                result.add(e);
            }
        }

        if (childKind != null) {
            List<String> childIds = childIdsForParent(ref.kind, ref.id, childKind);
            if (!childIds.isEmpty()) {
                result.addAll(session.createQuery("""
                                from ZircAuditEntry
                                where entityKind = :ck and entityId in :ids
                                """, AuditEntry.class)
                        .setParameter("ck", childKind)
                        .setParameterList("ids", childIds)
                        .list());
            }
        }

        result.sort(Comparator.comparing(AuditEntry::getAt,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    private static String leafOf(String path) {
        if (path == null || path.isEmpty()) return null;
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private static UiSchemaElement uiSchemaForKind(String kind) {
        return switch (kind) {
            case "submission" -> ZircFormSchema.uiSchema();
            case "mutation"   -> ZircMutationFormSchema.uiSchema();
            case "gene"       -> ZircGeneFormSchema.uiSchema();
            case "lesion"     -> ZircLesionFormSchema.uiSchema();
            case "assay"      -> ZircAssayFormSchema.uiSchema();
            case "phenotype"  -> ZircPhenotypeFormSchema.uiSchema();
            default -> null;
        };
    }

    // (parentKind, sectionLabel) → childKind for the form's container
    // sections (those whose body is a list of child entities rather than
    // a table of fields). Used by listSection to pull child-entity audit
    // rows in alongside the parent's own create/delete rows.
    private static final Map<String, String> CONTAINER_CHILD_KIND_BY_PARENT_AND_SECTION = Map.of(
            "submission|Mutations",        "mutation",
            "mutation|Genes",              "gene",
            "mutation|Lesions",            "lesion",
            "mutation|Genotyping Assays",  "assay",
            "mutation|Phenotypes",         "phenotype");

    private static String containerChildKindFor(String parentKind, String sectionLabel) {
        return CONTAINER_CHILD_KIND_BY_PARENT_AND_SECTION.get(parentKind + "|" + sectionLabel);
    }

    private static List<String> childIdsForParent(String parentKind, String parentId, String childKind) {
        Session s = HibernateUtil.currentSession();
        if ("submission".equals(parentKind) && "mutation".equals(childKind)) {
            return s.createQuery(
                    "select cast(m.id as string) from ZircMutation m where m.lineSubmission.zdbID = :p",
                    String.class)
                    .setParameter("p", parentId)
                    .list();
        }
        if ("mutation".equals(parentKind)) {
            long parent = Long.parseLong(parentId);
            String hql = switch (childKind) {
                case "gene"      -> "select cast(c.id as string) from ZircGene c where c.mutation.id = :p";
                case "lesion"    -> "select cast(c.id as string) from ZircLesion c where c.mutation.id = :p";
                case "assay"     -> "select cast(c.id as string) from ZircGenotypingAssay c where c.mutation.id = :p";
                case "phenotype" -> "select cast(c.id as string) from ZircPhenotype c where c.mutation.id = :p";
                default          -> null;
            };
            if (hql == null) return List.of();
            return s.createQuery(hql, String.class).setParameter("p", parent).list();
        }
        return List.of();
    }

    /**
     * Whole-entity-tree history. Routes by entity kind:
     * <ul>
     *   <li>{@code submission} → every audit row in the submission and its
     *       descendants (same as {@link #listAllForSubmission}).</li>
     *   <li>{@code mutation} → every audit row on the mutation plus its
     *       gene / lesion / assay / phenotype children.</li>
     *   <li>Anything else (gene / lesion / assay / phenotype) → only
     *       that entity's own audit rows.</li>
     * </ul>
     * Empty list when {@code recId} can't be parsed.
     */
    public List<AuditEntry> listForEntityTree(String recId) {
        EntityRef ref = parseRecId(recId);
        if (ref == null) return List.of();
        return switch (ref.kind) {
            case "submission" -> listAllForSubmission(ref.id);
            case "mutation"   -> listForMutationTree(ref.id);
            default           -> HibernateUtil.currentSession()
                    .createQuery("""
                            from ZircAuditEntry
                            where entityKind = :k and entityId = :i
                            order by at desc, id desc
                            """, AuditEntry.class)
                    .setParameter("k", ref.kind)
                    .setParameter("i", ref.id)
                    .list();
        };
    }

    private List<AuditEntry> listForMutationTree(String mutationId) {
        long mid = Long.parseLong(mutationId);
        String hql = """
                from ZircAuditEntry a where
                    (a.entityKind = 'mutation' and a.entityId = :mid)
                 or (a.entityKind = 'gene'      and a.entityId in
                       (select cast(g.id as string) from ZircGene g
                         where g.mutation.id = :midLong))
                 or (a.entityKind = 'lesion'    and a.entityId in
                       (select cast(l.id as string) from ZircLesion l
                         where l.mutation.id = :midLong))
                 or (a.entityKind = 'assay'     and a.entityId in
                       (select cast(ga.id as string) from ZircGenotypingAssay ga
                         where ga.mutation.id = :midLong))
                 or (a.entityKind = 'phenotype' and a.entityId in
                       (select cast(p.id as string) from ZircPhenotype p
                         where p.mutation.id = :midLong))
                order by a.at desc, a.id desc
                """;
        return HibernateUtil.currentSession()
                .createQuery(hql, AuditEntry.class)
                .setParameter("mid", mutationId)
                .setParameter("midLong", mid)
                .list();
    }

    /**
     * Submission-wide history: every audit row for the submission itself
     * plus every audit row for any descendant entity (mutations and their
     * genes / lesions / assays / phenotypes). Ordered newest-first so the
     * right-hand history panel can render the freshest changes at the top.
     */
    public List<AuditEntry> listAllForSubmission(String submissionZdbID) {
        if (submissionZdbID == null || submissionZdbID.isBlank()) return List.of();
        // The child-entity IDs are numeric Longs but audit.entityId is text;
        // we cast on the subquery side so the IN-comparison matches without
        // a per-row coercion.
        String hql = """
                from ZircAuditEntry a where
                    (a.entityKind = 'submission' and a.entityId = :sub)
                 or (a.entityKind = 'mutation' and a.entityId in
                       (select cast(m.id as string) from ZircMutation m
                         where m.lineSubmission.zdbID = :sub))
                 or (a.entityKind = 'gene'      and a.entityId in
                       (select cast(g.id as string) from ZircGene g
                         where g.mutation.lineSubmission.zdbID = :sub))
                 or (a.entityKind = 'lesion'    and a.entityId in
                       (select cast(l.id as string) from ZircLesion l
                         where l.mutation.lineSubmission.zdbID = :sub))
                 or (a.entityKind = 'assay'     and a.entityId in
                       (select cast(ga.id as string) from ZircGenotypingAssay ga
                         where ga.mutation.lineSubmission.zdbID = :sub))
                 or (a.entityKind = 'phenotype' and a.entityId in
                       (select cast(p.id as string) from ZircPhenotype p
                         where p.mutation.lineSubmission.zdbID = :sub))
                order by a.at desc, a.id desc
                """;
        return HibernateUtil.currentSession()
                .createQuery(hql, AuditEntry.class)
                .setParameter("sub", submissionZdbID)
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
