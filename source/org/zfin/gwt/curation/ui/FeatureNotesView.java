package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.RevertibleTextArea;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.HashSet;
import java.util.Set;

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
    RevertibleTextArea newNoteTextArea;
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

    public void addNoteReferenceCell(NoteDTO noteDTO, int rowindex) {
        dataTable.resizeRows(rowindex + 1);
        if (noteDTO.getPublicationDTO() != null) {
            Anchor pubAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(noteDTO.getPublicationDTO().getMiniRef()), "/" +
                    noteDTO.getPublicationDTO().getZdbID());
            dataTable.setWidget(rowindex, 0, pubAnchor);
        }
    }

    public void addNoteCuratorReferenceCell(CuratorNoteDTO noteDTO, int rowindex) {
        dataTable.resizeRows(rowindex + 1);
        dataTable.setText(rowindex, 0, noteDTO.getCurator().getDisplay());
    }

    public void addNoteTextAreaCell(TextArea textArea, int rowindex) {
        textArea.setWidth("600");
        dataTable.setWidget(rowindex, 1, textArea);
    }

    public void addControlCell(Button saveButton, Button revertButton, DeleteImage deleteImage, int rowindex) {
        HorizontalPanel panel = getControllPanel(saveButton, revertButton, deleteImage);
        dataTable.setWidget(rowindex, 2, panel);
    }

    private HorizontalPanel getControllPanel(Button saveButton, Button revertButton, DeleteImage deleteImage) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(saveButton);
        panel.add(revertButton);
        if (deleteImage != null)
            panel.add(deleteImage);
        return panel;
    }

    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, typeListBox);
        dataTable.setWidget(lastRow, col++, newNoteTextArea);
        dataTable.setWidget(lastRow, col++, getControllPanel(addButton, cancelButton, null));
    }


    public void resetGUI() {
        newNoteTextArea.setText("");
        typeListBox.setSelectedIndex(0);
        dataTable.resizeRows(0);
        endTableUpdate();
    }


    @Override
    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();
        fields.add(newNoteTextArea);
        return fields;

    }

    public void setPresenter(FeatureNotesPresenter presenter) {
        this.presenter = presenter;
    }
}


