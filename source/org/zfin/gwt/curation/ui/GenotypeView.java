package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.ExternalNoteDTO;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class GenotypeView extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);

    @UiTemplate("GenotypeView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, GenotypeView> {
    }

    public GenotypeView() {
        initWidget(binder.createAndBindUi(this));
        genotypeListToggle = new ShowHideWidget(showHideGenoList, genotypeListTable, true);
    }

    @UiField
    ZfinFlexTable genotypeListTable;
    @UiField
    Hyperlink showHideGenoList;
    @UiField
    Label noneDefinedGenoLabel;
    @UiField
    SimpleErrorElement errorElement;

    private ShowHideWidget genotypeListToggle;
    private String publicationID;

    public void setData(List<GenotypeDTO> genotypeDTOList) {
        genotypeListTable.removeAllRows();
        initGenotypeListTable();

        int index = 1;
        int groupIndex = 0;
        int rowIndex = 1;
        for (final GenotypeDTO genotype : genotypeDTOList) {
            int col = 0;
            Anchor html = new Anchor(SafeHtmlUtils.fromTrustedString(genotype.getName()), "/" + genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, html);
            InlineHTML handle = new InlineHTML(genotype.getNickName());
            handle.setTitle(genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, handle);
            VerticalPanel featurePanel = new VerticalPanel();
            if (genotype.getFeatureList() != null && genotype.getFeatureList().size() > 0)
                for (FeatureDTO featureDTO : genotype.getFeatureList())
                    featurePanel.add(new InlineHTML(featureDTO.getAbbreviation()));
            genotypeListTable.setWidget(index, col++, featurePanel);

            VerticalPanel publicNotePanel = addPublicNotes(genotype);
            VerticalPanel curatorNotePanel = addCuratorNotes(genotype);
            genotypeListTable.setWidget(index, col++, publicNotePanel);
            genotypeListTable.setWidget(index, col++, curatorNotePanel);
            DeleteImage deleteImage = new DeleteImage("/action/infrastructure/deleteRecord/" + genotype.getZdbID(), "Delete Genotype");
            genotypeListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            genotypeListTable.setWidget(index++, col++, deleteImage);
            groupIndex = genotypeListTable.setRowStyle(rowIndex++, null, genotype.getZdbID(), groupIndex);

        }
    }

    private Map<GenotypeDTO, Anchor> publicNoteAnchor = new HashMap<>();
    private Map<String, Anchor> curatorNoteAnchor = new HashMap<>();
    private List<PublicNoteWidgets> publicNoteWidgetsList = new ArrayList<>();
    private List<PublicNoteWidgets> privateNoteWidgetsList = new ArrayList<>();

    private VerticalPanel addPublicNotes(GenotypeDTO genotype) {
        VerticalPanel publicNotePanel = new VerticalPanel();
        if (genotype.getPublicNotes(publicationID) != null) {
            for (final ExternalNoteDTO note : genotype.getPublicNotes(publicationID)) {
                Anchor publicNote = new Anchor(getNoteStub(note.getNoteData()));
                HorizontalPanel panel = new HorizontalPanel();
                panel.add(publicNote);
                DeleteImage remove = new DeleteImage("Remove Note");
                panel.add(remove);
                publicNotePanel.add(panel);
                NotePopup publicNotePopup = new NotePopup(note, true);
                publicNoteWidgetsList.add(new PublicNoteWidgets(remove, note, publicNote, publicNotePopup));
            }
        }
        Anchor publicNoteAnchor = new Anchor("Add");
        addClickHandler(genotype, publicNoteAnchor, true);
        NotePopup publicNotePopup = new NotePopup(genotype, true);
        publicNoteWidgetsList.add(new PublicNoteWidgets(publicNotePopup, publicNoteAnchor));
        publicNotePanel.add(publicNoteAnchor);
        return publicNotePanel;
    }

    private VerticalPanel addCuratorNotes(GenotypeDTO genotype) {
        VerticalPanel curatorNotesPanel = new VerticalPanel();
        if (genotype.getPrivateNotes() != null) {
            for (final CuratorNoteDTO note : genotype.getPrivateNotes()) {
                Anchor curatorNote = new Anchor(getNoteStub(note.getNoteData()));
                HorizontalPanel panel = new HorizontalPanel();
                panel.add(curatorNote);
                DeleteImage remove = new DeleteImage("Remove Note");
                panel.add(remove);
                curatorNotesPanel.add(panel);
                NotePopup notePopup = new NotePopup(note, false);
                privateNoteWidgetsList.add(new PublicNoteWidgets(remove, note, curatorNote, notePopup));
            }
        }
        Anchor curatorNote = new Anchor("Add");
        NotePopup notePopup = new NotePopup(genotype, false);
        //privateNoteWidgetsList.add(new PublicNoteWidgets(remove, note, notePopup, notePopup));
        addClickHandler(genotype, curatorNote, false);
        curatorNoteAnchor.put(genotype.getDataZdbID(), curatorNote);
        curatorNotesPanel.add(curatorNote);

        return curatorNotesPanel;
    }

    private void addClickHandler(final GenotypeDTO noteDTO, final Anchor publicNote, final boolean isPublic) {
        publicNote.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                NotePopup publicNotePopup = new NotePopup(noteDTO, isPublic);
                publicNotePopup.show();
                publicNotePopup.center();
            }
        });
    }

    public static String getNoteStub(String note) {
        if (note.length() < 15)
            return note;
        else
            return note.substring(0, 15) + "...";
    }

    public Map<GenotypeDTO, Anchor> getPublicNoteAnchor() {
        return publicNoteAnchor;
    }

    public Map<String, Anchor> getCuratorNoteAnchor() {
        return curatorNoteAnchor;
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

    public ShowHideWidget getGenotypeListToggle() {
        return genotypeListToggle;
    }

    public Hyperlink getShowHideGenoList() {
        return showHideGenoList;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public SimpleErrorElement getErrorElement() {
        return errorElement;
    }

    public List<PublicNoteWidgets> getPublicNoteWidgetsList() {
        return publicNoteWidgetsList;
    }

    class PublicNoteWidgets {

        private ExternalNoteDTO note;
        private CuratorNoteDTO curatorNote;
        private DeleteImage deleteImage;
        private Anchor viewAnchor;
        private NotePopup notePopup;


        public PublicNoteWidgets(NotePopup notePopup, Anchor viewAnchor) {
            this.notePopup = notePopup;
            this.viewAnchor = viewAnchor;
        }

        public PublicNoteWidgets(DeleteImage deleteImage, ExternalNoteDTO note, Anchor viewAnchor, NotePopup notePopup) {
            this.deleteImage = deleteImage;
            this.note = note;
            this.viewAnchor = viewAnchor;
            this.notePopup = notePopup;
        }

        public PublicNoteWidgets(DeleteImage deleteImage, CuratorNoteDTO note, Anchor viewAnchor, NotePopup notePopup) {
            this.deleteImage = deleteImage;
            this.curatorNote = note;
            this.viewAnchor = viewAnchor;
            this.notePopup = notePopup;
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

        public NotePopup getNotePopup() {
            return notePopup;
        }

        public CuratorNoteDTO getCuratorNote() {
            return curatorNote;
        }

        public boolean hasDeleteLink() {
            return deleteImage != null;
        }

    }
}
