package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
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
public class CurationFilterPresenter extends Composite {

    private String publicationID;
    protected CurationFilterView view;
    private CurationFilterRPCAsync curationFilterRPCAsync = CurationFilterRPC.Application.getInstance();
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    public static final String ALL = "ALL";
    private ExpressionExperimentDTO experimentFilter = new ExpressionExperimentDTO();

    public CurationFilterPresenter(CurationFilterView curationFilterZone, String publicationID) {
        this.publicationID = publicationID;
        this.view = curationFilterZone;
        experimentFilter.setPublicationID(publicationID);
    }


    public void go() {
        setInitialValues();
    }

    public void setInitialValues() {
        curationFilterRPCAsync.getPossibleFilterValues(publicationID, new RetrieveFilterValuesCallback());
    }

    public void refreshFigureList() {
        curationRPCAsync.getFigures(publicationID, new RetrieveSelectionBoxValueCallback(view.getFigureList()));
    }

    public void refreshGeneList() {
        curationRPCAsync.getGenes(publicationID, new RetrieveSelectionBoxValueCallback(view.getGeneList()));
    }


    public void applyFigureListChange(ListBox list) {
        String figureID = applyGeneralChanges(list);
        saveFilterInfo(figureID, FilterType.FIG);
        AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.SELECT_FIGURE_FX, experimentFilter, view.getFigureID()));
    }

    private String applyGeneralChanges(ListBox list) {
        String id = list.getValue(list.getSelectedIndex());
        //Window.alert("fish box changed to: " + fishName );

        if (id.length() == 0)
            id = null;

        view.setBackgroundColorForListBox(id, list);
        return id;
    }


    public void applyGeneListChange(ListBoxWrapper listBox) {
        String geneID = applyGeneralChanges(listBox);
        MarkerDTO gene = new MarkerDTO();
        gene.setZdbID(geneID);
        experimentFilter.setGene(gene);
        saveFilterInfo(geneID, FilterType.GENE);
        AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.SELECT_GENE_FX, experimentFilter, view.getFigureID()));
    }

    public void applyFishListChange(ListBoxWrapper listBox) {
        String fishID = applyGeneralChanges(listBox);
        saveFilterInfo(fishID, FilterType.FISH);
        experimentFilter.setFishID(fishID);
        AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.SELECT_FISH_FX, experimentFilter, view.getFigureID()));
    }

    public ExpressionExperimentDTO getExperimentFilter() {
        return experimentFilter;
    }

    private void saveFilterInfo(String value, FilterType type) {
        String errorMessage = "Error while saving session info";
        curationFilterRPCAsync.setFilterType(publicationID, value, type.toString(), new VoidAsyncCallback(errorMessage, null, null));
    }

    public void reset() {
        experimentFilter.setGene(null);
        experimentFilter.setFishID(null);
        experimentFilter.setFeatureID(null);

        // update the filter state on the server
        saveFilterInfo(null, FilterType.FIG);
        saveFilterInfo(null, FilterType.GENE);
        saveFilterInfo(null, FilterType.FISH);
        AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.FILTER_FX, experimentFilter, null));
    }

    public void setFilterValues(ExpressionExperimentDTO experimentFilter, String figureID) {
        if (figureID != null) {
            selectFilterElement(view.getFigureList(), figureID);
        } else {
            view.getFigureList().setSelectedIndex(0);
            view.setBackgroundColorForListBox(null, view.getFigureList());
        }
        if (experimentFilter.getFishID() == null) {
            view.getFishList().setSelectedIndex(0);
        } else {
            selectFilterElement(view.getFishList(), experimentFilter.getFishID());
            view.setBackgroundColorForListBox(null, view.getFishList());
        }
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

    public enum FilterType {
        ALT,
        FIG,
        FISH,
        GENO,
        GENE
    }

    ///// handler and listener


    private class RetrieveFilterValuesCallback implements AsyncCallback<FilterValuesDTO> {
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
            view.getFishList().clear();
            view.getFishList().addItem(ALL, "");
            for (FishDTO fishDTO : valuesDTO.getFishes()) {
                view.getFishList().addItem(fishDTO.getName(), fishDTO.getZdbID());
            }

            view.getFigureList().clear();
            view.getFigureList().addItem(ALL, "");
            for (FigureDTO figureDTO : valuesDTO.getFigures()) {
                view.getFigureList().addItem(figureDTO.getLabel(), figureDTO.getZdbID());
            }

            view.getGeneList().clear();
            view.getGeneList().addItem(ALL, "");
            for (MarkerDTO gene : valuesDTO.getMarkers()) {
                view.getGeneList().addItem(gene.getName(), gene.getZdbID());
            }

            readSavedFilterValues();
        }

        private void readSavedFilterValues() {
            curationFilterRPCAsync.getFilterValues(publicationID, new RetrieveSelectFilterValuesCallback());
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
                selectFilterElement(view.getFishList(), filterValues.getFish().getZdbID());
                String fishID = filterValues.getFish().getZdbID();
                experimentFilter.setFishID(fishID);
                view.setBackgroundColorForListBox(fishID, view.getFishList());
            }
            if (filterValues.getFigure() != null) {
                selectFilterElement(view.getFigureList(), filterValues.getFigure().getZdbID());
                String figureID = filterValues.getFigure().getZdbID();
                view.setBackgroundColorForListBox(figureID, view.getFigureList());
            }
            if (filterValues.getMarker() != null) {
                String geneID = filterValues.getMarker().getZdbID();
                selectFilterElement(view.getGeneList(), geneID);
                MarkerDTO gene = new MarkerDTO();
                gene.setZdbID(filterValues.getMarker().getZdbID());
                experimentFilter.setGene(gene);
                view.setBackgroundColorForListBox(geneID, view.getGeneList());
            }
            AppUtils.EVENT_BUS.fireEvent(new ChangeCurationFilterEvent(EventType.FILTER, experimentFilter, view.getFigureID()));
        }


    }

}
