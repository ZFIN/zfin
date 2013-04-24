package org.zfin.marker;

import org.zfin.expression.Figure;


public class MarkerFigure {
    private Marker construct;
    private Figure figure;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Long id;



    public Marker getConstruct() {
        return construct;
    }

    public void setConstruct(Marker construct) {
        this.construct = construct;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }


}
