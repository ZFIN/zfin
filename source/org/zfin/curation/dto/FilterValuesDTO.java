package org.zfin.curation.dto;

import org.zfin.curation.dto.FigureDTO;
import org.zfin.curation.dto.MarkerDTO;
import org.zfin.curation.dto.FishDTO;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds values for all filter elements.
 */
public class FilterValuesDTO implements IsSerializable {

    private FigureDTO figure;
    private MarkerDTO marker;
    private FishDTO fish;

    private List<FigureDTO> figures;
    private List<MarkerDTO> markers;
    private List<FishDTO> fishes;

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
}
