package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;

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
    TextArea seqReference;
    @UiField
    RevertibleTextArea<String> seqVariant;
    @UiField
    StringTextBox seqVariantSmall;
    @UiField
    Button reverseComplVarButton;
    @UiField
    TableRowElement sequenceOfReferenceRow;
    @UiField
    TableRowElement sequenceOfReferenceSmallRow;
    @UiField
    TextBox seqReferenceSmall;
    @UiField
    TableRowElement sequenceOfVariantRow;
    @UiField
    TableRowElement sequenceOfVariantSmallRow;

    public GenomicMutationDetailView() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @UiHandler("seqVariant")
    void onKeyChangeSeqVar(@SuppressWarnings("unused") KeyUpEvent event) {
        seqVariantSmall.setText(seqVariant.getText());
        handleChanges();
    }

    @UiHandler("seqVariantSmall")
    void onKeyChangeSeqVarSmall(@SuppressWarnings("unused") KeyUpEvent event) {
        seqVariant.setText(seqVariantSmall.getText());
        handleChanges();
    }

    @UiHandler("reverseComplVarButton")
    void onClickComplSeqButton(@SuppressWarnings("unused") ClickEvent event) {
        String complemented = reverseComplement(seqVariant.getText().toUpperCase());
        seqVariant.setText(complemented);
        seqVariantSmall.setText(complemented);
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

    private String getRefSeqText() {
        boolean smallVisible = sequenceOfReferenceSmallRow.getStyle().getDisplay().equals(Style.Display.TABLE_ROW.getCssName());
        if (smallVisible) {
            return seqReferenceSmall.getText();
        }
        return seqReference.getText();
    }

    private String getVarSeqText() {
        boolean smallVisible = sequenceOfVariantSmallRow.getStyle().getDisplay().equals(Style.Display.TABLE_ROW.getCssName());
        if (smallVisible) {
            return seqVariantSmall.getText();
        }
        return seqVariant.getText();
    }

    public FeatureGenomeMutationDetailChangeDTO getDto() {
        String currentRefText = getRefSeqText();
        boolean hasRefSeq = currentRefText != null && !currentRefText.trim().isEmpty();
        String currentVarText = getVarSeqText();
        boolean hasVarSeq = currentVarText != null && !currentVarText.trim().isEmpty();
        if (!hasRefSeq && !hasVarSeq)
            return null;
        FeatureGenomeMutationDetailChangeDTO dto = new FeatureGenomeMutationDetailChangeDTO();

        dto.setFgmdSeqRef(currentRefText != null ? currentRefText.toUpperCase() : null);
        dto.setFgmdSeqVar(hasVarSeq ? currentVarText.toUpperCase() : null);

        return dto;
    }

    public void showFields(FeatureAddView.MutationDetailType type,boolean knownInsertionSite) {
        switch (type) {
            case POINT_MUTATION:

                    showBothPointMutation();

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
            case INDEL:
                showBoth();
                break;
            case MNV:
                showBoth();
                break;
        }
    }

    public void resetGUI() {

        seqReference.setText("");
        seqReferenceSmall.setText("");
        seqVariant.setText("");
        seqVariantSmall.setText("");

        clearError();
    }

    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();

        fields.add(seqVariant);

        return fields;
    }

    public void showVariantSeq() {

        showRow(sequenceOfReferenceRow, false);
        showRow(sequenceOfReferenceSmallRow, false);
        showRow(sequenceOfVariantRow, true);
        showRow(sequenceOfVariantSmallRow, false);

    }

    public void showReferenceSeq() {

        showRow(sequenceOfVariantRow, false);
        showRow(sequenceOfVariantSmallRow, false);
        showRow(sequenceOfReferenceSmallRow, false);
        showRow(sequenceOfReferenceRow, true);

    }

    public void showBoth() {

        showRow(sequenceOfVariantRow, true);
        showRow(sequenceOfVariantSmallRow, false);
        showRow(sequenceOfReferenceSmallRow, false);
        showRow(sequenceOfReferenceRow, true);

    }

    public void showBothPointMutation() {

        showRow(sequenceOfVariantRow, false);
        showRow(sequenceOfVariantSmallRow, true);
        showRow(sequenceOfReferenceRow, false);
        showRow(sequenceOfReferenceSmallRow, true);

    }

    public void showTgFields() {

        showRow(sequenceOfVariantRow, false);
        showRow(sequenceOfVariantSmallRow, false);
        showRow(sequenceOfReferenceRow, false);
        showRow(sequenceOfReferenceSmallRow, false);

    }


    public void setReferenceSequence(String seq) {
        String value = seq != null ? seq : "";
        seqReference.setText(value);
        seqReferenceSmall.setText(value);
    }

    public void setReferenceSequenceLoading() {
        seqReference.setText("Loading...");
        seqReferenceSmall.setText("Loading...");
    }

    public void populateFields(FeatureGenomeMutationDetailChangeDTO dto, FeatureTypeEnum type, Boolean knownInsSite) {
        if (dto == null) {
            seqReference.setText("");
            seqReferenceSmall.setText("");
            seqVariant.setText("");
            seqVariantSmall.setText("");
            return;
        }
        seqReference.setText(dto.getFgmdSeqRef());
        seqReferenceSmall.setText(dto.getFgmdSeqRef() != null ? dto.getFgmdSeqRef() : "");
        String varValue = dto.getFgmdSeqVar() != null ? dto.getFgmdSeqVar() : "";
        seqVariant.setText(varValue);
        seqVariantSmall.setText(varValue);
        switch (type) {
            case POINT_MUTATION:

                showBothPointMutation();

                break;
            case INSERTION:
                showVariantSeq();
                break;
            case DELETION:
                showReferenceSeq();
                break;
            case TRANSGENIC_INSERTION:
                // showTgFields();
                if (knownInsSite) {

                    showBoth();
                }
                else{
                    showTgFields();
                }
                break;
            case INDEL:
                showBoth();
                break;
            case MNV:
                showBoth();
                break;
        }



    }

}


