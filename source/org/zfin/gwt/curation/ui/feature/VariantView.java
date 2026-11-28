package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.VariantDTO;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.StringTextBox;
import org.zfin.gwt.root.util.StringUtils;

import java.util.HashSet;

/**
 * One variant of a Complex feature on the feature curation form (ZFIN-10623): the variant's type,
 * its chromosome location and its genomic, DNA, transcript and protein mutation details -- the same
 * widgets a single-variant feature uses, one set per variant.
 */
public class VariantView extends Composite implements MutationDetailWidgets {

    /** The types a variant of a Complex feature can have (mockup: Complex Feature Type.v3). */
    static final FeatureTypeEnum[] VARIANT_TYPES = {FeatureTypeEnum.POINT_MUTATION, FeatureTypeEnum.DELETION,
            FeatureTypeEnum.INSERTION, FeatureTypeEnum.INDEL, FeatureTypeEnum.MNV};

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("VariantView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, VariantView> {
    }

    @UiField
    Label variantTypeLabel;
    @UiField
    StringListBox variantTypeBox;
    @UiField
    Anchor cancelLink;
    @UiField
    StringTextBox chromosome;
    @UiField
    StringListBox assembly;
    @UiField
    Label startLocationLabel;
    @UiField
    NumberTextBox startLocation;
    @UiField
    HTML startLocHint;
    @UiField
    HorizontalPanel endLocationPanel;
    @UiField
    NumberTextBox endLocation;
    @UiField
    HTML endLocHint;
    @UiField
    StringListBox evidenceCode;
    @UiField
    GenomicMutationDetailView genomicMutationDetailView;
    @UiField
    MutationDetailDNAView mutationDetailDnaView;
    @UiField
    MutationDetailTranscriptView mutationDetailTranscriptView;
    @UiField
    MutationDetailProteinView mutationDetailProteinView;

    private final AbstractFeatureView featureView;
    private final MutationDetailPresenter mutationDetailPresenter;
    private int variantNumber;
    private String zdbID;
    // set by any edit, cleared when the block is filled from a saved variant
    private boolean changed;
    // the vocabulary lists load asynchronously; a saved variant is shown once they are there
    private boolean listsLoaded;
    private VariantDTO pendingDto;

    VariantView(AbstractFeatureView featureView) {
        initWidget(uiBinder.createAndBindUi(this));
        this.featureView = featureView;

        variantTypeBox.addNull();
        for (FeatureTypeEnum type : VARIANT_TYPES) {
            variantTypeBox.addItem(type.getDisplay(), type.name());
        }
        copyItems(featureView.featureAssembly, assembly);
        copyItems(featureView.featureEvidenceCode, evidenceCode);

        mutationDetailPresenter = new MutationDetailPresenter(this) {
            @Override
            public void handleDirty() {
                onChange();
            }
        };
        genomicMutationDetailView.setPresenter(mutationDetailPresenter);
        mutationDetailDnaView.setPresenter(mutationDetailPresenter);
        mutationDetailTranscriptView.setPresenter(mutationDetailPresenter);
        mutationDetailProteinView.setPresenter(mutationDetailPresenter);
        // accession numbers are checked on blur, which does not go through handleChanges()
        mutationDetailDnaView.zfinAccessionBox.getPresenter().addBlurHandler(event -> onChange());
        mutationDetailProteinView.zfinAccessionBox.getPresenter().addBlurHandler(event -> onChange());

        mutationDetailPresenter.go(() -> {
            listsLoaded = true;
            if (pendingDto != null) {
                VariantDTO dto = pendingDto;
                pendingDto = null;
                populate(dto);
            }
        });
        applyVariantType();
    }

    private static void copyItems(ListBox from, ListBox to) {
        to.clear();
        for (int i = 0; i < from.getItemCount(); i++) {
            to.addItem(from.getItemText(i), from.getValue(i));
        }
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

    /**
     * @param removable Variants 1 and 2 are always there (a Complex feature has at least two);
     *                  any further variant can be cancelled
     */
    void setVariantNumber(int variantNumber, boolean removable) {
        this.variantNumber = variantNumber;
        variantTypeLabel.setText("Variant " + variantNumber + " Type");
        cancelLink.setVisible(removable);
    }

    @UiHandler("cancelLink")
    void onClickCancel(@SuppressWarnings("unused") ClickEvent event) {
        featureView.removeVariantView(this);
    }

    @UiHandler("variantTypeBox")
    void onChangeVariantType(@SuppressWarnings("unused") ChangeEvent event) {
        applyVariantType();
        onLocationChange();
    }

    @UiHandler("chromosome")
    void onKeyUpChromosome(@SuppressWarnings("unused") KeyUpEvent event) {
        onLocationChange();
    }

    @UiHandler("chromosome")
    void onChangeChromosome(@SuppressWarnings("unused") ChangeEvent event) {
        onLocationChange();
    }

    @UiHandler("assembly")
    void onChangeAssembly(@SuppressWarnings("unused") ChangeEvent event) {
        onLocationChange();
    }

    @UiHandler("startLocation")
    void onChangeStartLocation(@SuppressWarnings("unused") ChangeEvent event) {
        if (getVariantType() == FeatureTypeEnum.POINT_MUTATION) {
            endLocation.setNumber(startLocation.getBoxValue());
        }
        onLocationChange();
    }

    @UiHandler("endLocation")
    void onChangeEndLocation(@SuppressWarnings("unused") ChangeEvent event) {
        onLocationChange();
    }

    @UiHandler("evidenceCode")
    void onChangeEvidenceCode(@SuppressWarnings("unused") ChangeEvent event) {
        onChange();
    }

    private void onLocationChange() {
        FeatureTypeEnum type = getVariantType();
        String typeName = type == null ? null : type.name();
        Integer start = startLocation.getBoxValue();
        Integer end = type == FeatureTypeEnum.POINT_MUTATION ? start : endLocation.getBoxValue();
        featureView.presenter.fetchReferenceSequence(typeName, chromosome.getText(), assembly.getSelectedItemText(),
                start, end, genomicMutationDetailView);
        AbstractFeaturePresenter.autoCalcDeletionLength(typeName, start, end, mutationDetailDnaView);
        onChange();
    }

    private void onChange() {
        changed = true;
        featureView.onVariantChanged();
    }

    FeatureTypeEnum getVariantType() {
        return FeatureTypeEnum.getTypeForName(variantTypeBox.getSelected());
    }

    /** Show the location hints and mutation detail fields that go with the variant's type. */
    private void applyVariantType() {
        FeatureTypeEnum type = getVariantType();
        AbstractFeatureView.MutationDetailType detailType = type == null ? null
                : AbstractFeatureView.MutationDetailType.getType(type.name());
        // the detail widgets start hidden; the main form shows them in showMutationDetail()
        boolean showDetails = detailType != null;
        genomicMutationDetailView.changePanel.setVisible(showDetails);
        mutationDetailDnaView.changePanel.setVisible(showDetails);
        mutationDetailTranscriptView.changePanel.setVisible(showDetails);
        mutationDetailProteinView.proteinChangePanel.setVisible(showDetails);
        if (showDetails) {
            mutationDetailDnaView.showFields(detailType);
            genomicMutationDetailView.showFields(detailType, false);
        }
        AbstractFeatureView.configureLocationFields(type, startLocationLabel, endLocationPanel,
                startLocHint, endLocHint, startLocation, endLocation);
    }

    boolean isChanged() {
        return changed;
    }

    VariantDTO getDto() {
        if (pendingDto != null) {
            // not shown yet: the saved variant is unchanged
            return pendingDto;
        }
        FeatureTypeEnum type = getVariantType();
        VariantDTO dto = new VariantDTO();
        dto.setZdbID(zdbID);
        dto.setVariantNumber(variantNumber);
        dto.setVariantType(type);
        dto.setChromosome(emptyToNull(chromosome.getText()));
        dto.setAssembly(emptyToNull(assembly.getSelectedItemText()));
        dto.setStartLocation(startLocation.getBoxValue());
        dto.setEndLocation(type == FeatureTypeEnum.POINT_MUTATION ? startLocation.getBoxValue() : endLocation.getBoxValue());
        dto.setEvidence(emptyToNull(evidenceCode.getSelectedItemText()));
        dto.setFgmdChangeDTO(genomicMutationDetailView.getDto());
        dto.setDnaChangeDTO(mutationDetailDnaView.getDto());
        dto.setProteinChangeDTO(mutationDetailProteinView.getDto());
        dto.setTranscriptChangeDTOSet(new HashSet<>(mutationDetailPresenter.getDtoSet()));
        return dto;
    }

    private static String emptyToNull(String value) {
        return StringUtils.isEmptyTrim(value) ? null : value.trim();
    }

    /** Fill the block from a saved variant (editing), or from the form's own fields (type changed to Complex). */
    void populate(VariantDTO dto) {
        zdbID = dto.getZdbID();
        if (!listsLoaded) {
            pendingDto = dto;
            return;
        }
        FeatureTypeEnum type = dto.getVariantType();
        variantTypeBox.setIndexForValue(type == null ? null : type.name());
        chromosome.setText(dto.getChromosome() == null ? "" : dto.getChromosome());
        selectText(assembly, dto.getAssembly());
        startLocation.setNumber(dto.getStartLocation());
        endLocation.setNumber(dto.getEndLocation());
        selectText(evidenceCode, dto.getEvidence());
        applyVariantType();
        // a variant curated before variants had types shows both sequences
        genomicMutationDetailView.populateFields(dto.getFgmdChangeDTO(), type == null ? FeatureTypeEnum.INDEL : type, false);
        mutationDetailDnaView.populateFields(dto.getDnaChangeDTO());
        mutationDetailProteinView.populateFields(dto.getProteinChangeDTO());
        mutationDetailPresenter.setDtoSet(dto.getTranscriptChangeDTOSet());
        changed = false;
    }

    private static void selectText(StringListBox listBox, String text) {
        if (text == null || listBox.setIndexForText(text) < 0) {
            listBox.setSelectedIndex(0);
        }
    }

    /**
     * The defaults a single-variant feature gets on save (AbstractFeatureView.onclickSaveButton):
     * a protein consequence from the amino acids entered, a transcript consequence for a point mutation.
     */
    void applySaveDefaults() {
        FeatureTypeEnum type = getVariantType();
        AbstractFeatureView.applyMutationDetailDefaults(this, type == null ? null : type.name());
    }

    /** @return the problem with this variant's protein change, or null */
    String validateMutationDetails() {
        if (mutationDetailProteinView.hasAASelected() && mutationDetailProteinView.hasPlusMinusUsed()) {
            return "Variant " + variantNumber + ": Cannot select Amino Acids and defines plus / minus fields";
        }
        FeatureDTO proteinOnly = new FeatureDTO();
        proteinOnly.setProteinChangeDTO(mutationDetailProteinView.getDto());
        String error = mutationDetailPresenter.isValid(proteinOnly);
        return error == null ? null : "Variant " + variantNumber + ": " + error;
    }
}
