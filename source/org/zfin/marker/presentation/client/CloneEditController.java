package org.zfin.marker.presentation.client;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import org.zfin.marker.presentation.dto.*;
import org.zfin.marker.presentation.event.*;

import java.util.ArrayList;
import java.util.List;

/**
 */
public final class CloneEditController implements PublicationChangeListener {

    // gui elements
    private MarkerNameEdit markerNameEdit = new MarkerNameEdit("markerName");
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
    private String curatorID;
    private CloneDTO cloneDTO;

    // lookup
    private static final String LOOKUP_TRANSCRIPT_ZDBID = "zdbID";
    private static final String LOOKUP_CURATOR_ZDBID = "curatorID";

    // listeners
    private List<MarkerLoadListener> markerLoadListeners = new ArrayList<MarkerLoadListener>();

    public void initGUI() {

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
        publicationLookupBox.addPublication(new PublicationAbstractDTO("VEGA Database Links", "ZDB-PUB-030703-1"));

    }

    protected void loadClone() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            cloneZdbID = transcriptDictionary.get(LOOKUP_TRANSCRIPT_ZDBID);
            curatorID = transcriptDictionary.get(LOOKUP_CURATOR_ZDBID);

            CloneRPCService.App.getInstance().getCloneForZdbID(cloneZdbID,
                    new MarkerEditCallBack<CloneDTO>("failed to find clone: ") {
                        public void onSuccess(CloneDTO returnedCloneDTO) {
//                            setCloneDomain(cloneDTO);
                            setCloneDomain(returnedCloneDTO);
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    protected void addListeners() {

        markerNameEdit.addMarkerChangeListener(new MarkerChangeListener() {
            public void changeMarkerProperties(MarkerChangeEvent markerChangeEvent) {
                if (false == markerChangeEvent.getMarkerDTO().getName().equals(getCloneDTO().getName())) {
                    if (null == previousNamesBox.validateNewRelatedEntity(markerChangeEvent.getMarkerDTO().getName())) {
                        previousNamesBox.addRelatedEntity(getCloneDTO().getName(), publication);
                    }
                }
                getCloneDTO().copyFrom(markerChangeEvent.getMarkerDTO());
            }
        });

        // direct attribution listeners
        dbLinkTable.addDBLinkTableListener(new DirectAttributionDBLinkTableListener(directAttributionTable));

        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        previousNamesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));

        addMarkerDomainListener(new MarkerLoadListener() {
            public void markerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
                MarkerDTO dto = markerLoadEvent.getMarkerDTO();
                directAttributionTable.setZdbID(dto.getZdbID());
                directAttributionTable.setRecordAttributions(dto.getRecordAttributions());
                markerNameEdit.setDomain(dto);
                previousNamesBox.setRelatedEntities(cloneDTO.getZdbID(), cloneDTO.getAliasAttributes());
                curatorNoteBox.setZdbID(dto.getZdbID());
                curatorNoteBox.setCuratorZdbID(curatorID);
                curatorNoteBox.setNotes(dto.getCuratorNotes());
                publicNoteBox.setZdbID(dto.getZdbID());
                publicNoteBox.setCuratorZdbID(curatorID);
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


        publicationLookupBox.addPublicationChangeListener(previousNamesBox);
        publicationLookupBox.addPublicationChangeListener(relatedGenesBox);
        publicationLookupBox.addPublicationChangeListener(dbLinkTable);
        publicationLookupBox.addPublicationChangeListener(directAttributionTable);
        publicationLookupBox.addPublicationChangeListener(this);
    }


    protected void setCloneDomain(CloneDTO newCloneDTO) {
        cloneDTO = newCloneDTO;
        cloneZdbID = cloneDTO.getZdbID();
        fireMarkerDomainLoaded(new MarkerLoadEvent(cloneDTO));
    }


    public void publicationChanged(PublicationChangeEvent event) {
        publication = event.getPublication();
    }

    public CloneDTO getCloneDTO() {
        return cloneDTO;
    }

    public void setCloneDTO(CloneDTO cloneDTO) {
        this.cloneDTO = cloneDTO;
    }


    public void addMarkerDomainListener(MarkerLoadListener markerLoadListener) {
        markerLoadListeners.add(markerLoadListener);
    }

    public void fireMarkerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
        for (MarkerLoadListener markerLoadListener : markerLoadListeners) {
            markerLoadListener.markerDomainLoaded(markerLoadEvent);
        }
    }
}
