package org.zfin.expression.presentation;

import java.util.List;

public class FigureResult {
    String publication;
    String figure;
    String startStage;
    String endStage;
    List<String> fish;
    List<String> anatomy;

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public String getFigure() {
        return figure;
    }

    public void setFigure(String figure) {
        this.figure = figure;
    }

    public String getStartStage() {
        return startStage;
    }

    public void setStartStage(String startStage) {
        this.startStage = startStage;
    }

    public String getEndStage() {
        return endStage;
    }

    public void setEndStage(String endStage) {
        this.endStage = endStage;
    }

    public List<String> getFish() {
        return fish;
    }

    public List<String> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(List<String> anatomy) {
        this.anatomy = anatomy;
    }
}
