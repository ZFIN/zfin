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
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageService {

    public static Logger log = LogManager.getLogger(ImageService.class);

    private final static File IMAGE_LOADUP_DIR = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString());
    private final static String THUMB = "_thumb";
    private final static String THUMB_DIMENSIONS = "1000x64";

    private final static String MEDIUM = "_medium";
    private final static String MEDIUM_DIMENSIONS = "500x550";


    public static Image processImage(Figure figure, MultipartFile file, Person owner, String publicationZdbId) throws IOException {
        return processImage(figure, owner, false, file.getOriginalFilename(), file.getInputStream(), publicationZdbId);
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

        String extension = FilenameUtils.getExtension(fileName);

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

        Files.copy(imageStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        try {
            convertImageToThumbnail(destinationFile.getAbsolutePath(), thumbnailFile.getAbsolutePath(), false);
            convertImageToMedium(destinationFile.getAbsolutePath(), mediumFile.getAbsolutePath(), false);
        } catch (IOException e) {
            log.error("Error converting image to thumbnail or medium", e);
            e.printStackTrace();
        }

        return image;
    }

    public static String convertImageToMedium(String imageFilename, String mediumFilename, boolean previewCommandOnly) throws IOException {
        return convertImageToDimensions(imageFilename, mediumFilename, MEDIUM_DIMENSIONS, previewCommandOnly);
    }

    public static String convertImageToThumbnail(String imageFilename, String thumbnailFilename, boolean previewCommandOnly) throws IOException {
        return convertImageToDimensions(imageFilename, thumbnailFilename, THUMB_DIMENSIONS, previewCommandOnly);
    }

    public static String convertImageToDimensions(String imageFilename, String thumbnailFilename, String dimensions, boolean previewCommandOnly) throws IOException {
        String convertBinary = ZfinPropertiesEnum.CONVERT_BINARY_PATH.value();
        if (convertBinary == null) {
            throw new RuntimeException("Environment variable CONVERT_BINARY_PATH must be set");
        }
        if (!FileUtil.checkFileExists(convertBinary)) {
            log.error("Cannot find imagemagick's \"convert\" binary at: " + convertBinary);
            File convertBinaryFile = findConvertBinaryInPath();
            if (convertBinaryFile == null) {
                throw new RuntimeException("Cannot find imagemagick's \"convert\" binary at: " + convertBinary + " or in PATH");
            } else {
                log.error("Found convert binary at: " + convertBinaryFile.getAbsolutePath());
                convertBinary = convertBinaryFile.getAbsolutePath();
            }
        }
        String[] makeThumbCommand = {convertBinary, "-thumbnail", dimensions, imageFilename, thumbnailFilename};
        log.info("running makeThumb command: " + makeThumbCommand);
        if (!previewCommandOnly) {
            Runtime.getRuntime().exec(makeThumbCommand);
        }
        return String.join(" ", makeThumbCommand);
    }

    private static void createDestinationParentDirectoryIfNotExists(String publicationZdbId) throws IOException {
        File destinationDirectory = getDestinationParentDirectory(publicationZdbId, true);
        FileUtils.forceMkdir(destinationDirectory);
    }

    private static File findConvertBinaryInPath() {
        try {
            Process process = Runtime.getRuntime().exec("which convert");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                File file = new File(line.trim());
                if (file.exists()) {
                    return file;
                }
            }
        } catch (IOException e) {
            log.error("Error finding convert binary in path", e);
        }
        return null;
    }
}
