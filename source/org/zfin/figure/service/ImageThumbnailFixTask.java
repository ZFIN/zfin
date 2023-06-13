package org.zfin.figure.service;


import org.apache.commons.lang3.StringUtils;
import org.zfin.expression.Image;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.*;
import java.util.*;

public class ImageThumbnailFixTask extends AbstractScriptWrapper {
    private boolean dryRun = true;
    private int imageCount = 0;
    private int imageMissingOriginalCount = 0;
    private int imageMissingThumbnailCount = 0;
    private int imageMissingMediumCount = 0;


    public static void main(String[] args) throws IOException {
        ImageThumbnailFixTask task = new ImageThumbnailFixTask();
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
        imageCount = images.size();

        runFixesForImagesMissingThumbnails(images);
        printResults();

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

    private void runFixesForImagesMissingThumbnails(List<Image> images) throws IOException {
        for (Image image : images) {
            String thumbnail = image.getThumbnail();
            String fullSizeImagePath = ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + image.getImageFilename();

            boolean fullImageExists = new File(fullSizeImagePath).exists();
            if (!fullImageExists) {
                System.out.println("Full size image does not exist: " + fullSizeImagePath);
                imageMissingOriginalCount++;
                continue;
            }

            //thumbnail
            String destinationPath = ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + thumbnail;
            boolean fileExists = new File(destinationPath).exists();
            if (!fileExists) {
                String cmd = ImageService.convertImageToThumbnail(fullSizeImagePath, destinationPath, dryRun);
                imageMissingThumbnailCount++;
                if (dryRun) {
                    System.out.println("Preview command: " + cmd);
                } else {
                    System.out.println("Ran command: " + cmd);
                }
            }

            //medium
            String medium = image.getMedium();
            destinationPath = ZfinPropertiesEnum.LOADUP_FULL_PATH + File.separator + medium;
            fileExists = new File(destinationPath).exists();
            if (!fileExists) {
                String cmd = ImageService.convertImageToMedium(fullSizeImagePath, destinationPath, dryRun);
                imageMissingMediumCount++;
                if (dryRun) {
                    System.out.println("Preview command: " + cmd);
                } else {
                    System.out.println("Ran command: " + cmd);
                }
            }
        }
    }

    private void printResults() {
        System.out.println("Image count: " + imageCount);
        System.out.println("Image missing original count: " + imageMissingOriginalCount);
        System.out.println("Image missing thumbnail count: " + imageMissingThumbnailCount);
        System.out.println("Image missing medium count: " + imageMissingMediumCount);
    }

}

