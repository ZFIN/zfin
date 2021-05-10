package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.AbstractComposite;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.IntegerTextBox;
import org.zfin.gwt.root.ui.Revertible;

/**
 */
public class DBLengthEntryField extends AbstractComposite implements Revertible {

    // gui elements
    private IntegerTextBox lengthField = new IntegerTextBox();
    private Button updateButton = new Button("update");
    private Button revertButton = new Button("revert");
    private HorizontalPanel panel = new HorizontalPanel();

    private Label lengthLabel;
    HTML spacerLabel = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

    // internal data
    private DBLinkDTO dbLinkDTO = null;

    // parent
    // todo: change to use listener later?
    private DBLinkTable parent = null;

    public DBLengthEntryField(DBLinkTable dbLinkTable, DBLinkDTO dbLinkDTO) {
        this.parent = dbLinkTable;
        setDbLinkDTO(dbLinkDTO);
        initGUI();
        setValues();
        initWidget(panel);
    }

    protected void initGUI() {

        lengthLabel = new Label((dbLinkDTO.getLength() == null ? "" : dbLinkDTO.getLength().toString()));

        panel.add(spacerLabel);
        panel.add(lengthLabel);

        handleDirty();
    }

    @Override
    protected void setValues() {
        // if not editable, then create other widgets
        if (false == dbLinkDTO.isEditable()) {
//            spacerLabel.setHTML();
            HTML readOnlyLabel = new HTML("<font size=-1>(read-only)</font>");
            panel.add(readOnlyLabel);

            return;
        }

    }

    @Override
    protected void revertGUI() {
        lengthField.setText(dbLinkDTO.getLength().toString());
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        lengthField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                handleDirty();
            }
        });

        updateButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                DBLinkDTO newDTO = dbLinkDTO.deepCopy();
                newDTO.setLength(lengthField.getBoxValue());
                parent.fireDBLinkUpdated(new RelatedEntityEvent<DBLinkDTO>(newDTO));

            }
        });


        revertButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                revertGUI();
                handleDirty();
            }
        });

    }

    public void setDbLinkDTO(DBLinkDTO dbLinkDTO) {
        this.dbLinkDTO = dbLinkDTO;
        if (this.dbLinkDTO != null && this.dbLinkDTO.getLength() != null) {
            lengthField.setText(this.dbLinkDTO.getLength().toString());
        } else {
            lengthField.setText("");
        }
        handleDirty();
    }

    public DBLinkDTO getDbLinkDTO() {
        return dbLinkDTO;
    }

    @Override
    public boolean isDirty() {
        return lengthField.isDirty(dbLinkDTO.getLength());
    }

    @Override
    public void working() {
        updateButton.setText(TEXT_WORKING);
        updateButton.setEnabled(false);
        revertButton.setEnabled(false);
        lengthField.setEnabled(false);
    }

    @Override
    public void notWorking() {
        updateButton.setText(TEXT_SAVE);
        updateButton.setEnabled(true);
        revertButton.setEnabled(true);
        lengthField.setEnabled(true);
    }

    public boolean handleDirty() {
        boolean dirty = lengthField.isDirty(dbLinkDTO.getLength());
        updateButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (false == dirty) {
            fireEventSuccess();
        }
        return dirty;
    }

}
