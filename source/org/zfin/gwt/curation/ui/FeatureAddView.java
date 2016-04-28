package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.TransgenicSuffix;
import org.zfin.gwt.root.ui.Revertible;
import org.zfin.gwt.root.ui.StringTextBox;

public class FeatureAddView extends AbstractFeatureView implements Revertible {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureAddView> {
    }

    private FeatureAddPresenter addPresenter;
    @UiField
    StringTextBox featureAliasBox;
    @UiField
    StringTextBox featureSequenceBox;
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
        // if no consequence is selected and AA selection is used then default to substitution
        if (mutationDetailProteinView.proteinTermList.getSelectedIndex() == 0 &&
                mutationDetailProteinView.hasNonStopAASelected()) {
            mutationDetailProteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_SUBSTITUTION);
        }
        // if no consequence is selected and plus AA is used then default to Insertion
        if (mutationDetailProteinView.hasPlusFieldOnly()) {
            mutationDetailProteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_INSERTION);
        }
        // if no consequence is selected and minus AA is used then default to Deletion
        if (mutationDetailProteinView.hasMinusFieldOnly()) {
            mutationDetailProteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_DELETION);
        }
        // if no transcript consequence and protein: AA > AA for a Point mutation create
        if (getFeatureType().equals(FeatureTypeEnum.POINT_MUTATION.getName())) {
            if (mutationDetailTranscriptView.getPresenter().getDtoSet().isEmpty()) {
                // substitution is a missense consequence on the transcript level
                if (mutationDetailProteinView.hasNonStopAASelected())
                    mutationDetailTranscriptView.getPresenter().setMissenseTerm(this);
                else if (mutationDetailProteinView.hasStopCodon())
                    mutationDetailTranscriptView.getPresenter().setStopGainTerm(this);
            }
        }
        addPresenter.createFeature();
    }

    protected void handleChanges() {
        clearErrors();
        addPresenter.handleDirty();
    }

    @UiHandler("knownInsertionCheckBox")
    void onClickKnownInsertionSite(@SuppressWarnings("unused") ClickEvent event) {
        if (knownInsertionCheckBox.getValue()) {
            showMutationDetail();
            mutationDetailDnaView.showTgFields();
        } else {
            hideMutationDetail();
        }
        super.onClickKnownInsertionSite(event);
    }

    @UiHandler("featureTypeBox")
    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        super.onChangeFeatureType(event);
        publicNoteBox.setEnabled(true);
        curatorNoteBox.setEnabled(true);
        featureAliasBox.setEnabled(true);
        featureSequenceBox.setEnabled(true);
        handleDirty();
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
        featureAliasBox.setEnabled(false);
        featureAliasBox.clear();
        featureSequenceBox.setEnabled(false);
        featureSequenceBox.clear();
        mutageeBox.setEnabled(false);
        mutageeBox.setSelectedIndex(0);
        mutagenBox.setEnabled(false);
        mutagenBox.setSelectedIndex(0);
        publicNoteBox.setEnabled(false);
        publicNoteBox.setText("");
        curatorNoteBox.setEnabled(false);
        curatorNoteBox.setText("");
        knownInsertionCheckBox.setEnabled(false);
        knownInsertionCheckBox.setValue(false);
        featureDisplayName.clear();
        mutationDetailDnaView.resetGUI();
        mutationDetailTranscriptView.fullResetGUI();
        mutationDetailProteinView.resetGUI();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean handleDirty() {
        addPresenter.handleDirty();
        return true;
    }

    public void setPresenter(FeatureAddPresenter presenter) {
        this.presenter = presenter;
        addPresenter = presenter;
    }

    public void clearErrors() {
        errorLabel.clearAllErrors();
    }

    public void working() {
        super.working();
        featureAliasBox.setEnabled(false);
        featureSequenceBox.setEnabled(false);
        publicNoteBox.setEnabled(false);
        curatorNoteBox.setEnabled(false);
    }


}
