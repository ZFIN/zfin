package org.zfin.figure.service;


import org.apache.commons.lang3.StringUtils;
import org.zfin.expression.Image;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

public class ImageThumbnailAndMediumSwapTask extends AbstractScriptWrapper {
    private boolean dryRun = true;

    public static void main(String[] args) throws IOException {
        ImageThumbnailAndMediumSwapTask task = new ImageThumbnailAndMediumSwapTask();
        task.runTask();
        System.exit(0);
    }

    public void runTask() throws IOException {
        initAll();
        initDryRun();

        if (dryRun) {
            System.out.println("Dry run.  No changes will be made. Set RUN_IMAGE_FIXES environment variable, or runImageFixes property to run.");
        } else {
            System.out.println("Executing changes with imagemagick.");
        }

        FigureRepository figureRepository = RepositoryFactory.getFigureRepository();
        List<Image> images = figureRepository.getAllImagesWithFigures();

        LOG.info("Found " + images.size() + " images with figures.");
        runFixesForThumbnailsAndMediums(images);

    }

    private void initDryRun() {
        String RUN_IMAGE_FIXES = System.getenv("RUN_IMAGE_FIXES");
        if (StringUtils.isNotEmpty(RUN_IMAGE_FIXES)) {
            dryRun = false;
        }
        if (StringUtils.isNotEmpty(System.getProperty("runImageFixes"))) {
            dryRun = false;
        }
    }

    private void runFixesForThumbnailsAndMediums(List<Image> images) throws IOException {
        for (Image image : images) {
            String thumbnail = image.getThumbnail();
            String medium = image.getMedium();
            String fullSizeImagePath = ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + image.getImageFilename();
            File thumbnailFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + thumbnail);
            File mediumFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + medium);

            if (!thumbnailFile.exists()) {
//                System.out.println("Thumbnail file does not exist: " + thumbnailFile);
                continue;
            }
            if (!mediumFile.exists()) {
//                System.out.println("Medium file does not exist: " + mediumFile);
                continue;
            }

            try {
                checkAnomalies(thumbnailFile, mediumFile);
            } catch (Exception e) {
                System.out.println(" error: " + thumbnailFile + " " + mediumFile);
                System.out.println(" error: " + e.getMessage());
            }

        }
    }

    private void checkAnomalies(File thumbnailFile, File mediumFile) {
        String fileStem = thumbnailFile.getAbsolutePath().substring(0, thumbnailFile.getAbsolutePath().lastIndexOf('.'));
        if (fileStem.endsWith("_thumb")) {
            throw new RuntimeException("Thumbnail file should not end with _thumb");
        }
        fileStem = mediumFile.getAbsolutePath().substring(0, mediumFile.getAbsolutePath().lastIndexOf('.'));
        if (fileStem.endsWith("_medium")) {
            throw new RuntimeException("Medium file should not end with _medium");
        }

        String extension = thumbnailFile.getAbsolutePath().substring(thumbnailFile.getAbsolutePath().lastIndexOf('.'));

        if (thumbnailFile.length() > mediumFile.length()) {
            System.out.println("Anomaly detected: " + mediumFile + " has fewer pixels than " + thumbnailFile);
            printFilePixelDimensions(mediumFile);
            printFilePixelDimensions(thumbnailFile);
            swapFiles(thumbnailFile, mediumFile);
        }

    }

    private void swapFiles(File filename, File mediumFile) {
        if (!dryRun) {
            String extension = filename.getAbsolutePath().substring(filename.getAbsolutePath().lastIndexOf('.'));
            try {
                String randomString = System.currentTimeMillis() + "-" + generateRandomString(6);
                File tmpFile = new File(filename + "." + randomString + ".tmp");
                System.out.println("Swapping " + filename + " and " + mediumFile + " via " + tmpFile);
                Files.move(filename.toPath(), tmpFile.toPath());
                Files.move(mediumFile.toPath(), filename.toPath());
                Files.move(tmpFile.toPath(), mediumFile.toPath());
            } catch (IOException e) {
                System.out.println("Error copying file: " + e.getMessage());
            }
        }
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private void printFilePixelDimensions(File imageFile) {
        try {
            BufferedImage bimg = ImageIO.read(imageFile);
            int width = bimg.getWidth();
            int height = bimg.getHeight();
            System.out.println(imageFile + ": " + width + "x" + height);
        } catch (IOException e) {
            System.out.println(imageFile + ": error reading dimensions");
        }
    }

}

