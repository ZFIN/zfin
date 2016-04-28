package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.MutationDetailDnaChangeDTO;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.StringTextBox;
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

    private MutationDetailPresenter presenter;
    @UiField
    StringListBox nucleotideChangeList;
    @UiField
    StringTextBox sequenceOfReference;
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
    @UiField
    Grid dataTable;
    @UiField
    HTML validSequenceCharacter;
    @UiField
    HTML faultySequenceCharacter;
    @UiField
    Label positionDash;

    public MutationDetailDNAView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("positionStart")
    void onBlurPositionStart(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(positionStart);
    }

    @UiHandler("positionEnd")
    void onBlurPositionEnd(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(positionEnd);
    }

    @UiHandler("minusBasePair")
    void onBlurMinusBasePair(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(minusBasePair);
    }

    @UiHandler("plusBasePair")
    void onBlurPlusBasePair(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(plusBasePair);
    }

    @UiHandler("exonNumber")
    void onBlurExonNumber(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(exonNumber);
    }

    @UiHandler("intronNumber")
    void onBlurIntronNumber(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(intronNumber);
    }

    @UiHandler("sequenceOfReference")
    void onBlurSequence(@SuppressWarnings("unused") BlurEvent event) {
        if (!sequenceOfReference.isEmpty())
            presenter.checkValidAccession(sequenceOfReference.getBoxValue(), "DNA");
        else {
            faultySequenceCharacter.setVisible(false);
            validSequenceCharacter.setVisible(false);
        }

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
    }

    public void showPointMutationUI() {
        dataTable.resizeRows(1);
        dataTable.resizeColumns(1);
        int row = 0;
        dataTable.setWidget(row, 0, nucleotideChangeList);
        positionStart.setVisible(true);
        positionDash.setVisible(false);
        positionEnd.setVisible(false);
    }

    public void showPlusBP() {
        dataTable.resizeRows(1);
        dataTable.resizeColumns(3);
        int row = 0;
        dataTable.setText(row, 0, "plus");
        dataTable.setWidget(row, 1, plusBasePair);
        dataTable.setText(row, 2, "bp");
        showHideBaseFields(true);
    }

    public void showMinusBP() {
        dataTable.resizeRows(1);
        dataTable.resizeColumns(3);
        int row = 0;
        dataTable.setText(row, 0, "minus");
        dataTable.setWidget(row, 1, minusBasePair);
        dataTable.setText(row, 2, "bp");
        showHideBaseFields(true);
    }

    public void showPlusMinusBP() {
        dataTable.resizeRows(2);
        dataTable.resizeColumns(3);
        int row = 0;
        dataTable.setText(row, 0, "plus");
        dataTable.setWidget(row, 1, plusBasePair);
        dataTable.setText(row, 2, "bp");
        row++;
        dataTable.setText(row, 0, "minus");
        dataTable.setWidget(row, 1, minusBasePair);
        dataTable.setText(row, 2, "bp");
        showHideBaseFields(true);
    }

    public void showTgFields() {
        dataTable.resizeRows(0);
        showHideBaseFields(false);
    }

    private void showHideBaseFields(boolean show) {
        positionStart.setVisible(show);
        positionEnd.setVisible(show);
        positionDash.setVisible(show);
        sequenceOfReference.setVisible(show);
    }

    public MutationDetailDnaChangeDTO getDto() {
        if (!hasEnteredValues())
            return null;
        MutationDetailDnaChangeDTO dto = new MutationDetailDnaChangeDTO();
        dto.setChangeTermOboId(WidgetUtil.getStringFromListBox(nucleotideChangeList));
        dto.setLocalizationTermOboID(WidgetUtil.getStringFromListBox(localizationTerm));
        dto.setPositionStart(WidgetUtil.getIntegerFromField(positionStart));
        // if positionEnd is invisible it means: end equals start.
        if (positionEnd.isVisible())
            dto.setPositionEnd(WidgetUtil.getIntegerFromField(positionEnd));
        else
            dto.setPositionEnd(dto.getPositionStart());
        dto.setNumberAddedBasePair(WidgetUtil.getIntegerFromField(plusBasePair));
        dto.setNumberRemovedBasePair(WidgetUtil.getIntegerFromField(minusBasePair));
        dto.setSequenceReferenceAccessionNumber(WidgetUtil.getStringFromField(sequenceOfReference));
        dto.setExonNumber(WidgetUtil.getIntegerFromField(exonNumber));
        dto.setIntronNumber(WidgetUtil.getIntegerFromField(intronNumber));
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
        }
    }

    public void resetGUI() {
        nucleotideChangeList.setSelectedIndex(0);
        positionStart.clear();
        positionEnd.clear();
        sequenceOfReference.clear();
        localizationTerm.setSelectedIndex(0);
        exonNumber.setVisible(false);
        intronNumber.setVisible(false);
        exonNumber.clear();
        intronNumber.clear();
        plusBasePair.clear();
        minusBasePair.clear();
        validSequenceCharacter.setVisible(false);
        faultySequenceCharacter.setVisible(false);
        clearError();
    }

    public Set<Widget> getValueFields() {
        Set<Widget> fields = new HashSet<>();
        fields.add(nucleotideChangeList);
        fields.add(localizationTerm);
        fields.add(positionStart);
        fields.add(positionEnd);
        fields.add(exonNumber);
        fields.add(intronNumber);
        fields.add(plusBasePair);
        fields.add(minusBasePair);
        fields.add(sequenceOfReference);
        return fields;
    }


    public void populateFields(MutationDetailDnaChangeDTO dto) {
        if (dto == null)
            return;
        nucleotideChangeList.setIndexForValue(dto.getChangeTermOboId());
        localizationTerm.setIndexForValue(dto.getLocalizationTermOboID());
        if (dto.getNumberAddedBasePair() != null)
            plusBasePair.setText(dto.getNumberAddedBasePair().toString());
        if (dto.getNumberRemovedBasePair() != null)
            minusBasePair.setText(dto.getNumberRemovedBasePair().toString());
        if (dto.getPositionStart() != null)
            positionStart.setText(dto.getPositionStart().toString());
        if (dto.getPositionEnd() != null)
            positionEnd.setText(dto.getPositionEnd().toString());
        if (dto.getExonNumber() != null)
            exonNumber.setText(dto.getExonNumber().toString());
        if (dto.getIntronNumber() != null)
            intronNumber.setText(dto.getIntronNumber().toString());
        sequenceOfReference.setText(dto.getSequenceReferenceAccessionNumber());
        onChangeLocalization(null);
    }

    public void setPresenter(MutationDetailPresenter presenter) {
        this.presenter = presenter;
    }
}


