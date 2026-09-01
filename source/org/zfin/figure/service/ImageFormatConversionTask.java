package org.zfin.figure.service;

import org.apache.commons.lang3.StringUtils;
import org.zfin.expression.Image;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getFigureRepository;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Repairs image rows stored in a format the site cannot serve honestly. As of August 2026
 * that is 53 rows: 52 TIFF and one BMP.
 * <p>
 * The TIFFs are the visible problem -- Chrome and Firefox draw nothing for a .tif however
 * valid the file is, and because all three filename columns carry the same extension those
 * figures are equally blank in figure galleries, expression search results and phenotype
 * summaries. The BMP renders, so it is not blank, but {@link ImageService}'s writer has no
 * bmp case and falls through to JPEG, meaning its _thumb.bmp and _medium.bmp hold JPEG
 * bytes under an extension that lies about them. Same defect, quieter symptom, same fix.
 * <p>
 * Selection is by extension rather than by a fixed list of IDs, so this also serves as an
 * audit: a later run that finds nothing is evidence the ingress paths are behaving.
 * <p>
 * For each affected row this re-derives the full-size, medium and thumbnail files as JPEG,
 * repoints img_image / img_thumbnail / img_medium at them, and records the old and new
 * values in the updates table.
 * <p>
 * The original file is deliberately LEFT ON DISK. Re-encoding to JPEG is lossy and for
 * these rows the TIFF is the only full-resolution copy that exists, so it is kept beside
 * the new .jpg -- the same layout {@link ImageService} now produces for a TIFF upload.
 * Nothing points at it any more; it is there to be recovered from.
 * <p>
 * Dry run by default: it prints what it would do and touches neither disk nor database.
 * Set RUN_IMAGE_FIXES=true (or -DrunImageFixes) to actually make the changes, and
 * FIX_PUB_IDS to a comma separated list of publication IDs to do one publication at a time.
 * <p>
 * Solr indexes image and thumbnail filenames, so a reindex is needed afterwards before the
 * converted images appear in search results.
 */
public class ImageFormatConversionTask extends AbstractScriptWrapper {

    private boolean dryRun = true;
    private List<String> restrictedPublications;

    private int convertedCount = 0;
    private int missingSourceCount = 0;
    private int failedCount = 0;

    public static void main(String[] args) throws IOException {
        ImageFormatConversionTask task = new ImageFormatConversionTask();
        task.runTask();
        System.exit(0);
    }

    public void runTask() {
        initAll();
        initConfig();

        List<Image> images = getFigureRepository().getAllImagesWithFigures().stream()
            .filter(image -> StringUtils.isNotEmpty(image.getImageFilename()))
            // A video still's img_image names the movie, not a picture -- its thumbnail and
            // medium are already JPEG. Re-encoding is meaningless here and repointing
            // img_image at a .jpg would break the link to the video.
            .filter(image -> !Boolean.TRUE.equals(image.getVideoStill()))
            .filter(image -> ImageService.needsJpegConversion(image.getImageFilename()))
            .filter(this::isInRestrictedPublications)
            .toList();

        System.out.println("Found " + images.size() + " images in a format browsers cannot render.");

        HibernateUtil.createTransaction();
        for (Image image : images) {
            convertImage(image);
        }

        // A dry run returns before touching either the entity or the disk, so the rollback
        // is belt and braces -- it guarantees nothing a report-only run did can be flushed.
        if (dryRun) {
            HibernateUtil.rollbackTransaction();
        } else {
            HibernateUtil.flushAndCommitCurrentSession();
        }

        printResults();
    }

    private void initConfig() {
        boolean runFixes = System.getenv().getOrDefault("RUN_IMAGE_FIXES", "false").equalsIgnoreCase("true")
            || StringUtils.isNotEmpty(System.getProperty("runImageFixes"));
        dryRun = !runFixes;

        String restrictedPublicationsString = System.getenv("FIX_PUB_IDS");
        if (StringUtils.isEmpty(restrictedPublicationsString)) {
            restrictedPublications = Collections.emptyList();
        } else {
            restrictedPublications = List.of(restrictedPublicationsString.split(","));
        }

        if (dryRun) {
            System.out.println("Dry run. No changes will be made. Set RUN_IMAGE_FIXES=true environment variable, or runImageFixes property, to run.");
        } else {
            System.out.println("Converting images to JPEG. Originals will be left on disk.");
        }

        if (restrictedPublications.isEmpty()) {
            System.out.println("Converting images for all publications. To restrict, set FIX_PUB_IDS environment variable to a comma separated list of IDs.");
        } else {
            System.out.println("Only converting images for publications: " + restrictedPublications);
        }
    }

    private boolean isInRestrictedPublications(Image image) {
        if (restrictedPublications.isEmpty()) {
            return true;
        }
        if (image.getFigure() == null || image.getFigure().getPublication() == null) {
            return false;
        }
        return restrictedPublications.contains(image.getFigure().getPublication().getZdbID());
    }

    private void convertImage(Image image) {
        File sourceFile = loadUpFile(image.getImageFilename());
        if (!sourceFile.exists()) {
            System.out.println(image.getZdbID() + ": source file does not exist, skipping: " + sourceFile);
            missingSourceCount++;
            return;
        }

        // Derive each new name from the value already in the column rather than rebuilding
        // it from the image ID, so a row with an unusual thumbnail or medium name keeps it.
        String newImageFilename = ImageService.jpegFilename(image.getImageFilename());
        String newThumbnailFilename = ImageService.jpegFilename(image.getThumbnail());
        String newMediumFilename = ImageService.jpegFilename(image.getMedium());

        File newImageFile = loadUpFile(newImageFilename);
        File newThumbnailFile = loadUpFile(newThumbnailFilename);
        File newMediumFile = loadUpFile(newMediumFilename);

        System.out.println(image.getZdbID() + ": " + image.getImageFilename() + " -> " + newImageFilename
            + " (keeping " + sourceFile.getName() + ")");

        if (dryRun) {
            System.out.println("  would write " + newImageFile);
            System.out.println("  would write " + newThumbnailFile);
            System.out.println("  would write " + newMediumFile);
            convertedCount++;
            return;
        }

        try {
            ImageService.convertImageToJpeg(sourceFile, newImageFile);
            ImageService.convertImageToThumbnail(newImageFile.getAbsolutePath(), newThumbnailFile.getAbsolutePath(), false);
            ImageService.convertImageToMedium(newImageFile.getAbsolutePath(), newMediumFile.getAbsolutePath(), false);
        } catch (IOException e) {
            // Leave the row pointing at the unreadable original rather than at a .jpg that
            // was never written -- a blank figure beats a 404, and the row stays selectable
            // by a later run.
            System.out.println("  error converting " + sourceFile + ": " + e.getMessage());
            failedCount++;
            return;
        }

        recordUpdate(image, "img_image", image.getImageFilename(), newImageFilename);
        recordUpdate(image, "img_thumbnail", image.getThumbnail(), newThumbnailFilename);
        recordUpdate(image, "img_medium", image.getMedium(), newMediumFilename);

        image.setImageFilename(newImageFilename);
        image.setThumbnail(newThumbnailFilename);
        image.setMedium(newMediumFilename);
        HibernateUtil.currentSession().update(image);

        convertedCount++;
    }

    private void recordUpdate(Image image, String fieldName, String oldValue, String newValue) {
        // No person is logged in during a script run, so the audit row is attributed to
        // nobody rather than to whichever curator happens to own the image.
        getInfrastructureRepository().insertUpdatesTableWithoutPerson(image.getZdbID(), fieldName,
            oldValue, newValue, "ImageFormatConversionTask: re-encoded as JPEG so browsers can render it; original kept on disk");
    }

    private File loadUpFile(String relativePath) {
        return new File(ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + relativePath);
    }

    private void printResults() {
        System.out.println("--------------------------------------------");
        System.out.println("Images converted: " + convertedCount);
        System.out.println("Images with missing source file: " + missingSourceCount);
        System.out.println("Images that failed to convert: " + failedCount);
        if (dryRun) {
            System.out.println("Dry run -- nothing was changed.");
        } else {
            System.out.println("Remember to reindex Solr: image and thumbnail filenames are indexed, so search results keep the old paths until you do.");
        }
    }
}
