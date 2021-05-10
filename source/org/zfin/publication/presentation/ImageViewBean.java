package org.zfin.publication.presentation;

import org.zfin.expression.Image;
import org.zfin.marker.Marker;

import java.util.List;

public class ImageViewBean {
    private Image image;
    private List<Marker> expressionGenes;

    public String getMediumImageURL() {
        if (image == null) return null;

        String imageURL = image.getMediumUrl();
        return imageURL;
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
