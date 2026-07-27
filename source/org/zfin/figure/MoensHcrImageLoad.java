package org.zfin.figure;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Transaction;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureFigure;
import org.zfin.expression.Image;
import org.zfin.figure.service.ImageService;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * ZFIN-10330 — one-off load of Moens lab HCR in situ images.
 * <p>
 * For each row of a tab-separated manifest, creates one figure and one image
 * under publication {@link #PUBLICATION_ZDB_ID}, physically copies the image
 * file into the loadup area, and generates thumbnail + medium derivatives
 * (all handled by {@link ImageService#processImage}). One image = one figure.
 * <p>
 * Manifest columns (tab-separated, one header line):
 * include, disk_filename, view, direction, form, preparation, fig_label, caption, note
 * Only rows with include == "Y" are loaded.
 * <p>
 * Args:
 * [0] manifest TSV path
 * [1] directory containing the image files named in disk_filename
 * <p>
 * Re-runnable: a row is skipped if a figure with the same label already exists
 * under the publication.
 */
@Log4j2
public class MoensHcrImageLoad extends AbstractScriptWrapper {

    // ZFINHELP-5462 / ZFIN-10330
    static final String PUBLICATION_ZDB_ID = "ZDB-PUB-260717-17";
    // Person the load runs as: image owner + audit submitter. NOTE: ImageService's
    // file-path processImage() hardcodes ZDB-PERS-030520-2 as the owner, so we
    // override image.owner explicitly below to keep the whole load attributed here.
    static final String OWNER_ZDB_ID = "ZDB-PERS-060413-1";

    private final String manifestPath;
    private final File imageDirectory;
    private Person owner;

    // Map the spreadsheet's free-text values to the controlled vocabularies that
    // img_view / img_direction / img_form / img_preparation are FK-constrained to
    // (image_view, image_direction, image_form, image_preparation). Keys are
    // lowercase/trimmed; anything unmapped falls back to "not specified".
    private static final Map<String, String> VIEW_MAP = Map.of(
        "ventral", "ventral",
        "lateral", "side view (lateral)");
    private static final Map<String, String> DIRECTION_MAP = Map.of(
        "anterior to the top", "anterior to top",
        "anterior to the left", "anterior to left",
        "not specified", "not specified");
    private static final Map<String, String> FORM_MAP = Map.of(
        "confocal single section", "still",
        "confocal maximum intensity projection", "still",
        "not specified", "not specified");
    private static final Map<String, String> PREP_MAP = Map.of(
        "whole mount", "whole-mount",
        "not specified", "not specified");

    private int loaded = 0;
    private int skippedExisting = 0;
    private int errors = 0;

    public MoensHcrImageLoad(String manifestPath, String imageDirectory) {
        this.manifestPath = manifestPath;
        this.imageDirectory = new File(imageDirectory);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: MoensHcrImageLoad <manifest.tsv> <imageDirectory>");
            System.exit(1);
        }
        MoensHcrImageLoad load = new MoensHcrImageLoad(args[0], args[1]);
        load.initAll();
        load.run();
        System.exit(0);
    }

    private void run() {
        if (!imageDirectory.isDirectory()) {
            throw new RuntimeException("Image directory not found: " + imageDirectory.getAbsolutePath());
        }
        List<String[]> rows = readManifest();

        Publication publication = getPublicationRepository().getPublication(PUBLICATION_ZDB_ID);
        if (publication == null) {
            throw new RuntimeException("Publication not found: " + PUBLICATION_ZDB_ID);
        }
        owner = HibernateUtil.currentSession()
            .createQuery("from Person where zdbID = :zdbID", Person.class)
            .setParameter("zdbID", OWNER_ZDB_ID)
            .uniqueResult();
        if (owner == null) {
            throw new RuntimeException("Owner person not found: " + OWNER_ZDB_ID);
        }

        // Run as a real, persisted Person so the audit records written by
        // insertUpdatesTable reference a saved Person instead of the transient
        // "Guest" that getCurrentSecurityUser() fabricates in a headless run.
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(owner, null));
        SecurityContextHolder.setContext(securityContext);

        log.info("Loading {} manifest rows for {}", rows.size(), PUBLICATION_ZDB_ID);
        for (String[] row : rows) {
            loadRow(row, publication);
        }
        log.info("Done. loaded={} skippedExisting={} errors={}", loaded, skippedExisting, errors);
    }

    private void loadRow(String[] row, Publication publication) {
        String diskFilename = col(row, 1);
        String view = vocab(VIEW_MAP, col(row, 2), "view");
        String direction = vocab(DIRECTION_MAP, col(row, 3), "direction");
        String form = vocab(FORM_MAP, col(row, 4), "form");
        String preparation = vocab(PREP_MAP, col(row, 5), "preparation");
        String figLabel = col(row, 6);
        String caption = col(row, 7);

        File imageFile = new File(imageDirectory, diskFilename);
        if (!imageFile.isFile()) {
            log.error("SKIP (file not found): {}", imageFile.getAbsolutePath());
            errors++;
            return;
        }
        if (figureLabelExists(publication.getZdbID(), figLabel)) {
            log.info("SKIP (figure label already loaded): {}", figLabel);
            skippedExisting++;
            return;
        }

        Transaction tx = HibernateUtil.createTransaction();
        try {
            FigureFigure figure = new FigureFigure();
            figure.setLabel(figLabel);
            figure.setCaption(caption);
            figure.setPublication(publication);
            HibernateUtil.currentSession().save(figure);

            // Copies the file into the loadup area and builds thumbnail + medium.
            Image image = ImageService.processImage(figure, imageFile.getAbsolutePath(), false, direction, publication.getZdbID());
            image.setOwner(owner);   // override ImageService's hardcoded ZDB-PERS-030520-2
            image.setView(view);
            image.setDirection(direction);
            image.setForm(form);
            image.setPreparation(preparation);
            HibernateUtil.currentSession().save(image);

            getInfrastructureRepository().insertUpdatesTable(publication, "fig_zdb_id", "create new record", figure.getZdbID(), null);

            tx.commit();
            loaded++;
            log.info("Loaded figure {} / image {} from {}", figure.getZdbID(), image.getZdbID(), diskFilename);
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            errors++;
            log.error("FAILED row for {}: {}", diskFilename, e.getMessage(), e);
        }
    }

    private boolean figureLabelExists(String pubZdbId, String label) {
        Long count = (Long) HibernateUtil.currentSession()
            .createQuery("select count(*) from Figure f where f.publication.zdbID = :pub and f.label = :label")
            .setParameter("pub", pubZdbId)
            .setParameter("label", label)
            .uniqueResult();
        return count != null && count > 0;
    }

    private List<String[]> readManifest() {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(manifestPath))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] cols = line.split("\t", -1);
                if ("Y".equalsIgnoreCase(col(cols, 0))) {
                    rows.add(cols);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read manifest: " + manifestPath, e);
        }
        return rows;
    }

    private static String col(String[] row, int i) {
        return i < row.length && row[i] != null ? row[i].trim() : "";
    }

    private static String vocab(Map<String, String> map, String raw, String field) {
        String key = raw == null ? "" : raw.trim().toLowerCase();
        if (key.isEmpty()) {
            return Image.NOT_SPECIFIED;
        }
        String mapped = map.get(key);
        if (mapped == null) {
            log.warn("Unmapped {} value '{}' -> '{}'", field, raw, Image.NOT_SPECIFIED);
            return Image.NOT_SPECIFIED;
        }
        return mapped;
    }
}
