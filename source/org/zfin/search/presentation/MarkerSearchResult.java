package org.zfin.search.presentation;

import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.PhenotypeOnMarkerBean;

public class MarkerSearchResult {
    String id;
    Marker marker;
    Marker targetGene;
    String matchingText;
    MarkerExpression markerExpression;
    PhenotypeOnMarkerBean markerPhenotype;
    String explain;
    Float score;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getTargetGene() {
        return targetGene;
    }

    public void setTargetGene(Marker gene) {
        this.targetGene = gene;
    }

    public String getMatchingText() {
        return matchingText;
    }

    public void setMatchingText(String matchingText) {
        this.matchingText = matchingText;
    }

    public MarkerExpression getMarkerExpression() {
        return markerExpression;
    }

    public void setMarkerExpression(MarkerExpression markerExpression) {
        this.markerExpression = markerExpression;
    }

    public PhenotypeOnMarkerBean getMarkerPhenotype() {
        return markerPhenotype;
    }

    public void setMarkerPhenotype(PhenotypeOnMarkerBean markerPhenotype) {
        this.markerPhenotype = markerPhenotype;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}
