package org.zfin.figure.service;


import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageService {

    public static Logger log = Logger.getLogger(ImageService.class);

    public static Image processImage(Figure figure, String filePath, Boolean isVideoStill,String direction) throws IOException {

        File imageLoadUpDir = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString());

        //create an initial image record, file paths and width/height will be wrong

        Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", "ZDB-PERS-030520-2"))  //Yvonne
                .uniqueResult();
        Image image = createPlaceholderImage(figure, owner, isVideoStill,direction);

        //get the image file
        File initialFile = new File(filePath);
        String extension = FilenameUtils.getExtension(initialFile.getName());
        String destinationBasename = image.getZdbID();
        String destinationFilename = destinationBasename + FilenameUtils.EXTENSION_SEPARATOR + extension;
        String thumbnailFilename = destinationBasename + "_thumb" + FilenameUtils.EXTENSION_SEPARATOR + extension;
        File destinationFile = new File(imageLoadUpDir, destinationFilename);
        File thumbnailFile = new File(imageLoadUpDir, thumbnailFilename);

        FileUtils.copyFile(initialFile, destinationFile);

        image.setImageFilename(destinationFilename);
        image.setThumbnail(thumbnailFilename);

        BufferedImage bimg = ImageIO.read(destinationFile);

        image.setWidth(bimg.getWidth());
        image.setHeight(bimg.getHeight());
        HibernateUtil.currentSession().save(image);

        File scriptDirectory = new File(ZfinPropertiesEnum.TARGETROOT + "/server_apps/sysexecs/make_thumbnail");
        CommandLine makeThumbnail = new CommandLine("./make_thumbnail.sh");
        makeThumbnail.addArgument(destinationFile.getAbsolutePath());
        makeThumbnail.addArgument(thumbnailFile.getAbsolutePath());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(scriptDirectory);
        executor.execute(makeThumbnail);

        return image;
    }

    /*

     */
    private static Image createPlaceholderImage(Figure figure, Person owner, Boolean isVideoStill,String direction) {

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
        //HibernateUtil.currentSession().save(figure);

        return image;
    }

}
