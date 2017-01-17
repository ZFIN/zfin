package org.zfin.gwt.curation.event;

import org.zfin.gwt.root.dto.ExpressionExperimentDTO;
import org.zfin.gwt.root.util.StringUtils;

public class ChangeCurationFilterEvent extends CurationEvent {

    private ExpressionExperimentDTO experimentFilter;
    private String figureID;


    public ChangeCurationFilterEvent(EventType type, ExpressionExperimentDTO experimentFilter, String figureID) {
        super(type, getFilterValues(experimentFilter, figureID));
        this.experimentFilter = experimentFilter;
        if (StringUtils.isNotEmpty(figureID))
            this.figureID = figureID;
    }

    private static String getFilterValues(ExpressionExperimentDTO experimentFilter, String figureID) {
        String filter = getFilterElementDisplay("Figure", figureID);
        if (experimentFilter != null) {
            if (experimentFilter.getGene() != null)
                filter += getFilterElementDisplay("Gene", experimentFilter.getGene().getZdbID());
            filter += getFilterElementDisplay("Fish", experimentFilter.getFishID());
            return filter;
        }
        return null;
    }

    private static String getFilterElementDisplay(String entity, String value) {
        if (value != null) {
            String display = entity;
            display += ": ";
            display += value;
            return display;
        }
        return "";
    }

    public ExpressionExperimentDTO getExperimentFilter() {
        return experimentFilter;
    }

    public String getFigureID() {
        return figureID;
    }
}
