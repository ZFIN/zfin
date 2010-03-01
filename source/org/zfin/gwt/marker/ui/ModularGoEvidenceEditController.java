package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import org.zfin.gwt.marker.event.RelatedEntityChangeListener;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.GoPubEnum;
import org.zfin.gwt.root.dto.PublicationDTO;

/**
 * A GWT class for adding proteins to genes on the markerview.apg page.
 */
public final class ModularGoEvidenceEditController extends AbstractRelatedEntityEditController<GoEvidenceDTO> {

    private ViewClickLabel<GoEvidenceDTO> viewClickLabel = new ViewGoEvidenceEditClickLabel<GoEvidenceDTO>("[Validate]", "","Ignore");
    private final PublicationLookupBox publicationLookupBox = new PublicationLookupBox();
    private final StandAloneGoEvdidenceHeaderEdit headerEdit = new StandAloneGoEvdidenceHeaderEdit();
    private final HandledInferenceListBox inferenceListBox = new HandledInferenceListBox();
    private final GoDetailsBox goTextArea = new GoDetailsBox();


    public void initGUI() {
        loadDTO();
        setValues();
        addListeners();
    }

    protected void setValues() {
        publicationLookupBox.clearPublications();
        publicationLookupBox.addPublication(new PublicationDTO(GoPubEnum.INTERPRO));
        publicationLookupBox.addPublication(new PublicationDTO(GoPubEnum.SPKW));
        publicationLookupBox.addPublication(new PublicationDTO(GoPubEnum.EC));
        publicationLookupBox.addPublication(new PublicationDTO(GoPubEnum.ROOT));
        publicationLookupBox.addPublication(new PublicationDTO("ISS from Ref. Genome", "ZDB-PUB-071010-1"));
        publicationLookupBox.addPublication(new PublicationDTO("ISS from Manually Curated Orthology", "ZDB-PUB-040216-1"));
        publicationLookupBox.addRecentPubs() ;
    }

    protected void addListeners() {
        publicationLookupBox.addPublicationChangeListener(headerEdit);

        synchronizeHandlesErrorListener( viewClickLabel );
        synchronizeHandlesErrorListener( headerEdit );
        synchronizeHandlesErrorListener( inferenceListBox );

        viewClickLabel.addViewClickedListeners(new ViewClickedListener(){
            @Override
            public void finishedView() {
                validateGoEvidence() ;
            }
        });

        headerEdit.addChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>(){
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                setDTO(dataChangedEvent.getDTO());
                publicationLookupBox.addRecentPublicationDTO(dataChangedEvent.getDTO().getPublicationZdbID());
            }
        });

        inferenceListBox.addGoTermChangeListeners(new RelatedEntityChangeListener<GoEvidenceDTO>(){
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                goTextArea.setDTO(dataChangedEvent.getDTO());
            }
        });

        inferenceListBox.addRelatedEnityChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>(){
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                setDTO(dataChangedEvent.getDTO());
            }
        });

        inferenceListBox.addRelatedEnityChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>(){
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                setDTO(dataChangedEvent.getDTO());
            }
        });
    }

    private void validateGoEvidence() {
        if(headerEdit.isDirty()) {
            setError("Header has unsaved changes.");
            headerEdit.setError("Header has unsaved changes.");
            return ;
        }
//        if(noteBox.isDirty() ){
//            setError("Notes has unsaved changes.");
//            noteBox.setError("Notes has unsaved changes.");
//            return ;
//        }
//        inferenceListBox.setDTO(dto);
//        goTextArea.setDTO(dto);
        GoEvidenceValidator.validate(viewClickLabel,dto) ;
    }


    protected void loadDTO() {
// load properties
        Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
        String zdbID = transcriptDictionary.get(LOOKUP_ZDBID);
        zdbID = zdbID.substring("Alternate".length());

        TermRPCService.App.getInstance().getMarkerGoTermEvidenceDTO(zdbID,
                new MarkerEditCallBack<GoEvidenceDTO>("failed to find markergoevidence: ") {
                    public void onSuccess(GoEvidenceDTO returnDTO) {
                        setDTO(returnDTO);
                    }
                });
    }

    @Override
    void setDTO(GoEvidenceDTO dto) {
        if(dto==null) return ;
        super.setDTO(dto);
//        noteBox.setDTO(dto);
        headerEdit.setDTO(dto);
        inferenceListBox.setDTO(dto);
        goTextArea.setDTO(dto);
        validateGoEvidence();
    }

   private class ViewGoEvidenceEditClickLabel<T extends GoEvidenceDTO> extends ViewClickLabel<T> {
        public ViewGoEvidenceEditClickLabel(String s, String s1, String s2) {
            super(s,s1,s2);
        }

       @Override
       public void setError(String message) {
           errorLabel.setText(message);
           messageLabel.setHTML("");
           messageLabel.setVisible(false);
       }
   }
}