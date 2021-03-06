package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
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
import org.zfin.gwt.root.dto.MutationDetailProteinChangeDTO;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.ZfinAccessionBox;
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
    Label positionDash;
    @UiField
    ZfinAccessionBox zfinAccessionBox;

    public MutationDetailProteinView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("proteinTermList")
    void onWTAaConsequenceChange(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("proteinWTTermList")
    void onWTAaChange(@SuppressWarnings("unused") ChangeEvent event) {
        useProteinListControls();
        handleChanges();
    }

    @UiHandler("proteinMutatedTerm")
    void onMutatedAaChange(@SuppressWarnings("unused") ChangeEvent event) {
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
    void onKeyChangePositionStart(@SuppressWarnings("unused") KeyUpEvent event) {
        handleChanges();
    }

    @UiHandler("positionStart")
    void onChangePositionStart(@SuppressWarnings("unused") ChangeEvent event) {
        if (validateNumber(positionStart)) {
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

    @UiHandler("minusAminoAcid")
    void onBlurMinusBasePair(@SuppressWarnings("unused") KeyUpEvent event) {
        validateNumber(minusAminoAcid);
        handleChanges();
        usePlusMinusControls();
        if (!minusAminoAcid.isEmpty())
            plusAminoAcid.clear();
    }

    @UiHandler("plusAminoAcid")
    void onBlurPlusBasePair(@SuppressWarnings("unused") KeyUpEvent event) {
        validateNumber(plusAminoAcid);
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
        dto.setSequenceReferenceAccessionNumber(WidgetUtil.getStringFromField(zfinAccessionBox.getAccessionNumber()));

        return dto;
    }


    public void resetGUI() {
        proteinTermList.setSelectedIndex(0);
        proteinWTTermList.setSelectedIndex(0);
        proteinMutatedTerm.setSelectedIndex(0);
        zfinAccessionBox.clear();
        positionEnd.clear();
        positionStart.clear();
        minusAminoAcid.clear();
        plusAminoAcid.clear();
        zfinAccessionBox.setFlagVisibility(false);
    }


    @Override
    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();
        fields.add(proteinMutatedTerm);
        fields.add(proteinWTTermList);
        fields.add(positionStart);
        fields.add(positionEnd);
        fields.add(proteinTermList);
        fields.add(minusAminoAcid);
        fields.add(plusAminoAcid);
        fields.add(zfinAccessionBox.getAccessionNumber());
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
        if (dto == null) {
            proteinTermList.setSelectedIndex(0);
            proteinWTTermList.setSelectedIndex(0);
            proteinMutatedTerm.setSelectedIndex(0);
            plusAminoAcid.clear();
            minusAminoAcid.clear();
            positionStart.clear();
            positionEnd.clear();
            return;
        }
        proteinTermList.setIndexForValue(dto.getConsequenceTermOboID());
        proteinWTTermList.setIndexForValue(dto.getWildtypeAATermOboID());
        proteinMutatedTerm.setIndexForValue(dto.getMutantAATermOboID());
        plusAminoAcid.setNumber(dto.getNumberAddedAminoAcid());
        minusAminoAcid.setNumber(dto.getNumberRemovedAminoAcid());
        positionStart.setNumber(dto.getPositionStart());
        positionEnd.setNumber(dto.getPositionEnd());
        zfinAccessionBox.getAccessionNumber().setText(dto.getSequenceReferenceAccessionNumber());
    }

    public boolean hasStopCodon() {
        return proteinMutatedTerm.getSelectedIndex() == 1;
    }

    public void resetMessages() {
        zfinAccessionBox.setFlagVisibility(false);
        clearError();
    }

}


