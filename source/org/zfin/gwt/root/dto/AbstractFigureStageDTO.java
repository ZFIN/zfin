package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object corresponding to a unique combination of
 * Experiment, Figure and Stage range.
 */
public abstract class AbstractFigureStageDTO<T extends ExpressedTermDTO> implements IsSerializable {

    protected FigureDTO figure;
    protected StageDTO start;
    protected StageDTO end;
    private String publicationID;
    private List<T> expressedTerms = new ArrayList<T>(10);

    /**
     * Return the stage range in the format:
     * [start stage] - [end stage]
     * if start and end stage are the same only return the start stage name.
     *
     * @return stage range.
     */
    public String getStageRange() {
        String startID = start.getZdbID();
        String endID = end.getZdbID();
        String startName = start.getName();
        String endName = end.getName();
        if (startID.equals(endID)) {
            return startName;
        }
        return startName + " - " + endName;
    }

    public FigureDTO getFigure() {
        return figure;
    }

    public void setFigure(FigureDTO figure) {
        this.figure = figure;
    }

    public List<T> getExpressedTerms() {
        return expressedTerms;
    }

    public void setExpressedTerms(List<T> expressedTerms) {
        this.expressedTerms = expressedTerms;
    }

    public void addExpressedTerm(T term) {
        expressedTerms.add(term);
    }

    public StageDTO getStart() {
        return start;
    }

    public void setStart(StageDTO start) {
        this.start = start;
    }

    public StageDTO getEnd() {
        return end;
    }

    public void setEnd(StageDTO end) {
        this.end = end;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public String getUniqueID() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(figure.getZdbID());
        sb.append(":");
        sb.append(start.getZdbID());
        sb.append(":");
        sb.append(end.getZdbID());
        return sb.toString();
    }

    /**
     * This is a compound ID of the format:
     * figureID:startStageID:endStageID.
     *
     * @param uniqueID concatenated unique ID.
     */
    public void setUniqueID(String uniqueID) {
        String[] ids = uniqueID.split(":");
        if (ids.length != 3)
            throw new RuntimeException("unique id '" + uniqueID + "'not in the format figureID:startStageID:endStageID");
        figure.setZdbID(ids[1]);
        if (start == null)
            start = new StageDTO();
        if (end == null)
            end = new StageDTO();
        start.setZdbID(ids[2]);
        end.setZdbID(ids[3]);
    }

}