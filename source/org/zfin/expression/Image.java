package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@Entity
@Table(name = "image")
@SecondaryTable(
    name = "image_stage",
    pkJoinColumns = @PrimaryKeyJoinColumn(name = "imgstg_img_zdb_id")
)
@Setter
@Getter
public class Image implements Serializable {

    public static String NOT_SPECIFIED = "not specified";

    @Id
    @GeneratedValue(generator = "Image")
    @GenericGenerator(name = "Image",
        strategy = "org.zfin.database.ZdbIdGenerator",
        parameters = {
            @Parameter(name = "type", value = "IMAGE"),
            @Parameter(name = "insertActiveData", value = "true")
        })
    @Column(name = "img_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_fig_zdb_id")
    @JsonView(View.API.class)
    private Figure figure;

    @Column(name = "img_label")
    private String label;

    @Column(name = "img_image", nullable = false)
    private String imageFilename;

    @Column(name = "img_image_with_annotation")
    private String imageWithAnnotationsFilename;

    @Column(name = "img_image_with_annotation_medium")
    private String imageWithAnnotationMediumFilename;

    @Column(name = "img_width", nullable = false)
    private Integer width;

    @Column(name = "img_height", nullable = false)
    private Integer height;

    @Column(name = "img_thumbnail", nullable = false)
    private String thumbnail;

    @Column(name = "img_medium", nullable = false)
    private String medium;

    @Column(name = "img_is_video_still", nullable = false)
    private Boolean videoStill;

    @Column(name = "img_view", nullable = false)
    private String view;

    @Column(name = "img_direction", nullable = false)
    private String direction;

    @Column(name = "img_form", nullable = false)
    private String form;

    @Column(name = "img_preparation", nullable = false)
    private String preparation;

    @Column(name = "img_external_name")
    private String externalName;

    @Column(name = "img_comments")
    private String comments;

    @Column(name = "img_inserted_date")
    private GregorianCalendar insertedDate;

    @Column(name = "img_updated_date")
    private GregorianCalendar updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_inserted_by")
    private Person insertedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_updated_by")
    private Person updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_owner_zdb_id", nullable = false)
    private Person owner;

    @OneToMany(mappedBy = "still", fetch = FetchType.LAZY)
    private Set<Video> videos;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "int_image_term",
        joinColumns = @JoinColumn(name = "iit_img_zdb_id"),
        inverseJoinColumns = @JoinColumn(name = "iit_term_zdb_id")
    )
    private Set<GenericTerm> terms;

    @Embedded
    private ImageStage imageStage;


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
