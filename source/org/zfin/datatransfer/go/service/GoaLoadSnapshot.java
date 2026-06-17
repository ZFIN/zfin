package org.zfin.datatransfer.go.service;

import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Point-in-time snapshot of {@code marker_go_term_evidence} rows attributed to
 * a given {@link GafOrganization}. Captured once before the GAF/GOA load and
 * once after, the pair drives the "Load impact" tables in the report.
 *
 * <p>Filtering is done through the {@code gafOrganization} association (FK to
 * {@code marker_go_term_evidence_annotation_organization}), matching how
 * {@code GafService.findOutdatedEntries} identifies the load's existing rows.
 */
public class GoaLoadSnapshot {

    private final Map<String, Long> countsByEvidenceCode;
    private final long distinctGoTerms;
    private final long distinctMarkers;
    private final Map<String, Long> countsBySource;

    private GoaLoadSnapshot(Map<String, Long> countsByEvidenceCode,
                            long distinctGoTerms,
                            long distinctMarkers,
                            Map<String, Long> countsBySource) {
        this.countsByEvidenceCode = countsByEvidenceCode;
        this.distinctGoTerms = distinctGoTerms;
        this.distinctMarkers = distinctMarkers;
        this.countsBySource = countsBySource;
    }

    public Map<String, Long> getCountsByEvidenceCode() { return countsByEvidenceCode; }
    public long getDistinctGoTerms()                   { return distinctGoTerms; }
    public long getDistinctMarkers()                   { return distinctMarkers; }
    public Map<String, Long> getCountsBySource()       { return countsBySource; }

    public long getTotalAnnotations() {
        return countsByEvidenceCode.values().stream().mapToLong(Long::longValue).sum();
    }

    public static GoaLoadSnapshot capture(GafOrganization gafOrganization) {
        return new GoaLoadSnapshot(
            countByEvidenceCode(gafOrganization),
            distinctGoTerms(gafOrganization),
            distinctMarkers(gafOrganization),
            countBySource(gafOrganization));
    }

    private static Map<String, Long> countByEvidenceCode(GafOrganization org) {
        String hql = "select ev.evidenceCode.code, count(ev) " +
                     "  from MarkerGoTermEvidence ev " +
                     " where ev.gafOrganization = :org " +
                     " group by ev.evidenceCode.code";
        List<Object[]> rows = HibernateUtil.currentSession()
            .createQuery(hql, Object[].class)
            .setParameter("org", org)
            .list();
        return toSortedMap(rows);
    }

    private static long distinctGoTerms(GafOrganization org) {
        String hql = "select count(distinct ev.goTerm) " +
                     "  from MarkerGoTermEvidence ev " +
                     " where ev.gafOrganization = :org";
        return scalar(hql, org);
    }

    private static long distinctMarkers(GafOrganization org) {
        String hql = "select count(distinct ev.marker) " +
                     "  from MarkerGoTermEvidence ev " +
                     " where ev.gafOrganization = :org";
        return scalar(hql, org);
    }

    private static Map<String, Long> countBySource(GafOrganization org) {
        String hql = "select ev.source.zdbID, count(ev) " +
                     "  from MarkerGoTermEvidence ev " +
                     " where ev.gafOrganization = :org " +
                     " group by ev.source.zdbID";
        List<Object[]> rows = HibernateUtil.currentSession()
            .createQuery(hql, Object[].class)
            .setParameter("org", org)
            .list();
        return toSortedMap(rows);
    }

    private static long scalar(String hql, GafOrganization org) {
        Long count = HibernateUtil.currentSession()
            .createQuery(hql, Long.class)
            .setParameter("org", org)
            .uniqueResult();
        return count == null ? 0L : count;
    }

    /** Sort by count descending so the most-used codes/sources land at the top. */
    private static Map<String, Long> toSortedMap(List<Object[]> rows) {
        Map<String, Long> out = new LinkedHashMap<>();
        rows.stream()
            .sorted((a, b) -> Long.compare((Long) b[1], (Long) a[1]))
            .forEach(r -> out.put((String) r[0], (Long) r[1]));
        return out;
    }
}
