package org.zfin.figure.service

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.expression.Image
import org.zfin.expression.Video
import org.zfin.framework.HibernateUtil
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared

/**
 * Created by kschaper on 7/1/14.
 */
class VideoServiceSpec extends AbstractZfinIntegrationSpec {

    @Shared File videoLoadUp
    @Shared String originalLoadup
    @Shared Image image

    @ClassRule @Shared TemporaryFolder tempDir;

    //these runs once for the whole class
    public def setupSpec() {
        originalLoadup = ZfinPropertiesEnum.LOADUP_FULL_PATH.toString()
        ZfinPropertiesEnum.LOADUP_FULL_PATH.setValue(tempDir.getRoot().absolutePath)
        videoLoadUp = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.VIDEO_LOAD.toString())
    }

    //these run before & after each test
    def setup() {
        HibernateUtil.currentSession().beginTransaction()
        image = RepositoryFactory.publicationRepository.getImageById("ZDB-IMAGE-101122-53")
    }

    def cleanup() {
        HibernateUtil.currentSession().getTransaction().rollback()
        HibernateUtil.closeSession()
    }

    public def cleanupSpec() {
        ZfinPropertiesEnum.LOADUP_FULL_PATH.setValue(originalLoadup)
    }

    def "When a video is added, the video should be returned, get an id, and the file should end up in videoLoadUp"() {
        when:
        Video video = VideoService.processVideo("test/resources/TestVideo.mp4", image);
        File videoFile = new File(videoLoadUp, video.videoFilename)

        then: "video object properties are set correctly"
        video
        video.id
        video.videoFilename.contains(String.valueOf(image.zdbID))

        and: "video file exists"
        videoFile.exists()
    }

    def "Image and video should be related after processing"() {
        when: "a new video is added"
        Video video = VideoService.processVideo("test/resources/TestVideo.mp4", image);

        then: "video is associated with image and vice-versa"
        video.still == image
        image.videos.contains(video)
    }

}
