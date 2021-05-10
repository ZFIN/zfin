package org.zfin.figure.service;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.expression.Image;
import org.zfin.expression.Video;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.IOException;

public class VideoService {

    public static Logger log = LogManager.getLogger(VideoService.class);

    /*
      A video is hung off of it's still image, so the still image needs to exist first
      to create the video object in the database.

      The video needs to be saved so that an id is generated, then the id is used for the filename
     */
    public static Video processVideo(String filePath, Image image) throws IOException {
        File videoLoadUp = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.value(), ZfinPropertiesEnum.VIDEO_LOAD.value());

        File initialFile = new File(filePath);
        String extension = FilenameUtils.getExtension(initialFile.getName());

        Video video = new Video();
        video.setStill(image);

        String destinationFilename = image.getZdbID() + FilenameUtils.EXTENSION_SEPARATOR + extension;
        video.setVideoFilename(destinationFilename);

        HibernateUtil.currentSession().save(video);

        File destinationFile = new File(videoLoadUp, destinationFilename);
        FileUtils.copyFile(initialFile, destinationFile);

        image.addVideo(video);

        return video;
    }

}
