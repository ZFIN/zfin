package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.mutant.PhenotypeExperiment;

import java.io.Serializable;
import java.util.*;

/**
 * Figure domain business object. It is a figure referenced in a publication.
 */
@Entity
@Table(name = "figure")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
    "CASE fig_label " +
    "WHEN 'text only' THEN 'TOD' " +
    "ELSE 'FIG' " +
    "END"
)
@Setter
@Getter
public abstract class Figure implements Serializable, Comparable<Figure>, ZdbID {

    public static String GELI = "GELI";

    @Id
    @GeneratedValue(generator = "Figure")
    @GenericGenerator(name = "Figure",
        strategy = "org.zfin.database.ZdbIdGenerator",
        parameters = {
            @Parameter(name = "type", value = "FIG"),
            @Parameter(name = "insertActiveData", value = "true")
        })
    @Column(name = "fig_zdb_id")
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String zdbID;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "fig_caption")
    private String caption;

    @Column(name = "fig_comments")
    private String comments;

    @Column(name = "fig_label")
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String label;

    @Column(name = "fig_full_label")
    private String orderingLabel;

    @Column(name = "fig_inserted_date")
    private GregorianCalendar insertedDate;

    @Column(name = "fig_updated_date")
    private GregorianCalendar updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fig_inserted_by")
    private Person insertedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fig_updated_by")
    private Person updatedBy;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "efs_fig_zdb_id")
    private Set<ExpressionFigureStage> expressionFigureStage;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "phenox_fig_zdb_id")
    private Set<PhenotypeExperiment> phenotypeExperiments;

    @OneToMany(mappedBy = "figure", fetch = FetchType.LAZY)
    private Set<Image> images;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "construct_figure",
        joinColumns = @JoinColumn(name = "consfig_fig_zdb_id"),
        inverseJoinColumns = @JoinColumn(name = "consfig_construct_zdb_id")
    )
    private Set<Marker> constructs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fig_source_zdb_id")
    @JsonView(View.GeneExpressionAPI.class)
    private Publication publication;

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

    public List<ExpressionResult2> getExpressionResults2() {
        if(CollectionUtils.isEmpty(expressionFigureStage))
            return null;

        return expressionFigureStage.stream().map(ExpressionFigureStage::getExpressionResultSet)
            .flatMap(Collection::stream).toList();
    }
}
