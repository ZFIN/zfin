package org.zfin.figure.service;


import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageService {

    public static Logger log = Logger.getLogger(ImageService.class);

    private final static File IMAGE_LOADUP_DIR = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString());
    private final static String THUMB = "_thumb";

    public static Image processImage(Figure figure, MultipartFile file, Person owner) throws IOException {
        return processImage(figure, owner, false, file.getOriginalFilename(), file.getInputStream());
    }

    public static Image processImage(Figure figure, String filePath, Boolean isVideoStill, String direction) throws IOException {
        // This method was made for the original Dorsky load, so it has a hard-coded owner
        Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", "ZDB-PERS-030520-2"))  //Yvonne
                .uniqueResult();
        return processImage(figure, owner, isVideoStill, filePath, new FileInputStream(filePath));
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
        image.setVideoStill(isVideoStill);
        HibernateUtil.currentSession().save(image);
        figure.addImage(image);
        return image;
    }

    private static Image processImage(Figure figure, Person owner, Boolean isVideoStill, String fileName, InputStream imageStream) throws IOException {
        Image image = createPlaceholderImage(figure, owner, isVideoStill);

        String extension = FilenameUtils.getExtension(fileName);
        String destinationBasename = image.getZdbID();
        String destinationFilename = destinationBasename + FilenameUtils.EXTENSION_SEPARATOR + extension;
        String thumbnailFilename = destinationBasename + THUMB + FilenameUtils.EXTENSION_SEPARATOR + extension;
        File destinationFile = new File(IMAGE_LOADUP_DIR, destinationFilename);
        File thumbnailFile = new File(IMAGE_LOADUP_DIR, thumbnailFilename);

        // we used to attempt to set the image's width and height properties here using ImageIO.read(), but it
        // choked on images with a CMYK color space (common for published images), so we omit that now.

        image.setImageFilename(destinationFilename);
        image.setThumbnail(thumbnailFilename);
        HibernateUtil.currentSession().save(image);

        RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(figure.getPublication(), "img_zdb_id",
                "create new record", image.getZdbID(), null);

        Files.copy(imageStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File scriptDirectory = new File(ZfinPropertiesEnum.TARGETROOT + "/server_apps/sysexecs/make_thumbnail");
        CommandLine makeThumbnail = new CommandLine("./make_thumbnail.sh");
        makeThumbnail.addArgument(destinationFile.getAbsolutePath());
        makeThumbnail.addArgument(thumbnailFile.getAbsolutePath());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(scriptDirectory);
        executor.execute(makeThumbnail);

        return image;
    }
}
