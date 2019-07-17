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
        //ZfinPropertiesEnum.LOADUP_FULL_PATH.setValue(tempDir.getRoot().absolutePath)
        imageLoadUp = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString())
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


    def "Regular sized, thumbnail & medium sized files should exist in loadUp"() {
        when: "a new image is created"
        def zdbId = "ZDB-PUB-110609-15"
        Image image = ImageService.processImage(figure, "test/resources/540x1130.jpg", false,Image.NOT_SPECIFIED, zdbId)
        File imageFile = new File(imageLoadUp, image.imageFilename)
        File thumbnailFile = new File(imageLoadUp, image.thumbnail)
        File mediumFile = new File(imageLoadUp, image.medium)

        then: "${imageFile}"
        imageFile.exists()
        thumbnailFile.exists()
        mediumFile.exists()
    }


    def "The figure and image are properly associated"() {
        when: "a new image is created"
        def zdbId = "ZDB-PUB-110609-15"
        Image image = ImageService.processImage(figure, "test/resources/540x1130.jpg", false,Image.NOT_SPECIFIED, zdbId)

        then: "it should be associated with the figure and vice-versa"
        image.figure == figure
        figure.images.contains(image)
    }


    def "Image should have placeholder dimensions when piped through processImage"() {
        when: "a new image is created"
        def zdbId = "ZDB-PUB-110609-15"
        Image image = ImageService.processImage(figure,  "test/resources/${width}x${height}.jpg", false,Image.NOT_SPECIFIED, zdbId)

        then: "main image has correct dimensions"
        image.width == -1
        image.height == -1

        where:
        width << [540, 900]
        height << [1130, 750]
    }

}
