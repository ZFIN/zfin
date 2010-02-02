package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import org.zfin.gwt.marker.event.*;
import org.zfin.gwt.root.dto.*;


/**
 */
public final class AlternateGeneEditController extends AbstractFullMarkerEditController<MarkerDTO>{

    // gui elements
    private ViewMarkerLabel<MarkerDTO> geneViewMarkerLabel = new ViewMarkerLabel<MarkerDTO>("[View Gene]", "/action/marker/gene-view?zdbID=","Discard");
    private GeneHeaderEdit geneHeaderEdit = new GeneHeaderEdit(headerDiv);
    private SupplierNameList supplierNameList = new SupplierNameList();
    private DBLinkTable dbLinkTable = new HandledDBLinkTable();


    public void initGUI() {

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
        publicationLookupBox.addRecentPubs() ;

        noteBox.removeEditMode(NoteBox.EditMode.EXTERNAL);
    }

    protected void loadDTO() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            String zdbID = transcriptDictionary.get(LOOKUP_ZDBID);

            // can be removed if this is the prime once
            zdbID = zdbID.substring("Alternate".length());

            MarkerRPCService.App.getInstance().getGeneForZdbID(zdbID,
                    new MarkerEditCallBack<MarkerDTO>("failed to find gene: ") {
                        public void onSuccess(MarkerDTO returnDTO) {
                            setDTO(returnDTO);
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    protected void addListeners() {
        super.addListeners();

        geneHeaderEdit.addChangeListener(new RelatedEntityChangeListener<MarkerDTO>() {
            public void dataChanged(RelatedEntityEvent<MarkerDTO> changeEvent) {
                if (false == changeEvent.getDTO().getName().equals(dto.getName())) {
                    if (null == previousNamesBox.validateNewRelatedEntity(changeEvent.getDTO().getName())) {
                        previousNamesBox.addRelatedEntity(dto.getName(), publicationZdbID);
                    }
                }
                dto = changeEvent.getDTO().deepCopy();
            }
        });

        // direct attribution listeners
        dbLinkTable.addDBLinkTableListener(new DirectAttributionDBLinkTableListener(directAttributionTable));

        addMarkerLoadListener(new MarkerLoadListener<MarkerDTO>() {
            public void markerLoaded(MarkerLoadEvent<MarkerDTO> markerLoadEvent) {
                final MarkerDTO markerDTO = markerLoadEvent.getMarkerDTO();
                directAttributionTable.setZdbID(markerDTO.getZdbID());
                directAttributionTable.setRecordAttributions(markerDTO.getRecordAttributions());
                geneHeaderEdit.setDTO(markerDTO);
                previousNamesBox.setRelatedEntities(markerDTO.getZdbID(), dto.getAliasAttributes());
                noteBox.setDTO(markerDTO);
                geneViewMarkerLabel.setDTO(markerDTO);
                supplierNameList.setDomain(markerDTO);

//                final List<DBLinkDTO> supportingSequenceLinks = markerDTO.getSupportingSequenceLinks();
//                MarkerRPCService.App.getInstance().getMarkerDBLinkAddReferenceDatabases(dto.getZdbID(),
//                        new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
//                            public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabases) {
//                                dbLinkTable.setReferenceDatabases(referenceDatabases);
//                                dbLinkTable.setZdbID(dto.getZdbID());
//                                dbLinkTable.setDBLinks(supportinSequenceLinks);
//                            }
//                        });
            }
        });

        geneViewMarkerLabel.addViewMarkerListeners(new ViewMarkerListener(){
            @Override
            public void finishedView() {
                if(geneHeaderEdit.isDirty()){
                    String error = "Name / type has unsaved change.";
                    geneHeaderEdit.setError(error);
                    geneViewMarkerLabel.setError(error);
                }
                else
                if(noteBox.isDirty() || noteBox.hasDirtyNotes()){
                    String error = "A note not saved.";
                    noteBox.setError(error);
                    geneViewMarkerLabel.setError(error);
                }
                else
                if(previousNamesBox.isDirty()){
                    String error = "Alias entry not added.";
                    geneViewMarkerLabel.setError(error);
                    previousNamesBox.setError(error);
                }
                else
                if(dbLinkTable.isDirty()){
                    String error = "Sequence not added.";
                    geneViewMarkerLabel.setError(error);
                    dbLinkTable.setError(error);
                }
                else {
                    geneViewMarkerLabel.continueToViewTranscript();
                }

            }
        });


        publicationLookupBox.addPublicationChangeListener(dbLinkTable);
        publicationLookupBox.addPublicationChangeListener(geneHeaderEdit);

        synchronizeHandlesErrorListener(geneViewMarkerLabel);
        synchronizeHandlesErrorListener(geneHeaderEdit);
        synchronizeHandlesErrorListener(noteBox);
        synchronizeHandlesErrorListener(dbLinkTable);
    }


}