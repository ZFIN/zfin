package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PostComposedPart;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Entry Point for GO curation tab module.
 */
public class GOCurationModule extends ConstructionZoneAdapater {

    private AttributionModule attributionModule = new AttributionModule();
    private GOViewTable goViewTable = new GOViewTable();
    public static final String GO_EVIDENCE_DISPLAY = "go-evidence-display";
    private GoAddBox goAddBox = new GoAddBox(goViewTable);
    public static final String GO_EVIDENCE_ADD = "go-evidence-add";
    public static final String GO_ADD_LINK = "go-add-link";

    private final static String ADD_NEW_GO_TEXT = "[Add New GO]";
    private final static String RIGHT_ARROW = "<img align=\"top\" src=\"/images/right.gif\" >" + ADD_NEW_GO_TEXT;
    private final static String DOWN_ARROW = "<img align=\"top\" src=\"/images/down.gif\" >" + ADD_NEW_GO_TEXT;
    private HTML addNewGoLink = new HTML(RIGHT_ARROW);
    private VerticalPanel addPanel = new VerticalPanel();

    // data
    private String publicationID;

    // listeners
    List<HandlesError> handlesErrorList = new ArrayList<HandlesError>();


    public GOCurationModule(String publicationID) {
        this.publicationID = publicationID;
        initGUI();
        addInternalListeners(this);
        loadDTO();

        openBox(false);
    }

    private void addInternalListeners(HandlesError handlesError) {
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


        attributionModule.addHandlesErrorListener(this);
        goAddBox.addHandlesErrorListener(this);
    }

    private void initGUI() {

        addNewGoLink.setStyleName("relatedEntityPubLink");
        addPanel.add(addNewGoLink);

        RootPanel.get(GO_ADD_LINK).add(addPanel);
        RootPanel.get(GO_EVIDENCE_ADD).add(goAddBox);

        openBox(false);
    }

    private void loadDTO() {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setPublicationZdbID(publicationID);

        goAddBox.setDTO(goEvidenceDTO);
        attributionModule.setDTO(goEvidenceDTO);
        goViewTable.setPublicationZdbID(publicationID);
    }

    private Map<PostComposedPart, List<OntologyDTO>> getTermEntryMap() {
        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = new TreeMap<PostComposedPart, List<OntologyDTO>>();
        List<OntologyDTO> superterm = new ArrayList<OntologyDTO>(1);
        superterm.add(OntologyDTO.GO);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);
        return termEntryMap;
    }

    public ConstructionZone getPileConstructionZoneModule() {
        return this;
    }

    @Override
    public void setError(String message) {
        Window.alert("error: " + message);
    }

    @Override
    public void clearError() {
        attributionModule.revertGUI();
        goAddBox.updateGenes();
        goViewTable.refreshGUI();
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
