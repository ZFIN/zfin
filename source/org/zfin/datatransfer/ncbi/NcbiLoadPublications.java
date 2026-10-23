package org.zfin.datatransfer.ncbi;

import jakarta.persistence.Tuple;
import org.zfin.framework.HibernateUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The publications the NCBI Gene load attributes its records to, as recorded in {@code load_publication}.
 *
 * <p>The SQL scripts of the load join to the same table. This is the Java side: the set of publications the load
 * creates records with, and the ones it treats as its own.
 */
public class NcbiLoadPublications {

    public static final String LOAD_NAME = "NCBI Gene";

    public record Row(String publicationZdbId, Integer precedence, boolean loadManaged) {
    }

    private final List<Row> rows;

    public NcbiLoadPublications(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    public static NcbiLoadPublications fromDatabase() {
        List<Tuple> tuples = HibernateUtil.currentSession().createNativeQuery("""
                        select lp.lp_pub_zdb_id, lp.lp_precedence, lp.lp_load_managed
                        from load_publication lp
                        join load l on l.ld_id = lp.lp_ld_id
                        where l.ld_name = :loadName
                        order by lp.lp_precedence nulls last, lp.lp_pub_zdb_id
                        """, Tuple.class)
                .setParameter("loadName", LOAD_NAME)
                .getResultList();
        List<Row> rows = new ArrayList<>();
        for (Tuple t : tuples) {
            Number precedence = t.get(1, Number.class);
            rows.add(new Row(t.get(0, String.class),
                    precedence == null ? null : precedence.intValue(),
                    Boolean.TRUE.equals(t.get(2, Boolean.class))));
        }
        return new NcbiLoadPublications(rows);
    }

    /** Publications the load creates records with (those with a precedence), strongest method first. */
    public List<String> createdByLoad() {
        return rows.stream()
                .filter(r -> r.precedence() != null)
                .map(Row::publicationZdbId)
                .toList();
    }

    /** Publications whose records the load deletes and replaces on each run. */
    public Set<String> loadManaged() {
        return rows.stream()
                .filter(Row::loadManaged)
                .map(Row::publicationZdbId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Stops the load early if a publication that its matching strategies attribute to is not recorded as one
     * the load creates records with, instead of letting those records go unrecognized.
     */
    public void requireCreatedByLoad(String... publicationZdbIds) {
        List<String> created = createdByLoad();
        List<String> missing = new ArrayList<>();
        for (String id : publicationZdbIds) {
            if (!created.contains(id)) {
                missing.add(id);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("load_publication has no entry with a precedence for the \"" + LOAD_NAME
                    + "\" load for: " + String.join(", ", missing));
        }
    }
}
