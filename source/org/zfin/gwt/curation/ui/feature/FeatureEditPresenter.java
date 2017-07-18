package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.curation.ui.FeatureServiceGWT;
import org.zfin.gwt.curation.ui.FeatureValidationService;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.BooleanCollector;

import java.util.List;

public class FeatureEditPresenter extends AbstractFeaturePresenter {

    private FeatureEditView view;
    private FeatureNotesPresenter featureNotesPresenter;
    private EditMutationDetailPresenter mutationDetailPresenter;

    public FeatureEditPresenter(FeatureEditView view, String publicationID) {
        super(view, publicationID);
        if (publicationID == null)
            throw new RuntimeException("NO pub ID found");
        this.view = view;
        this.
                dto = new FeatureDTO();
        dto.setPublicationZdbID(publicationID);
        featureNotesPresenter = new FeatureNotesPresenter(publicationID, view.featureNotesView);
        view.featureNotesView.setPresenter(featureNotesPresenter);
        mutationDetailPresenter = new EditMutationDetailPresenter(view);
        view.mutationDetailTranscriptView.setPresenter(mutationDetailPresenter);
        view.mutationDetailDnaView.setPresenter(mutationDetailPresenter);
        view.mutationDetailProteinView.setPresenter(mutationDetailPresenter);

    }

    public void go() {
        super.go();
        loadFeaturesForPub();
        featureNotesPresenter.go();
        mutationDetailPresenter.go();
    }

    public void refresh() {
        super.go();
        loadFeaturesForPub(true);
        featureNotesPresenter.go();
        mutationDetailPresenter.go();
    }

    public void refreshFeatureLists() {
        loadFeaturesForPub(true);
    }

    public void loadFeaturesForPub() {
        loadFeaturesForPub(false);
    }

    public void loadFeaturesForPub(boolean forceLoad) {
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_START);
        FeatureServiceGWT.getFeatureList(publicationID,
                new FeatureEditCallBack<List<FeatureDTO>>("Failed to find features for pub: " + publicationID, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        view.featureEditList.setEnabled(true);
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                    }

                    @Override
                    public void onSuccess(List<FeatureDTO> results) {
                        if (results == null || results.size() == 0) {
                            view.showHideToggle.setVisibility(false);
                            AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                            return;
                        }
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
                        view.onChangeFeatureType();
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                    }
                }, forceLoad);

    }


    public void onFeatureSelectionChange(String featureID) {
        FeatureRPCService.App.getInstance().getFeature(featureID, new FeatureEditCallBack<FeatureDTO>("Failed to remove attribution: ", this) {
            public void onSuccess(FeatureDTO featureDTO) {
                featureDTO.setPublicationZdbID(publicationID);
                dto = featureDTO;
                mutationDetailPresenter.setDto(featureDTO);
                view.onChangeFeatureType();
                revertGUI();
                view.removeFeatureLink.setUrl("/action/infrastructure/deleteRecord/" + dto.getZdbID());
                view.removeFeatureLink.setTitle("Delete Feature " + dto.getZdbID());
            }
        });

    }


    public void handleDirty() {
        view.featureDisplayName.setText(FeatureValidationService.generateFeatureDisplayName(createDTOFromGUI()));
        boolean isDirty = isDirty();
        onDirtyValueNotification(isDirty);
    }

    public boolean isDirty() {
        boolean isDirty = false;
        // this displays most changes
        // alias and notes are done automatically?
        BooleanCollector col = new BooleanCollector(true);

        if (view.knownInsertionCheckBox.getValue() != dto.getKnownInsertionSite()) {
            view.saveButton.setEnabled(true);
            col.addBoolean(true);
        }
        if (view.featureTypeBox.getSelected() != null && dto.getFeatureType() != null) {
            col.addBoolean(view.featureTypeBox.isDirty(dto.getFeatureType().name()));
        } else if ((view.featureTypeBox.getSelected() == null && dto.getFeatureType() != null)
                || (view.featureTypeBox.getSelected() != null && dto.getFeatureType() == null)
                ) {
            col.addBoolean(view.featureTypeBox.setDirty(true));
        }
        col.addBoolean(view.mutageeBox.isDirty(dto.getMutagee()));
        col.addBoolean(view.mutagenBox.isDirty(dto.getMutagen()));
        col.addBoolean(view.labDesignationBox.isDirty(dto.getLabPrefix()));
        col.addBoolean(view.featureSuffixBox.isDirty(dto.getTransgenicSuffix()));
        col.addBoolean(view.lineNumberBox.isDirty(dto.getLineNumber()));
        col.addBoolean(view.labOfOriginBox.isDirty(dto.getLabOfOrigin()));
        col.addBoolean(mutationDetailPresenter.isDirty());
        return col.arrivedValue();
    }


    public void onDirtyValueNotification(boolean isDirty) {
        view.saveButton.setEnabled(isDirty && FeatureValidationService.isFeatureSaveable(createDTOFromGUI()));
        view.revertButton.setEnabled(isDirty);
    }

    public FeatureDTO createDTOFromGUI() {
        FeatureDTO featureDTO = super.createDTOFromGUI(view);
        // set things from actual object that are not grabbed from GUI
        // alias and notes are already handled by the interface, i.e.
        // are added / removed from the feature via independent Ajax call.
        featureDTO.setZdbID(dto.getZdbID());
        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        featureDTO.setPublicNoteList(dto.getPublicNoteList());
        featureDTO.setCuratorNotes(dto.getCuratorNotes());
        return featureDTO;
    }

    protected void revertGUI() {
        if (dto == null || dto.getZdbID() == null || dto.getZdbID().trim().isEmpty()) {
            view.resetGUI();
            featureNotesPresenter.setFeatureDTO(null);
            featureNotesPresenter.rebuildGUI();
            return;
        }

        view.removeFeatureLink.setVisible(true);
        view.featureEditList.setIndexForValue(dto.getZdbID());
        if (dto.getFeatureType() != null) {
            view.featureTypeBox.setIndexForText(dto.getFeatureType().getDisplay());
            updateMutagenOnFeatureTypeChange();
            view.onChangeFeatureType(null);
        }
        view.featureAliasList.setDTO(dto);
        view.featureSequenceList.setDTO(dto);
        view.lineNumberBox.setValue(dto.getLineNumber());
        view.mutageeBox.setIndexForText(dto.getMutagee());
        featureNotesPresenter.setFeatureDTO(dto);
        featureNotesPresenter.rebuildGUI();
        view.featureNameBox.setText(FeatureValidationService.getNameFromFullName(dto));
        view.labOfOriginBox.setIndexForValue(dto.getLabOfOrigin());
        String selectedLab = view.labOfOriginBox.getSelectedText();
        if (selectedLab != null)
            onLabOfOriginChange(selectedLab, dto.getLabPrefix());
        view.knownInsertionCheckBox.setValue(dto.getKnownInsertionSite());
        // only call event handler if transgenic Insertion
        if (dto.getFeatureType().equals(FeatureTypeEnum.TRANSGENIC_INSERTION))
            view.onClickKnownInsertionSite(null);
        view.dominantCheckBox.setValue(dto.getDominant());
        view.featureDisplayName.setValue(dto.getName());
        view.featureSuffixBox.setIndexForText(dto.getTransgenicSuffix());
        view.mutationDetailProteinView.populateFields(dto.getProteinChangeDTO());
        view.mutationDetailDnaView.populateFields(dto.getDnaChangeDTO());
        mutationDetailPresenter.setDtoSet(dto.getTranscriptChangeDTOSet());
    }


    public void updateFeature() {
        FeatureDTO featureDTO = createDTOFromGUI();
        // if a public note was added (they persist immedately) update this feature with it
        // so validation can happen correctly
        if (featureDTO.getPublicNoteList() == null || featureDTO.getPublicNoteList().size() == 0)
            featureDTO.setPublicNoteList(featureNotesPresenter.featureDTO.getPublicNoteList());
        String errorMessage = FeatureValidationService.isValidToSave(featureDTO);
        if (errorMessage != null) {
            setError(errorMessage);
            return;
        }
        errorMessage = mutationDetailPresenter.isValid(featureDTO);
        if (errorMessage != null) {
            setError(errorMessage);
            return;
        }

        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        if (isDirty() && FeatureValidationService.isFeatureSaveable(featureDTO)) {
            clearError();
            view.working();
            FeatureRPCService.App.getInstance().editFeatureDTO(featureDTO,
                    new FeatureEditCallBack<FeatureDTO>("Failed to create feature:", this) {

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            view.notWorking();
                            view.revertButton.setEnabled(true);

                        }

                        @Override
                        public void onSuccess(final FeatureDTO result) {
                            result.setPublicationZdbID(dto.getPublicationZdbID());
                            dto = result;
                            view.notWorking();
                            view.setNote("Saved Feature [" + result.getName() + "]");
                            AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.EDIT_FEATURE, result.getName()));
                            view.resetGUI();

                        }
                    });

        }

    }
}
