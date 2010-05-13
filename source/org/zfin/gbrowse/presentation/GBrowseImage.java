package org.zfin.gbrowse.presentation;

/**
 * This class is a presentation object for displaying GBrowse images and
 * links to the browser.
 * <p/>
 * It doesn't have any intelligence, it just holds the Strings necessary
 * for displaying an image that's a link, or a bit of text that's a link,
 * and can also show an explanatory note.
 * <p/>
 * Currently images are built by GBrowseService.buildTranscriptGBrowseImage
 * and rendered by gbrowseImage.tag
 */
public class GBrowseImage {

    private String imageURL;
    private String linkURL;
    private boolean linkWithoutImage;
    private String linkText;
    private String note;

    public static final Integer TRANSCRIPT_IMAGE_WIDTH = 300;


    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLinkURL() {
        return linkURL;
    }

    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    public boolean getLinkWithoutImage() {
        return linkWithoutImage;
    }

    public void setLinkWithoutImage(boolean linkWithoutImage) {
        this.linkWithoutImage = linkWithoutImage;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getDefaultWidth() {
        return TRANSCRIPT_IMAGE_WIDTH;
    }
}
