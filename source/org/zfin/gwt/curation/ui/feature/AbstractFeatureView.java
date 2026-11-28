package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.VariantDTO;
import org.zfin.gwt.root.ui.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFeatureView extends Composite implements Revertible, MutationDetailWidgets {

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
    StringTextBox featureChromosome;
    @UiField
    StringListBox featureAssembly;
    @UiField
    NumberTextBox featureStartLoc;
    @UiField
    NumberTextBox featureEndLoc;
    @UiField
    StringListBox featureEvidenceCode;
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
    GenomicMutationDetailView genomicMutationDetailView;
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
    @UiField
    Label variantInfoFirstColumn;
    @UiField
    HorizontalPanel endLocationPanel;
    @UiField
    Label chromosomeLabel;
    @UiField
    HorizontalPanel chromosomePanel;
    @UiField
    Label startLocationLabel;
    @UiField
    HTML startLocHint;
    @UiField
    HTML endLocHint;
    // ZFIN-10623: the variants of a Complex feature, one VariantView each
    @UiField
    Label complexVariantsLabel;
    @UiField
    FlowPanel complexVariantsPanel;
    @UiField
    VerticalPanel variantBlocksPanel;
    @UiField
    Button addVariantButton;
    @UiField
    Label variantsMessage;

    private final List<VariantView> variantViews = new ArrayList<>();
    // variants shown when the feature was loaded, to tell whether one was added or cancelled since
    private int loadedVariantCount;
    // the type selected before the latest change, so a feature turned Complex keeps its variant
    private FeatureTypeEnum lastFeatureType;

    public AbstractFeatureView() {
    }

    @Override
    public GenomicMutationDetailView getGenomicMutationDetailView() {
        return genomicMutationDetailView;
    }

    @Override
    public MutationDetailDNAView getMutationDetailDnaView() {
        return mutationDetailDnaView;
    }

    @Override
    public MutationDetailTranscriptView getMutationDetailTranscriptView() {
        return mutationDetailTranscriptView;
    }

    @Override
    public MutationDetailProteinView getMutationDetailProteinView() {
        return mutationDetailProteinView;
    }

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
        boolean showChromosome = knownInsertionCheckBox.getValue();
        chromosomeLabel.setVisible(showChromosome);
        chromosomePanel.setVisible(showChromosome);
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
        final String labDesigSelected=labDesignationBox.getSelectedText();

            presenter.onLabDesigChange(labDesigSelected);


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

    @UiHandler("featureChromosome")
    void onKeyUpChr(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
        presenter.fetchReferenceSequenceIfReady();
    }

    @UiHandler("featureChromosome")
    void onChangeChromosome(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
        presenter.fetchReferenceSequenceIfReady();
    }

    @UiHandler("featureEvidenceCode")
    void onChangeEvidenceCode(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }


    @UiHandler("featureAssembly")
    void onChangeAssembly(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
        presenter.fetchReferenceSequenceIfReady();
    }

    @UiHandler("featureStartLoc")
    void onChangeStartLocation(@SuppressWarnings("unused") ChangeEvent event) {
        String featureType = featureTypeBox.getSelected();
        if (featureType != null && featureType.equals(FeatureTypeEnum.POINT_MUTATION.getName())) {
            featureEndLoc.setNumber(featureStartLoc.getBoxValue());
        }
        handleChanges();
        presenter.fetchReferenceSequenceIfReady();
        presenter.autoCalcDeletionLength();
    }

    @UiHandler("featureEndLoc")
    void onChangeEndLocation(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
        presenter.fetchReferenceSequenceIfReady();
        presenter.autoCalcDeletionLength();
    }




    @UiHandler("featureEvidenceCode")
    void onKeyUpEvidenceCode(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }


    @UiHandler("featureAssembly")
    void onKeyUpAssembly(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
        presenter.fetchReferenceSequenceIfReady();
    }

    /*@UiHandler("featureStartLoc")
    void onKeyUpStartLocation(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("featureEndLoc")
    void onKeyUpEndLocation(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }*/






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

    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        errorLabel.clearAllErrors();
        message.setText("");
        String featureType = featureTypeBox.getSelected();
        FeatureTypeEnum selectedType = FeatureTypeEnum.getTypeForName(featureType);
        // only when the curator changes the type: loading a feature into the edit form passes no event
        if (event != null && selectedType == FeatureTypeEnum.COMPLEX_SUBSTITUTION && variantViews.isEmpty()
                && lastFeatureType != null && MutationDetailType.hasFeatureType(lastFeatureType.name(), false)) {
            // the variant entered so far becomes Variant 1 of the Complex feature
            addVariantView().populate(getSingleVariantDTO(lastFeatureType));
        }
        lastFeatureType = selectedType;

        if (hasMutationDetails()) {
            showMutationDetail();
            mutationDetailDnaView.showFields(MutationDetailType.getType(featureType));
            genomicMutationDetailView.showFields(MutationDetailType.getType(featureType),knownInsertionCheckBox.getValue());
        } else {
            hideMutationDetail();
            mutationDetailDnaView.changePanel.setVisible(false);
            genomicMutationDetailView.changePanel.setVisible(false);
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
        variantInfoFirstColumn.setVisible(false);
        mutationDetailDnaView.changePanel.setVisible(false);
        mutationDetailTranscriptView.changePanel.setVisible(false);
        mutationDetailProteinView.proteinChangePanel.setVisible(false);
        genomicMutationDetailView.changePanel.setVisible(false);
    }

    protected void showMutationDetail() {
        noMutationDetailMessage.setVisible(false);
        dnaChangeFirstColumn.setVisible(true);
        transcriptChangeFirstColumn.setVisible(true);
        proteinChangeFirstColumn.setVisible(true);
        variantInfoFirstColumn.setVisible(true);
        mutationDetailDnaView.changePanel.setVisible(true);
        mutationDetailTranscriptView.changePanel.setVisible(true);
        mutationDetailProteinView.proteinChangePanel.setVisible(true);
        genomicMutationDetailView.changePanel.setVisible(true);
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
            case INSERTION:
                // just uses the defaults
                featureNameBox.setText("");
                break;
            case INVERSION:
            case TRANSLOC:
            case DEFICIENCY:
                // enable feature names
                featureNameBox.setEnabled(true);
                break;
            case COMPLEX_SUBSTITUTION:
                // ZFIN-10623: the feature name box is blocked out; the name is lab designation +
                // line number. Location and mutation details are entered per variant.
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

        boolean showChromosomeRow = featureTypeSelected == FeatureTypeEnum.POINT_MUTATION
                || featureTypeSelected == FeatureTypeEnum.INSERTION
                || featureTypeSelected == FeatureTypeEnum.INDEL
                || featureTypeSelected == FeatureTypeEnum.DELETION
                || (featureTypeSelected == FeatureTypeEnum.TRANSGENIC_INSERTION && knownInsertionCheckBox.getValue());
        chromosomeLabel.setVisible(showChromosomeRow);
        chromosomePanel.setVisible(showChromosomeRow);

        configureLocationFields(featureTypeSelected, startLocationLabel, endLocationPanel, startLocHint, endLocHint,
                featureStartLoc, featureEndLoc);

        boolean isComplex = featureTypeSelected == FeatureTypeEnum.COMPLEX_SUBSTITUTION;
        showComplexVariants(isComplex);
        if (isComplex) {
            noMutationDetailMessage.setVisible(false);
        }

        presenter.updateMutagenOnFeatureTypeChange(featureTypeSelected);
        presenter.autoCalcDeletionLength();
    }

    /**
     * Location field labels and hints for a variant type: shared by the feature's own location row
     * and the location row of each variant of a Complex feature (VariantView).
     */
    static void configureLocationFields(FeatureTypeEnum type, Label startLocationLabel, HorizontalPanel endLocationPanel,
                                        HTML startLocHint, HTML endLocHint, NumberTextBox startLoc, NumberTextBox endLoc) {
        boolean isPointMutation = type == FeatureTypeEnum.POINT_MUTATION;
        endLocationPanel.setVisible(!isPointMutation);
        startLocationLabel.setText(isPointMutation ? "Location" : "Start Location");
        if (isPointMutation) {
            endLoc.setNumber(startLoc.getBoxValue());
        }

        boolean isDeletionOrIndel = type == FeatureTypeEnum.DELETION
                || type == FeatureTypeEnum.INDEL;
        boolean isInsertion = type == FeatureTypeEnum.INSERTION;
        boolean showLocHints = isDeletionOrIndel || isInsertion;
        startLocHint.setVisible(showLocHints);
        endLocHint.setVisible(showLocHints);
        if (isDeletionOrIndel) {
            startLocHint.setHTML("Position of<br/>1st nucleotide deleted");
            endLocHint.setHTML("Position of<br/>last nucleotide deleted");
        } else if (isInsertion) {
            startLocHint.setHTML("Position of<br/>5' flanking nucleotide");
            endLocHint.setHTML("Position of<br/>3' flanking nucleotide");
        }
    }

    // ─── variants of a Complex feature (ZFIN-10623) ───────────────────────────

    boolean isComplexSelected() {
        return FeatureTypeEnum.getTypeForName(featureTypeBox.getSelected()) == FeatureTypeEnum.COMPLEX_SUBSTITUTION;
    }

    /** A Complex feature has at least two variants, so it always shows two variant blocks. */
    private void showComplexVariants(boolean show) {
        complexVariantsLabel.setVisible(show);
        complexVariantsPanel.setVisible(show);
        if (show) {
            while (variantViews.size() < 2) {
                addVariantView();
            }
        }
    }

    @UiHandler("addVariantButton")
    void onClickAddVariant(@SuppressWarnings("unused") ClickEvent event) {
        addVariantView();
        handleChanges();
    }

    VariantView addVariantView() {
        VariantView variantView = new VariantView(this);
        variantViews.add(variantView);
        variantBlocksPanel.add(variantView);
        renumberVariantViews();
        return variantView;
    }

    void removeVariantView(VariantView variantView) {
        variantViews.remove(variantView);
        variantBlocksPanel.remove(variantView);
        renumberVariantViews();
        handleChanges();
    }

    private void renumberVariantViews() {
        for (int i = 0; i < variantViews.size(); i++) {
            variantViews.get(i).setVariantNumber(i + 1, i >= 2);
        }
    }

    void onVariantChanged() {
        handleChanges();
    }

    List<VariantDTO> getVariantDTOs() {
        List<VariantDTO> dtos = new ArrayList<>();
        for (VariantView variantView : variantViews) {
            dtos.add(variantView.getDto());
        }
        return dtos;
    }

    /** Show the saved variants of a Complex feature (at least two blocks). */
    void populateVariants(List<VariantDTO> variants) {
        clearVariants();
        if (variants != null) {
            for (VariantDTO variant : variants) {
                addVariantView().populate(variant);
            }
        }
        loadedVariantCount = variantViews.size();
        showComplexVariants(isComplexSelected());
        if (variantViews.size() > loadedVariantCount) {
            // the padding blocks count as loaded: they are not an addition
            loadedVariantCount = variantViews.size();
        }
    }

    void clearVariants() {
        variantViews.clear();
        variantBlocksPanel.clear();
        loadedVariantCount = 0;
        variantsMessage.setText("");
    }

    /** True when a variant was edited, added or cancelled since the feature was loaded. */
    boolean areVariantsChanged() {
        if (variantViews.size() != loadedVariantCount) {
            return true;
        }
        for (VariantView variantView : variantViews) {
            if (variantView.isChanged()) {
                return true;
            }
        }
        return false;
    }

    void setVariantsMessage(String text) {
        variantsMessage.setText(text == null ? "" : text);
    }

    /** @return the first problem with a variant's protein change, or null */
    String validateVariantMutationDetails() {
        for (VariantView variantView : variantViews) {
            String error = variantView.validateMutationDetails();
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /** The single variant entered in the feature's own fields, as a variant of the given type. */
    private VariantDTO getSingleVariantDTO(FeatureTypeEnum type) {
        VariantDTO dto = new VariantDTO();
        dto.setVariantType(type);
        dto.setChromosome(featureChromosome.getText());
        dto.setAssembly(featureAssembly.getSelectedItemText());
        dto.setStartLocation(featureStartLoc.getBoxValue());
        dto.setEndLocation(featureEndLoc.getBoxValue());
        dto.setEvidence(featureEvidenceCode.getSelectedItemText());
        dto.setFgmdChangeDTO(genomicMutationDetailView.getDto());
        dto.setDnaChangeDTO(mutationDetailDnaView.getDto());
        dto.setProteinChangeDTO(mutationDetailProteinView.getDto());
        if (mutationDetailTranscriptView.getPresenter() != null) {
            dto.setTranscriptChangeDTOSet(new java.util.HashSet<>(mutationDetailTranscriptView.getPresenter().getDtoSet()));
        }
        return dto;
    }

    public void resetGUI() {
        clearVariants();
        showComplexVariants(false);
        lastFeatureType = null;
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
        featureNameBox.clear();
        featureNameBox.setDirty(false);
        dominantCheckBox.setValue(false);
        featureEvidenceCode.setSelectedIndex(0);
        featureEvidenceCode.setDirty(false);
        featureChromosome.setDirty(false);
        featureChromosome.clear();
        featureAssembly.setSelectedIndex(0);
        featureAssembly.setDirty(false);
        featureStartLoc.setDirty(false);
        featureStartLoc.clear();
        featureEndLoc.setDirty(false);
        featureEndLoc.clear();
        featureDisplayName.clear();
        featureDisplayName.setDirty(false);
        mutationDetailDnaView.resetGUI();
        mutationDetailTranscriptView.fullResetGUI();
        mutationDetailProteinView.resetGUI();
        genomicMutationDetailView.resetGUI();
        knownInsertionCheckBox.setValue(false);
        featureSuffixPanel.setVisible(false);
        chromosomeLabel.setVisible(true);
        chromosomePanel.setVisible(true);
        endLocationPanel.setVisible(true);
        startLocationLabel.setText("Start Location");
        startLocHint.setVisible(false);
        endLocHint.setVisible(false);
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
       /* lineNumberBox.setEnabled(false);
        featureChromosome.setEnabled(false);
        featureChrAssembly.setEnabled(false);
        featureStartLoc.setEnabled(false);
        featureEndLoc.setEnabled(false);
        featureEvidenceCode.setEnabled(false);*/
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
        if (isComplexSelected()) {
            for (VariantView variantView : variantViews) {
                variantView.applySaveDefaults();
            }
            return;
        }
        applyMutationDetailDefaults(this, getFeatureType());
    }

    /**
     * Defaults filled in on save for one variant: the feature's own, or one variant of a Complex
     * feature (VariantView).
     *
     * @param featureType the type of the variant, as FeatureTypeEnum name
     */
    static void applyMutationDetailDefaults(MutationDetailWidgets widgets, String featureType) {
        MutationDetailProteinView proteinView = widgets.getMutationDetailProteinView();
        MutationDetailTranscriptView transcriptView = widgets.getMutationDetailTranscriptView();
        // if no consequence is selected and AA selection is used then default to substitution
        if (proteinView.proteinTermList.getSelectedIndex() == 0 &&
                proteinView.hasNonStopAASelected()) {
            proteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_SUBSTITUTION);
        }
        // if no consequence is selected and plus AA is used then default to Insertion
        if (proteinView.proteinTermList.getSelectedIndex() == 0 &&
                proteinView.hasPlusFieldOnly()) {
            proteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_INSERTION);
        }
        // if no consequence is selected and minus AA is used then default to Deletion
        if (proteinView.proteinTermList.getSelectedIndex() == 0 &&
                proteinView.hasMinusFieldOnly()) {
            proteinView.proteinTermList.setIndexForText(MutationDetailProteinView.AMINO_ACID_DELETION);
        }
        // if no transcript consequence and protein: AA > AA for a Point mutation create
        if (FeatureTypeEnum.POINT_MUTATION.getName().equals(featureType)) {
            // substitution is a missense consequence on the transcript level
            if (proteinView.hasNonStopAASelected()) {
                if (transcriptView.getPresenter().getDtoSet().isEmpty()) {
                    transcriptView.getPresenter().setMissenseTerm();
                }
            }
            else if (proteinView.hasStopCodon()) {
                if (transcriptView.getPresenter().getDtoSet().isEmpty()) {
                    transcriptView.getPresenter().setStopGainTerm();
                }
            }
        }
    }

    enum MutationDetailType {
        POINT_MUTATION,
        DELETION,
        INSERTION,
        INDEL,
        MNV,
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

    void setFeatureEvidenceCodeList() {
        featureEvidenceCode.addItem("");
        featureEvidenceCode.addItem("IC");
        featureEvidenceCode.addItem("TAS");
        featureEvidenceCode.addItem("IEA");
    }

    void setFeatureAssemblyList() {
        featureAssembly.addItem("");
        featureAssembly.addItem("GRCz12tu");
        featureAssembly.addItem("GRCz11");
    }



}
