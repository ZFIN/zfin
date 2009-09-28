package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.i18n.client.Dictionary;

import java.util.List;
import java.util.ArrayList;

import org.zfin.marker.presentation.dto.TranscriptDTO;
import org.zfin.marker.presentation.dto.*;
import org.zfin.marker.presentation.event.*;

/**
 */
public final class TranscriptEditController implements PublicationChangeListener, HandlesError{

    private String targetedGenesTitle = "targetedGeneTitle";
    private String proteinTitle = "proteinTitle";
    private String curatorNoteDiv = "curatorNoteName";
    private String publicNoteDiv = "publicNoteName";


    // gui elements
    private ViewTranscriptLabel viewTranscriptLabel = new ViewTranscriptLabel("viewTranscript");
    private TranscriptHeaderEdit transcriptHeaderEdit = new TranscriptHeaderEdit("markerName");
    private DirectAttributionTable directAttributionTable = new HandledDirectAttributionTable("directAttributionName") ;;
    private PreviousNamesBox previousNamesBox = new PreviousNamesBox("aliasName") ;
    private RelatedMarkerBox relatedGenesBox =
            new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.GENE_PRODUCES_TRANSCRIPT,false,"geneName") ;
    private RelatedMarkerBox targetedGenesBox =
            new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.TRANSCRIPT_TARGETS_GENE,true,"targetedGeneName") ;
    private RelatedMarkerBox relatedClonesBox  =
            new RelatedCloneBox(MarkerRelationshipEnumTypeGWTHack.CLONE_CONTAINS_TRANSCRIPT,false,"cloneRelatedName") ;
    private RelatedEntityBox relatedProteinsBox  = new RelatedProteinBox("proteinName");
    private ProteinSequenceArea proteinSequenceArea = new ProteinSequenceArea("newProteinName");
    private NucleotideSequenceArea nucleotideSequenceArea = new NucleotideTranscriptSequenceArea("rnaName");
    private PublicationLookupBox publicationLookupBox = new PublicationLookupBox("publicationName");
    private DBLinkTable supportingSequencesTable = new HandledDBLinkTable("dbLinksName") ;
    private CuratorNoteBox curatorNoteBox = new CuratorNoteBox(curatorNoteDiv) ;
    private PublicNoteBox publicNoteBox = new PublicNoteBox(publicNoteDiv) ;

    // internal data
    private String publicationZdbID;
    private String zdbID;
    private String curatorID;
    private TranscriptDTO transcriptDTO;

    // listeners
    private List<MarkerLoadListener> markerLoadListeners = new ArrayList<MarkerLoadListener>() ;
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>() ;


    // lookup
    private static final String LOOKUP_TRANSCRIPT_ZDBID = "zdbID";
    private static final String LOOKUP_CURATOR_ZDBID = "curatorID";

    public void initGUI() {
        // constructors if not set
        // set listeners
        addListeners();
        // set values
        setValues();
        // load transcript
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                loadTranscript();
            }
        });
    }


    private void addListeners() {
        transcriptHeaderEdit.addTranscriptListeners(new TranscriptChangeListener(){
            public void changeTranscriptProperties(TranscriptChangeEvent transcriptChangeEvent) {
                if(false==transcriptChangeEvent.getTranscriptDTO().getName().equals(getTranscriptDTO().getName())){
                    if(null==previousNamesBox.validateNewRelatedEntity(transcriptChangeEvent.getTranscriptDTO().getName())){
                        previousNamesBox.addRelatedEntity(getTranscriptDTO().getName(),publicationZdbID);
                    }
                }
                // set to the new one
                transcriptDTO.copyFrom(transcriptChangeEvent.getTranscriptDTO());

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
                if (false == transcriptDTO.getTranscriptType().equals("mRNA")) {
                    Window.alert("Can only add protein if transcript type is mRNA");
                    proteinSequenceArea.activate();
                    return;
                }
                TranscriptRPCService.App.getInstance().addProteinSequence(zdbID, sequenceAddEvent.getSequenceDTO().getSequence(),
                        publicationZdbID,
                        sequenceAddEvent.getReferenceDatabaseDTO().getZdbID(),
                        new MarkerEditCallBack<DBLinkDTO>("failed to add sequence: ",proteinSequenceArea) {
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

        viewTranscriptLabel.addViewTranscriptListeners(new ViewTranscriptListener(){
            /**
             * from fogbugz 4357, check direct attribution, nucleotide sequence + attribution
             */
            public void finishedView() {
                if(directAttributionTable.getNumberOfPublications()==0){
                    viewTranscriptLabel.setError("Transcript requires attribution");
                    directAttributionTable.setError("Transcript requires attribution");
                }
                else
                if(nucleotideSequenceArea.getNumberOfSequences()==0){
                    viewTranscriptLabel.setError("Transcript requires sequence");
                    nucleotideSequenceArea.setError("Transcript requires sequence");
                }
                else
                if(nucleotideSequenceArea.getNumberOfAttributions()==0){
                    viewTranscriptLabel.setError("Transcript sequence requires attribution");
                    nucleotideSequenceArea.setError("Transcript sequence requires attribution");
                }
                else{
                    viewTranscriptLabel.continueToViewTranscript();
                }
            }
        });

        previousNamesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable)) ;
        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable)) ;
        targetedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable)) ;
        relatedClonesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable)) ;
        relatedProteinsBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable)) ;
        nucleotideSequenceArea.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable)) ;

        addMarkerDomainListener(new MarkerLoadListener(){
            public void markerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
                directAttributionTable.setZdbID(zdbID);
                directAttributionTable.setRecordAttributions(transcriptDTO.getRecordAttributions());
                curatorNoteBox.setZdbID(zdbID);
                viewTranscriptLabel.setZdbID(zdbID);
                curatorNoteBox.setCuratorZdbID(curatorID);
                curatorNoteBox.setNotes(transcriptDTO.getCuratorNotes());
                publicNoteBox.setZdbID(zdbID);
                publicNoteBox.setCuratorZdbID(curatorID);
                publicNoteBox.setNotes(transcriptDTO.getPublicNotes());
                transcriptHeaderEdit.setTranscriptDomain(transcriptDTO);
                previousNamesBox.setRelatedEntities(transcriptDTO.getZdbID(),transcriptDTO.getAliasAttributes());
                relatedProteinsBox.setRelatedEntities(transcriptDTO.getZdbID(),transcriptDTO.getRelatedProteinAttributes());
                nucleotideSequenceArea.setMarkerDTO(transcriptDTO);
                targetedGenesBox.setRelatedEntities(transcriptDTO.getZdbID(),transcriptDTO.getTargetedGeneAttributes());
                relatedClonesBox.setRelatedEntities(transcriptDTO.getZdbID(),transcriptDTO.getRelatedCloneAttributes());
                relatedGenesBox.setRelatedEntities(transcriptDTO.getZdbID(),transcriptDTO.getRelatedGeneAttributes());
                // has to be done in this order, otherwise, its not sure which ones are read only or not
                supportingSequencesTable.addDBLinkTableListener(new DirectAttributionDBLinkTableListener(directAttributionTable));
                final List<DBLinkDTO> supportingSequencesLinks = transcriptDTO.getSupportingSequenceLinks() ;
                TranscriptRPCService.App.getInstance().getTranscriptSupportingSequencesReferenceDatabases(
                        new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                            public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                                supportingSequencesTable.setReferenceDatabases(referenceDatabaseDTOs);
                                supportingSequencesTable.setZdbID(zdbID);
                                supportingSequencesTable.setDBLinks(supportingSequencesLinks);
                            }
                        });


            }
        }) ;

        publicationLookupBox.addPublicationChangeListener(transcriptHeaderEdit);
        publicationLookupBox.addPublicationChangeListener(previousNamesBox);
        publicationLookupBox.addPublicationChangeListener(directAttributionTable);
        publicationLookupBox.addPublicationChangeListener(relatedGenesBox);
        publicationLookupBox.addPublicationChangeListener(targetedGenesBox);
        publicationLookupBox.addPublicationChangeListener(relatedClonesBox);
        publicationLookupBox.addPublicationChangeListener(relatedProteinsBox);
        publicationLookupBox.addPublicationChangeListener(supportingSequencesTable);
        publicationLookupBox.addPublicationChangeListener(nucleotideSequenceArea);
        publicationLookupBox.addPublicationChangeListener(proteinSequenceArea);
        publicationLookupBox.addPublicationChangeListener(this);

        addHandlesErrorListener(transcriptHeaderEdit);
        transcriptHeaderEdit.addHandlesErrorListener(this);
        addHandlesErrorListener(previousNamesBox);
        previousNamesBox.addHandlesErrorListener(this);
        addHandlesErrorListener(directAttributionTable);
        directAttributionTable.addHandlesErrorListener(this);
        addHandlesErrorListener(relatedGenesBox);
        relatedGenesBox.addHandlesErrorListener(this);
        addHandlesErrorListener(targetedGenesBox);
        targetedGenesBox.addHandlesErrorListener(this);
        addHandlesErrorListener(relatedClonesBox);
        relatedClonesBox.addHandlesErrorListener(this);
        addHandlesErrorListener(relatedProteinsBox);
        relatedProteinsBox.addHandlesErrorListener(this);
        addHandlesErrorListener(supportingSequencesTable);
        supportingSequencesTable.addHandlesErrorListener(this);
        addHandlesErrorListener(nucleotideSequenceArea);
        nucleotideSequenceArea.addHandlesErrorListener(this);
        addHandlesErrorListener(proteinSequenceArea);
        proteinSequenceArea.addHandlesErrorListener(this);
    }

    private void setValues() {
        publicationLookupBox.clearPublications();
        publicationLookupBox.addPublication(new PublicationAbstractDTO("VEGA Database Links", "ZDB-PUB-030703-1"));
        publicationLookupBox.addPublication(new PublicationAbstractDTO("Microarray Expression to Gene Association in ZFIN", "ZDB-PUB-071218-1"));
        publicationLookupBox.addPublication(new PublicationAbstractDTO("miRBase", "ZDB-PUB-081217-13"));


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
                        EasyListBox databaseListBoxWrapper = proteinSequenceArea.getDatabaseListBoxWrapper();
                        databaseListBoxWrapper.addItem(EasyListBox.EMPTY_CHOICE, EasyListBox.NULL_STRING);
                        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs) {
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getBlastName(), referenceDatabaseDTO.getZdbID());
                        }
                        proteinSequenceArea.activate();
                    }

                });
    }

    protected void handleTranscriptTypes() {
        boolean proteinsVisible = transcriptDTO.getTranscriptType().equals("mRNA"); //messenger, not micro!
        proteinSequenceArea.setVisible(proteinsVisible);
        relatedProteinsBox.setVisible(proteinsVisible);
        RootPanel.get(proteinTitle).setVisible(proteinsVisible);

        boolean targetedGenesVisible = transcriptDTO.getTranscriptType().equals("miRNA");
        RootPanel.get(targetedGenesTitle).setVisible(targetedGenesVisible);
        targetedGenesBox.setVisible(targetedGenesVisible);

        TranscriptRPCService.App.getInstance().getTranscriptAddableNucleotideSequenceReferenceDatabases(transcriptDTO,
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("error loading available sequence databases: ") {
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        EasyListBox databaseListBoxWrapper = nucleotideSequenceArea.getDatabaseListBoxWrapper();
                        databaseListBoxWrapper.clear();
                        databaseListBoxWrapper.addItem(EasyListBox.EMPTY_CHOICE, EasyListBox.NULL_STRING);
                        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs) {
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getBlastName(), referenceDatabaseDTO.getZdbID());
                        }
                        nucleotideSequenceArea.activate();
                    }
                });
    }

    protected void loadTranscript() {
        try {
            // load properties
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            zdbID = transcriptDictionary.get(LOOKUP_TRANSCRIPT_ZDBID);
            curatorID = transcriptDictionary.get(LOOKUP_CURATOR_ZDBID);
            TranscriptRPCService.App.getInstance().getTranscriptForZdbID(zdbID,
                    new MarkerEditCallBack<TranscriptDTO>("failed to find zdbID: " + zdbID + " ") {
                        public void onSuccess(TranscriptDTO transcriptDTO) {
                            setTranscriptDomain(transcriptDTO);
                            handleTranscriptTypes();
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }


    public void setTranscriptDomain(TranscriptDTO transcriptDTO) {
        this.transcriptDTO = transcriptDTO;
        zdbID = this.transcriptDTO.getZdbID();
        fireMarkerDomainLoaded(new MarkerLoadEvent(transcriptDTO));
    }

    public void publicationChanged(PublicationChangeEvent event) {
        publicationZdbID = event.getPublication() ;
    }

    public TranscriptDTO getTranscriptDTO() {
        return transcriptDTO;
    }

    public void setTranscriptDTO(TranscriptDTO transcriptDTO) {
        this.transcriptDTO = transcriptDTO;
    }

    public void addMarkerDomainListener(MarkerLoadListener markerLoadListener){
        markerLoadListeners.add(markerLoadListener) ;
    }

    public void fireMarkerDomainLoaded(MarkerLoadEvent markerLoadEvent){
        for(MarkerLoadListener markerLoadListener : markerLoadListeners){
            markerLoadListener.markerDomainLoaded(markerLoadEvent);
        }
    }

    public void fireEventSuccess(){
        for(HandlesError handlesError: handlesErrorListeners){
            handlesError.clearError();
        }
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError) ;
    }

    public void setError(String message) {
        // not doing anything with this
    }

    public void clearError() {
        fireEventSuccess();
    }
}
