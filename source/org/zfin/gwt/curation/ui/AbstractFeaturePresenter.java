package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeaturePrefixDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.OrganizationDTO;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.List;

public abstract class AbstractFeaturePresenter implements HandlesError {

    private AbstractFeatureView view;
    protected final String ZF_PREFIX = "zf";
    protected FeatureDTO dto;
    String publicationID;

    public AbstractFeaturePresenter(AbstractFeatureView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
        dto = new FeatureDTO();
        dto.setPublicationZdbID(publicationID);
    }


    public void go() {
        setLabOfOriginsValues();
    }

    private void setLabOfOriginsValues() {
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

    protected void updateMutagenOnFeatureTypeChange() {
        final FeatureTypeEnum featureTypeSelected = FeatureTypeEnum.getTypeForDisplay(view.featureTypeBox.getSelectedText());
        updateMutagenOnFeatureTypeChange(featureTypeSelected);
    }

    public void updateMutagenOnFeatureTypeChange(final FeatureTypeEnum featureTypeSelected) {
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
                        view.mutagenBox.setIndexForText(dto.getMutagen());
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

    public abstract void handleDirty();

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        Window.alert("Hello");
    }

    public void onLabOfOriginChange(String labOfOriginSelected) {
        onLabOfOriginChange(labOfOriginSelected, null);
    }

    public void onLabOfOriginChange(String labOfOriginSelected, final String labPrefix) {
        if (view.labOfOriginBox.isSelectedNull())
            return;
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
                        if (labPrefix != null)
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
        featureDTO.setAbbreviation(FeatureValidationService.getAbbreviationFromName(featureDTO));

        return featureDTO;
    }

}
