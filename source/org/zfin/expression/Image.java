package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.Person;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Image domain business object. This is an actual image in ZFIN being referenced
 * in a figure and a publication.
 */
public class Image implements Serializable {

    public static String NOT_SPECIFIED = "not specified";

    private String zdbID;
    private Figure figure;
    private String label;
    private String imageFilename;
    private String imageWithAnnotationsFilename;
    private Integer width;
    private Integer height;
    private String thumbnail;
    private Set<GenericTerm> terms;
    private Boolean videoStill;
    private String view;
    private String direction;
    private String form;
    private String preparation;
    private Person owner;



    private String externalName;
    private String comments;

    private Set<Video> videos;

    private ImageStage imageStage;


    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public String getImageWithAnnotationsFilename() {
        return imageWithAnnotationsFilename;
    }

    public void setImageWithAnnotationsFilename(String imageWithAnnotationsFilename) {
        this.imageWithAnnotationsFilename = imageWithAnnotationsFilename;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Set<GenericTerm> getTerms() {
        return terms;
    }

    public void setTerms(Set<GenericTerm> terms) {
        this.terms = terms;
    }

    public DevelopmentStage getStart() {
        if (imageStage == null) {
            return null;
        } else {
            return imageStage.getStart();
        }
    }

    public DevelopmentStage getEnd() {
        if (imageStage == null) {
            return null;
        } else {
            return imageStage.getEnd();
        }
    }

    public ImageStage getImageStage() {
        return imageStage;
    }

    public void setImageStage(ImageStage imageStage) {
        this.imageStage = imageStage;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public Boolean getVideoStill() {
        return videoStill;
    }

    public void setVideoStill(Boolean videoStill) {
        this.videoStill = videoStill;
    }

    public Set<Video> getVideos() {
        return videos;
    }

    public void setVideos(Set<Video> videos) {
        this.videos = videos;
    }

    public Video getFirstVideo() {
        if (videos == null || videos.size() == 0) {
            return null;
        } else {
            return videos.iterator().next();
        }
    }

    public void addVideo(Video video) {
        if (videos == null) {
            videos = new HashSet<>();
        }
        videos.add(video);
    }

    public boolean equals(Object otherImage) {
        if (!(otherImage instanceof Image)) {
            return false;
        }

        Image image = (Image) otherImage;
        return getZdbID().equals(image.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

    public String getDisplayedImageFilename() {
        if (imageWithAnnotationsFilename != null && !imageWithAnnotationsFilename.equals("")) {
            return imageWithAnnotationsFilename;
        }
        return imageFilename;
    }

    public String getUrl() {
        return "/imageLoadUp/" + getDisplayedImageFilename();
    }

    public String getMediumUrl() {
        return "/imageLoadUp/medium/" + getDisplayedImageFilename();
    }

    public String getThumbnailUrl() {
        return "/imageLoadUp/" + getThumbnail();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
