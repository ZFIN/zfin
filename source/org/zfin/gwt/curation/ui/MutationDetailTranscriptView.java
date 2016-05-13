package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import org.zfin.gwt.root.dto.MutationDetailTranscriptChangeDTO;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.HashSet;
import java.util.Set;

public class MutationDetailTranscriptView extends AbstractViewComposite {

    public static final String LOSS = "exon loss";
    public static final String GAIN = "intron gain";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("MutationDetailTranscriptView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, MutationDetailTranscriptView> {
    }

    @UiField
    StringListBox consequenceList;
    @UiField
    NumberTextBox exonNumber;
    @UiField
    NumberTextBox intronNumber;
    @UiField
    FlowPanel changePanel;
    @UiField
    Button addConsequenceButton;
    @UiField
    Grid dataTable;

    public MutationDetailTranscriptView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("addConsequenceButton")
    void onClickSaveButton(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addTranscriptConsequence(getDtoFromForm());
        resetGUI();
        handleChanges();
    }

    @UiHandler("exonNumber")
    void onBlurExonNumber(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(exonNumber);
    }

    @UiHandler("intronNumber")
    void onBlurIntronNumber(@SuppressWarnings("unused") BlurEvent event) {
        validateNumber(intronNumber);
    }

    @UiHandler("consequenceList")
    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        if (consequenceList.getSelectedText().equals(LOSS)) {
            WidgetUtil.showHideBoxField(exonNumber, true);
            WidgetUtil.showHideBoxField(intronNumber, false);
        } else if (consequenceList.getSelectedText().equals(GAIN)) {
            WidgetUtil.showHideBoxField(exonNumber, false);
            WidgetUtil.showHideBoxField(intronNumber, true);
        } else {
            WidgetUtil.showHideBoxField(exonNumber, false);
            WidgetUtil.showHideBoxField(intronNumber, false);
        }
    }

    public void addConsequenceRow(MutationDetailTranscriptChangeDTO dto, Anchor deleteLabel, int elementIndex) {
        dataTable.resizeRows(elementIndex + 3);
        int row = elementIndex + 2;
        dataTable.setText(row, 0, dto.getConsequenceName());
        dataTable.setText(row, 1, getNumberString(dto.getExonNumber()));
        dataTable.setText(row, 2, getNumberString(dto.getIntronNumber()));
        dataTable.setWidget(row, 3, deleteLabel);
    }

    private String getNumberString(Integer number) {
        if (number == null)
            return "";
        return number.toString();
    }

    public MutationDetailTranscriptChangeDTO getDtoFromForm() {
        MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
        dto.setConsequenceOboID(WidgetUtil.getStringFromListBox(consequenceList));
        dto.setConsequenceName(WidgetUtil.getSelectedStringFromListBox(consequenceList));
        dto.setExonNumber(exonNumber.getBoxValue());
        dto.setIntronNumber(intronNumber.getBoxValue());
        return dto;
    }

    public void resetGUI() {
        consequenceList.setSelectedIndex(0);
        exonNumber.clear();
        intronNumber.clear();
    }

    public void fullResetGUI() {
        consequenceList.setSelectedIndex(0);
        exonNumber.clear();
        intronNumber.clear();
        emptyDataTable();
        presenter.rebuildGUI();
    }

    public void emptyDataTable() {
        dataTable.resizeRows(2);
    }

    public MutationDetailPresenter getPresenter() {
        return presenter;
    }

    @Override
    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();
        fields.add(consequenceList);
        fields.add(exonNumber);
        fields.add(intronNumber);
        return fields;

    }

    public void populateFields(Set<MutationDetailTranscriptChangeDTO> transcriptChangeDTOSet) {

    }


    public void setPresenter(MutationDetailPresenter presenter) {
        this.presenter = presenter;
    }
}


