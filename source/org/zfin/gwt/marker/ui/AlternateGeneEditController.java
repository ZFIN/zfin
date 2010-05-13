package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import org.zfin.gwt.marker.event.DirectAttributionDBLinkTableListener;
import org.zfin.gwt.marker.event.MarkerLoadEvent;
import org.zfin.gwt.marker.event.MarkerLoadListener;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerRPCService;
import org.zfin.gwt.root.ui.PublicationSessionKey;

import java.util.List;


/**
 */
public final class AlternateGeneEditController extends AbstractFullMarkerEditController<MarkerDTO> {

    // gui elements
    private ViewClickLabel<MarkerDTO> geneViewClickLabel = new ViewClickLabel<MarkerDTO>("[View Gene]", "/action/marker/gene-view?zdbID=", "Discard");
    private GeneHeaderEdit geneHeaderEdit = new GeneHeaderEdit(headerDiv);
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
        publicationLookupBox.setKey(PublicationSessionKey.GENE);
        publicationLookupBox.getRecentPubs();

        markerNoteBox.removeEditMode(MarkerNoteBox.EditMode.EXTERNAL);
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
                markerNoteBox.setDTO(markerDTO);
                geneViewClickLabel.setDTO(markerDTO);

                final List<DBLinkDTO> supportingSequenceLinks = markerDTO.getSupportingSequenceLinks();
                MarkerRPCService.App.getInstance().getGeneDBLinkAddReferenceDatabases(dto.getZdbID(),
                        new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                            public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabases) {
                                dbLinkTable.setReferenceDatabases(referenceDatabases);
                                dbLinkTable.setZdbID(dto.getZdbID());
                                dbLinkTable.setDBLinks(supportingSequenceLinks);
                            }
                        });
            }
        });

        geneViewClickLabel.addViewClickedListeners(new ViewClickedListener() {
            @Override
            public void finishedView() {
                if (geneHeaderEdit.isDirty()) {
                    String error = "Name / type has unsaved change.";
                    geneHeaderEdit.setError(error);
                    geneViewClickLabel.setError(error);
                } else if (markerNoteBox.isDirty() || markerNoteBox.hasDirtyNotes()) {
                    String error = "A note not saved.";
                    markerNoteBox.setError(error);
                    geneViewClickLabel.setError(error);
                } else if (previousNamesBox.isDirty()) {
                    String error = "Alias entry not added.";
                    geneViewClickLabel.setError(error);
                    previousNamesBox.setError(error);
                } else if (dbLinkTable.isDirty()) {
                    String error = "Sequence not added.";
                    geneViewClickLabel.setError(error);
                    dbLinkTable.setError(error);
                } else {
                    geneViewClickLabel.continueToViewTranscript();
                }

            }
        });


        publicationLookupBox.addPublicationChangeListener(dbLinkTable);
        publicationLookupBox.addPublicationChangeListener(geneHeaderEdit);

        synchronizeHandlesErrorListener(geneViewClickLabel);
        synchronizeHandlesErrorListener(geneHeaderEdit);
        synchronizeHandlesErrorListener(markerNoteBox);
        synchronizeHandlesErrorListener(dbLinkTable);
    }


}