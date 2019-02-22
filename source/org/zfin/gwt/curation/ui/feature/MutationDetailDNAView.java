package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.curation.ui.AbstractViewComposite;
import org.zfin.gwt.root.dto.MutationDetailDnaChangeDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.HashSet;
import java.util.Set;

public class MutationDetailDNAView extends AbstractViewComposite {

    public static final String SPLICE_JUNCTION = "SO:0001421";
    public static final String THREE_PRIME_SPLICE = "SO:0000164";
    public static final String FIVE_PRIME_SPLICE = "SO:0000163";
    public static final String EXON = "SO:0000147";
    public static final String INTRON = "SO:0000188";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("MutationDetailDNAView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, MutationDetailDNAView> {
    }

    @UiField
    StringListBox nucleotideChangeList;
    @UiField
    StringListBox localizationTerm;
    @UiField
    NumberTextBox exonNumber;
    @UiField
    NumberTextBox intronNumber;
    @UiField
    Grid dnaDataTable;
    @UiField
    NumberTextBox positionStart;
    @UiField
    NumberTextBox positionEnd;
    @UiField
    Label exonLabel;
    @UiField
    Label intronLabel;
    @UiField
    FlowPanel changePanel;
    @UiField
    NumberTextBox minusBasePair;
    @UiField
    NumberTextBox plusBasePair;
   /* @UiField
    StringTextBox insertedSequence;
    @UiField
    StringTextBox deletedSequence;*/
    @UiField
    TableRowElement nucleotideChangeRow;
    @UiField
    TableRowElement insertionLengthRow;
   /* @UiField
    TableRowElement insertionSequenceRow;*/
    @UiField
    TableRowElement deletionLengthRow;
   /* @UiField
    TableRowElement deletionSequenceRow;*/
    @UiField
    Label positionDash;
    @UiField
    ZfinAccessionBox zfinAccessionBox;

    public MutationDetailDNAView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("positionStart")
    void onKeyChangePositionStart(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("positionStart")
    void onChangePositionStart(@SuppressWarnings("unused") ChangeEvent event) {
        if (validateNumber(positionStart)) {
            if (positionEnd.isVisible())
                validateStartEnd(positionStart, positionEnd);
        }
        handleChanges();
    }

    @UiHandler("positionEnd")
    void onKeyChangePositionEnd(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("positionEnd")
    void onChangePositionEnd(@SuppressWarnings("unused") ChangeEvent event) {
        if (validateNumber(positionEnd)) {
            validateStartEnd(positionStart, positionEnd);
        }
        handleChanges();
    }

    @UiHandler("minusBasePair")
    void onChangeMinusBasePair(@SuppressWarnings("unused") KeyUpEvent event) {
        validateNumber(minusBasePair);
        handleChanges();
    }

    @UiHandler("plusBasePair")
    void onChangePlusBasePair(@SuppressWarnings("unused") KeyUpEvent event) {
        validateNumber(plusBasePair);
        handleChanges();
    }

    /*@UiHandler("insertedSequence")
    void onChangeInsertedSequence(KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("deletedSequence")
    void onChangeDeletedSequence(KeyUpEvent event) {
        handleChanges();
    }*/

    @UiHandler("exonNumber")
    void onChangeExonNumber(@SuppressWarnings("unused") KeyUpEvent event) {
        validateNumber(exonNumber);
        handleChanges();
    }

    @UiHandler("intronNumber")
    void onChangeIntronNumber(@SuppressWarnings("unused") KeyUpEvent event) {
        validateNumber(intronNumber);
        handleChanges();
    }

    @UiHandler("nucleotideChangeList")
    void onNucleotideChange(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("localizationTerm")
    void onChangeLocalization(@SuppressWarnings("unused") ChangeEvent event) {
        if (localizationTerm.getSelected().equals(SPLICE_JUNCTION) ||
                localizationTerm.getSelected().equals(FIVE_PRIME_SPLICE) ||
                localizationTerm.getSelected().equals(THREE_PRIME_SPLICE)) {
            WidgetUtil.showHideBoxField(intronNumber, true);
            WidgetUtil.showHideBoxField(exonNumber, true);
        } else if (localizationTerm.getSelected().equals(EXON)) {
            WidgetUtil.showHideBoxField(intronNumber, false);
            WidgetUtil.showHideBoxField(exonNumber, true);
        } else if (localizationTerm.getSelected().equals(INTRON)) {
            WidgetUtil.showHideBoxField(intronNumber, true);
            WidgetUtil.showHideBoxField(exonNumber, false);
        } else {
            WidgetUtil.showHideBoxField(intronNumber, false);
            WidgetUtil.showHideBoxField(exonNumber, false);
        }
        handleChanges();
    }

    public void showPointMutationUI() {
        showRow(nucleotideChangeRow, true);
        showRow(insertionLengthRow, false);
        //0showRow(insertionSequenceRow, false);
        showRow(deletionLengthRow, false);
        //showRow(deletionSequenceRow, false);
        positionStart.setVisible(true);
        positionDash.setVisible(false);
        positionEnd.setVisible(false);
    }

    public void showPlusBP() {
        showRow(nucleotideChangeRow, false);
        showRow(insertionLengthRow, true);
        //showRow(insertionSequenceRow, true);
        showRow(deletionLengthRow, false);
        //showRow(deletionSequenceRow, false);
        showHideBaseFields(true);
    }

    public void showMinusBP() {
        showRow(nucleotideChangeRow, false);
        showRow(insertionLengthRow, false);
        //showRow(insertionSequenceRow, false);
        showRow(deletionLengthRow, true);
        //showRow(deletionSequenceRow, true);
        showHideBaseFields(true);
    }

    public void showPlusMinusBP() {
        showRow(nucleotideChangeRow, false);
        showRow(insertionLengthRow, true);
        //showRow(insertionSequenceRow, true);
        showRow(deletionLengthRow, true);
        //showRow(deletionSequenceRow, true);
        showHideBaseFields(true);
    }

    public void showTgFields() {
        showRow(nucleotideChangeRow, false);
        showRow(insertionLengthRow, false);
        //showRow(insertionSequenceRow, false);
        showRow(deletionLengthRow, false);
        //showRow(deletionSequenceRow, false);
        showHideBaseFields(false);
    }

    private void showHideBaseFields(boolean show) {
        positionStart.setVisible(show);
        positionEnd.setVisible(show);
        positionDash.setVisible(show);
        zfinAccessionBox.setVisible(show);
    }

    private void showRow(TableRowElement row, boolean show) {
        row.getStyle().setDisplay(show ? Style.Display.TABLE_ROW : Style.Display.NONE);
    }

    public MutationDetailDnaChangeDTO getDto() {
        if (!hasEnteredValues())
            return null;
        MutationDetailDnaChangeDTO dto = new MutationDetailDnaChangeDTO();
        dto.setChangeTermOboId(WidgetUtil.getStringFromListBox(nucleotideChangeList));
        dto.setLocalizationTermOboID(WidgetUtil.getStringFromListBox(localizationTerm));
        dto.setPositionStart(positionStart.getBoxValue());
        // if positionEnd is invisible it means: end equals start.
        if (positionEnd.isVisible())
            dto.setPositionEnd(positionEnd.getBoxValue());
        else
            dto.setPositionEnd(dto.getPositionStart());
        dto.setNumberAddedBasePair(plusBasePair.getBoxValue());
        dto.setNumberRemovedBasePair(minusBasePair.getBoxValue());
       /* dto.setInsertedSequence(insertedSequence.getBoxValue());
        dto.setDeletedSequence(deletedSequence.getBoxValue());*/
        dto.setSequenceReferenceAccessionNumber(WidgetUtil.getStringFromField(zfinAccessionBox.getAccessionNumber()));
        dto.setExonNumber(exonNumber.getBoxValue());
        dto.setIntronNumber(intronNumber.getBoxValue());
        return dto;
    }

    public void showFields(FeatureAddView.MutationDetailType type) {
        switch (type) {
            case POINT_MUTATION:
                showPointMutationUI();
                break;
            case INSERTION:
                showPlusBP();
                break;
            case DELETION:
                showMinusBP();
                break;
            case INDEL:
                showPlusMinusBP();
                break;
            case TRANSGENIC_INSERTION:
                showTgFields();
                break;
            case SEQUENCE_VARIANT:
                showTgFields();
                break;
        }
    }

    public void resetGUI() {
        nucleotideChangeList.setSelectedIndex(0);
        positionStart.clear();
        positionEnd.clear();
        localizationTerm.setSelectedIndex(0);
        exonNumber.setVisible(false);
        intronNumber.setVisible(false);
        exonNumber.clear();
        intronNumber.clear();
        plusBasePair.clear();
        minusBasePair.clear();
        /*insertedSequence.clear();
        deletedSequence.clear();*/
        zfinAccessionBox.clear();
        zfinAccessionBox.setFlagVisibility(false);
        clearError();
    }

    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();
        fields.add(nucleotideChangeList);
        fields.add(localizationTerm);
        fields.add(positionStart);
        fields.add(positionEnd);
        fields.add(exonNumber);
        fields.add(intronNumber);
        fields.add(plusBasePair);
        fields.add(minusBasePair);
      /*  fields.add(insertedSequence);
        fields.add(deletedSequence);*/
        fields.add(zfinAccessionBox.getAccessionNumber());
        return fields;
    }


    public void populateFields(MutationDetailDnaChangeDTO dto) {
        if (dto == null) {
            nucleotideChangeList.setSelectedIndex(0);
            localizationTerm.setSelectedIndex(0);
            intronNumber.clear();
            exonNumber.clear();
            positionStart.clear();
            positionEnd.clear();
            plusBasePair.clear();
            minusBasePair.clear();
            /*insertedSequence.clear();
            deletedSequence.clear();*/
            return;
        }
        nucleotideChangeList.setIndexForValue(dto.getChangeTermOboId());
        localizationTerm.setIndexForValue(dto.getLocalizationTermOboID());
        plusBasePair.setNumber(dto.getNumberAddedBasePair());
        minusBasePair.setNumber(dto.getNumberRemovedBasePair());
      /*  insertedSequence.setText(dto.getInsertedSequence());
        deletedSequence.setText(dto.getDeletedSequence());*/
        positionStart.setNumber(dto.getPositionStart());
        positionEnd.setNumber(dto.getPositionEnd());
        exonNumber.setNumber(dto.getExonNumber());
        intronNumber.setNumber(dto.getIntronNumber());
        zfinAccessionBox.getAccessionNumber().setText(dto.getSequenceReferenceAccessionNumber());
        onChangeLocalization(null);
    }

    public void resetMessages() {
        zfinAccessionBox.setFlagVisibility(false);
        clearError();
    }


}


