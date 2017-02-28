package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry Point for GO curation tab module.
 */
public class GoCurationModule extends ConstructionZoneAdapater implements ZfinCurationModule {

    private GoCurationViewTable goViewTable = new GoCurationViewTable();
    public static final String GO_EVIDENCE_DISPLAY = "go-evidence-display";
    public static final String GO_EVIDENCE_DISPLAY_FILTER = "go-evidence-display-filter";
    private GoCurationAddBox goAddBox = new GoCurationAddBox(goViewTable);
    public static final String GO_EVIDENCE_ADD = "go-evidence-add";
    public static final String GO_ADD_LINK = "go-add-link";

    // new go link stuff
    private final static String ADD_NEW_GO_TEXT = "[Add New GO]";
    private final static String RIGHT_ARROW = "<img align=\"top\" src=\"/images/right.gif\" >" + ADD_NEW_GO_TEXT;
    private final static String DOWN_ARROW = "<img align=\"top\" src=\"/images/down.gif\" >" + ADD_NEW_GO_TEXT;
    private HTML addNewGoLink = new HTML(RIGHT_ARROW);
    private VerticalPanel addPanel = new VerticalPanel();

    // get filter bar
    private HorizontalPanel filterPanel = new HorizontalPanel();
    private ListBoxWrapper geneFilterListBox = new ListBoxWrapper();
    public static final String GENE_FILTER_ALL = "ALL";


    // data
    private String publicationID;

    // listeners
    List<HandlesError> handlesErrorList = new ArrayList<>();


    public GoCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    public void init() {
        initGUI();
        addInternalListeners(this);
        loadDTO();
        openBox(false);
    }

    @Override
    public void refresh() {
        loadDTO();
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {
        if (event.getEventType().is(EventType.MARKER_ATTRIBUTION) || event.getEventType().is(EventType.MARKER_DEATTRIBUTION))
            goAddBox.updateGenes();
        if (event.getEventType().is(EventType.CREATE_FISH))
            goAddBox.getInferenceListBox().setAvailableValues();
    }

    @Override
    public void handleTabToggle() {

    }

    @Override
    public void updateTermInfo(String termName, String ontologyName) {

    }

    protected void addInternalListeners(HandlesError handlesError) {
        goAddBox.addChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                goViewTable.setValues();
            }
        });

        addNewGoLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openBox(!goAddBox.isVisible());
            }

        });

        geneFilterListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                goViewTable.doGeneFilter(geneFilterListBox.getSelectedText());
            }
        });

        goAddBox.addHandlesErrorListener(this);
        goViewTable.addHandlesErrorListener(this);
    }

    private void initGUI() {
        addNewGoLink.setStyleName("relatedEntityPubLink");
        addPanel.add(addNewGoLink);

        RootPanel.get(GO_ADD_LINK).add(addPanel);
        RootPanel.get(GO_EVIDENCE_ADD).add(goAddBox);

        filterPanel.add(new HTML("<b valign=\"top\">Filter Gene:</b>"));
        filterPanel.add(geneFilterListBox);
        RootPanel.get(GO_EVIDENCE_DISPLAY_FILTER).add(filterPanel);
        filterPanel.setStyleName("curation-filter");
        filterPanel.setWidth("100%");

        openBox(false);
    }

    private void loadDTO() {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setPublicationZdbID(publicationID);
        goAddBox.setDTO(goEvidenceDTO);
        goViewTable.setZdbID(publicationID);
        updateGeneFilter();
        goAddBox.updateGenes();
    }

    private void updateGeneFilter() {
        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_FIGURE_LIST_START);
        MarkerGoEvidenceRPCService.App.getInstance().getMarkerGoTermEvidencesForPub(publicationID,
                new MarkerEditCallBack<List<GoEvidenceDTO>>("Failed to find pub: " + publicationID + " ") {
                    @Override
                    public void onSuccess(List<GoEvidenceDTO> result) {
                        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_FIGURE_LIST_STOP);
                        geneFilterListBox.clear();
                        geneFilterListBox.addItem(GENE_FILTER_ALL);
                        for (GoEvidenceDTO goEvidenceDTO : result) {
                            if (false == geneFilterListBox.containsItemText(goEvidenceDTO.getMarkerDTO().getName())) {
                                geneFilterListBox.addItem(goEvidenceDTO.getMarkerDTO().getName());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_FIGURE_LIST_STOP);
                    }
                });


    }

    public static ZfinModule getModuleInfo() {
        return new ZfinModule(CurationTab.GO.getName(), GoCurationModule.class.getName());
    }

    public ConstructionZone getPileConstructionZoneModule() {
        return this;
    }

    @Override
    public void setError(String message) {
        GWT.log("Error: " + message);
    }

    @Override
    public void clearError() {
        goAddBox.updateGenes();
        goViewTable.refreshGUI();
        updateGeneFilter();
        openBox(false);
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorList) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorList.add(handlesError);
    }

    public void openBox(boolean b) {
        goAddBox.setVisible(b);
        addNewGoLink.setHTML((b ? DOWN_ARROW : RIGHT_ARROW));
    }
}
