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
    @Override
    public void setupSpec() {
        def imageLoadPath = ZfinPropertiesEnum.IMAGE_LOAD.toString()

        // use relative path, otherwise tempDir throws IOException
        if (imageLoadPath.startsWith("/")) {
            imageLoadPath = "." + imageLoadPath
        }

        tempDir.newFolder(imageLoadPath, "medium")
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

    @Override
    public void cleanupSpec() {
        ZfinPropertiesEnum.LOADUP_FULL_PATH.setValue(originalLoadup)
    }

    def "Regular sized, thumbnail & medium sized files should exist in loadUp"() {
        when: "a new image is created"
        def zdbId = "ZDB-PUB-110609-15"
        Image image = ImageService.processImage(figure, "test/resources/540x1130.jpg", false,Image.NOT_SPECIFIED, zdbId)
        File imageFile = new File(imageLoadUp, image.imageFilename)
        File thumbnailFile = new File(imageLoadUp, image.thumbnail)
        File mediumFile = new File(imageLoadUp, image.medium)
        waitFor(thumbnailFile::exists, 5000) && waitFor(mediumFile::exists, 5000) //wait maximum of 5 seconds for files to exist

        thumbnailFile.deleteOnExit()
        mediumFile.deleteOnExit()
        imageFile.deleteOnExit()        

        then: "${imageFile} and associated files should exist"
        imageFile.exists() && thumbnailFile.exists() && mediumFile.exists() //example thumbnailFile path: /opt/zfin/loadUp/pubs/2011/ZDB-PUB-110609-15/ZDB-IMAGE-211115-1_thumb.jpg
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


    def "A TIFF upload is stored as JPEG, with the original TIFF kept alongside it"() {
        when: "a TIFF image is uploaded"
        def zdbId = "ZDB-PUB-110609-15"
        def source = new File("test/resources/540x1130.tif")
        Image image = ImageService.processImage(figure, source.path, false, Image.NOT_SPECIFIED, zdbId)

        File imageFile = new File(imageLoadUp, image.imageFilename)
        File thumbnailFile = new File(imageLoadUp, image.thumbnail)
        File mediumFile = new File(imageLoadUp, image.medium)
        File archivedOriginal = new File(imageLoadUp, image.imageFilename.replaceFirst(/\.jpg$/, ".tif"))
        waitFor(thumbnailFile::exists, 5000) && waitFor(mediumFile::exists, 5000)

        imageFile.deleteOnExit()
        thumbnailFile.deleteOnExit()
        mediumFile.deleteOnExit()
        archivedOriginal.deleteOnExit()

        then: "all three stored files are JPEGs a browser can render"
        image.imageFilename.endsWith(".jpg")
        image.thumbnail.endsWith(".jpg")
        image.medium.endsWith(".jpg")
        imageFile.exists() && thumbnailFile.exists() && mediumFile.exists()

        and: "the untouched TIFF is kept for its full resolution"
        archivedOriginal.exists()
        archivedOriginal.bytes == source.bytes
    }


    /* Usage: waitFor(time) { condition }
    Loops over the condition, waiting for it to be true. If the condition becomes true before the timeout (in milliseconds)
    expires, the function quits immediately and returns true. If the condition is still false as the timeout expires, the
    function returns false.
    */
    def waitFor(closure, time) {
        def start = System.currentTimeMillis()
        while(System.currentTimeMillis() - start < time)
        {
            if(closure())
                return true
            sleep(10)
        }
        return false
    }
    
}
