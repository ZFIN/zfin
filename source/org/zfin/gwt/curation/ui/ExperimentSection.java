package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.ExperimentDTO;

import java.util.Set;

/**
 * This interface defines an experiment section composite.
 */
public interface ExperimentSection {

    /**
     * Retrieves all experiments that are selcted (checked)
     * @return set of experiments DTOs
     */
    Set<ExperimentDTO> getSelectedExperiment();

    /**
     * This method will update the experiment section after new expression records
     * have been created. It will do cleanup work:
     * 1) re-calculate the number of expressions per experiment
     * 2) un-check all experiments and default the 'Clear All' and 'Show only ...' links.
     */
    public void notifyAddedExpression();

    /**
     * Un-select all experiments that may be selected.
     */
    void unselectAllExperiments();

    /**
     * Set (select) the given experiment. If this experiment is not found in the current list of
     * experiments no experiment is selected.
     * @param experiment experiment that is being selected
     */
    void setSingleExperiment(ExperimentDTO experiment);

    /**
     * Notify that all expressions on the given experiments are removed.
     * This requires an update on the number of expressions being used per experiment
     * @param experiment experiment on which expressions where removed
     */
    void notifyRemovedExpression(ExperimentDTO experiment);

    /**
     * Clear all error messages that are displayed in the experiment section.
     */
    void clearErrorMessages();

    /**
     * Set filter for experiment section. Only display experiments that match the filter elements.
     * @param experimentFilter example experiment
     */
    void setExperimentFilter(ExperimentDTO experimentFilter);

    /**
     * Initialize the experiment section.
     */
    void runModule();

    /**
     * Apply the filter elements.
     * @param experimentFilter experiment filter.
     */
    void applyFilterElements(ExperimentDTO experimentFilter);
}
