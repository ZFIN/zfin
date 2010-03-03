package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import org.zfin.gwt.marker.event.*;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.ui.PublicationSessionKey;

/**
 */
public final class AntibodyEditController extends AbstractFullMarkerEditController<AntibodyDTO> {


    // gui elements
    private final ViewClickLabel antibodyViewClickLabel = new ViewClickLabel("[View Antibody]","/action/antibody/detail?antibody.zdbID=","Discard");
    private final AntibodyHeaderEdit antibodyHeaderEdit = new AntibodyHeaderEdit();
    private final AntibodyBox antibodyBox = new AntibodyBox();
    private final RelatedMarkerBox relatedGenesBox = new RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY, true, geneDiv);
    private final SupplierNameList supplierNameList = new SupplierNameList();

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

        markerNoteBox.setDefaultEditMode(MarkerNoteBox.EditMode.EXTERNAL);

        publicationLookupBox.clearPublications();
        publicationLookupBox.addPublication(new PublicationDTO("Antibody Data Submissions","ZDB-PUB-080117-1" ));
        publicationLookupBox.addPublication(new PublicationDTO("Manually Curated Data","ZDB-PUB-020723-5" ));
        publicationLookupBox.addPublication(new PublicationDTO("Antibody information from supplier","ZDB-PUB-081107-1"));
        publicationLookupBox.setKey(PublicationSessionKey.ANTIBODY);
        publicationLookupBox.getRecentPubs() ;

    }

    protected void loadDTO() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
            String zdbID = transcriptDictionary.get(LOOKUP_ZDBID);
            try {
                String defaultPubzdbID = transcriptDictionary.get("antibodyDefPubZdbID");
                if(defaultPubzdbID!=null){
                    publicationLookupBox.publicationChanged(new PublicationChangeEvent(defaultPubzdbID));
                }
            } catch (Exception e) {
                // no default pub, thats okay.
            }

            AntibodyRPCService.App.getInstance().getAntibodyForZdbID(zdbID,
                    new MarkerEditCallBack<AntibodyDTO>("failed to find antibody: ") {
                        public void onSuccess(AntibodyDTO dto) {
                            setDTO(dto);
                        }
                    });
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    protected void addListeners() {
        super.addListeners();

        antibodyViewClickLabel.addViewClickedListeners(new ViewClickedListener(){
            @Override
            public void finishedView() {
                if(antibodyBox.isDirty()){
                    String error = "Antibody has unsaved data change(s).";
                    antibodyViewClickLabel.setError(error);
                    antibodyBox.setError(error);
                }
                else
                if(antibodyHeaderEdit.isDirty()){
                    String error = "Antibody has unsaved name change.";
                    antibodyViewClickLabel.setError(error);
                    antibodyHeaderEdit.setError(error);
                }
                else
                if(markerNoteBox.isDirty() || markerNoteBox.hasDirtyNotes()){
                    String error = "Antibody has unsaved note change(s).";
                    antibodyViewClickLabel.setError(error);
                    markerNoteBox.setError(error);
                }
                else
                if(previousNamesBox.isDirty()){
                    String error = "Alias entry not added.";
                    antibodyViewClickLabel.setError(error);
                    previousNamesBox.setError(error);
                }
                else
                if(relatedGenesBox.isDirty()){
                    String error = "Gene entry not added.";
                    antibodyViewClickLabel.setError(error);
                    relatedGenesBox.setError(error);
                }
                else{
                    antibodyViewClickLabel.continueToViewTranscript();
                }

            }
        });

//        antibodyHeaderEdit.setPreviousNamesBox(previousNamesBox);
        antibodyHeaderEdit.addChangeListener(new RelatedEntityChangeListener<AntibodyDTO>() {
            public void dataChanged(RelatedEntityEvent<AntibodyDTO> changeEvent) {
                if (false == changeEvent.getDTO().getName().equals(dto.getName())) {
                    if (null == previousNamesBox.validateNewRelatedEntity(changeEvent.getDTO().getName())) {
                        previousNamesBox.addRelatedEntity(dto.getName(), publicationZdbID);
                    }
                }
                dto.copyFrom(changeEvent.getDTO());
            }
        });



        // direct attribution listeners
        relatedGenesBox.addRelatedEntityCompositeListener(new DirectAttributionAddsRelatedEntityListener(directAttributionTable));

        addMarkerLoadListener(new MarkerLoadListener<AntibodyDTO>() {
            public void markerLoaded(MarkerLoadEvent<AntibodyDTO> markerLoadEvent) {
                AntibodyDTO dto = markerLoadEvent.getMarkerDTO();
                directAttributionTable.setZdbID(dto.getZdbID());
                directAttributionTable.setRecordAttributions(dto.getRecordAttributions());
                antibodyViewClickLabel.setDTO(dto);
                antibodyHeaderEdit.setDTO(dto);
                previousNamesBox.setRelatedEntities(dto.getZdbID(), dto.getAliasAttributes());
                antibodyBox.setDTO(dto);
                markerNoteBox.setDTO(dto);
                supplierNameList.setDTO(dto);
                relatedGenesBox.setRelatedEntities(dto.getZdbID(), dto.getRelatedGeneAttributes());
            }
        });


        publicationLookupBox.addPublicationChangeListener(antibodyHeaderEdit);
        publicationLookupBox.addPublicationChangeListener(relatedGenesBox);
        publicationLookupBox.addPublicationChangeListener(markerNoteBox);

        synchronizeHandlesErrorListener(antibodyHeaderEdit);
        synchronizeHandlesErrorListener(relatedGenesBox);
        synchronizeHandlesErrorListener(antibodyBox);
        synchronizeHandlesErrorListener(markerNoteBox);
        synchronizeHandlesErrorListener(antibodyViewClickLabel);
    }


    protected void setDTO(AntibodyDTO antibodyDTO) {
        super.setDTO(antibodyDTO);
        relatedGenesBox.setType(MarkerRelationshipEnumTypeGWTHack.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        relatedGenesBox.setZdbIDThenAbbrev(false);
    }

}
