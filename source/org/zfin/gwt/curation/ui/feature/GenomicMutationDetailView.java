package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

import org.zfin.gwt.curation.ui.AbstractViewComposite;
import org.zfin.gwt.root.dto.FeatureGenomeMutationDetailChangeDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.MutationDetailDnaChangeDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.WidgetUtil;


import java.util.HashSet;
import java.util.Set;

public class GenomicMutationDetailView extends AbstractViewComposite {


    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("GenomicMutationDetailView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, GenomicMutationDetailView> {
    }


    @UiField
    Grid genomicDetailsTable;
    @UiField
    FlowPanel changePanel;
    @UiField
    StringTextBox seqReference;
    @UiField
    StringTextBox seqVariant;
    @UiField
    Button reverseComplRefButton;
    @UiField
    Button reverseComplVarButton;
    @UiField
    TableRowElement sequenceOfReferenceRow;
    @UiField
    TableRowElement sequenceOfVariantRow;

    public GenomicMutationDetailView() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @UiHandler("seqReference")
    void onKeyDownseqRef(@SuppressWarnings("unused") KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)

            seqReference.setText(seqReference.getText().toUpperCase());

        handleChanges();

    }
    @UiHandler("seqReference")
    void onKeyChangeSeqReference(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("seqVariant")
    void onKeyDownseqVar(@SuppressWarnings("unused") KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)

            seqVariant.setText(seqVariant.getText().toUpperCase());

        handleChanges();

    }
    @UiHandler("seqVariant")
    void onKeyChangeSeqVar(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("reverseComplRefButton")
    void onClickComplRefButton(@SuppressWarnings("unused") ClickEvent event) {

        seqReference.setText(reverseComplement(seqReference.getText().toUpperCase()));


    }

    @UiHandler("reverseComplVarButton")
    void onClickComplSeqButton(@SuppressWarnings("unused") ClickEvent event) {
        seqVariant.setText(reverseComplement(seqVariant.getText().toUpperCase()));

    }


    private String reverseComplement(String sequence) {

        StringBuilder sequence1 = new StringBuilder();

        // append a string into StringBuilder input1
        sequence1.append(sequence);

        // reverse StringBuilder input1
        sequence1 = sequence1.reverse();

        // print reversed String


        String complemented = "";
        for (int i = 0; i < sequence1.length(); i++) {
            if (sequence1.charAt(i) == 'A') {
                complemented = complemented + "T";
            } else if (sequence1.charAt(i) == 'T') {
                complemented = complemented + "A";
            } else if (sequence1.charAt(i) == 'C') {
                complemented = complemented + "G";
            } else if (sequence1.charAt(i) == 'G') {
                complemented = complemented + "C";
            }
        }
        return complemented;
    }

    private void showRow(TableRowElement row, boolean show) {
        row.getStyle().setDisplay(show ? Style.Display.TABLE_ROW : Style.Display.NONE);
    }

    public FeatureGenomeMutationDetailChangeDTO getDto() {
        if (!hasEnteredValues())
            return null;
        FeatureGenomeMutationDetailChangeDTO dto = new FeatureGenomeMutationDetailChangeDTO();

        dto.setFgmdSeqRef(seqReference.getBoxValue());
        dto.setFgmdSeqVar(seqVariant.getBoxValue());

        return dto;
    }

    public void showFields(FeatureAddView.MutationDetailType type,boolean knownInsertionSite) {
        switch (type) {
            case POINT_MUTATION:

                    showBoth();

                break;
            case INSERTION:
                showVariantSeq();
                break;
            case DELETION:
                showReferenceSeq();
                break;
            case TRANSGENIC_INSERTION:
               // showTgFields();
                if (knownInsertionSite) {

                    showBoth();
                }
                else{
                    showTgFields();
                }
                break;
            case SEQUENCE_VARIANT:
                showTgFields();
                break;
            case INDEL:
                showBoth();
                break;
            case MNV:
                showBoth();
                break;
        }
    }

    public void resetGUI() {

        seqReference.clear();
        seqVariant.clear();

        clearError();
    }

    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();

        fields.add(seqReference);
        fields.add(seqVariant);

        return fields;
    }

    public void showVariantSeq() {

        showRow(sequenceOfReferenceRow, false);
        showRow(sequenceOfVariantRow, true);

    }

    public void showReferenceSeq() {

        showRow(sequenceOfVariantRow, false);
        showRow(sequenceOfReferenceRow, true);

    }

    public void showBoth() {

        showRow(sequenceOfVariantRow, true);
        showRow(sequenceOfReferenceRow, true);

    }

    public void showTgFields() {

        showRow(sequenceOfVariantRow, false);
        showRow(sequenceOfReferenceRow, false);

    }


    public void populateFields(FeatureGenomeMutationDetailChangeDTO dto, FeatureTypeEnum type, Boolean knownInsSite) {
        if (dto == null) {
            seqReference.clear();
            seqVariant.clear();
            return;
        }
        seqReference.setText(dto.getFgmdSeqRef());
        seqVariant.setText(dto.getFgmdSeqVar());
        showFields(type,knownInsSite);
    }

}


