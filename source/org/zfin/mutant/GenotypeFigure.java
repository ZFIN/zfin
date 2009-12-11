package org.zfin.mutant;

import java.io.Serializable;


public class GenotypeFigure implements Serializable {
    public String getGenozdbID() {
        return genozdbID;
    }

    public void setGenozdbID(String genozdbID) {
        this.genozdbID = genozdbID;
    }

    public String getFigzdbID() {
        return figzdbID;
    }

    public void setFigzdbID(String figzdbID) {
        this.figzdbID = figzdbID;
    }

    public String getMorphzdbID() {
        return morphzdbID;
    }

    public void setMorphzdbID(String morphzdbID) {
        this.morphzdbID = morphzdbID;
    }

    public String getSubtermzdbID() {
        return subtermzdbID;
    }

    public void setSubtermzdbID(String subtermzdbID) {
        this.subtermzdbID = subtermzdbID;
    }

    public String getSupertermzdbID() {
        return supertermzdbID;
    }

    public void setSupertermzdbID(String supertermzdbID) {
        this.supertermzdbID = supertermzdbID;
    }

    private String genozdbID;
    private String figzdbID;
    private String morphzdbID;
    private String subtermzdbID;
    private String supertermzdbID;


}
