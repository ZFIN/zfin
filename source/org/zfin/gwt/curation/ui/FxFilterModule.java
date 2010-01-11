package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Filter bar aka banana bar.
 * This bar contains three filter elements: figure, gene and fish.
 * Setting a value filters out only experiments and expressions with
 * those characteristics. The filter bar is always visible.
 * <p/>
 * General: Selecting a non-default value will set the background color of the list box to red to make
 * it very visible to the user that he filtered the records. Setting it back to 'ALL' will remove the background
 * color. Any change to one of the filter elements will re-read the appropriate records.
 * <p/>
 * Life cycle: The filter values have a certain life cycle, i.e. they are remembered as follows:
 * A) Figure: saved in the database and stored forever
 * B) Fish and Gene: The values are stored in the user session and thus are lost after session timeout or logging out.
 * Reloading the page or coming back to the FX page will prepopulate the filter with the values that are available
 * at that point in time.
 * <p/>
 * 1) Only Fig: Selecting a Figure applies only to expressions as only they are associated with figures.
 * The list of figure annotations is reread.
 * 2) Only Gene: Selecting a gene applies to both sections and displays only record with experiments that
 * have the selected gene associated.
 * 3) Only Fish: Selecting a applies applies to both sections and displays only record with experiments that
 * have the selected fish associated.
 * 4) Reset: Clicking the button will set all three filter elements to their default (='ALL') and re-read both
 * section.
 */
public class FxFilterModule extends Composite {

    // GUI elements
    public static final String FILTER_BAR_GENES = "curation-filter-genes";
    public static final String FILTER_BAR_FISH = "curation-filter-fish";
    public static final String FILTER_BAR_FIGURE = "curation-filter-figure";
    public static final String FILTER_BAR_RESET = "curation-filter-reset";
    public static final String ALL = "ALL";

    // filter bar (aka banana bar)
    private ListBoxWrapper geneList = new ListBoxWrapper(false);
    private ListBoxWrapper fishList = new ListBoxWrapper(false);
    private ListBoxWrapper figureList = new ListBoxWrapper(false);

    private String geneID;
    private String fishID;
    private String figureID;
    private ExperimentDTO experimentFilter = new ExperimentDTO();

    // Attributes are injected through constructor
    private String publicationID;
    //private CurationEntryPoint curationEntryPoint;
    private ExperimentSection experimentModule;
    private ExpressionSection expressionModule;
    private StructurePile structureModule;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public FxFilterModule(String publicationID) {
        super();
        this.publicationID = publicationID;
        experimentFilter.setPublicationID(publicationID);
        initGUI();
        setInitialValues();
    }

    public void setExperimentSection(ExperimentSection experimentModule){
        this.experimentModule = experimentModule;
    }

    public void setExpressionSection(ExpressionSection expressionModule){
        this.expressionModule = expressionModule;
    }

    public void setPileStructure(StructurePile structureModule){
        this.structureModule = structureModule;
    }

    private void setInitialValues() {
        curationRPCAsync.getPossibleFilterValues(publicationID, new RetrieveFishCallback());
    }

    private void readSavedFilterValues() {
        curationRPCAsync.getFilterValues(publicationID, new RetrieveSelectFilterValuesCallback());
    }

    private void initGUI() {
        RootPanel.get(FILTER_BAR_GENES).add(geneList);
        geneList.addChangeHandler(new ChangeFilterListener(FilterType.GENE));

        RootPanel.get(FILTER_BAR_FISH).add(fishList);
        fishList.addChangeHandler(new ChangeFilterListener(FilterType.GENO));

        RootPanel.get(FILTER_BAR_FIGURE).add(figureList);
        figureList.addChangeHandler(new ChangeFilterListener(FilterType.FIG));

        Button reset = new Button("Reset");
        reset.addClickHandler(new ResetListener());
        RootPanel.get(FILTER_BAR_RESET).add(reset);

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

    public void applyExperimentFilter(FilterType type) {
        experimentModule.clearErrorMessages();
        geneID = geneList.getValue(geneList.getSelectedIndex());
        String fishName = fishList.getItemText(fishList.getSelectedIndex());
        fishID = fishList.getValue(fishList.getSelectedIndex());
        figureID = figureList.getValue(figureList.getSelectedIndex());
        //Window.alert("gene box changed to: " + geneName + "(" + geneID + ")");
        //Window.alert("fish box changed to: " + fishName );

        // need to do this as GWT converts null to "null' string!!!!!!!!!
        if (geneID.length() == 0)
            geneID = null;
        if (fishID.length() == 0)
            fishID = null;
        if (figureID.length() == 0)
            figureID = null;

        experimentFilter = new ExperimentDTO();
        experimentFilter.setGeneZdbID(geneID);
        setBackgroundColorForListBox(fishID, fishList);
        setBackgroundColorForListBox(geneID, geneList);
        setBackgroundColorForListBox(figureID, figureList);
        if (!fishName.equals(ALL))
            experimentFilter.setFishName(fishName);
        experimentFilter.setFishID(fishID);
        experimentFilter.setPublicationID(publicationID);
        experimentModule.setExperimentFilter(experimentFilter);
        expressionModule.setExperimentFilter(experimentFilter);
        expressionModule.setFigureID(figureID);
        switch (type) {
            case FIG:
                saveFilterInfo(figureID, FilterType.FIG);
            case GENO:
                saveFilterInfo(fishID, FilterType.GENO);
            case GENE:
                saveFilterInfo(geneID, FilterType.GENE);
        }
        updateExperimentAndExpressionSection(type);
    }

    private void setBackgroundColorForListBox(String fishID, ListBoxWrapper fishList) {
        if (fishID != null)
            DOM.setStyleAttribute(fishList.getElement(), "backgroundColor", "khaki");
        else
            DOM.setStyleAttribute(fishList.getElement(), "backgroundColor", "white");
    }

    private void saveFilterInfo(String value, FilterType type) {
        String errorMessage = "Error while saving session info";
        curationRPCAsync.setFilterType(publicationID, value, type.toString(), new VoidAsyncCallback(new Label(errorMessage), null));
    }

    /**
     * This loads the dependent module. Run this method only once at startup.
     * If you need to reload individual module call their methods such as
     * experimentTable.retrieveExperiments() or
     * expressionTable.retrieveExpressions()
     */
    private void runDependentModules() {
        structureModule.runModule();
        experimentModule.setExperimentFilter(experimentFilter);
        experimentModule.runModule();
        expressionModule.setExperimentFilter(experimentFilter);
        expressionModule.setFigureID(figureID);
        expressionModule.runModule();

    }

    class RetrieveFishCallback implements AsyncCallback<FilterValuesDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                Window.alert(String.valueOf(throwable));
            } else {
                Window.alert("Fatal exception: " + throwable);
            }
        }

        public void onSuccess(FilterValuesDTO valuesDTO) {
//                Window.alert("brought back: " + genes.size() );
            fishList.clear();
            fishList.addItem(ALL, "");
            for (FishDTO fishDTO : valuesDTO.getFishes()) {
                fishList.addItem(fishDTO.getName(), fishDTO.getZdbID());
            }

            figureList.clear();
            figureList.addItem(ALL, "");
            for (FigureDTO figureDTO : valuesDTO.getFigures()) {
                figureList.addItem(figureDTO.getLabel(), figureDTO.getZdbID());
            }

            geneList.clear();
            geneList.addItem(ALL, "");
            for (MarkerDTO gene : valuesDTO.getMarkers()) {
                geneList.addItem(gene.getAbbreviation(), gene.getZdbID());
            }
            readSavedFilterValues();
        }
    }

    private class RetrieveSelectFilterValuesCallback implements AsyncCallback<FilterValuesDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                Window.alert(String.valueOf(throwable));
            } else {
                Window.alert("Fatal exception: " + throwable);
            }
        }

        public void onSuccess(FilterValuesDTO filterValues) {
//                Window.alert("brought back: " + genes.size() );

            if (filterValues.getFish() != null) {
                selectFilterElement(fishList, filterValues.getFish().getZdbID());
                fishID = filterValues.getFish().getZdbID();
                experimentFilter.setFishID(fishID);
                setBackgroundColorForListBox(fishID, fishList);
            }
            if (filterValues.getFigure() != null) {
                selectFilterElement(figureList, filterValues.getFigure().getZdbID());
                figureID = filterValues.getFigure().getZdbID();
                setBackgroundColorForListBox(figureID, figureList);
            }
            if (filterValues.getMarker() != null) {
                String geneID = filterValues.getMarker().getZdbID();
                selectFilterElement(geneList, geneID);
                experimentFilter.setGeneZdbID(filterValues.getMarker().getZdbID());
                setBackgroundColorForListBox(geneID, geneList);
            }
            runDependentModules();
        }

    }

    private class ChangeFilterListener implements ChangeHandler {

        private FilterType type;

        private ChangeFilterListener(FilterType type) {
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
            geneList.setSelectedIndex(0);
            fishList.setSelectedIndex(0);
            figureID = null;
            geneID = null;
            setBackgroundColorForListBox(null, fishList);
            setBackgroundColorForListBox(null, geneList);
            setBackgroundColorForListBox(null, figureList);
            experimentFilter.setGeneZdbID(null);
            experimentFilter.setFishID(null);
            updateExperimentAndExpressionSection(null);
            // update the filter state on the server
            saveFilterInfo(null, FilterType.FIG);
            saveFilterInfo(null, FilterType.GENE);
            saveFilterInfo(null, FilterType.GENO);
        }

    }

    /**
     * Changes in the filter criteria should cause and update in the
     * experiment and expression section. Figure changes only affect the expression section.
     *
     * @param type Filter Type
     */
    private void updateExperimentAndExpressionSection(FilterType type) {
        if (type == null || type != FilterType.FIG) {
            experimentModule.applyFilterElements(experimentFilter);
        }
        expressionModule.applyFilterElements(figureID, experimentFilter);
    }

    private boolean isOneOrMOreFilterSet() {
        boolean resetNeeded = false;
        if (figureList.getSelectedIndex() > 0)
            resetNeeded = true;
        if (geneList.getSelectedIndex() > 0)
            resetNeeded = true;
        if (fishList.getSelectedIndex() > 0)
            resetNeeded = true;
        if (!resetNeeded)
            return true;
        return false;
    }

    public enum FilterType {
        FIG,
        GENO,
        GENE
    }

}