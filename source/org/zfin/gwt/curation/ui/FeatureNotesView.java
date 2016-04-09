package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.DeleteImage;

public class FeatureNotesView extends AbstractViewComposite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureNotesView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, FeatureNotesView> {
    }

    private FeatureNotesPresenter presenter;
    @UiField
    Grid dataTable;
    @UiField
    Button addButton;
    @UiField
    Button cancelButton;
    @UiField
    TextArea newNoteTextArea;
    @UiField
    StringListBox typeListBox;

    public FeatureNotesView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("addButton")
    void onClickAddButton(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addNote();
    }

    @UiHandler("cancelButton")
    void onClickCancelButton(@SuppressWarnings("unused") ClickEvent event) {
        resetGUI();
    }

    public void addNoteTypeCell(NoteDTO noteDTO, int rowindex) {
        dataTable.resizeRows(rowindex + 1);
        Anchor pubAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(noteDTO.getPublicationDTO().getMiniRef()), "/" +
                noteDTO.getPublicationDTO().getZdbID());

        dataTable.setWidget(rowindex, 0, pubAnchor);
    }

    public void addNoteTextAreaCell(TextArea textArea, int rowindex) {
        textArea.setWidth("600");
        dataTable.setWidget(rowindex, 1, textArea);
    }

    public void addDeleteNoteImageCell(DeleteImage deleteImage, int rowindex) {
        dataTable.setWidget(rowindex, 2, deleteImage);
    }

    public void addSaveButtonCell(Button saveButton, int rowindex) {
        dataTable.setWidget(rowindex, 3, saveButton);
    }

    public void addRevertButtonCell(Button revertButton, int rowindex) {
        dataTable.setWidget(rowindex, 4, revertButton);
    }

    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, typeListBox);
        dataTable.setWidget(lastRow, col++, newNoteTextArea);
        dataTable.setWidget(lastRow, col++, addButton);
        dataTable.setWidget(lastRow, col++, cancelButton);
    }


    public void resetGUI() {
        newNoteTextArea.setText("");
        typeListBox.setSelectedIndex(0);
    }


    public void setPresenter(FeatureNotesPresenter presenter) {
        this.presenter = presenter;
    }
}


