package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.DirectAttributionAddsRelatedEntityListener;
import org.zfin.gwt.marker.event.DirectAttributionDBLinkTableListener;
import org.zfin.gwt.marker.event.MarkerLoadEvent;
import org.zfin.gwt.marker.event.MarkerLoadListener;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.PublicationSessionKey;

import java.util.List;

/**
 */
public final class CloneEditController extends AbstractFullMarkerEditController<CloneDTO> {


    // hidable titles
    public static final String genesTitle = "geneTitle";

    // gui elements
    private ViewClickLabel cloneViewClickLabel = new ViewClickLabel("[View Clone]", "/action/marker/clone-view?zdbID=", "Discard");
    private HTML relatedGeneTitle = new HTML("Contains Gene:");
    private CloneHeaderEdit cloneHeaderEdit = new CloneHeaderEdit(headerDiv);
    private CloneBox cloneBox = new CloneBox(dataDiv);
    private RelatedMarkerBox relatedGenesBox = new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.CLONE_CONTAINS_GENE, true, geneDiv);
    private SupplierNameList supplierNameList = new SupplierNameList();
    private DBLinkTable dbLinkTable = new HandledDBLinkTable();


    public void initGUI() {

        RootPanel.get(genesTitle).add(relatedGeneTitle);

        addListeners();
        setValues();

        DeferredCommand.addCommand(new Command() {
            public void execute() {
                loadDTO();
            }
        });
    }

    protected void setValues() {
        publicationLookupBox.clearPublications();
        publicationLookupBox.addPublication(new PublicationDTO("VEGA Database Links", "ZDB-PUB-030703-1"));
        publicationLookupBox.setKey(PublicationSessionKey.CLONE);
        publicationLookupBox.getRecentPubs();

        markerNoteBox.removeEditMode(MarkerNoteBox.EditMode.EXTERNAL);
    }

    protected void loadDTO() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            String zdbID = transcriptDictionary.get(LOOKUP_ZDBID);

            CloneRPCService.App.getInstance().getCloneForZdbID(zdbID,
                    new MarkerEditCallBack<CloneDTO>("failed to find clone: ") {
                        public void onSuccess(CloneDTO returnedCloneDTO) {
                            setDTO(returnedCloneDTO);
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    protected void addListeners() {
        super.addListeners();

        cloneHeaderEdit.addChangeListener(new RelatedEntityChangeListener<CloneDTO>() {
            public void dataChanged(RelatedEntityEvent<CloneDTO> cloneChangeEvent) {
                if (false == cloneChangeEvent.getDTO().getName().equals(dto.getName())) {
                    if (null == previousNamesBox.validateNewRelatedEntity(cloneChangeEvent.getDTO().getName())) {
                        previousNamesBox.addRelatedEntity(dto.getName(), publicationZdbID);
                    }
                }
                dto.copyFrom(cloneChangeEvent.getDTO());
            }
        });

        // direct attribution listeners
        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        dbLinkTable.addDBLinkTableListener(new DirectAttributionDBLinkTableListener(directAttributionTable));

        addMarkerLoadListener(new MarkerLoadListener<CloneDTO>() {
            public void markerLoaded(MarkerLoadEvent<CloneDTO> markerLoadEvent) {
                final CloneDTO dto = markerLoadEvent.getMarkerDTO();
                directAttributionTable.setZdbID(dto.getZdbID());
                directAttributionTable.setRecordAttributions(dto.getRecordAttributions());
                cloneHeaderEdit.setDTO(dto);
                previousNamesBox.setRelatedEntities(dto.getZdbID(), dto.getAliasAttributes());
                markerNoteBox.setDTO(dto);
                cloneViewClickLabel.setDTO(dto);
                cloneBox.setDTO(dto);
                supplierNameList.setDTO(dto);
                relatedGenesBox.setRelatedEntities(dto.getZdbID(), dto.getRelatedGeneAttributes());

                final List<DBLinkDTO> supportinSequenceLinks = dto.getSupportingSequenceLinks();
                CloneRPCService.App.getInstance().getCloneDBLinkAddReferenceDatabases(dto.getZdbID(),
                        new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                            public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabases) {
                                dbLinkTable.setReferenceDatabases(referenceDatabases);
                                dbLinkTable.setZdbID(dto.getZdbID());
                                dbLinkTable.setDBLinks(supportinSequenceLinks);
                            }
                        });
            }
        });

        cloneViewClickLabel.addViewClickedListeners(new ViewClickedListener() {
            @Override
            public void finishedView() {
                if (cloneHeaderEdit.isDirty()) {
                    String error = "Name / type has unsaved change.";
                    cloneHeaderEdit.setError(error);
                    cloneViewClickLabel.setError(error);
                } else if (markerNoteBox.isDirty() || markerNoteBox.hasDirtyNotes()) {
                    String error = "A note not saved.";
                    markerNoteBox.setError(error);
                    cloneViewClickLabel.setError(error);
                } else if (cloneBox.isDirty()) {
                    String error = "Clone data is dirty.";
                    cloneBox.setError(error);
                    cloneViewClickLabel.setError(error);
                } else if (previousNamesBox.isDirty()) {
                    String error = "Alias entry not added.";
                    cloneViewClickLabel.setError(error);
                    previousNamesBox.setError(error);
                } else if (relatedGenesBox.isDirty()) {
                    String error = "Gene entry not added.";
                    cloneViewClickLabel.setError(error);
                    relatedGenesBox.setError(error);
                } else if (dbLinkTable.isDirty()) {
                    String error = "Sequence not added.";
                    cloneViewClickLabel.setError(error);
                    dbLinkTable.setError(error);
                } else {
                    cloneViewClickLabel.continueToViewTranscript();
                }

            }
        });


        publicationLookupBox.addPublicationChangeListener(dbLinkTable);
        publicationLookupBox.addPublicationChangeListener(cloneHeaderEdit);
        publicationLookupBox.addPublicationChangeListener(relatedGenesBox);

        synchronizeHandlesErrorListener(cloneViewClickLabel);
        synchronizeHandlesErrorListener(cloneHeaderEdit);
        synchronizeHandlesErrorListener(relatedGenesBox);
        synchronizeHandlesErrorListener(markerNoteBox);
        synchronizeHandlesErrorListener(cloneBox);
        synchronizeHandlesErrorListener(dbLinkTable);
    }


    protected void setDTO(CloneDTO newCloneDTO) {
        super.setDTO(newCloneDTO);
        if (dto.getZdbID().startsWith("ZDB-EST") || dto.getZdbID().startsWith("ZDB-CDNA")) {
            relatedGeneTitle.setHTML("<b>Encoded by Gene:</b>");
            relatedGenesBox.setType(MarkerRelationshipEnumTypeGWTHack.GENE_ENCODES_SMALL_SEGMENT);
            relatedGenesBox.setZdbIDThenAbbrev(false);
        } else {
            relatedGeneTitle.setHTML("<b>Clone contains Gene:</b>");
        }
    }

}
