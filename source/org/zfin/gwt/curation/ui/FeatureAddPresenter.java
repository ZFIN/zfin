package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FeatureAddPresenter implements HandlesError {

    private FeatureAddView view;
    protected final String ZF_PREFIX = "zf";
    private FeatureDTO dto;
    private String publicationID;

    public FeatureAddPresenter(FeatureAddView featureAddBox, String publicationID) {
        this.publicationID = publicationID;
        this.view = featureAddBox;
        dto = new FeatureDTO();
        dto.setPublicationZdbID(publicationID);
    }


    public void go() {
        setValues();
    }

    private void setValues() {
        FeatureRPCService.App.getInstance().getLabsOfOriginWithPrefix(new FeatureEditCallBack<List<OrganizationDTO>>("Failed to load labs", this) {
            public void onSuccess(List<OrganizationDTO> list) {
                view.labOfOriginBox.clear();
                view.labOfOriginBox.addNull();
                for (OrganizationDTO labDTO : list) {
                    view.labOfOriginBox.addItem(labDTO.getName(), labDTO.getZdbID());
                }
            }
        });

    }

    public void onFeatureTypeChange(final FeatureTypeEnum featureTypeSelected) {
        FeatureRPCService.App.getInstance().getMutagensForFeatureType(featureTypeSelected,
                new FeatureEditCallBack<List<String>>("Failed to return mutagen for feature type: " + featureTypeSelected.getName(), this) {
                    @Override
                    public void onSuccess(List<String> result) {
                        if (featureTypeSelected != FeatureTypeEnum.UNSPECIFIED) {
                            if (result != null && result.size() > 0) {
                                view.mutagenBox.clear();
                                if (result.size() == 1) {
                                    view.mutagenBox.addItem(result.get(0));
                                } else {
                                    view.mutagenBox.addItem("not specified");
                                    for (String mut : result) {
                                        if (!view.mutagenBox.containsValue(mut)) {
                                            view.mutagenBox.addItem(mut);
                                        }
                                    }
                                }
                                view.mutagenBox.setEnabled(true);
                            }
                        }

                    }
                }

        );
        handleDirty();
    }

    @Override
    public void setError(String message) {
        view.errorLabel.setError(message);
    }

    @Override
    public void clearError() {
        view.message.setText("");
        view.errorLabel.setError("");
    }

    @Override
    public void fireEventSuccess() {

    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {

    }

    public void onLabOfOriginChange(String labOfOriginSelected) {
        FeatureRPCService.App.getInstance().getPrefix(labOfOriginSelected,
                new FeatureEditCallBack<List<FeaturePrefixDTO>>("Failed to load lab prefixes", this) {

                    @Override
                    public void onSuccess(List<FeaturePrefixDTO> labPrefixList) {
                        view.labDesignationBox.clear();
                        boolean hasZf = false;
                        for (FeaturePrefixDTO featurePrefixDTO : labPrefixList) {
                            if (hasZf || featurePrefixDTO.getPrefix().equals(ZF_PREFIX)) {
                                hasZf = true;
                            }
                            if (featurePrefixDTO.isActive()) {
                                view.labDesignationBox.addItem(featurePrefixDTO.getPrefix() + " (current)", featurePrefixDTO.getPrefix());
                            } else {
                                view.labDesignationBox.addItem(featurePrefixDTO.getPrefix());
                            }
                            view.labDesignationBox.addItem(featurePrefixDTO.getPrefix());
                        }
                        // always has zf
                        if (!hasZf) {
                            view.labDesignationBox.addItem(ZF_PREFIX);
                        }
                        view.labDesignationBox.setIndexForValue(dto.getLabPrefix());
                        handleDirty();
                        clearError();

                    }

                });

    }

    public FeatureDTO createDTOFromGUI() {

        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setName(view.featureDisplayName.getText());
        if (view.featureNameBox.isVisible()) {
            featureDTO.setOptionalName(view.featureNameBox.getText());
        }

        FeatureTypeEnum featureTypeEnum = FeatureTypeEnum.getTypeForName(view.featureTypeBox.getSelected());
        if (featureTypeEnum != null) {
            featureDTO.setFeatureType(featureTypeEnum);
            if (!featureTypeEnum.isUnspecified()) {
                featureDTO.setLineNumber(view.lineNumberBox.getText());
                featureDTO.setLabPrefix(view.labDesignationBox.getSelected());
                featureDTO.setLabOfOrigin(view.labOfOriginBox.getSelected());
            }
        }
        featureDTO.setMutagen(view.mutagenBox.getSelected());
        featureDTO.setMutagee(view.mutageeBox.getSelected());
        featureDTO.setDominant(view.dominantCheckBox.getValue());
        featureDTO.setKnownInsertionSite(view.knownInsertionCheckBox.getValue());
        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        featureDTO.setTransgenicSuffix(view.featureSuffixBox.getSelectedText());
        featureDTO.setFeatureSequence(view.featureSequenceBox.getText());
        featureDTO.setAbbreviation(FeatureValidationService.getAbbreviationFromName(featureDTO));

        if (StringUtils.isNotEmptyTrim(view.featureAliasBox.getText())) {
            featureDTO.setAlias(view.featureAliasBox.getText());
        }
        if (StringUtils.isNotEmptyTrim(view.publicNoteBox.getText())) {
            NoteDTO publicNoteDTO = new NoteDTO();
            publicNoteDTO.setNoteData(view.publicNoteBox.getText());
            publicNoteDTO.setPublicationZdbID(publicationID);
            featureDTO.addPublicNote(publicNoteDTO);
        }

        if (StringUtils.isNotEmptyTrim(view.curatorNoteBox.getText())) {
            List<NoteDTO> curatorNoteDTOs = new ArrayList<>();
            NoteDTO noteDTO = new NoteDTO();
            noteDTO.setNoteData(view.curatorNoteBox.getText());
            curatorNoteDTOs.add(noteDTO);
            featureDTO.setCuratorNotes(curatorNoteDTOs);
        }

        return featureDTO;
    }

    public boolean handleDirty() {
        view.featureDisplayName.setText(FeatureValidationService.generateFeatureDisplayName(createDTOFromGUI()));
        view.saveButton.setEnabled(FeatureValidationService.isFeatureSaveable(createDTOFromGUI()));
        return true;
    }

    public void createFeature() {
        FeatureDTO featureDTO = createDTOFromGUI();

        String errorMessage = FeatureValidationService.isValidToSave(featureDTO);
        if (errorMessage != null) {
            setError(errorMessage);
            return;
        }
        view.working();
        FeatureRPCService.App.getInstance().createFeature(featureDTO, new FeatureEditCallBack<FeatureDTO>("Failed to create feature:", this) {

            @Override
            public void onFailure(Throwable throwable) {
                super.onFailure(throwable);
                view.notWorking();
                handleDirty();
            }

            @Override
            public void onSuccess(final FeatureDTO result) {
                fireEventSuccess();
//                    Window.alert("Feature successfully created");
                view.featureTypeBox.setSelectedIndex(0);
                view.message.setText("Feature created: " + result.getName() + " [" + result.getZdbID() + "]");
                view.notWorking();
                view.saveButton.setEnabled(false);
                view.clearErrors();
            }
        });

    }


}
