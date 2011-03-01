package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.GoDefaultPublication;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;

/**
 * A GWT class for adding proteins to genes on the markerview.apg page.
 */
public final class GoMarkerEditController extends AbstractRelatedEntityEditController<MarkerDTO> {

    private DockPanel mainPanel = new DockPanel();
    private final PublicationLookupBox publicationLookupBox = new PublicationLookupBox(null);
    private GoMarkerViewTable goViewTable = new GoMarkerViewTable(publicationLookupBox);
    private GoMarkerAddBox goAddBox = new GoMarkerAddBox(goViewTable);
    public static final String GO_EVIDENCE_DISPLAY = "go-evidence-display";
    public static final String GO_EVIDENCE_ADD = "go-evidence-add";
    public static final String GO_ADD_LINK = "go-add-link";

    // new go link stuff
    private final static String ADD_NEW_GO_TEXT = "[Add New GO]";
    private final static String RIGHT_ARROW = "<img align=\"top\" src=\"/images/right.gif\" >" + ADD_NEW_GO_TEXT;
    private final static String DOWN_ARROW = "<img align=\"top\" src=\"/images/down.gif\" >" + ADD_NEW_GO_TEXT;
    private HTML addNewGoLink = new HTML(RIGHT_ARROW);
    private VerticalPanel addPanel = new VerticalPanel();


    public final static String STATE_STRING = "state";
    public final static String GENE_ZDBID = "geneZdbID";

//    private Action action;

    public GoMarkerEditController() {
        initGUI();
        setValues();
        addListeners();
        loadDTO();
    }

    public void initGUI() {
        addNewGoLink.setStyleName("relatedEntityPubLink");
        addPanel.add(addNewGoLink);

        RootPanel.get(GO_ADD_LINK).add(addPanel);
        RootPanel.get(GO_EVIDENCE_ADD).add(goAddBox);
        mainPanel.add(publicationLookupBox, DockPanel.EAST);
        mainPanel.add(goViewTable, DockPanel.SOUTH);
        publicationLookupBox.setNoPubSelectedMessage(
                "<strong><font color=red>Please select a valid pub to display available inferences</font></strong>");
        // center panel
        mainPanel.add(goAddBox, DockPanel.CENTER);
        goAddBox.setVisible(false);
        RootPanel.get(StandardDivNames.viewDiv).add(mainPanel);

        openBox(true);
    }

    protected void setValues() {
        publicationLookupBox.clearPublications();
        for (GoDefaultPublication goPubEnum : GoDefaultPublication.getCurationPublications()) {
            publicationLookupBox.addPublication(new PublicationDTO(goPubEnum));
        }
        publicationLookupBox.setKey(PublicationSessionKey.GOCURATION);
        publicationLookupBox.getRecentPubs();
    }


    @Override
    public void clearError() {
        goViewTable.refreshGUI();
        openBox(false);
    }

    protected void addListeners() {
        publicationLookupBox.addPublicationChangeListener(goAddBox);

        goAddBox.addChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                publicationLookupBox.addRecentPublicationDTO(dataChangedEvent.getDTO().getPublicationZdbID());
            }
        });

        addNewGoLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openBox(!goAddBox.isVisible());
            }

        });

        goAddBox.addHandlesErrorListener(this);
        goViewTable.addHandlesErrorListener(this);

    }

    private void validateGoEvidence() {
        if (goAddBox.isDirty()) {
            setError("Select a pub to enable interface.");
            goAddBox.setError("Select a pub to enable interface.");
            return;
        }
    }


    protected void loadDTO() {
        Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
        final String zdbID = transcriptDictionary.get(LOOKUP_ZDBID);
        MarkerRPCService.App.getInstance().getGeneOnlyForZdbID(zdbID, new MarkerEditCallBack<MarkerDTO>("Failed to find gene for: " + zdbID) {
            @Override
            public void onSuccess(MarkerDTO result) {
                setDTO(result);
            }
        });

    }

    @Override
    void setDTO(MarkerDTO dto) {
        if (dto == null) return;
        super.setDTO(dto);
        goViewTable.setZdbID(dto.getZdbID());

        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setMarkerDTO(dto);
        goAddBox.setDTO(goEvidenceDTO);
        goAddBox.setValues();
        validateGoEvidence();
    }

    public void openBox(boolean b) {
        goAddBox.setVisible(b);
        addNewGoLink.setHTML((b ? DOWN_ARROW : RIGHT_ARROW));
    }
}