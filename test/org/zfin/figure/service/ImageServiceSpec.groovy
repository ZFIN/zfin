package org.zfin.figure.service

import org.hibernate.Transaction
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.expression.Figure
import org.zfin.expression.Image
import org.zfin.framework.HibernateUtil
import org.zfin.profile.Person
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared

class ImageServiceSpec extends AbstractZfinIntegrationSpec {

    @Shared Person owner
    @Shared Figure figure
    @Shared String originalLoadup
    @Shared File imageLoadUp
    @Shared Transaction tx

    @ClassRule @Shared TemporaryFolder tempDir

    //these runs once for the whole class
    def setupSpec() {
        tempDir.newFolder(ZfinPropertiesEnum.IMAGE_LOAD.toString(), "medium")
        originalLoadup = ZfinPropertiesEnum.LOADUP_FULL_PATH.toString()
        ZfinPropertiesEnum.LOADUP_FULL_PATH.setValue(tempDir.getRoot().absolutePath)
        imageLoadUp = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString())
    }

    //these run before & after each test
    def setup() {
        tx = HibernateUtil.currentSession().beginTransaction()
        owner = RepositoryFactory.profileRepository.getPerson("ZDB-PERS-981104-1")
        figure = RepositoryFactory.figureRepository.getFigure("ZDB-FIG-090324-15")
    }

    def cleanup() {
        tx.rollback()
        HibernateUtil.closeSession()
    }

    def cleanupSpec() {
        ZfinPropertiesEnum.LOADUP_FULL_PATH.setValue(originalLoadup)
    }

    def "when a new image is created, it should have a zdb_id, zdbID should be part of filename"() {
        when: "a new image is created"
        Image image = ImageService.processImage(figure,  "test/resources/540x1130.jpg", false,Image.NOT_SPECIFIED)

        then: "it should have a zdbID generated for it"
        image.zdbID

        and: "the file should be named correctly"
        image.imageFilename.contains(image.zdbID)
        image.imageFilename.startsWith("ZDB-IMAGE")
    }


    def "Regular sized, thumbnail & medium sized files should exist in loadUp"() {
        when: "a new image is created"
        Image image = ImageService.processImage(figure, "test/resources/540x1130.jpg", false,Image.NOT_SPECIFIED)
        File imageFile = new File(imageLoadUp, image.imageFilename)
        File thumbnailFile = new File(imageLoadUp, image.thumbnail)
        File mediumFile = new File(new File(imageLoadUp.toString(), "medium"), image.imageFilename)

        then: "the main, thumbnail, and medium images exist in the imageLoadUp directory"
        imageFile.exists()
        thumbnailFile.exists()
        mediumFile.exists()
    }


    def "The figure and image are properly associated"() {
        when: "a new image is created"
        Image image = ImageService.processImage(figure, "test/resources/540x1130.jpg", false,Image.NOT_SPECIFIED)

        then: "it should be associated with the figure and vice-versa"
        image.figure == figure
        figure.images.contains(image)
    }


    def "Image should have placeholder dimensions when piped through processImage"() {
        when: "a new image is created"
        Image image = ImageService.processImage(figure,  "test/resources/${width}x${height}.jpg", false,Image.NOT_SPECIFIED)

        then: "main image has correct dimensions"
        image.width == -1
        image.height == -1

        where:
        width << [540, 900]
        height << [1130, 750]
    }

}
