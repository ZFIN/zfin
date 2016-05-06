package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.MutationDetailProteinChangeDTO;
import org.zfin.gwt.root.ui.IsDirty;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.StringTextBox;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.HashSet;
import java.util.Set;

public class MutationDetailProteinView extends AbstractViewComposite {

    public static final String AMINO_ACID_SUBSTITUTION = "amino acid substitution";
    public static final String AMINO_ACID_INSERTION = "amino acid insertion";
    public static final String AMINO_ACID_DELETION = "amino acid deletion";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("MutationDetailProteinView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, MutationDetailProteinView> {
    }

    @UiField
    Grid proteinDataTable;
    @UiField
    StringListBox proteinMutatedTerm;
    @UiField
    StringListBox proteinWTTermList;
    @UiField
    StringListBox proteinTermList;
    @UiField
    StringTextBox sequenceOfReference;
    @UiField
    NumberTextBox positionEnd;
    @UiField
    NumberTextBox positionStart;
    @UiField
    FlowPanel proteinChangePanel;
    @UiField
    NumberTextBox minusAminoAcid;
    @UiField
    NumberTextBox plusAminoAcid;
    @UiField
    HTML validSequenceCharacter;
    @UiField
    HTML faultySequenceCharacter;
    @UiField
    Label positionDash;

    public MutationDetailProteinView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("proteinTermList")
    void onWTAaConsequenceChange(ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("proteinWTTermList")
    void onWTAaChange(ChangeEvent event) {
        useProteinListControls();
        handleChanges();
    }

    @UiHandler("proteinMutatedTerm")
    void onMutatedAaChange(ChangeEvent event) {
        useProteinListControls();
        handleChanges();
    }

    private void useProteinListControls() {
        if (hasAASelected()) {
            positionEnd.setVisible(false);
            positionDash.setVisible(false);
            plusAminoAcid.setText("");
            minusAminoAcid.setText("");
        } else {
            positionEnd.setVisible(true);
            positionDash.setVisible(true);
        }
    }

    @UiHandler("positionStart")
    void onBlurPositionStart(@SuppressWarnings("unused") BlurEvent event) {
        if (validateNumber(positionStart))
            handleChanges();
    }

    @UiHandler("positionEnd")
    void onBlurPositionEnd(@SuppressWarnings("unused") BlurEvent event) {
        if (validateNumber(positionEnd))
            handleChanges();
    }

    @UiHandler("minusAminoAcid")
    void onBlurMinusBasePair(@SuppressWarnings("unused") BlurEvent event) {
        if (validateNumber(minusAminoAcid))
            handleChanges();
        usePlusMinusControls();
        if (!minusAminoAcid.isEmpty())
            plusAminoAcid.clear();
    }

    @UiHandler("plusAminoAcid")
    void onBlurPlusBasePair(@SuppressWarnings("unused") BlurEvent event) {
        if (validateNumber(plusAminoAcid))
            handleChanges();
        usePlusMinusControls();
        if (!plusAminoAcid.isEmpty())
            minusAminoAcid.clear();
    }

    private void usePlusMinusControls() {
        if (!plusAminoAcid.isEmpty() || !minusAminoAcid.isEmpty()) {
            proteinWTTermList.setSelectedIndex(0);
            proteinMutatedTerm.setSelectedIndex(0);
            positionEnd.setVisible(true);
            positionDash.setVisible(true);
        }
    }

    @UiHandler("sequenceOfReference")
    void onBlurSequence(@SuppressWarnings("unused") BlurEvent event) {
        if (!sequenceOfReference.isEmpty())
            presenter.checkValidAccession(sequenceOfReference.getBoxValue(), "Polypeptide");
        else {
            faultySequenceCharacter.setVisible(false);
            validSequenceCharacter.setVisible(false);
        }
        handleChanges();
    }


    public MutationDetailProteinChangeDTO getDto() {
        if (!hasEnteredValues())
            return null;
        MutationDetailProteinChangeDTO dto = new MutationDetailProteinChangeDTO();
        dto.setConsequenceTermOboID(WidgetUtil.getStringFromListBox(proteinTermList));
        dto.setWildtypeAATermOboID(WidgetUtil.getStringFromListBox(proteinWTTermList));
        dto.setMutantAATermOboID(WidgetUtil.getStringFromListBox(proteinMutatedTerm));
        dto.setPositionStart(positionStart.getBoxValue());
        // if positionEnd is invisible it means: end equals start.
        if (positionEnd.isVisible())
            dto.setPositionEnd(positionEnd.getBoxValue());
        else
            dto.setPositionEnd(dto.getPositionStart());
        dto.setNumberAddedAminoAcid(plusAminoAcid.getBoxValue());
        dto.setNumberRemovedAminoAcid(minusAminoAcid.getBoxValue());
        dto.setSequenceReferenceAccessionNumber(WidgetUtil.getStringFromField(sequenceOfReference));

        return dto;
    }


    public void resetGUI() {
        proteinTermList.setSelectedIndex(0);
        proteinWTTermList.setSelectedIndex(0);
        proteinMutatedTerm.setSelectedIndex(0);
        sequenceOfReference.clear();
        positionEnd.clear();
        positionStart.clear();
        minusAminoAcid.clear();
        plusAminoAcid.clear();
        validSequenceCharacter.setVisible(false);
        faultySequenceCharacter.setVisible(false);
    }


    @Override
    public Set<Widget> getValueFields() {
        Set<Widget> fields = new HashSet<>();
        fields.add(proteinMutatedTerm);
        fields.add(proteinWTTermList);
        fields.add(positionStart);
        fields.add(positionEnd);
        fields.add(proteinTermList);
        fields.add(minusAminoAcid);
        fields.add(plusAminoAcid);
        fields.add(sequenceOfReference);
        return fields;
    }

    public Set<IsDirty> getIsDirtyFields() {
        Set<IsDirty> fields = new HashSet<>();
        for (Widget widget : getValueFields())
            fields.add((IsDirty) widget);
        return fields;
    }

    public boolean hasAASelected() {
        return proteinWTTermList.getSelectedIndex() > 0 || proteinMutatedTerm.getSelectedIndex() > 0;
    }

    public boolean hasNonStopAASelected() {
        return proteinWTTermList.getSelectedIndex() > 0 && proteinMutatedTerm.getSelectedIndex() > 1;
    }

    public boolean hasPlusMinusUsed() {
        return !plusAminoAcid.isEmpty() || !minusAminoAcid.isEmpty();
    }

    public boolean hasPlusFieldOnly() {
        return !plusAminoAcid.isEmpty() && minusAminoAcid.isEmpty();
    }

    public boolean hasMinusFieldOnly() {
        return plusAminoAcid.isEmpty() && !minusAminoAcid.isEmpty();
    }

    public void populateFields(MutationDetailProteinChangeDTO dto) {
        if (dto == null)
            return;
        proteinTermList.setIndexForValue(dto.getConsequenceTermOboID());
        proteinWTTermList.setIndexForValue(dto.getWildtypeAATermOboID());
        proteinMutatedTerm.setIndexForValue(dto.getMutantAATermOboID());
        if (dto.getNumberAddedAminoAcid() != null)
            plusAminoAcid.setText(dto.getNumberAddedAminoAcid().toString());
        if (dto.getNumberRemovedAminoAcid() != null)
            minusAminoAcid.setText(dto.getNumberRemovedAminoAcid().toString());
        if (dto.getPositionStart() != null)
            positionStart.setText(dto.getPositionStart().toString());
        if (dto.getPositionEnd() != null)
            positionEnd.setText(dto.getPositionEnd().toString());
        sequenceOfReference.setText(dto.getSequenceReferenceAccessionNumber());
    }

    public boolean hasStopCodon() {
        return proteinMutatedTerm.getSelectedIndex() == 1;
    }

}


