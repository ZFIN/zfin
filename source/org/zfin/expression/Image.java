package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.api.View;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.Person;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Image domain business object. This is an actual image in ZFIN being referenced
 * in a figure and a publication.
 */
@Setter
@Getter
public class Image implements Serializable {

    public static String NOT_SPECIFIED = "not specified";

    @JsonView(View.API.class)
    private String zdbID;
    @JsonView(View.API.class)
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
    private String medium;
    private String externalName;
    private String comments;
    private Set<Video> videos;
    private ImageStage imageStage;
    private GregorianCalendar insertedDate;
    private GregorianCalendar updatedDate;
    private Person insertedBy;
    private Person updatedBy;
    private String imageWithAnnotationMediumFilename;


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
        if (!(otherImage instanceof Image image)) {
            return false;
        }

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

    @JsonView(View.API.class)
    public String getUrl() {
        return "/imageLoadUp/" + getDisplayedImageFilename();
    }

    @JsonView(View.API.class)
    public String getMediumUrl() {
        return "/imageLoadUp/" + getMedium();
    }

    public String getThumbnailUrl() {
        return "/imageLoadUp/" + getThumbnail();
    }

    public boolean hideOrientationInformation() {
        //if all of the orientation information is "not specified", then we don't show the orientation information
        return NOT_SPECIFIED.equals(getPreparation()) &&
               NOT_SPECIFIED.equals(getForm()) &&
               NOT_SPECIFIED.equals(getDirection()) &&
               NOT_SPECIFIED.equals(getView());
    }

}
