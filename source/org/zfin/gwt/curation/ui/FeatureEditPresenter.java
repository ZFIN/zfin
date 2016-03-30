package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.ui.FeatureEditCallBack;

import java.util.List;

public class FeatureEditPresenter extends AbstractFeaturePresenter {

    private FeatureEditView view;

    public FeatureEditPresenter(FeatureEditView view, String publicationID) {
        super(view, publicationID);
        if (publicationID == null)
            throw new RuntimeException("NO pub ID found");
        this.view = view;
        dto = new FeatureDTO();
        dto.setPublicationZdbID(publicationID);
    }

    public void go() {
        super.go();
        loadFeaturesForPub();
    }

    public void loadFeaturesForPub() {
        FeatureRPCService.App.getInstance().getFeaturesForPub(publicationID,
                new FeatureEditCallBack<List<FeatureDTO>>("Failed to find features for pub: " + publicationID, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        view.featureEditList.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(List<FeatureDTO> results) {
                        view.featureEditList.clear();
                        view.featureEditList.addItem("");
                        for (FeatureDTO dto : results) {
                            if (view.featureEditList.setIndexForValue(dto.getZdbID()) < 0) {
                                view.featureEditList.addItem(dto.getName(), dto.getZdbID());
                            }
                        }

                        view.featureEditList.setIndexForValue(dto.getZdbID());
                        view.featureEditList.setEnabled(true);
                        view.featureAliasList.setDTO(dto);
                        view.featureSequenceList.setDTO(dto);
                        revertGUI();
                    }
                });
    }


    public void onFeatureSelectionChange(String featureID) {
        FeatureRPCService.App.getInstance().getFeature(featureID, new FeatureEditCallBack<FeatureDTO>("Failed to remove attribution: ", this) {
            public void onSuccess(FeatureDTO featureDTO) {
                featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
                dto = featureDTO;
                loadFeaturesForPub();
                view.removeFeatureLink.setUrl("/action/infrastructure/deleteRecord/" + dto.getZdbID());
                view.removeFeatureLink.setTitle("Delete Feature " + dto.getZdbID());


/*
                updateForFeatureType();
*/
            }
        });

    }


    public boolean handleDirty() {
        view.featureDisplayName.setText(FeatureValidationService.generateFeatureDisplayName(createDTOFromGUI()));
        boolean isDirty = isDirty();
        view.saveButton.setEnabled(isDirty && FeatureValidationService.isFeatureSaveable(createDTOFromGUI()));
////        revertButton.setEnabled(isDirty);
        return true;
    }

    public boolean isDirty() {
        boolean isDirty = false;
        // this displays most changes
        // alias and notes are done automatically?
        isDirty = view.featureDisplayName.isDirty(dto.getName()) || isDirty;
        if (view.knownInsertionCheckBox.getValue() != dto.getKnownInsertionSite()) {
            view.saveButton.setEnabled(true);
            isDirty = true;
        }
        if (view.featureTypeBox.getSelected() != null && dto.getFeatureType() != null) {
            isDirty = view.featureTypeBox.isDirty(dto.getFeatureType().name()) || isDirty;
        } else if ((view.featureTypeBox.getSelected() == null && dto.getFeatureType() != null)
                || (view.featureTypeBox.getSelected() != null && dto.getFeatureType() == null)
                ) {
            isDirty = view.featureTypeBox.setDirty(true);
        }
        isDirty = (view.mutageeBox.isDirty(dto.getMutagee()) || isDirty);
        isDirty = (view.mutagenBox.isDirty(dto.getMutagen()) || isDirty);
        isDirty = (view.labDesignationBox.isDirty(dto.getLabPrefix()) || isDirty);
        isDirty = (view.lineNumberBox.isDirty(dto.getLineNumber()) || isDirty);
        isDirty = (view.labOfOriginBox.isDirty(dto.getLabOfOrigin()) || isDirty);
////        isDirty = (view.featureSequenceBox.isDirty(dto.getFeatureSequence()) || isDirty) ;
        return isDirty;
    }

    public FeatureDTO createDTOFromGUI() {
        FeatureDTO featureDTO = super.createDTOFromGUI();
        // set things from actual object that are not grabbed from GUI
        // alias and notes are already handled by the interface, i.e.
        // are added / removed from the feature via independent Ajax call.
        featureDTO.setZdbID(dto.getZdbID());
        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        return featureDTO;
    }

    protected void revertGUI() {
/*
        if(dto.getZdbID()==null){
            resetGUI();
        }
        else{
            removeRelatedEntityButton.setVisible(true);
            featureEditList.setIndexForValue(dto.getZdbID()) ;
        }
*/
        if (dto.getFeatureType() != null) {
            view.featureTypeBox.setIndexForText(dto.getFeatureType().getDisplay());
            updateMutagenOnFeatureTypeChange();
        }
        view.featureAliasList.setDTO(dto);
        view.featureSequenceList.setDTO(dto);
        view.lineNumberBox.setValue(dto.getLineNumber());
        view.mutageeBox.setIndexForText(dto.getMutagee());
        view.featureNoteBox.setDTO(dto);
        view.featureNoteBox.revertGUI();
        view.featureNameBox.setText(FeatureValidationService.getNameFromFullName(dto));
        view.labOfOriginBox.setIndexForValue(dto.getLabOfOrigin());
        String selectedLab = view.labOfOriginBox.getSelectedText();
        if (selectedLab != null)
            onLabOfOriginChange(selectedLab, dto.getLabPrefix());
        view.knownInsertionCheckBox.setValue(dto.getKnownInsertionSite());
        view.dominantCheckBox.setValue(dto.getDominant());
        view.featureDisplayName.setValue(dto.getName());
        view.featureSuffixBox.setIndexForText(dto.getTransgenicSuffix());

    }

}
