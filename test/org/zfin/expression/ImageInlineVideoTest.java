package org.zfin.expression;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Covers the two images from 2000 whose img_image names the movie itself. They predate
 * the video table, so nothing hangs a source element off them and the poster would
 * otherwise be pointed at the .mp4 -- an element that neither draws nor plays.
 */
public class ImageInlineVideoTest {

    private static Image still(String imageFilename, boolean videoStill, Set<Video> videos) {
        Image image = new Image();
        image.setImageFilename(imageFilename);
        image.setVideoStill(videoStill);
        image.setVideos(videos);
        return image;
    }

    private static Video video(String filename) {
        Video video = new Video();
        video.setVideoFilename(filename);
        return video;
    }

    @Test
    public void stillWhoseImageNamesTheMovieAndHasNoVideoRowIsInline() {
        assertTrue(still("ZDB-IMAGE-001220-19.mp4", true, null).isInlineVideo());
        assertTrue(still("ZDB-IMAGE-001220-18.mp4", true, Set.of()).isInlineVideo());
    }

    @Test
    public void stillBackedByTheVideoTableIsNotInline() {
        // The video row already supplies the source, so nothing needs synthesising.
        assertFalse(still("ZDB-IMAGE-001220-19.mp4", true,
            Set.of(video("ZDB-IMAGE-001220-19.mp4"))).isInlineVideo());
    }

    @Test
    public void modernLayoutIsUntouched() {
        assertFalse(still("2013/ZDB-PUB-130724-1/ZDB-IMAGE-140902-100.png", true,
            Set.of(video("ZDB-IMAGE-140902-100.mp4"), video("ZDB-IMAGE-140902-100.webm"))).isInlineVideo());
    }

    @Test
    public void videoStillMissingItsVideoRowIsInlineOnlyWhenTheImageIsAMovie() {
        // Otherwise we would synthesise a source element pointing at a picture.
        assertFalse(still("2013/ZDB-PUB-130724-1/ZDB-IMAGE-140902-100.png", true, Set.of()).isInlineVideo());
    }

    @Test
    public void ordinaryFigureImageIsNeverInline() {
        assertFalse(still("2019/ZDB-PUB-190412-1/ZDB-IMAGE-190708-24.jpg", false, null).isInlineVideo());
    }

    @Test
    public void videoFilenameRecognisesMovieContainersCaseInsensitively() {
        assertTrue(Video.isVideoFilename("ZDB-IMAGE-001220-19.mp4"));
        assertTrue(Video.isVideoFilename("ZDB-IMAGE-001220-19.MP4"));
        assertTrue(Video.isVideoFilename("a/b/c.mov"));
        assertTrue(Video.isVideoFilename("a/b/c.webm"));
    }

    @Test
    public void videoFilenameRejectsPicturesAndNonsense() {
        assertFalse(Video.isVideoFilename("c.jpg"));
        assertFalse(Video.isVideoFilename("c.tif"));
        assertFalse(Video.isVideoFilename("no-extension"));
        assertFalse(Video.isVideoFilename(null));
    }
}
