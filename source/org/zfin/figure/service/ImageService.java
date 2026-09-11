package org.zfin.figure.service;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageService {

    public static Logger log = LogManager.getLogger(ImageService.class);

    private final static File IMAGE_LOADUP_DIR = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString());
    private final static String THUMB = "_thumb";
    private final static String THUMB_DIMENSIONS = "1000x64";

    private final static String MEDIUM = "_medium";
    private final static String MEDIUM_DIMENSIONS = "500x550";

    // Formats a browser will actually render. Anything else -- TIFF above all -- is
    // re-encoded as JPEG on the way in: Chrome and Firefox draw nothing for a .tif,
    // however valid the file is, so an unconverted upload is an invisible figure.
    private final static Set<String> WEB_SAFE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private final static String CONVERTED_EXTENSION = "jpg";
    // ImageIO's ~0.75 default is visibly lossy on confocal fluorescence detail.
    private final static float JPEG_QUALITY = 0.9f;
    // An uploaded filename is curator-supplied, so its extension is only pasted into a
    // path when it looks like a plain word.
    private final static Pattern SAFE_EXTENSION = Pattern.compile("\\w{1,10}");


    public static Image processImage(Figure figure, MultipartFile file, Person owner, String publicationZdbId) throws IOException {
        return processImage(figure, owner, false, file.getOriginalFilename(), file.getInputStream(), publicationZdbId);
    }

    public static Image processImage(Figure figure, String filePath, Boolean isVideoStill, String direction, String publicationZdbId) throws IOException {
        // This method was made for the original Dorsky load, so it has a hard-coded owner
        Person owner = (Person) HibernateUtil.currentSession().createQuery("from Person where zdbID = :zdbID", Person.class)
            .setParameter("zdbID", "ZDB-PERS-030520-2")  //Yvonne
            .uniqueResult();
        return processImage(figure, owner, isVideoStill, filePath, new FileInputStream(filePath), publicationZdbId);
    }

    private static Image createPlaceholderImage(Figure figure, Person owner, Boolean isVideoStill) {
        Image image = new Image();
        image.setFigure(figure);
        image.setWidth(-1);
        image.setHeight(-1);
        image.setLabel("");
        image.setView(Image.NOT_SPECIFIED);
        image.setDirection(Image.NOT_SPECIFIED);
        image.setForm(Image.NOT_SPECIFIED);
        image.setPreparation(Image.NOT_SPECIFIED);
        image.setOwner(owner);
        image.setImageFilename("tmpvalue");
        image.setThumbnail("tmpvalue");
        image.setMedium("tmpvalue");
        image.setVideoStill(isVideoStill);
        image.setInsertedDate(new GregorianCalendar());
        image.setInsertedBy(owner);
        image.setUpdatedDate(new GregorianCalendar());
        image.setUpdatedBy(owner);
        HibernateUtil.currentSession().save(image);
        figure.addImage(image);
        return image;
    }

    public static File getDestinationParentDirectory(String publicationZdbId, boolean absolutePath) {
        String pubYear = "";
        String pattern = "^(ZDB-PUB-)(\\d{2})(\\d{2})(\\d{2})(-\\d+)$";
        Pattern pubYearPattern = Pattern.compile(pattern);
        Matcher pubYearMatch = pubYearPattern.matcher(publicationZdbId);

        if (pubYearMatch.find()) {
            pubYear = pubYearMatch.group(2);
            if (pubYear.toString().startsWith("9")) {
                pubYear = "19" + pubYear;
            } else {
                pubYear = "20" + pubYear;
            }
        }

        String destinationFolderPath = pubYear + "/" + publicationZdbId;

        File containingFolder;
        if (absolutePath) {
            containingFolder = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), destinationFolderPath);
        } else {
            containingFolder = new File(destinationFolderPath);
        }
        return containingFolder;
    }

    private static Image processImage(Figure figure, Person owner, Boolean isVideoStill, String fileName, InputStream imageStream, String publicationZdbId) throws IOException {
        Image image = createPlaceholderImage(figure, owner, isVideoStill);

        String sourceExtension = FilenameUtils.getExtension(fileName);
        String extension = outputExtension(sourceExtension);
        boolean convertToJpeg = !extension.equals(sourceExtension);

        createDestinationParentDirectoryIfNotExists(publicationZdbId);
        File destinationDirectory = getDestinationParentDirectory(publicationZdbId, false);
        String destinationBasename = destinationDirectory + "/" + image.getZdbID();
        String destinationFilename = destinationBasename + FilenameUtils.EXTENSION_SEPARATOR + extension;
        String thumbnailFilename = destinationBasename + THUMB + FilenameUtils.EXTENSION_SEPARATOR + extension;
        String mediumFilename = destinationBasename + MEDIUM + FilenameUtils.EXTENSION_SEPARATOR + extension;
        File destinationFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), destinationFilename);
        File thumbnailFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), thumbnailFilename);
        File mediumFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), mediumFilename);

        // we used to attempt to set the image's width and height properties here using ImageIO.read(), but it
        // choked on images with a CMYK color space (common for published images), so we omit that now.

        image.setImageFilename(destinationFilename);
        image.setThumbnail(thumbnailFilename);
        image.setMedium(mediumFilename);
        HibernateUtil.currentSession().save(image);

        RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(figure.getPublication(), "img_zdb_id",
            "create new record", image.getZdbID(), null);

        // Re-encode rather than copy when the source format is not web-safe, so the
        // stored bytes always match the .jpg extension recorded above. The upload itself
        // is kept alongside the JPEG: re-encoding is lossy, and for a TIFF the original is
        // the only full-resolution copy that will ever exist.
        if (convertToJpeg) {
            copyAsJpeg(imageStream, destinationFile, archivalDestination(destinationBasename, sourceExtension));
        } else {
            Files.copy(imageStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            convertImageToThumbnail(destinationFile.getAbsolutePath(), thumbnailFile.getAbsolutePath(), false);
            convertImageToMedium(destinationFile.getAbsolutePath(), mediumFile.getAbsolutePath(), false);
        } catch (IOException e) {
            log.error("Error converting image to thumbnail or medium", e);
            e.printStackTrace();
        }

        return image;
    }

    /**
     * Whether a stored filename names a format a browser refuses to render, and so needs
     * re-encoding. Drives both the upload path and {@link ImageFormatConversionTask}'s
     * selection of legacy rows, so the two can never disagree about what counts as broken.
     */
    public static boolean needsJpegConversion(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        return !WEB_SAFE_EXTENSIONS.contains(extension == null ? "" : extension.toLowerCase());
    }

    /**
     * The same name with a lowercase .jpg extension -- what a converted row's three
     * filename columns become.
     */
    public static String jpegFilename(String filename) {
        return FilenameUtils.removeExtension(filename) + FilenameUtils.EXTENSION_SEPARATOR + CONVERTED_EXTENSION;
    }

    /**
     * Re-encode an existing file as JPEG at full size. The source is left untouched: for
     * the legacy TIFFs this is the only archival copy, and re-encoding is lossy.
     */
    public static void convertImageToJpeg(File sourceFile, File destinationFile) throws IOException {
        BufferedImage source = readImage(sourceFile);
        if (source == null) {
            throw new IOException("Unable to read image for JPEG conversion: " + sourceFile.getAbsolutePath());
        }
        writeJpeg(toRgb(source), destinationFile);
    }

    public static String convertImageToMedium(String imageFilename, String mediumFilename, boolean previewCommandOnly) throws IOException {
        return convertImageToDimensions(imageFilename, mediumFilename, MEDIUM_DIMENSIONS, previewCommandOnly);
    }

    public static String convertImageToThumbnail(String imageFilename, String thumbnailFilename, boolean previewCommandOnly) throws IOException {
        return convertImageToDimensions(imageFilename, thumbnailFilename, THUMB_DIMENSIONS, previewCommandOnly);
    }

    public static String convertImageToDimensions(String imageFilename, String thumbnailFilename, String dimensions, boolean previewCommandOnly) throws IOException {
        String[] parts = dimensions.split("x");
        int maxWidth = Integer.parseInt(parts[0]);
        int maxHeight = Integer.parseInt(parts[1]);

        String description = "resize " + imageFilename + " -> " + thumbnailFilename + " (" + dimensions + ")";
        log.info(description);

        if (!previewCommandOnly) {
            resizeImage(new File(imageFilename), new File(thumbnailFilename), maxWidth, maxHeight);
        }
        return description;
    }

    private static void createDestinationParentDirectoryIfNotExists(String publicationZdbId) throws IOException {
        File destinationDirectory = getDestinationParentDirectory(publicationZdbId, true);
        FileUtils.forceMkdir(destinationDirectory);
    }

    /**
     * Resize an image to fit within maxWidth x maxHeight, preserving aspect ratio.
     * Handles CMYK images by converting to RGB before resizing.
     */
    static void resizeImage(File inputFile, File outputFile, int maxWidth, int maxHeight) throws IOException {
        BufferedImage original = readImage(inputFile);
        if (original == null) {
            throw new IOException("Unable to read image: " + inputFile.getAbsolutePath());
        }

        // Convert CMYK to RGB if necessary
        if (original.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            log.info("Converting CMYK image to RGB: " + inputFile.getName());
            ColorConvertOp op = new ColorConvertOp(
                    ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
            original = op.filter(original, null);
        }

        double scale = Math.min(
                (double) maxWidth / original.getWidth(),
                (double) maxHeight / original.getHeight());

        String extension = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();

        // Don't upscale. Copying the source verbatim is only correct when the
        // destination is the same format; otherwise it would store the source's bytes
        // under an extension that lies about them.
        if (scale >= 1.0) {
            if (extension.equals(FilenameUtils.getExtension(inputFile.getName()).toLowerCase())) {
                Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                writeImage(toRgb(original), outputFile, extension);
            }
            return;
        }

        int w = (int) (original.getWidth() * scale);
        int h = (int) (original.getHeight() * scale);

        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        // White ground: a transparent source would otherwise composite onto black,
        // since the RGB canvas has no alpha to carry it.
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, w, h, null);
        g.dispose();

        writeImage(resized, outputFile, extension);
    }

    /**
     * The extension the three stored files share. Web-safe uploads keep their own
     * extension verbatim, case included, so existing .PNG rows keep behaving exactly as
     * they always have; everything else becomes a lowercase .jpg.
     */
    private static String outputExtension(String sourceExtension) {
        String normalized = sourceExtension == null ? "" : sourceExtension.toLowerCase();
        return WEB_SAFE_EXTENSIONS.contains(normalized) ? sourceExtension : CONVERTED_EXTENSION;
    }

    /**
     * Write the image in whatever format the destination's extension asks for. The
     * tif/tiff case is kept for the existing rows whose derivative filenames still end
     * in .tif and get regenerated by {@link ImageThumbnailFixTask}.
     */
    private static void writeImage(BufferedImage image, File outputFile, String extension) throws IOException {
        String formatName = switch (extension) {
            case "png" -> "png";
            case "gif" -> "gif";
            case "tif", "tiff" -> "tiff";
            default -> "jpg";
        };

        if ("jpg".equals(formatName)) {
            writeJpeg(image, outputFile);
            return;
        }
        if (!ImageIO.write(image, formatName, outputFile)) {
            // Fallback to JPEG if the format isn't supported
            log.warn("Could not write format '" + formatName + "', falling back to JPEG: " + outputFile.getName());
            writeJpeg(image, outputFile);
        }
    }

    /**
     * Where the untouched upload is kept when we re-encode it: the same folder and
     * basename as the derivatives, distinguished only by the source extension. That is
     * already how the legacy TIFFs sit on disk next to their derivatives, so the repair
     * task and the upload path end up with identical layouts. Null when the extension
     * isn't safe to paste into a path, in which case nothing is archived.
     */
    private static File archivalDestination(String destinationBasename, String sourceExtension) {
        if (sourceExtension == null || !SAFE_EXTENSION.matcher(sourceExtension).matches()) {
            return null;
        }
        return new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(),
            destinationBasename + FilenameUtils.EXTENSION_SEPARATOR + sourceExtension);
    }

    /**
     * Decode the incoming stream and store it as JPEG, keeping the original bytes at
     * archivalFile. The stream is spooled to a temp file first because {@link #readImage}
     * needs a File to retry alternate ImageIO readers with, which a one-shot InputStream
     * cannot support -- and having it on disk is what makes archiving the original
     * possible at all. The archival copy is written before the conversion so that an
     * undecodable upload still leaves its bytes behind rather than being discarded.
     */
    private static void copyAsJpeg(InputStream imageStream, File destinationFile, File archivalFile) throws IOException {
        File sourceCopy = Files.createTempFile("zfin-image-", ".tmp").toFile();
        try {
            Files.copy(imageStream, sourceCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            archiveOriginal(sourceCopy, archivalFile);
            BufferedImage source = readImage(sourceCopy);
            if (source == null) {
                throw new IOException("Unable to read image for JPEG conversion: " + destinationFile.getName());
            }
            writeJpeg(toRgb(source), destinationFile);
        } finally {
            FileUtils.deleteQuietly(sourceCopy);
        }
    }

    /**
     * Keep the pre-conversion bytes. A failure here is logged rather than thrown: the
     * JPEG the pages actually render is what the upload owes the curator, and losing the
     * archival copy should not fail the upload or roll back the image record.
     */
    private static void archiveOriginal(File sourceCopy, File archivalFile) {
        if (archivalFile == null) {
            return;
        }
        try {
            FileUtils.copyFile(sourceCopy, archivalFile);
            log.info("Archived pre-conversion original: " + archivalFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Could not archive pre-conversion original: " + archivalFile.getAbsolutePath(), e);
        }
    }

    private static void writeJpeg(BufferedImage image, File outputFile) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName(CONVERTED_EXTENSION).next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Flatten onto a white RGB canvas. JPEG carries neither an alpha channel nor CMYK,
     * and without the white fill a transparent source composites onto black.
     */
    private static BufferedImage toRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgb;
    }

    /**
     * Read an image file, trying all available ImageIO readers.
     * This handles cases where the default reader fails (e.g. certain CMYK JPEGs).
     */
    private static BufferedImage readImage(File file) throws IOException {
        // Try the simple path first
        BufferedImage img = ImageIO.read(file);
        if (img != null) {
            return img;
        }

        // If that failed, try each reader explicitly (some readers handle CMYK)
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    return reader.read(0);
                } catch (Exception e) {
                    log.debug("Reader " + reader.getClass().getName() + " failed for " + file.getName(), e);
                } finally {
                    reader.dispose();
                }
            }
        }

        log.error("No ImageIO reader could read: " + file.getAbsolutePath());
        return null;
    }
}
