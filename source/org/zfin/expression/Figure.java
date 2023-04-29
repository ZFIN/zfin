package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.mutant.PhenotypeExperiment;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Figure domain business object. It is a figure referenced in a publication.
 */
@Setter
@Getter
public abstract class Figure implements Serializable, Comparable<Figure>, ZdbID {

    public static String GELI = "GELI";

    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String zdbID;
    private String caption;
    private String comments;
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String label;
    private String orderingLabel;
    private Set<ExpressionResult> expressionResults;
    private Set<PhenotypeExperiment> phenotypeExperiments;
    private Set<Image> images;
    @JsonView(View.GeneExpressionAPI.class)
    private Publication publication;
    private Set<Marker> constructs;
    private GregorianCalendar insertedDate;
    private GregorianCalendar updatedDate;
    private Person insertedBy;
    private Person updatedBy;

    public void addImage(Image image) {
        if (images == null) {
            images = new HashSet<>();
        }
        images.add(image);
    }

    public abstract FigureType getType();

    public boolean equals(Object otherFigure) {
        if (!(otherFigure instanceof Figure figure)) {
            return false;
        }

        return getZdbID().equals(figure.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

    public int compareTo(Figure compFig) {
        if (orderingLabel == null) {
            return -1;
        }
        if (compFig == null || compFig.getOrderingLabel() == null) {
            return 1;
        }
        return orderingLabel.compareTo(compFig.getOrderingLabel());
    }


    @JsonView({View.Default.class, View.API.class})
    public boolean isImgless() {
        return images == null || images.isEmpty();
    }


    public Image getImg() {
        if (isImgless()) {
            return null;
        }

        return getImages().iterator().next();
    }

    public int getCaptionWordCount() {
        if (caption == null) {
            return 0;
        }
        return caption.length();
    }

    public String getConciseCaption() {
        if (getCaptionWordCount() > 780) {
            return caption.substring(0, 780);
        }
        return caption;
    }

    public int getConciseCaptionWordCount() {
        if (getConciseCaption() == null) {
            return 0;
        }
        return getConciseCaption().length();
    }

    public boolean isGeli() {
        if (comments != null && comments.equals(GELI)) {
            return true;
        }
        return false;
    }

}
