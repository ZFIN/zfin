package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.ui.*;

public abstract class AbstractFeatureView extends Composite implements Revertible {

    AbstractFeaturePresenter presenter;

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    StringListBox featureTypeBox;
    @UiField
    CheckBox knownInsertionCheckBox;
    @UiField
    HorizontalPanel knownInsertionSite;
    @UiField
    StringListBox featureSuffixBox;
    @UiField
    HorizontalPanel featureSuffixPanel;
    @UiField
    StringTextBox featureNameBox;
    @UiField
    StringTextBox featureDisplayName;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Label message;
    @UiField
    StringListBox mutageeBox;
    @UiField
    StringListBox mutagenBox;
    @UiField
    StringListBox labOfOriginBox;
    @UiField
    StringTextBox lineNumberBox;
    @UiField
    StringListBox labDesignationBox;
    @UiField
    CheckBox dominantCheckBox;
    @UiField
    Button saveButton;
    @UiField
    MutationDetailDNAView mutationDetailDnaView;
    @UiField
    MutationDetailProteinView mutationDetailProteinView;
    @UiField
    MutationDetailTranscriptView mutationDetailTranscriptView;
    @UiField
    Label noMutationDetailMessage;
    @UiField
    Label proteinChangeFirstColumn;
    @UiField
    Label transcriptChangeFirstColumn;
    @UiField
    Label dnaChangeFirstColumn;

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    @UiHandler("errorLabel")
    void onClickErrorLabel(@SuppressWarnings("unused") ClickEvent event) {
        clearErrors();
    }

    @UiHandler("message")
    void onClickMessage(@SuppressWarnings("unused") ClickEvent event) {
        message.setText("");
    }

    @UiHandler("knownInsertionCheckBox")
    void onClickKnownInsertionSite(@SuppressWarnings("unused") ClickEvent event) {
        if (knownInsertionCheckBox.getValue()) {
            showMutationDetail();
            mutationDetailDnaView.showTgFields();
        } else {
            hideMutationDetail();
        }
        if (knownInsertionCheckBox.getValue()) {
            featureNameBox.setVisible(true);
            featureNameBox.setEnabled(false);
            featureSuffixBox.setVisible(true);
            featureSuffixPanel.setVisible(true);
            saveButton.setEnabled(true);
        } else {
            featureNameBox.setVisible(false);
            featureNameBox.setEnabled(false);
            featureSuffixPanel.setVisible(true);
            featureSuffixPanel.setVisible(true);
        }
        handleChanges();
    }

    @UiHandler("labOfOriginBox")
    void onChangeLabOfOrigin(@SuppressWarnings("unused") ChangeEvent event) {
        if (labOfOriginBox.isSelectedNull()) return;
        final String labOfOriginSelected = labOfOriginBox.getSelectedText();
        presenter.onLabOfOriginChange(labOfOriginSelected);
    }

    @UiHandler("labDesignationBox")
    void onChangeLabOfDesignation(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("mutageeBox")
    void onChangeMutagee(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("mutagenBox")
    void onChangeMutagen(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("featureSuffixBox")
    void onChangeFeatureSuffix(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("dominantCheckBox")
    void onClickDominantSite(@SuppressWarnings("unused") ClickEvent event) {
        handleChanges();
    }

    @UiHandler("lineNumberBox")
    void onKeyUpLineNumber(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    protected void handleChanges() {
        clearErrors();
        presenter.handleDirty();
    }

    @UiHandler("featureNameBox")
    void onKeyUpFeatureName(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("featureNameBox")
    void onChangeFeatureName(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("featureTypeBox")
    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        errorLabel.clearAllErrors();
        message.setText("");
        String featureType = featureTypeBox.getSelected();
        if (hasMutationDetails()) {
            showMutationDetail();
            mutationDetailDnaView.showFields(MutationDetailType.getType(featureType));
        } else {
            hideMutationDetail();
            mutationDetailDnaView.changePanel.setVisible(false);
        }
        handleDirty();

        onChangeFeatureType();
    }

    protected String getFeatureType() {
        return featureTypeBox.getSelected();
    }

    protected void hideMutationDetail() {
        noMutationDetailMessage.setVisible(true);
        dnaChangeFirstColumn.setVisible(false);
        transcriptChangeFirstColumn.setVisible(false);
        proteinChangeFirstColumn.setVisible(false);
        mutationDetailDnaView.changePanel.setVisible(false);
        mutationDetailTranscriptView.changePanel.setVisible(false);
        mutationDetailProteinView.proteinChangePanel.setVisible(false);
    }

    protected void showMutationDetail() {
        noMutationDetailMessage.setVisible(false);
        dnaChangeFirstColumn.setVisible(true);
        transcriptChangeFirstColumn.setVisible(true);
        proteinChangeFirstColumn.setVisible(true);
        mutationDetailDnaView.changePanel.setVisible(true);
        mutationDetailTranscriptView.changePanel.setVisible(true);
        mutationDetailProteinView.proteinChangePanel.setVisible(true);
    }


    public boolean hasMutationDetails() {
        return MutationDetailType.hasFeatureType(featureTypeBox.getSelected(), knownInsertionCheckBox.getValue());
    }


    void onChangeFeatureType() {
        final FeatureTypeEnum featureTypeSelected = FeatureTypeEnum.getTypeForDisplay(featureTypeBox.getSelectedText());
        if (featureTypeSelected == null) {
/*
            resetInterface();
*/
            saveButton.setEnabled(false);
            return;
        }
        lineNumberBox.setEnabled(true);
        knownInsertionCheckBox.setEnabled(false);
        dominantCheckBox.setEnabled(true);
        labOfOriginBox.setEnabled(true);
        labDesignationBox.setEnabled(true);
        mutageeBox.setEnabled(true);
        mutagenBox.setEnabled(false);
        featureSuffixPanel.setVisible(false);
        featureSuffixBox.setVisible(false);
        featureNameBox.setVisible(true);
        featureNameBox.setEnabled(false);

        switch (featureTypeSelected) {
            case TRANSGENIC_INSERTION:
                knownInsertionCheckBox.setEnabled(true);
                featureSuffixBox.setVisible(true);
                featureSuffixBox.setEnabled(true);
                featureSuffixPanel.setVisible(true);
                saveButton.setEnabled(true);
                break;
            case POINT_MUTATION:
            case DELETION:
            case SEQUENCE_VARIANT:
            case INSERTION:
                // just uses the defaults
                featureNameBox.setText("");
                break;
            case INVERSION:
            case TRANSLOC:
            case DEFICIENCY:
            case COMPLEX_SUBSTITUTION:
                // enable feature names
                featureNameBox.setEnabled(true);
                break;
            case UNSPECIFIED:
                dominantCheckBox.setEnabled(false);
                labDesignationBox.setEnabled(false);
                lineNumberBox.setEnabled(false);
                labOfOriginBox.setEnabled(false);
/*
                featureAliasBox.setEnabled(false);
                featureSequenceBox.setEnabled(false);
*/
                mutageeBox.setEnabled(false);
                mutagenBox.setEnabled(false);
                lineNumberBox.setText("");
                featureNameBox.setVisible(true);
                featureNameBox.setEnabled(true);
                break;

        }

        presenter.updateMutagenOnFeatureTypeChange(featureTypeSelected);
    }

    public void resetGUI() {
        featureTypeBox.setSelectedIndex(0);
        labOfOriginBox.setSelectedIndex(0);
        labOfOriginBox.setDirty(false);
        labDesignationBox.clear();
        labDesignationBox.setDirty(false);
        mutageeBox.setSelectedIndex(0);
        mutageeBox.setDirty(false);
        mutagenBox.setSelectedIndex(0);
        mutagenBox.setDirty(false);
        lineNumberBox.clear();
        lineNumberBox.setDirty(false);
        featureDisplayName.clear();
        featureDisplayName.setDirty(false);
        mutationDetailDnaView.resetGUI();
        mutationDetailTranscriptView.fullResetGUI();
        mutationDetailProteinView.resetGUI();
        knownInsertionCheckBox.setValue(false);
        featureSuffixPanel.setVisible(false);
        saveButton.setEnabled(false);
    }


    public void clearErrors() {
        errorLabel.clearAllErrors();
    }

    public void setError(String message) {
        errorLabel.setStyleName("clickable-error");
        errorLabel.setError(message + "[close]");
    }

    public void working() {
        saveButton.setText(TEXT_WORKING);
        saveButton.setEnabled(false);
        featureTypeBox.setEnabled(false);
        labOfOriginBox.setEnabled(false);
        labDesignationBox.setEnabled(false);
        lineNumberBox.setEnabled(false);
        dominantCheckBox.setEnabled(false);
        featureNameBox.setEnabled(false);
        mutageeBox.setEnabled(false);
        mutagenBox.setEnabled(false);
        knownInsertionCheckBox.setEnabled(false);
    }

    public void notWorking() {
        saveButton.setText(TEXT_SAVE);
        saveButton.setEnabled(true);
        featureTypeBox.setEnabled(true);
        onChangeFeatureType();
    }

    public void setNote(String text) {
        message.setText(text);
        message.setStyleName("clickable");

    }

    public void onclickSaveButton(ClickEvent event) {
        // if no consequence is selected and AA selection is used then default to substitution
        if (mutationDetailProteinView.proteinTermList.getSelectedIndex() == 0 &&
                mutationDetailProteinView.hasNonStopAASelected()) {
            mutationDetailProteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_SUBSTITUTION);
        }
        // if no consequence is selected and plus AA is used then default to Insertion
        if (mutationDetailProteinView.proteinTermList.getSelectedIndex() == 0 &&
                mutationDetailProteinView.hasPlusFieldOnly()) {
            mutationDetailProteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_INSERTION);
        }
        // if no consequence is selected and minus AA is used then default to Deletion
        if (mutationDetailProteinView.proteinTermList.getSelectedIndex() == 0 &&
                mutationDetailProteinView.hasMinusFieldOnly()) {
            mutationDetailProteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_DELETION);
        }
        // if no transcript consequence and protein: AA > AA for a Point mutation create
        if (getFeatureType().equals(FeatureTypeEnum.POINT_MUTATION.getName())) {
            // substitution is a missense consequence on the transcript level
            if (mutationDetailProteinView.hasNonStopAASelected()) {
                if (mutationDetailTranscriptView.getPresenter().getDtoSet().isEmpty()) {
                    mutationDetailTranscriptView.getPresenter().setMissenseTerm();
                }
            }
            else if (mutationDetailProteinView.hasStopCodon()) {
                if (mutationDetailTranscriptView.getPresenter().getDtoSet().isEmpty()) {
                    mutationDetailTranscriptView.getPresenter().setStopGainTerm();
                }
            }
        }
    }

    enum MutationDetailType {
        POINT_MUTATION,
        DELETION,
        INSERTION,
        INDEL,
        TRANSGENIC_INSERTION;

        public static boolean hasFeatureType(String featureType, boolean knownInsertionSite) {
            for (MutationDetailType type : values()) {
                if (type.name().equals(featureType)) {
                    return type != MutationDetailType.TRANSGENIC_INSERTION || knownInsertionSite;
                }
            }
            return false;
        }

        public static MutationDetailType getType(String featureType) {
            for (MutationDetailType type : values()) {
                if (type.name().equals(featureType)) {
                    return type;
                }
            }
            return null;
        }
    }


}
