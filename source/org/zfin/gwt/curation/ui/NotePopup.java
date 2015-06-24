package org.zfin.gwt.curation.ui;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.NoteDTO;

/**
 * Created by cmpich on 6/19/15.
 */
public class NotePopup extends PopupPanel {

    private Button save;
    private GenotypeDTO genotypeDTO;
    private TextArea textArea;
    private boolean isPublic;
    private NoteDTO noteDTO;

    // New note on genotype
    public NotePopup(final GenotypeDTO genotypeDTO, boolean isPublic) {
        // set auto hide to true
        super(true);
        this.genotypeDTO = genotypeDTO;
        this.isPublic = isPublic;
        makeBackgroundDarker();
        VerticalPanel vPanel = new VerticalPanel();
        textArea = new TextArea();
        textArea.setHeight("100px");
        textArea.setWidth("250px");
        vPanel.add(textArea);
        save = new Button("Save");
        vPanel.add(save);
        setWidget(vPanel);
    }

    public Button getSave() {
        return save;
    }

    public GenotypeDTO getGenotypeDTO() {
        return genotypeDTO;
    }

    public NoteDTO getNoteDTO() {
        return noteDTO;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isPublicNewNote() {
        return genotypeDTO != null && isPublic();
    }

    public boolean isPrivateNewNote() {
        return genotypeDTO != null && !isPublic();
    }

    public boolean isPublicExistingNote() {
        return noteDTO != null && isPublic();
    }

    public boolean isPrivateExistingNote() {
        return noteDTO != null && !isPublic();
    }

    private void makeBackgroundDarker() {
        setGlassEnabled(true);
        Style glassStyle = getGlassElement().getStyle();
        glassStyle.setProperty("width", "100%");
        glassStyle.setProperty("height", "100%");
        glassStyle.setProperty("backgroundColor", "#000");
        glassStyle.setProperty("opacity", "0.45");
    }

    public NotePopup(final NoteDTO externalNoteDTO, boolean isPublic) {
        // set auto hide to true
        super(true);
        this.noteDTO = externalNoteDTO;
        this.isPublic = isPublic;
        makeBackgroundDarker();
        VerticalPanel vPanel = new VerticalPanel();
        textArea = new TextArea();
        textArea.setHeight("100px");
        textArea.setWidth("250px");
        textArea.setText(externalNoteDTO.getNoteData());
        vPanel.add(textArea);
        save = new Button("Save");
        vPanel.add(save);
        setWidget(vPanel);
    }
}

