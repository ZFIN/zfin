package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.ChangeCurationFilterEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ListBoxWrapper;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Filter bar aka banana bar.
 * This bar contains three filter elements: figure, gene and fish.
 * Setting a value filters out only experiments and expressions with
 * those characteristics. The filter bar is always visible.
 * <p>
 * General: Selecting a non-default value will set the background color of the list box to red to make
 * it very visible to the user that he filtered the records. Setting it back to 'ALL' will remove the background
 * color. Any change to one of the filter elements will re-read the appropriate records.
 * <p>
 * Life cycle: The filter values have a certain life cycle, i.e. they are remembered as follows:
 * A) Figure: saved in the database and stored forever
 * B) Fish and Gene: The values are stored in the user session and thus are lost after session timeout or logging out.
 * Reloading the page or coming back to the FX page will prepopulate the filter with the values that are available
 * at that point in time.
 * <p>
 * 1) Only Fig: Selecting a Figure applies only to expressions as only they are associated with figures.
 * The list of figure annotations is reread.
 * 2) Only Gene: Selecting a gene applies to both sections and displays only record with experiments that
 * have the selected gene associated.
 * 3) Only Fish: Selecting a applies applies to both sections and displays only record with experiments that
 * have the selected fish associated.
 * 4) Reset: Clicking the button will set all three filter elements to their default (='ALL') and re-read both
 * section.
 */
public class CurationFilterModule extends Composite {

    // GUI elements
    public static final String FILTER_BAR_FISH = "curation-filter-fish";
    public static final String FILTER_BAR_FIGURE = "curation-filter-figure";
    public static final String FILTER_BAR_FEATURE = "curation-filter-feature";
    public static final String FILTER_BAR_RESET = "curation-filter-reset";
    public static final String ALL = "ALL";

    // filter bar (aka banana bar)
    private ListBoxWrapper fishList = new ListBoxWrapper(false);
    private ListBoxWrapper figureList = new ListBoxWrapper(false);
    private ListBoxWrapper featureList = new ListBoxWrapper(false);

    private String fishID;
    private String figureID;
    private String featureID;
    private ExpressionExperimentDTO experimentFilter = new ExpressionExperimentDTO();
    private boolean useFeatureFilter;

    // Attributes are injected through constructor
    private String publicationID;
    private ExpressionSection expressionModule;
    private StructurePile structureModule;

    // RPC class being used for this section.
    private CurationFilterRPCAsync curationFilterRPCAsync = CurationFilterRPC.Application.getInstance();
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public CurationFilterModule(ExpressionSection expressionModule, StructurePile structureModule, String publicationID) {
        this.expressionModule = expressionModule;
        this.structureModule = structureModule;
        this.publicationID = publicationID;
        experimentFilter.setPublicationID(publicationID);
        initGUI();
        setInitialValues();
    }

    public void setInitialValues() {
        curationFilterRPCAsync.getPossibleFilterValues(publicationID, new RetrieveFilterValueCallback());
    }

    public void refreshFigureList() {
        curationRPCAsync.getFigures(publicationID, new RetrieveSelectionBoxValueCallback(figureList));
    }

    public void refreshFishList() {
        curationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack(fishList, true));
    }

    private void initGUI() {
        RootPanel.get(FILTER_BAR_FISH).add(fishList);
        fishList.addChangeHandler(new ChangeFilterHandler(FilterType.FISH));

        RootPanel.get(FILTER_BAR_FIGURE).add(figureList);
        figureList.addChangeHandler(new ChangeFilterHandler(FilterType.FIG));

        RootPanel featurePanel = RootPanel.get(FILTER_BAR_FEATURE);
        if (featurePanel != null) {
            useFeatureFilter = true;
            featurePanel.add(featureList);
            featureList.addChangeHandler(new ChangeFilterHandler(FilterType.ALT));
        }
        Button reset = new Button("Reset");
        reset.addClickHandler(new ResetListener());
        RootPanel.get(FILTER_BAR_RESET).add(reset);

    }

    @SuppressWarnings({"AssignmentToNull", "SuppressionAnnotation"})
    public void applyExperimentFilter(FilterType type) {
        String fishName = fishList.getItemText(fishList.getSelectedIndex());
        fishID = fishList.getValue(fishList.getSelectedIndex());
        figureID = figureList.getValue(figureList.getSelectedIndex());
        if (useFeatureFilter)
            featureID = featureList.getValue(featureList.getSelectedIndex());
        //Window.alert("gene box changed to: " + geneName + "(" + geneID + ")");

        // need to do this as GWT converts null to "null' string!!!!!!!!!
        if (fishID.length() == 0)
            fishID = null;
        if (figureID.length() == 0)
            figureID = null;
        if (useFeatureFilter)
            if (featureID.length() == 0)
                featureID = null;

        experimentFilter = new ExpressionExperimentDTO();
        experimentFilter.setFeatureID(featureID);
        fishList.setBackgroundColor();
        figureList.setBackgroundColor();
        featureList.setBackgroundColor();
        if (!fishName.equals(ALL))
            experimentFilter.setFishName(fishName);
        experimentFilter.setFishID(fishID);
        experimentFilter.setPublicationID(publicationID);
        expressionModule.setExperimentFilter(experimentFilter);
        expressionModule.setFigureID(figureID);
        switch (type) {
            case FIG:
                saveFilterInfo(figureID, FilterType.FIG);
                AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.SELECT_FIGURE_PHENO, experimentFilter, figureID));
                break;
            case FISH:
                saveFilterInfo(fishID, FilterType.FISH);
                AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.SELECT_FISH_PHENO, experimentFilter, figureID));
                break;
            case ALT:
                saveFilterInfo(featureID, FilterType.ALT);
                AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.SELECT_FEATURE_PHENO, experimentFilter, figureID));
        }
        updateExperimentAndExpressionSection(type);
    }

    private void saveFilterInfo(String value, FilterType type) {
        String errorMessage = "Error while saving session info";
        curationFilterRPCAsync.setFilterType(publicationID, value, type.toString(), new VoidAsyncCallback(errorMessage, null, null));
    }

    public void readSavedFilterValues() {
        curationFilterRPCAsync.getFilterValues(publicationID, new RetrieveSelectFilterValuesCallback());
    }

    public void setFilterValues(ExpressionExperimentDTO experimentFilter, String figureID) {
        if (figureID != null) {
            selectFilterElement(figureList, figureID);
        } else {
            figureList.setSelectedIndex(0);
        }
        if (experimentFilter.getFishID() == null) {
            fishList.setSelectedIndex(0);
        } else {
            fishList.setSelectedItemByValue(experimentFilter.getFishID());
            selectFilterElement(fishList, experimentFilter.getFishID());
        }
        fishList.setBackgroundColor();
        figureList.setBackgroundColor();
    }

    /**
     * Select an entry from a filter box that was used.
     *
     * @param list List box
     * @param id   ID
     */
    private void selectFilterElement(ListBox list, String id) {
        if (StringUtils.isEmpty(id))
            return;

        int numberOfEntries = list.getItemCount();
        for (int row = 0; row < numberOfEntries; row++) {
            String gene = list.getValue(row);
            if (gene.equals(id)) {
                list.setSelectedIndex(row);
                break;
            }
        }
    }

    public void refreshFeatureList() {
        curationFilterRPCAsync.getFeatureValues(publicationID, new RetrieveSelectionBoxValueCallback(featureList));
    }


    private class RetrieveFilterValueCallback implements AsyncCallback<FilterValuesDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                GWT.log(String.valueOf(throwable));
            } else {
                GWT.log("Fatal exception: " + throwable);
            }
        }

        @SuppressWarnings({"MethodParameterOfConcreteClass"})
        public void onSuccess(FilterValuesDTO valuesDTO) {
//                Window.alert("brought back: " + genes.size() );
            fishList.clear();
            fishList.addItem(ALL, "");
            for (FishDTO fishDTO : valuesDTO.getFishes()) {
                fishList.addItem(fishDTO.getHandle(), fishDTO.getZdbID());
            }

            figureList.clear();
            figureList.addItem(ALL, "");
            for (FigureDTO figureDTO : valuesDTO.getFigures()) {
                figureList.addItem(figureDTO.getLabel(), figureDTO.getZdbID());
            }

            if (useFeatureFilter) {
                featureList.clear();
                featureList.addItem(ALL, "");
                for (FeatureDTO feature : valuesDTO.getFeatures()) {
                    featureList.addItem(feature.getAbbreviation(), feature.getZdbID());
                }
            }
            readSavedFilterValues();
        }

    }

    private class RetrieveSelectFilterValuesCallback implements AsyncCallback<FilterValuesDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                GWT.log(String.valueOf(throwable));
            } else {
                GWT.log("Fatal exception: " + throwable);
            }
        }

        @SuppressWarnings({"MethodParameterOfConcreteClass"})
        public void onSuccess(FilterValuesDTO filterValues) {
//                Window.alert("brought back: " + genes.size() );

            if (filterValues.getFish() != null) {
                selectFilterElement(fishList, filterValues.getFish().getZdbID());
                fishID = filterValues.getFish().getZdbID();
                experimentFilter.setFishID(fishID);
            }
            if (filterValues.getFigure() != null) {
                selectFilterElement(figureList, filterValues.getFigure().getZdbID());
                figureID = filterValues.getFigure().getZdbID();
            }
            if (filterValues.getFeature() != null) {
                featureID = filterValues.getFeature().getZdbID();
                selectFilterElement(featureList, featureID);
                experimentFilter.setFeatureID(filterValues.getFeature().getZdbID());
            }
            fishList.setBackgroundColor();
            figureList.setBackgroundColor();
            featureList.setBackgroundColor();
            runDependentModules();
        }

        /**
         * This loads the dependent module. Run this method only once at startup.
         * If you need to reload individual module call their methods such as
         * experimentTable.retrieveExperiments() or
         * expressionTable.retrieveExpressions()
         */
        private void runDependentModules() {
            structureModule.runModule();
            expressionModule.setExperimentFilter(experimentFilter);
            expressionModule.setFigureID(figureID);
            expressionModule.runModule();

        }

    }

    private class ChangeFilterHandler implements ChangeHandler {

        private FilterType type;

        private ChangeFilterHandler(FilterType type) {
            this.type = type;
        }

        public void onChange(ChangeEvent event) {
            applyExperimentFilter(type);
        }
    }

    /**
     * Set all three filters: Figure, Gene and Fish to their default value, i.e. ALL
     */
    private class ResetListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            // check if any of the filters is not set to ALL
            if (isOneOrMOreFilterSet())
                return;

            figureList.setSelectedIndex(0);
            fishList.setSelectedIndex(0);
            featureList.setSelectedIndex(0);
            figureID = null;
            featureID = null;
            fishList.setBackgroundColor();
            figureList.setBackgroundColor();
            featureList.setBackgroundColor();
            experimentFilter.setFishID(null);
            experimentFilter.setFeatureID(null);
            updateExperimentAndExpressionSection(null);
            // update the filter state on the server
            saveFilterInfo(null, FilterType.FIG);
            saveFilterInfo(null, FilterType.FISH);
            saveFilterInfo(null, FilterType.ALT);
        }

        private boolean isOneOrMOreFilterSet() {
            boolean resetNeeded = false;
            if (figureList.getSelectedIndex() > 0)
                resetNeeded = true;
            if (fishList.getSelectedIndex() > 0)
                resetNeeded = true;
            if (featureList.getSelectedIndex() > 0)
                resetNeeded = true;
            if (!resetNeeded)
                return true;
            return false;
        }


    }

    /**
     * Changes in the filter criteria should cause and update in the
     * experiment and expression section. Figure changes only affect the expression section.
     *
     * @param type Filter Type
     */
    private void updateExperimentAndExpressionSection(FilterType type) {
        expressionModule.applyFilterElements(figureID, experimentFilter);
        //AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.FILTER_PHENO, experimentFilter, figureID));
    }

    public enum FilterType {
        // Feature 
        ALT,
        FIG,
        FISH,
        GENE
    }

}
