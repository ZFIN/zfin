package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.StringTextBox;
import org.zfin.gwt.root.util.DeleteImage;

//public class MutationDetailView extends AbstractViewComposite {
public class MutationDetailView extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("MutationDetailView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, MutationDetailView> {
    }

    private MutationDetailPresenter presenter;
    @UiField
    StringListBox nucleotideChange;
    @UiField
    StringTextBox sequenceOfReference;
    @UiField
    StringTextBox position;
    @UiField
    StringListBox localizationTerm;
    @UiField
    StringTextBox exonNumber;
    @UiField
    StringTextBox intronNumber;
    @UiField
    Grid dnaDataTable;
    @UiField
    ShowHideToggle showHideDnaConsequence;
    @UiField
    ShowHideToggle showHideTranscriptConsequence;
    @UiField
    Button saveDnaInfo;
    @UiField
    Grid proteinDataTable;
    @UiField
    Button addTranscriptConsequence;
    @UiField
    StringTextBox intronNumberTranscript;
    @UiField
    StringTextBox exonNumberTranscript;
    @UiField
    ShowHideToggle showHideProteinConsequence;

    public MutationDetailView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("showHideDnaConsequence")
    void onClickShowHideDnaConsequence(@SuppressWarnings("unused") ClickEvent event) {
        showHideDnaConsequence.toggleVisibility();
    }

    @UiHandler("showHideTranscriptConsequence")
    void onClickShowHideTrasncriptConsequence(@SuppressWarnings("unused") ClickEvent event) {
        showHideTranscriptConsequence.toggleVisibility();
    }

    @UiHandler("showHideProteinConsequence")
    void onClickShowHideProteinConsequence(@SuppressWarnings("unused") ClickEvent event) {
        showHideProteinConsequence.toggleVisibility();
    }


    private HorizontalPanel getControllPanel(Button saveButton, Button revertButton, DeleteImage deleteImage) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(saveButton);
        panel.add(revertButton);
        if (deleteImage != null)
            panel.add(deleteImage);
        return panel;
    }

    public void resetGUI() {
    }


    public void setPresenter(MutationDetailPresenter presenter) {
        this.presenter = presenter;
    }
}


