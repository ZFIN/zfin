package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.ExternalNoteDTO;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Table of associated genotypes
 */
public class GenotypeView extends SingleGridBaseComposite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private GenotypePresenter presenter;

    public void setPresenter(GenotypePresenter presenter) {
        this.presenter = presenter;
    }

    public GenotypePresenter getPresenter() {
        return presenter;
    }

    @UiTemplate("GenotypeView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, GenotypeView> {
    }

    public GenotypeView() {
        initWidget(binder.createAndBindUi(this));
        sectionVisibilityToggle = new ShowHideWidget(showHideSection, genotypeListTable, true);
    }

    @UiField
    ZfinFlexTable genotypeListTable;
    @UiField
    Label noneDefinedGenoLabel;

    private String publicationID;

    @UiHandler("showHideSection")
    void onShowHideClick(@SuppressWarnings("unused") ClickEvent event) {
        sectionVisibilityToggle.toggleVisibility();
    }

    public void setData(List<GenotypeDTO> genotypeDTOList) {
        genotypeListTable.removeAllRows();
        initGenotypeListTable();

        int index = 1;
        int groupIndex = 0;
        int rowIndex = 1;
        for (final GenotypeDTO genotype : genotypeDTOList) {
            int col = 0;
            Anchor html = new Anchor(SafeHtmlUtils.fromTrustedString(genotype.getNamePlusBackground()), "/" + genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, html);
            InlineHTML handle = new InlineHTML(genotype.getNickName());
            handle.setTitle(genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, handle);
            VerticalPanel featurePanel = new VerticalPanel();
            if (genotype.getFeatureList() != null && genotype.getFeatureList().size() > 0)
                for (FeatureDTO featureDTO : genotype.getFeatureList())
                    featurePanel.add(new InlineHTML(featureDTO.getAbbreviation()));
            genotypeListTable.setWidget(index, col++, featurePanel);

            VerticalPanel curatorNotePanel = addCuratorNotes(genotype);
            genotypeListTable.setWidget(index, col++, getPublicNotesPanel(genotype));
            genotypeListTable.setWidget(index, col++, curatorNotePanel);
            DeleteImage deleteImage = new DeleteImage("/action/infrastructure/deleteRecord/" + genotype.getZdbID(), "Delete Genotype");
            genotypeListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            genotypeListTable.setWidget(index++, col, deleteImage);
            groupIndex = genotypeListTable.setRowStyle(rowIndex++, null, genotype.getZdbID(), groupIndex);

        }
    }

    private List<PublicNoteWidgets> publicNoteWidgetsList = new ArrayList<>();
    private List<PublicNoteWidgets> privateNoteWidgetsList = new ArrayList<>();

    private VerticalPanel getPublicNotesPanel(GenotypeDTO genotype) {
        VerticalPanel publicNotePanel = new VerticalPanel();
        publicNotePanel.setWidth("100%");
        if (genotype.getPublicNotes(publicationID) != null) {
            for (final ExternalNoteDTO note : genotype.getPublicNotes(publicationID)) {
                Anchor publicNote = new Anchor(getNoteStub(note.getNoteData()));
                DeleteImage remove = new DeleteImage("Remove Note");
                publicNoteWidgetsList.add(new PublicNoteWidgets(remove, note, publicNote));
                TextArea textNote = new TextArea();
                textNote.setWidth("100%");
                textNote.setText(note.getNoteData());
                Button savePublicNoteButton = getNotesUI(publicNotePanel, textNote, publicNote, remove);
                presenter.addSavePublicNoteButtonClickHandler(savePublicNoteButton, textNote, note);
            }
        }
        TextArea publicNote = new TextArea();
        Button savePublicNoteButton = getNotesUI(publicNotePanel, publicNote, new Anchor("Add"));
        presenter.addCreatePublicNoteButtonClickHandler(savePublicNoteButton, publicNote, genotype);
        return publicNotePanel;
    }

    private VerticalPanel addCuratorNotes(GenotypeDTO genotype) {
        VerticalPanel curatorNotesPanel = new VerticalPanel();
        curatorNotesPanel.setWidth("100%");
        if (genotype.getPrivateNotes() != null) {
            for (final CuratorNoteDTO note : genotype.getPrivateNotes()) {
                Anchor curatorNote = new Anchor(getNoteStub(note.getNoteData()));
                DeleteImage remove = new DeleteImage("Remove Note");
                TextArea textNote = new TextArea();
                textNote.setWidth("100%");
                textNote.setText(note.getNoteData());
                Button savePublicNoteButton = getNotesUI(curatorNotesPanel, textNote, curatorNote, remove);
                presenter.addSaveCuratorNoteButtonClickHandler(savePublicNoteButton, textNote, note);
                privateNoteWidgetsList.add(new PublicNoteWidgets(remove, note, curatorNote));
            }
        }
        TextArea publicNote = new TextArea();
        Button saveCuratorNoteButton = getNotesUI(curatorNotesPanel, publicNote, new Anchor("Add"));
        presenter.addCreateCuratorNoteButtonClickHandler(saveCuratorNoteButton, publicNote, genotype);
        return curatorNotesPanel;
    }

    private Button getNotesUI(VerticalPanel notePanel, final TextArea publicNote, final Anchor publicNoteAnchor) {
        return getNotesUI(notePanel, publicNote, publicNoteAnchor, null);
    }

    private Button getNotesUI(VerticalPanel notePanel, final TextArea publicNote, final Anchor publicNoteAnchor, final DeleteImage deleteImage) {
        final FlowPanel editPanel = new FlowPanel();
        Button savePublicNoteButton = new Button("Save");
        Button cancelPublicNoteButton = new Button("Cancel");
        editPanel.add(publicNote);
        addButtonsToPanel(editPanel, savePublicNoteButton, cancelPublicNoteButton);
        notePanel.add(editPanel);
        if (deleteImage != null) {
            HorizontalPanel hNotePanel = new HorizontalPanel();
            hNotePanel.add(publicNoteAnchor);
            hNotePanel.add(deleteImage);
            notePanel.add(hNotePanel);
        } else {
            notePanel.add(publicNoteAnchor);
        }
        editPanel.setVisible(false);
        publicNoteAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                editPanel.setVisible(true);
                publicNoteAnchor.setVisible(false);
                if (deleteImage != null)
                    deleteImage.setVisible(false);
            }
        });
        cancelPublicNoteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                editPanel.setVisible(false);
                publicNoteAnchor.setVisible(true);
                if (deleteImage != null)
                    deleteImage.setVisible(true);
            }
        });
        return savePublicNoteButton;
    }

    private void addButtonsToPanel(Panel notePanel, Button savePublicNoteButton, Button cancelPublicNoteButton) {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(savePublicNoteButton);
        buttonPanel.add(cancelPublicNoteButton);
        notePanel.add(buttonPanel);
    }

    public static String getNoteStub(String note) {
        if (note.length() < 15)
            return note;
        else
            return note.substring(0, 15) + "...";
    }

    private void initGenotypeListTable() {
        int col = 0;
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Display Name");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Genotype Nickname");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Feature");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Public Note");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Curator Note");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col, "Delete");
        genotypeListTable.getRowFormatter().setStyleName(0, "table-header");
    }


    public Label getNoneDefinedGenoLabel() {
        return noneDefinedGenoLabel;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public List<PublicNoteWidgets> getPublicNoteWidgetsList() {
        return publicNoteWidgetsList;
    }

    public List<PublicNoteWidgets> getPrivateNoteWidgetsList() {
        return privateNoteWidgetsList;
    }

    class PublicNoteWidgets {

        private ExternalNoteDTO note;
        private CuratorNoteDTO curatorNote;
        private DeleteImage deleteImage;
        private Anchor viewAnchor;

        public PublicNoteWidgets(DeleteImage deleteImage, ExternalNoteDTO note, Anchor viewAnchor) {
            this.deleteImage = deleteImage;
            this.note = note;
            this.viewAnchor = viewAnchor;
        }

        public PublicNoteWidgets(DeleteImage deleteImage, CuratorNoteDTO note, Anchor viewAnchor) {
            this.deleteImage = deleteImage;
            this.curatorNote = note;
            this.viewAnchor = viewAnchor;
        }

        public DeleteImage getDeleteImage() {
            return deleteImage;
        }

        public ExternalNoteDTO getNote() {
            return note;
        }

        public Anchor getViewAnchor() {
            return viewAnchor;
        }

        public CuratorNoteDTO getCuratorNote() {
            return curatorNote;
        }

        public boolean hasDeleteLink() {
            return deleteImage != null;
        }

    }
}
