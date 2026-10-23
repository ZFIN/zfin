package org.zfin.datatransfer.ncbi;

import org.hibernate.Session;
import org.zfin.framework.HibernateUtil;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * The stored copy of NCBI's list of zebrafish genes that are not in its current annotation release,
 * in {@code external_resource.ncbi_not_in_current_release}.
 *
 * <p>The list comes from a live NCBI query that quietly returns an empty list when it fails, and an
 * empty list would make every gene look as if it were in the current release. So a fetched list only
 * replaces the stored one when it is plausible: not empty, and not less than half the size of what is
 * stored. Otherwise the stored list stays and is what the load should use.
 *
 * <p>Works inside the caller's transaction; it does not start or commit one.
 */
public class NcbiNotInCurrentReleaseList {

    public static final String TABLE = "external_resource.ncbi_not_in_current_release";

    public enum Result {
        REPLACED("Stored the fetched list"),
        KEPT_EMPTY_FETCH("Kept the stored list: the fetch returned no gene IDs"),
        KEPT_SHRUNK("Kept the stored list: the fetch was less than half its size");

        private final String description;

        Result(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * @param effectiveIds the list the load should use: the fetched one if it was stored, otherwise the stored one
     */
    public record Outcome(Result result, int previousCount, int fetchedCount, List<String> effectiveIds) {
        public String message() {
            return result.getDescription() + " (stored before: " + previousCount
                    + ", fetched: " + fetchedCount + ", in use: " + effectiveIds.size() + ").";
        }

        public boolean replaced() {
            return result == Result.REPLACED;
        }
    }

    static Result decide(int previousCount, int fetchedCount) {
        if (fetchedCount == 0) {
            return Result.KEPT_EMPTY_FETCH;
        }
        if (previousCount > 0 && fetchedCount * 2 < previousCount) {
            return Result.KEPT_SHRUNK;
        }
        return Result.REPLACED;
    }

    static List<String> normalize(Collection<String> ids) {
        TreeSet<String> cleaned = new TreeSet<>();
        if (ids != null) {
            for (String id : ids) {
                if (id != null && !id.isBlank()) {
                    cleaned.add(id.trim());
                }
            }
        }
        return List.copyOf(cleaned);
    }

    public List<String> storedIds() {
        return HibernateUtil.currentSession()
                .createNativeQuery("select ncbi_gene_id from " + TABLE + " order by ncbi_gene_id", String.class)
                .getResultList();
    }

    public Outcome refresh(Collection<String> fetchedIds) {
        List<String> fetched = normalize(fetchedIds);
        List<String> stored = storedIds();
        Result result = decide(stored.size(), fetched.size());
        if (result != Result.REPLACED) {
            return new Outcome(result, stored.size(), fetched.size(), stored);
        }
        replaceAll(fetched);
        return new Outcome(result, stored.size(), fetched.size(), fetched);
    }

    private void replaceAll(List<String> ids) {
        Session session = HibernateUtil.currentSession();
        session.doWork(connection -> {
            try (PreparedStatement delete = connection.prepareStatement("delete from " + TABLE)) {
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "insert into " + TABLE + " (ncbi_gene_id) values (?)")) {
                for (String id : ids) {
                    insert.setString(1, id);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        });
    }
}
