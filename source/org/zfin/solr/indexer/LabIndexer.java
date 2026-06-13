package org.zfin.solr.indexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.zfin.profile.Lab;
import org.zfin.repository.RepositoryFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * First entity migrated off the DIH pipeline. Mirrors the stored-field
 * shape the legacy {@code <entity name="lab">} block in
 * {@code db-data-config.xml} produced — verified by diffing
 * {@link org.zfin.solr.diagnostics.DumpTool} output ({@code gradle solrDiag
 * -PdiagArgs="dump ..."}) against the Solr 8 baseline (see
 * {@code baseline-solr8/docs.ndjson}) — so the resulting Solr docs are
 * drop-in compatible with the rest of the index.
 *
 * <p>Stored fields emitted, per the baseline dump:
 * <pre>
 *   id              = lab.zdb_id
 *   name            = lab.name
 *   full_name       = lab.name
 *   category        = "Community"        (literal)
 *   type            = "Lab"              (literal)
 *   url             = "/" + lab.zdb_id
 *   date            = creation date parsed from zdb_id (YYMMDD segment)
 *   profile_image   = lab.image          (only when non-null)
 * </pre>
 *
 * <p>The legacy DIH config's {@code SELECT} also pulled
 * {@code proper_name}, {@code name_sort}, {@code biography},
 * {@code postal_address}, {@code country}, {@code email_address},
 * {@code lab_member_name}, and {@code xref}, but the baseline dump shows
 * none of those landed in any of the 1,866 lab docs — DIH's default
 * column→field auto-mapping evidently didn't fire for them. We mirror
 * the baseline rather than the SELECT so the cutover is byte-for-byte
 * compat; adding those fields back is a separate, intentional change.
 *
 * <p><b>Date precision note:</b> we emit a full {@code YYYY-MM-DD} date
 * here. The Solr 8 baseline emitted {@code YYYY-01-01} because the DIH
 * config wrapped its postgres {@code get_date_from_id} call in a
 * second {@code to_date(..., '%Y-%m-%d')} whose C-strftime format string
 * postgres doesn't understand — only the leading 4-digit year survived
 * the round trip. The correct day is the right value for downstream
 * date queries / sorting; the year-only output was a bug.
 */
public class LabIndexer implements SolrIndexer {

    private static final Logger logger = LogManager.getLogger(LabIndexer.class);

    private static final int BATCH_SIZE = 200;

    @Override
    public String name() { return "lab"; }

    @Override
    public void index(SolrClient solr) throws Exception {
        var labs = RepositoryFactory.getProfileRepository().getLabs();
        logger.info("LabIndexer: pushing {} labs", labs.size());

        List<SolrInputDocument> batch = new ArrayList<>(BATCH_SIZE);
        int n = 0;
        for (Lab lab : labs) {
            batch.add(toDoc(lab));
            if (batch.size() >= BATCH_SIZE) {
                solr.add(batch);
                n += batch.size();
                batch.clear();
                if (n % 1000 == 0) logger.info("  pushed {} / {}", n, labs.size());
            }
        }
        if (!batch.isEmpty()) {
            solr.add(batch);
            n += batch.size();
        }
        logger.info("LabIndexer: pushed {} labs total (commit deferred to driver)", n);
    }

    private SolrInputDocument toDoc(Lab lab) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id",            lab.getZdbID());
        doc.addField("name",          lab.getName());
        doc.addField("full_name",     lab.getName());
        doc.addField("category",      "Community");
        doc.addField("type",          "Lab");
        doc.addField("url",           "/" + lab.getZdbID());
        doc.addField("date",          dateFromZdbId(lab.getZdbID()));
        if (lab.getImage() != null && !lab.getImage().isEmpty()) {
            doc.addField("profile_image", lab.getImage());
        }
        return doc;
    }

    // Parse in UTC: DIH's postgres-emitted DATE became midnight UTC in
    // Solr (e.g. 2000-11-21T00:00:00Z); parsing in local tz would shift
    // every value by the JVM's offset. We can't reuse
    // ActiveData.getDateFromId because it runs validateID first, and
    // LAB isn't in ActiveData.Type.
    private static final SimpleDateFormat DATE_FROM_ID;
    static {
        DATE_FROM_ID = new SimpleDateFormat("yyMMdd");
        DATE_FROM_ID.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private Date dateFromZdbId(String zdbId) {
        if (zdbId == null) return null;
        String[] parts = zdbId.split("-");
        if (parts.length < 3) return null;
        try {
            synchronized (DATE_FROM_ID) {
                return DATE_FROM_ID.parse(parts[2]);
            }
        } catch (ParseException e) {
            return null;
        }
    }
}
