package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Holds values for all filter elements.
 */
public class FilterValuesDTO implements IsSerializable {

    private FigureDTO figure;
    private MarkerDTO marker;
    private FishDTO fish;
    private FeatureDTO feature;

    private List<FigureDTO> figures;
    private List<MarkerDTO> markers;
    private List<FishDTO> fishes;
    private List<FeatureDTO> features;

    public List<FigureDTO> getFigures() {
        return figures;
    }

    public void setFigures(List<FigureDTO> figures) {
        this.figures = figures;
    }

    public List<MarkerDTO> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerDTO> markers) {
        this.markers = markers;
    }

    public List<FishDTO> getFishes() {
        return fishes;
    }

    public void setFishes(List<FishDTO> fishes) {
        this.fishes = fishes;
    }

    public FigureDTO getFigure() {
        return figure;
    }

    public void setFigure(FigureDTO figure) {
        this.figure = figure;
    }

    public MarkerDTO getMarker() {
        return marker;
    }

    public void setMarker(MarkerDTO marker) {
        this.marker = marker;
    }

    public FishDTO getFish() {
        return fish;
    }

    public void setFish(FishDTO fish) {
        this.fish = fish;
    }

    public FeatureDTO getFeature() {
        return feature;
    }

    public void setFeature(FeatureDTO feature) {
        this.feature = feature;
    }

    public List<FeatureDTO> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureDTO> features) {
        this.features = features;
    }
}
