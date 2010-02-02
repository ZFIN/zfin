package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.*;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.dto.TranscriptDTO;
import org.zfin.gwt.root.ui.AbstractListBox;

import java.util.List;

/**
 */
public final class TranscriptEditController extends AbstractFullMarkerEditController<TranscriptDTO>{

    public static final String targetedGenesTitle = "targetedGeneTitle";
    public static final String proteinTitle = "proteinTitle";
    public static final String proteinDiv = "proteinName";
    public static final String newProteinDiv = "newProteinName";
    public static final String rnaDiv = "rnaName";
    public static final String targetedGeneDiv = "targetedGeneName";
    public static final String cloneRelatedDiv = "cloneRelatedName";


    // gui elements
    private final ViewMarkerLabel transcriptViewMarkerLabel = new ViewMarkerLabel("[View Transcript]","/action/marker/transcript-view?zdbID=","Ignore");
    private final TranscriptHeaderEdit transcriptHeaderEdit = new TranscriptHeaderEdit();
    private final RelatedMarkerBox relatedGenesBox =
            new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.GENE_PRODUCES_TRANSCRIPT, false, geneDiv);
    private final RelatedMarkerBox targetedGenesBox =
            new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.TRANSCRIPT_TARGETS_GENE, true, targetedGeneDiv);
    private final RelatedMarkerBox relatedClonesBox =
            new RelatedCloneBox(MarkerRelationshipEnumTypeGWTHack.CLONE_CONTAINS_TRANSCRIPT, false, cloneRelatedDiv);
    private final RelatedEntityBox relatedProteinsBox = new RelatedProteinBox(proteinDiv);
    private final ProteinSequenceArea proteinSequenceArea = new ProteinSequenceArea(newProteinDiv);
    private final NucleotideSequenceArea nucleotideSequenceArea = new NucleotideTranscriptSequenceArea(rnaDiv);
    private final DBLinkTable dbLinkTable = new HandledDBLinkTable();


    public void initGUI() {
        // constructors if not set
        // set listeners
        addListeners();
        // set values
        setValues();
        // load transcript
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                loadDTO();
            }
        });
    }


    protected void addListeners() {
        super.addListeners();
        transcriptHeaderEdit.addChangeListener(new RelatedEntityChangeListener<TranscriptDTO>() {
            public void dataChanged(RelatedEntityEvent<TranscriptDTO> changeEvent) {
                if (false == changeEvent.getDTO().getName().equals(dto.getName())) {
                    if (null == previousNamesBox.validateNewRelatedEntity(changeEvent.getDTO().getName())) {
                        previousNamesBox.addRelatedEntity(dto.getName(), publicationZdbID);
                    }
                }

                // set to the new one
                dto.copyFrom(changeEvent.getDTO());

                handleTranscriptTypes();
            }
        });


        proteinSequenceArea.addSequenceAddListener(new SequenceAddListener() {
            public void add(SequenceAddEvent sequenceAddEvent) {
                proteinSequenceArea.inactivate();
                String sequenceStatus = proteinSequenceArea.checkSequence();
                if (sequenceStatus != null) {
                    Window.alert(sequenceStatus);
                    proteinSequenceArea.activate();
                    return;
                }
                if (false == dto.getTranscriptType().equals("mRNA")) {
                    Window.alert("Can only add protein if transcript type is mRNA");
                    proteinSequenceArea.activate();
                    return;
                }
                TranscriptRPCService.App.getInstance().addProteinSequence(dto.getZdbID(), sequenceAddEvent.getSequenceDTO().getSequence(),
                        publicationZdbID,
                        sequenceAddEvent.getReferenceDatabaseDTO().getZdbID(),
                        new MarkerEditCallBack<DBLinkDTO>("failed to add sequence: ", proteinSequenceArea) {
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                proteinSequenceArea.activate();
                            }

                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                // already added to database, so just update GUI
                                relatedProteinsBox.addRelatedEntityToGUI(dbLinkDTO);
//                                if(publicationZdbID!=null && publicationZdbID.length()>=16){
//                                    directAttributionTable.addPublication(publicationZdbID) ;
//                                }
                                proteinSequenceArea.resetAndHide();
                                proteinSequenceArea.activate();
                            }
                        });

            }

            public void cancel(SequenceAddEvent sequenceAddEvent) {
                proteinSequenceArea.resetAndHide();
                proteinSequenceArea.activate();
            }

            public void start(SequenceAddEvent sequenceAddEvent) {
                // do nothing here so far
            }
        });

        transcriptViewMarkerLabel.addViewMarkerListeners(new ViewMarkerListener() {
            /**
             * from fogbugz 4357, check direct attribution, nucleotide sequence + attribution
             */
            public void finishedView() {
                if (directAttributionTable.getNumberOfPublications() == 0) {
                    transcriptViewMarkerLabel.setError("Transcript requires attribution");
                    directAttributionTable.setError("Transcript requires attribution");
                }
                else
                if (nucleotideSequenceArea.getNumberOfSequences() == 0) {
                    transcriptViewMarkerLabel.setError("Transcript requires sequence");
                    nucleotideSequenceArea.setError("Transcript requires sequence");
                }
                else
                if (nucleotideSequenceArea.getNumberOfAttributions() == 0) {
                    transcriptViewMarkerLabel.setError("Transcript sequence requires attribution");
                    nucleotideSequenceArea.setError("Transcript sequence requires attribution");
                }
                else
                if(transcriptHeaderEdit.isDirty()){
                    String error = "Name/type/status has unsaved changes.";
                    transcriptViewMarkerLabel.setError(error);
                    transcriptHeaderEdit.setError(error);
                }
                else
                if(noteBox.isDirty() || noteBox.hasDirtyNotes()){
                    String error = "Note changes not saved.";
                    transcriptViewMarkerLabel.setError(error);
                    noteBox.setError(error);
                }
                else
                if(relatedGenesBox.isDirty()){
                    String error = "Gene entry not added.";
                    transcriptViewMarkerLabel.setError(error);
                    relatedGenesBox.setError(error);
                }
                else
                if(relatedClonesBox.isDirty()){
                    String error = "Clone entry not added.";
                    transcriptViewMarkerLabel.setError(error);
                    relatedClonesBox.setError(error);
                }
                else
                if(previousNamesBox.isDirty()){
                    String error = "Alias entry not added.";
                    transcriptViewMarkerLabel.setError(error);
                    previousNamesBox.setError(error);
                }
                else
                if(dbLinkTable.isDirty()){
                    String error = "Supporting sequence not added.";
                    transcriptViewMarkerLabel.setError(error);
                    dbLinkTable.setError(error);
                }
                else {
                    transcriptViewMarkerLabel.continueToViewTranscript();
                }

            }
        });

        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        targetedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        relatedClonesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        relatedProteinsBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        nucleotideSequenceArea.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));
        dbLinkTable.addDBLinkTableListener(new DirectAttributionDBLinkTableListener(directAttributionTable));

        addMarkerLoadListener(new MarkerLoadListener<TranscriptDTO>() {
            public void markerLoaded(MarkerLoadEvent<TranscriptDTO> markerLoadEvent) {
                // ignores DTO? assume already set
                directAttributionTable.setZdbID(dto.getZdbID());
                directAttributionTable.setRecordAttributions(dto.getRecordAttributions());
                noteBox.setDTO(dto);
                transcriptViewMarkerLabel.setDTO(dto);
                transcriptHeaderEdit.setDTO(dto);
                previousNamesBox.setRelatedEntities(dto.getZdbID(), dto.getAliasAttributes());
                relatedProteinsBox.setRelatedEntities(dto.getZdbID(), dto.getRelatedProteinAttributes());
                nucleotideSequenceArea.setMarkerDTO(dto);
                targetedGenesBox.setRelatedEntities(dto.getZdbID(), dto.getTargetedGeneAttributes());
                relatedClonesBox.setRelatedEntities(dto.getZdbID(), dto.getRelatedCloneAttributes());
                relatedGenesBox.setRelatedEntities(dto.getZdbID(), dto.getRelatedGeneAttributes());
                // has to be done in this order, otherwise, its not sure which ones are read only or not
                final List<DBLinkDTO> supportingSequencesLinks = dto.getSupportingSequenceLinks();
                TranscriptRPCService.App.getInstance().getTranscriptSupportingSequencesReferenceDatabases(
                        new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                            public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                                dbLinkTable.setReferenceDatabases(referenceDatabaseDTOs);
                                dbLinkTable.setZdbID(dto.getZdbID());
                                dbLinkTable.setDBLinks(supportingSequencesLinks);
                            }
                        });


            }
        });

        publicationLookupBox.addPublicationChangeListener(transcriptHeaderEdit);
        publicationLookupBox.addPublicationChangeListener(relatedGenesBox);
        publicationLookupBox.addPublicationChangeListener(targetedGenesBox);
        publicationLookupBox.addPublicationChangeListener(relatedClonesBox);
        publicationLookupBox.addPublicationChangeListener(relatedProteinsBox);
        publicationLookupBox.addPublicationChangeListener(nucleotideSequenceArea);
        publicationLookupBox.addPublicationChangeListener(proteinSequenceArea);
        publicationLookupBox.addPublicationChangeListener(dbLinkTable);

        synchronizeHandlesErrorListener(transcriptViewMarkerLabel);
        synchronizeHandlesErrorListener(transcriptHeaderEdit);
        synchronizeHandlesErrorListener(relatedGenesBox);
        synchronizeHandlesErrorListener(relatedClonesBox);
        synchronizeHandlesErrorListener(relatedProteinsBox);
        synchronizeHandlesErrorListener(noteBox);
        synchronizeHandlesErrorListener(nucleotideSequenceArea);
        synchronizeHandlesErrorListener(proteinSequenceArea);
        synchronizeHandlesErrorListener(dbLinkTable);
    }

    protected void setValues() {
        publicationLookupBox.clearPublications();
        publicationLookupBox.addPublication(new PublicationDTO("VEGA Database Links", "ZDB-PUB-030703-1"));
        publicationLookupBox.addPublication(new PublicationDTO("Microarray Expression to Gene Association in ZFIN", "ZDB-PUB-071218-1"));
        publicationLookupBox.addPublication(new PublicationDTO("miRBase", "ZDB-PUB-081217-13"));
        publicationLookupBox.addPublication(new PublicationDTO("Manual Annotation of Genome", "ZDB-PUB-091007-1"));
        publicationLookupBox.addRecentPubs() ;

        noteBox.removeEditMode(NoteBox.EditMode.EXTERNAL);

        TranscriptRPCService.App.getInstance().getTranscriptTypes(
                new MarkerEditCallBack<List<String>>("failed to load types: ") {
                    public void onSuccess(List<String> list) {
                        transcriptHeaderEdit.setTranscriptTypes(list);
                    }
                });


        TranscriptRPCService.App.getInstance().getTranscriptStatuses(
                new MarkerEditCallBack<List<String>>("failed to load statuses: ") {
                    public void onSuccess(List<String> list) {
                        transcriptHeaderEdit.setTranscriptStatuses(list);
                    }
                });

        TranscriptRPCService.App.getInstance().getTranscriptEditAddProteinSequenceReferenceDatabases(
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("failed to load  sequence databases: ") {
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        AbstractListBox databaseListBoxWrapper = proteinSequenceArea.getDatabaseListBoxWrapper();
                        databaseListBoxWrapper.addItem(AbstractListBox.EMPTY_CHOICE, AbstractListBox.NULL_STRING);
                        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs) {
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getBlastName(), referenceDatabaseDTO.getZdbID());
                        }
                        proteinSequenceArea.activate();
                    }

                });
    }

    void handleTranscriptTypes() {
        boolean proteinsVisible = dto.getTranscriptType().equals("mRNA"); //messenger, not micro!
        proteinSequenceArea.setVisible(proteinsVisible);
        relatedProteinsBox.setVisible(proteinsVisible);
        RootPanel.get(proteinTitle).setVisible(proteinsVisible);

        boolean targetedGenesVisible = dto.getTranscriptType().equals("miRNA");
        RootPanel.get(targetedGenesTitle).setVisible(targetedGenesVisible);
        targetedGenesBox.setVisible(targetedGenesVisible);

        TranscriptRPCService.App.getInstance().getTranscriptAddableNucleotideSequenceReferenceDatabases(dto,
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        AbstractListBox databaseListBoxWrapper = nucleotideSequenceArea.getDatabaseListBoxWrapper();
                        databaseListBoxWrapper.clear();
                        databaseListBoxWrapper.addItem(AbstractListBox.EMPTY_CHOICE, AbstractListBox.NULL_STRING);
                        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs) {
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getBlastName(), referenceDatabaseDTO.getZdbID());
                        }
                        nucleotideSequenceArea.activate();
                    }
                });
    }

    protected void loadDTO() {
        try {
            // load properties
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            String zdbID = transcriptDictionary.get(LOOKUP_ZDBID);
            TranscriptRPCService.App.getInstance().getTranscriptForZdbID(zdbID,
                    new MarkerEditCallBack<TranscriptDTO>("failed to find zdbID: " + zdbID + " ") {
                        public void onSuccess(TranscriptDTO transcriptDTO) {
                            setDTO(transcriptDTO);
                            handleTranscriptTypes();
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }



}
