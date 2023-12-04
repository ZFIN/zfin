package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.ui.AbstractViewComposite;
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
    Button infoButton;
    @UiField
    RevertibleTextArea newNoteTextArea;
    @UiField
    StringListBox typeListBox;
    @UiField
    StringListBox noteTypeListBox;



    public FeatureNotesView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("addButton")
    void onClickAddButton(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addNote();
    }

    @UiHandler("cancelButton")
    void onClickCancelButton(@SuppressWarnings("unused") ClickEvent event) {
        clearGUI();
    }

    @UiHandler("infoButton")
    void onClickInfoButton(@SuppressWarnings("unused") ClickEvent event) {
        //alert the user that this is a public note and will be visible to all users
        Window.alert(
            "“variant” notes refer to the DNA change, consequence in the RNA/protein, effect of the mutation on the protein domains,…\n\n" +
            "“feature” notes refer to the actual allele (e.g: homozygote viable, hypomorph,…)\n\n" +
            "If you have information about both feature and variant, you will need to enter this information in 2 different notes.\n\n" );
    }

    @UiHandler("typeListBox")
    void onChangeNoteType(@SuppressWarnings("unused") ChangeEvent event) {
        String noteType=typeListBox.getSelected();
        if (noteType.equals("Public")) {
            noteTypeListBox.setVisible(true);
        }
        else{
            noteTypeListBox.setVisible(false);
            }
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
    public void addNoteTypeListBox(StringListBox textArea, int rowindex) {
        dataTable.setWidget(rowindex, 2, textArea);
    }
    public void addNoteTagCell(Label textArea, int rowindex) {

        dataTable.setWidget(rowindex, 3, textArea);
    }

    public void addControlCell(Button saveButton, Button revertButton, DeleteImage deleteImage, int rowindex) {
        HorizontalPanel panel = getControlPanel(saveButton, revertButton, deleteImage, null);
        dataTable.setWidget(rowindex, 4, panel);
    }

    private HorizontalPanel getControlPanel(Button saveButton, Button revertButton, DeleteImage deleteImage, Button infoButton) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(saveButton);
        panel.add(revertButton);
        if (deleteImage != null)
            panel.add(deleteImage);
        if (infoButton != null)
            panel.add(infoButton);
        return panel;
    }

    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, typeListBox);
        dataTable.setWidget(lastRow, col++, newNoteTextArea);
        dataTable.setWidget(lastRow, col++, noteTypeListBox);;
        dataTable.setWidget(lastRow, col++, getControlPanel(addButton, cancelButton, null, infoButton));

    }


    public void resetGUI() {
        newNoteTextArea.setText("");
        typeListBox.setSelectedIndex(0);
        noteTypeListBox.setVisible(true);
        noteTypeListBox.setSelectedIndex(0);
        dataTable.resizeRows(0);
        endTableUpdate();
    }

    public void clearGUI() {
        newNoteTextArea.setText("");
        typeListBox.setSelectedIndex(0);
        noteTypeListBox.setVisible(true);
        noteTypeListBox.setSelectedIndex(0);
    }


    @Override
    public Set<IsDirtyWidget> getValueFields() {
        Set<IsDirtyWidget> fields = new HashSet<>();
        fields.add(newNoteTextArea);
        fields.add(noteTypeListBox);
        return fields;

    }

    public void setPresenter(FeatureNotesPresenter presenter) {
        this.presenter = presenter;
    }
}


