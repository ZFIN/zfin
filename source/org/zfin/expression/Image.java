package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

import java.io.Serializable;
import java.util.Set;

/**
 * Image domain business object. This is an actual image in ZFIN being referenced
 * in a figure and a publication.
 */
public class Image implements Serializable {

    private String zdbID;
    private Figure figure;
    private String imageFilename;
    private String imageWithAnnotationsFilename;
    private Integer width;
    private Integer height;
    private String thumbnail;
    private Set<Term> terms;

    private ImageStage imageStage;


    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
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

    public Set<Term> getTerms() {
        return terms;
    }

    public void setTerms(Set<Term> terms) {
        this.terms = terms;
    }

    public DevelopmentStage getStart() {
        if (imageStage == null)
            return null;
        else return imageStage.getStart();
    }

    public DevelopmentStage getEnd() {
        if (imageStage == null) 
            return null;
        else return imageStage.getEnd();
    }

    public ImageStage getImageStage() {
        return imageStage;
    }

    public void setImageStage(ImageStage imageStage) {
        this.imageStage = imageStage;
    }

    public boolean equals(Object otherImage) {
        if (!(otherImage instanceof Image))
            return false;

        Image image = (Image) otherImage;
        return getZdbID().equals(image.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

}
