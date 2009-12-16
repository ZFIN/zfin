package org.zfin.publication.presentation;

import org.zfin.expression.Image;
import org.zfin.marker.Marker;

import java.util.List;

public class ImageViewBean {
    private Image image;
    private List<Marker> expressionGenes;

    public String getMediumImageURL() {
        if (image == null) return null;

        StringBuffer imageURL = new StringBuffer("/imageLoadUp/medium/");

        if (image.getImageWithAnnotationsFilename() != null)
            return imageURL.append(image.getImageWithAnnotationsFilename()).toString();
        else
            return imageURL.append(image.getImageFilename()).toString();
    }


    public Image getImage() {
        if (image == null)
            image = new Image();
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Marker> getExpressionGenes() {
        return expressionGenes;
    }

    public void setExpressionGenes(List<Marker> expressionGenes) {
        this.expressionGenes = expressionGenes;
    }
}
