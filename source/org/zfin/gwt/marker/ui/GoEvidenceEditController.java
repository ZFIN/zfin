package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

/**
 * A GWT class for adding proteins to genes on the markerview.apg page.
 */
public final class GoEvidenceEditController extends AbstractRelatedEntityEditController<GoEvidenceDTO> {

    private DockPanel mainPanel = new DockPanel();
    private final PublicationLookupBox publicationLookupBox = new PublicationLookupBox(null);
    private final AbstractGoEvidenceHeader headerEdit;
    private final TermInfoComposite termInfoComposite = new TermInfoComposite();
    private final Dictionary dictionary = Dictionary.getDictionary("MarkerProperties");

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public static enum Action {
        EDIT, CLONE, ADD
    }

    public final static String STATE_STRING = "state";
    public final static String GENE_ZDBID = "geneZdbID";

    private Action action;

    public GoEvidenceEditController() {
        action = Action.valueOf(dictionary.get(STATE_STRING));
        setValues();
        switch (action) {
            case EDIT:
                headerEdit = new GoEvidenceHeaderEdit(null);
                break;
            case ADD:
                headerEdit = new GoEvidenceHeaderAdd(null);
                break;
            case CLONE:
                headerEdit = new GoEvidenceHeaderAdd(null);
                break;
            default:
                throw new RuntimeException("Bad action value: " + action);
        }
        initGUI();
        addListeners();
        loadDTO();
    }

    public void initGUI() {
        mainPanel.add(publicationLookupBox, DockPanel.EAST);
        publicationLookupBox.setNoPubSelectedMessage(
                "<strong><font color=red>Please select a valid pub to display available inferences</font></strong>");
        // center panel
        mainPanel.add(headerEdit, DockPanel.CENTER);
        mainPanel.add(termInfoComposite, DockPanel.SOUTH);
        RootPanel.get(StandardDivNames.viewDiv).add(mainPanel);
    }

    protected void setValues() {
        publicationLookupBox.clearPublications();
        for (GoCurationDefaultPublications goPubEnum : GoCurationDefaultPublications.values()) {
            publicationLookupBox.addPublication(new PublicationDTO(goPubEnum));
        }
        publicationLookupBox.setKey(PublicationSessionKey.GOCURATION);
        publicationLookupBox.getRecentPubs();
    }

    protected void addListeners() {
        publicationLookupBox.addPublicationChangeListener(headerEdit);

        synchronizeHandlesErrorListener(headerEdit);


        headerEdit.addChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                setDTO(dataChangedEvent.getDTO());
                publicationLookupBox.addRecentPublicationDTO(dataChangedEvent.getDTO().getPublicationZdbID());
            }
        });

        headerEdit.addGoTermChangeListeners(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                String termID = "GO:" + dataChangedEvent.getDTO().getGoTerm().getDataZdbID();
                lookupRPC.getTermInfo(OntologyDTO.GO, termID, new TermInfoCallBack(termInfoComposite, termID));
            }
        });

    }

    private void validateGoEvidence() {
        if (headerEdit.isDirty()) {
            setError("Header has unsaved changes.");
            headerEdit.setError("Header has unsaved changes.");
            return;
        }
    }


    protected void loadDTO() {
        String markerGoEvidenceZdbID;
// load properties
        switch (action) {
            case EDIT:
                markerGoEvidenceZdbID = dictionary.get(LOOKUP_ZDBID);
                MarkerGoEvidenceRPCService.App.getInstance().getMarkerGoTermEvidenceDTO(markerGoEvidenceZdbID,
                        new MarkerEditCallBack<GoEvidenceDTO>("failed to find markergoevidence: ") {
                            public void onSuccess(GoEvidenceDTO returnDTO) {
                                if (action == Action.CLONE) {
                                    publicationZdbID = dictionary.get(PUB_ZDBID);
                                    returnDTO.setPublicationZdbID(publicationZdbID);
                                    returnDTO.setZdbID(null);
                                }
                                setDTO(returnDTO);
                                publicationZdbID = dto.getPublicationZdbID();
                                publicationLookupBox.publicationChanged(new PublicationChangeEvent(publicationZdbID));
                            }
                        });
                break;
            case CLONE:
                markerGoEvidenceZdbID = dictionary.get(LOOKUP_ZDBID);
                MarkerGoEvidenceRPCService.App.getInstance().getMarkerGoTermEvidenceDTO(markerGoEvidenceZdbID,
                        new MarkerEditCallBack<GoEvidenceDTO>("failed to find markergoevidence: ") {
                            public void onSuccess(GoEvidenceDTO returnDTO) {
                                publicationZdbID = dictionary.get(PUB_ZDBID);
                                returnDTO.setPublicationZdbID(publicationZdbID);
                                returnDTO.setZdbID(null);
                                setDTO(returnDTO);
                            }
                        });
                publicationLookupBox.setVisible(false);
                break;
            case ADD:
                String geneZdbID = dictionary.get(GENE_ZDBID);
                publicationZdbID = dictionary.get(PUB_ZDBID);
                // if adding where pub is not given, then must be null
                if (publicationZdbID.equals("null")) {
                    publicationLookupBox.setVisible(true);
                } else {
                    publicationLookupBox.setVisible(false);
                }
                MarkerRPCService.App.getInstance().getGeneForZdbID(geneZdbID,
                        new MarkerEditCallBack<MarkerDTO>("failed to find marker: ") {
                            public void onSuccess(MarkerDTO returnDTO) {
                                GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
                                if (publicationZdbID != null
                                        &&
                                        false == publicationZdbID.isEmpty()
                                        && false == publicationZdbID.equals(IsDirty.NULL_STRING)
                                        ) {
                                    goEvidenceDTO.setPublicationZdbID(publicationZdbID);
                                }
                                goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.IMP);
                                goEvidenceDTO.setMarkerDTO(returnDTO);
                                setDTO(goEvidenceDTO);
                            }
                        });
                break;
            default:
                setError("bad state: " + action);
        }
    }

    @Override
    void setDTO(GoEvidenceDTO dto) {
        if (dto == null) return;
        super.setDTO(dto);
        headerEdit.setDTO(dto);
        String termID = "GO:" + dto.getGoTerm().getDataZdbID();
        lookupRPC.getTermInfo(OntologyDTO.GO, termID, new TermInfoCallBack(termInfoComposite, termID));
        validateGoEvidence();
    }
}