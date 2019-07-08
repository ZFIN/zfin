package org.zfin.figure.service;


import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageService {

    public static Logger log = LogManager.getLogger(ImageService.class);

    private final static File IMAGE_LOADUP_DIR = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString());
    private final static String THUMB = "_thumb";
    private final static String MEDIUM = "_medium";

    public static Image processImage(Figure figure, MultipartFile file, Person owner, String publicationZdbId) throws IOException {
        return processImage(figure, owner, false, file.getOriginalFilename(), file.getInputStream(), publicationZdbId);
    }

    public static Image processImage(Figure figure, String filePath, Boolean isVideoStill, String direction, String publicationZdbId) throws IOException {
        // This method was made for the original Dorsky load, so it has a hard-coded owner
        Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", "ZDB-PERS-030520-2"))  //Yvonne
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

    private static Image processImage(Figure figure, Person owner, Boolean isVideoStill, String fileName, InputStream imageStream, String publicationZdbId) throws IOException {
        Image image = createPlaceholderImage(figure, owner, isVideoStill);

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

        String extension = FilenameUtils.getExtension(fileName);
        String destinationBasename = pubYear+"/"+publicationZdbId+"/"+ image.getZdbID();
        String destinationFilename = destinationBasename + FilenameUtils.EXTENSION_SEPARATOR + extension;
        String thumbnailFilename = destinationBasename + THUMB + FilenameUtils.EXTENSION_SEPARATOR + extension;
        String mediumFilename = destinationBasename + MEDIUM + FilenameUtils.EXTENSION_SEPARATOR + extension;
        File destinationFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), destinationFilename);
        File thumbnailFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString() , thumbnailFilename);
        String mediumFileName = image.getZdbID() + "_medium" + FilenameUtils.EXTENSION_SEPARATOR + extension;
        File mediumFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), mediumFileName);

        // we used to attempt to set the image's width and height properties here using ImageIO.read(), but it
        // choked on images with a CMYK color space (common for published images), so we omit that now.

        image.setImageFilename(destinationFilename);
        image.setThumbnail(thumbnailFilename);
        image.setMedium(mediumFilename);
        HibernateUtil.currentSession().save(image);

        RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(figure.getPublication(), "img_zdb_id",
                "create new record", image.getZdbID(), null);

        Files.copy(imageStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String makeThumbnail = "/bin/convert -thumbnail 1000x64 " + destinationFile + " " + thumbnailFile;
        String makeMedium = "/bin/convert -thumbnail 500x550 " + destinationFile + " " + mediumFile;
        Runtime rtt = Runtime.getRuntime();
        Runtime rtm = Runtime.getRuntime();
        rtt.exec(makeThumbnail);
        rtm.exec(makeMedium);

        return image;
    }
}
