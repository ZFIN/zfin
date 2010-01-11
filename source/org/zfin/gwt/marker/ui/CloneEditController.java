package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.*;
import org.zfin.gwt.root.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 */
public final class CloneEditController implements PublicationChangeListener , HandlesError{


    // hidable titles
    private String targetedGenesTitle = "geneTitle";

    // gui elements
    private HTML relatedGeneTitle = new HTML("Contains Gene:");
    private CloneHeaderEdit cloneHeaderEdit = new CloneHeaderEdit("markerName");
    private CloneBox cloneBox = new HandledCloneBox("cloneDataName");
    private DirectAttributionTable directAttributionTable = new HandledDirectAttributionTable("directAttributionName");
    private PreviousNamesBox previousNamesBox = new PreviousNamesBox("aliasName");
    private RelatedMarkerBox relatedGenesBox = new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.CLONE_CONTAINS_GENE, true, "geneName");
    private PublicationLookupBox publicationLookupBox = new PublicationLookupBox("publicationName");
    private DBLinkTable dbLinkTable = new HandledDBLinkTable("dbLinksName");
    private SupplierNameList supplierNameList = new SupplierNameList("supplierName");
    private CuratorNoteBox curatorNoteBox = new CuratorNoteBox("curatorNoteName");
    private PublicNoteBox publicNoteBox = new PublicNoteBox("publicNoteName");

    // internal data
    private String publication;
    private String cloneZdbID;
    private CloneDTO cloneDTO;

    // lookup
    private static final String LOOKUP_TRANSCRIPT_ZDBID = "zdbID";

    // listeners
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();
    private List<MarkerLoadListener> markerLoadListeners = new ArrayList<MarkerLoadListener>();

    public void initGUI() {

        RootPanel.get(targetedGenesTitle).add(relatedGeneTitle);

        addListeners();
        setValues();

        DeferredCommand.addCommand(new Command() {
            public void execute() {
                loadClone();
            }
        });
    }

    private void setValues() {
        publicationLookupBox.clearPublications();
        publicationLookupBox.addPublication(new PublicationDTO("VEGA Database Links", "ZDB-PUB-030703-1"));

    }

    protected void loadClone() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            cloneZdbID = transcriptDictionary.get(LOOKUP_TRANSCRIPT_ZDBID);

            CloneRPCService.App.getInstance().getCloneForZdbID(cloneZdbID,
                    new MarkerEditCallBack<CloneDTO>("failed to find clone: ") {
                        public void onSuccess(CloneDTO returnedCloneDTO) {
                            setCloneDomain(returnedCloneDTO);
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    protected void addListeners() {

        cloneHeaderEdit.setPreviousNamesBox(previousNamesBox);
        cloneHeaderEdit.addMarkerChangeListener(new CloneChangeListener() {
            public void changeCloneProperties(CloneChangeEvent cloneChangeEvent) {
                if (false == cloneChangeEvent.getDTO().getName().equals(getCloneDTO().getName())) {
                    if (null == previousNamesBox.validateNewRelatedEntity(cloneChangeEvent.getDTO().getName())) {
                        previousNamesBox.addRelatedEntity(getCloneDTO().getName(), publication);
                    }
                }
                getCloneDTO().copyFrom(cloneChangeEvent.getDTO());
            }
        });

        // direct attribution listeners
        dbLinkTable.addDBLinkTableListener(new DirectAttributionDBLinkTableListener(directAttributionTable));

        previousNamesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));

        addMarkerDomainListener(new MarkerLoadListener() {
            public void markerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
                MarkerDTO dto = markerLoadEvent.getMarkerDTO();
                directAttributionTable.setZdbID(dto.getZdbID());
                directAttributionTable.setRecordAttributions(dto.getRecordAttributions());
                cloneHeaderEdit.setDomain((CloneDTO) dto);
                previousNamesBox.setRelatedEntities(cloneDTO.getZdbID(), cloneDTO.getAliasAttributes());
                curatorNoteBox.setZdbID(dto.getZdbID());
                curatorNoteBox.setNotes(dto.getCuratorNotes());
                publicNoteBox.setZdbID(dto.getZdbID());
                publicNoteBox.setNotes(dto.getPublicNotes());
                cloneBox.setDomain((CloneDTO) dto);
                supplierNameList.setDomain(dto);
                relatedGenesBox.setRelatedEntities(cloneDTO.getZdbID(), cloneDTO.getRelatedGeneAttributes());

                final List<DBLinkDTO> supportinSequenceLinks = cloneDTO.getSupportingSequenceLinks();
                CloneRPCService.App.getInstance().getCloneDBLinkAddReferenceDatabases(dto.getZdbID(),
                        new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                            public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabases) {
                                dbLinkTable.setReferenceDatabases(referenceDatabases);
                                dbLinkTable.setZdbID(cloneZdbID);
                                dbLinkTable.setDBLinks(supportinSequenceLinks);
                            }
                        });
            }
        });


        publicationLookupBox.addPublicationChangeListener(cloneHeaderEdit);
        publicationLookupBox.addPublicationChangeListener(previousNamesBox);
        publicationLookupBox.addPublicationChangeListener(relatedGenesBox);
        publicationLookupBox.addPublicationChangeListener(dbLinkTable);
        publicationLookupBox.addPublicationChangeListener(directAttributionTable);
        publicationLookupBox.addPublicationChangeListener(this);

        addHandlesErrorListener(cloneHeaderEdit);
        cloneHeaderEdit.addHandlesErrorListener(this);
        addHandlesErrorListener(previousNamesBox);
        previousNamesBox.addHandlesErrorListener(this);
        addHandlesErrorListener(directAttributionTable);
        directAttributionTable.addHandlesErrorListener(this);
        addHandlesErrorListener(relatedGenesBox);
        relatedGenesBox.addHandlesErrorListener(this);
        addHandlesErrorListener(cloneBox);
        cloneBox.addHandlesErrorListener(this);
        addHandlesErrorListener(dbLinkTable);
        dbLinkTable.addHandlesErrorListener(this);
    }


    protected void setCloneDomain(CloneDTO newCloneDTO) {
        cloneDTO = newCloneDTO;
        cloneZdbID = cloneDTO.getZdbID();
        if(cloneZdbID.startsWith("ZDB-EST") || cloneZdbID.startsWith("ZDB-CDNA")){
            relatedGeneTitle.setHTML("<b>Encoded by Gene:</b>");
            relatedGenesBox.setType(MarkerRelationshipEnumTypeGWTHack.GENE_ENCODES_SMALL_SEGMENT);
            relatedGenesBox.setZdbIDThenAbbrev(false);
        }
        else{
            relatedGeneTitle.setHTML("<b>Clone contains Gene:</b>");
        }
        fireMarkerDomainLoaded(new MarkerLoadEvent(cloneDTO));
    }


    public void publicationChanged(PublicationChangeEvent event) {
        publication = event.getPublication();
    }

    public CloneDTO getCloneDTO() {
        return cloneDTO;
    }

    public void addMarkerDomainListener(MarkerLoadListener markerLoadListener) {
        markerLoadListeners.add(markerLoadListener);
    }

    public void fireMarkerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
        for (MarkerLoadListener markerLoadListener : markerLoadListeners) {
            markerLoadListener.markerDomainLoaded(markerLoadEvent);
        }
    }

    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    public void setError(String message) {
        // not doing anything with this
    }

    public void clearError() {
        fireEventSuccess();
    }
}
