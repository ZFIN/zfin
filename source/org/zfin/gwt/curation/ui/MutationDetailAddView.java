package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.StringTextBox;
import org.zfin.gwt.root.util.DeleteImage;

//public class MutationDetailView extends AbstractViewComposite {
public class MutationDetailAddView extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("MutationDetailAddView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, MutationDetailAddView> {
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
    Grid proteinDataTable;
    @UiField
    StringTextBox intronNumberTranscript;
    @UiField
    StringTextBox exonNumberTranscript;

    public MutationDetailAddView() {
        initWidget(uiBinder.createAndBindUi(this));
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


