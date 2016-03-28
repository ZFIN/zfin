package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.TransgenicSuffix;
import org.zfin.gwt.root.ui.*;

public class FeatureAddView extends Composite implements Revertible {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureAddView> {
    }

    private FeatureAddPresenter presenter;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    Grid featureConstructionPanel;
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
    Button saveButton;
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
    StringTextBox featureNameBox;
    @UiField
    StringTextBox featureAliasBox;
    @UiField
    StringTextBox featureSequenceBox;
    @UiField
    StringTextBox featureDisplayName;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Label message;
    @UiField
    TextArea curatorNoteBox;
    @UiField
    TextArea publicNoteBox;

    public FeatureAddView() {
        initWidget(uiBinder.createAndBindUi(this));
        featureTypeBox.clear();
        featureTypeBox.addNull();
        for (FeatureTypeEnum featureTypeEnum : FeatureTypeEnum.values()) {
            featureTypeBox.addItem(featureTypeEnum.getDisplay(), featureTypeEnum.name());
        }
        featureSuffixBox.addEnumValues(TransgenicSuffix.values());
        mutageeBox.addEnumValues(Mutagee.values());
    }

    @UiHandler("saveButton")
    void onClickSaveButton(@SuppressWarnings("unused") ClickEvent event) {
        presenter.createFeature();
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    @UiHandler("knownInsertionCheckBox")
    void onClickKnownInsertionSite(@SuppressWarnings("unused") ClickEvent event) {
        if (knownInsertionCheckBox.getValue()) {
            featureNameBox.setVisible(true);
            featureNameBox.setEnabled(false);
            featureSuffixBox.setVisible(true);
            featureSuffixPanel.setVisible(true);
///            constructTextBox.setVisible(false);
            saveButton.setEnabled(true);
        } else {
            featureNameBox.setVisible(false);
///            constructTextBox.setVisible(true);
            featureNameBox.setEnabled(false);
            featureSuffixPanel.setVisible(true);
/*
            constructTextBox.setEnabled(false);
*/
        }
    }

    @UiHandler("labOfOriginBox")
    void onChangeLabOfOrigin(@SuppressWarnings("unused") ChangeEvent event) {
        if (labOfOriginBox.isSelectedNull()) return;
        final String labOfOriginSelected = labOfOriginBox.getSelectedText();
        presenter.onLabOfOriginChange(labOfOriginSelected);
    }

    @UiHandler("labDesignationBox")
    void onChangeLabOfDesignation(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("mutageeBox")
    void onChangeMutagee(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("mutagenBox")
    void onChangeMutagen(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("featureSuffixBox")
    void onChangeFeatureSuffix(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("dominantCheckBox")
    void onClickDominantSite(@SuppressWarnings("unused") ClickEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("lineNumberBox")
    void onKeyUpLineNumber(@SuppressWarnings("unused") KeyUpEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("featureNameBox")
    void onKeyUpFeatureName(@SuppressWarnings("unused") KeyUpEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("featureNameBox")
    void onChangeFeatureName(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.handleDirty();
    }

    @UiHandler("knownInsertionCheckBox")
    void onClickKnownInsertion(@SuppressWarnings("unused") ClickEvent event) {
        if (knownInsertionCheckBox.getValue()) {
            featureNameBox.setVisible(true);
            featureNameBox.setEnabled(false);
            featureSuffixBox.setVisible(true);
            featureSuffixPanel.setVisible(true);
///            constructTextBox.setVisible(false);
            saveButton.setEnabled(true);
        } else {
            featureNameBox.setVisible(false);
///            constructTextBox.setVisible(true);
            featureNameBox.setEnabled(false);
            featureSuffixBox.setVisible(true);
            featureSuffixPanel.setVisible(true);
///            constructTextBox.setEnabled(false);
        }
        presenter.handleDirty();
    }

    @UiHandler("featureTypeBox")
    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        errorLabel.clearAllErrors();
        final FeatureTypeEnum featureTypeSelected = FeatureTypeEnum.getTypeForDisplay(featureTypeBox.getSelectedText());
        if (featureTypeSelected == null) {
            resetInterface();
            saveButton.setEnabled(false);
            return;
        }

        knownInsertionCheckBox.setEnabled(false);
        labOfOriginBox.setEnabled(true);
        publicNoteBox.setEnabled(true);
        curatorNoteBox.setEnabled(true);
        lineNumberBox.setEnabled(true);
        dominantCheckBox.setEnabled(true);
        labDesignationBox.setEnabled(true);
        mutageeBox.setEnabled(true);
        mutagenBox.setEnabled(false);
        featureSuffixPanel.setVisible(false);
        featureSuffixBox.setVisible(false);
        featureAliasBox.setEnabled(true);
        featureSequenceBox.setEnabled(true);
        featureNameBox.setVisible(true);
        featureNameBox.setEnabled(false);
//        constructTextBox.setVisible(false);

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
                featureAliasBox.setEnabled(false);
                featureSequenceBox.setEnabled(false);
                mutageeBox.setEnabled(false);
                mutagenBox.setEnabled(false);
                lineNumberBox.setText("");
//                constructTextBox.setVisible(false);
                featureNameBox.setVisible(true);
                featureNameBox.setEnabled(true);
                break;

        }

        mutagenBox.clear();

        presenter.onFeatureTypeChange(featureTypeSelected);
    }


    public void resetInterface() {
        labOfOriginBox.setEnabled(false);
        labOfOriginBox.setSelectedIndex(0);
        labDesignationBox.setEnabled(false);
        labDesignationBox.clear();
        labDesignationBox.setSelectedIndex(0);
        lineNumberBox.setEnabled(false);
        lineNumberBox.clear();
        dominantCheckBox.setEnabled(false);
        dominantCheckBox.setValue(false);
        featureNameBox.setEnabled(false);
        featureNameBox.clear();
///        constructTextBox.setText("");
        featureAliasBox.setEnabled(false);
        featureAliasBox.clear();
        featureSequenceBox.setEnabled(false);
        featureSequenceBox.clear();
/*
        featureAliasList.working();
        featureAliasList.resetInput();
        featureSequenceList.working();
        featureSequenceList.resetInput();
*/
        mutageeBox.setEnabled(false);
        mutageeBox.setSelectedIndex(0);
        mutagenBox.setEnabled(false);
        mutagenBox.setSelectedIndex(0);
        publicNoteBox.setEnabled(false);
        publicNoteBox.setText("");
        curatorNoteBox.setEnabled(false);
        curatorNoteBox.setText("");
//        featureNoteBox.revertGUI();
        knownInsertionCheckBox.setEnabled(false);
        knownInsertionCheckBox.setValue(false);
        featureDisplayName.clear();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean handleDirty() {
        return false;
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
        featureAliasBox.setEnabled(false);
        featureSequenceBox.setEnabled(false);
        mutageeBox.setEnabled(false);
        mutagenBox.setEnabled(false);
        publicNoteBox.setEnabled(false);
        curatorNoteBox.setEnabled(false);
        knownInsertionCheckBox.setEnabled(false);
    }

    public void notWorking() {
        saveButton.setText(TEXT_SAVE);
        saveButton.setEnabled(true);
        featureTypeBox.setEnabled(true);
/*
        updateForFeatureType();
*/
    }


    public void setPresenter(FeatureAddPresenter presenter) {
        this.presenter = presenter;
    }

    public void clearErrors() {
        errorLabel.clearAllErrors();
    }

}
