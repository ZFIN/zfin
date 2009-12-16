package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.DBLinkDTO;
import org.zfin.marker.presentation.event.DBLinkTableEvent;

/**
 */
public class DBLengthEntryField extends Composite {

    // gui elements
    private TextBox lengthField = new TextBox();
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
        initWidget(panel);
    }

    protected void initGUI() {

        lengthLabel = new Label((dbLinkDTO.getLength() == null ? "" : dbLinkDTO.getLength().toString()));

        // if not editable, then create other widgets
        if (false == dbLinkDTO.isEditable()) {
//            spacerLabel.setHTML();
            HTML readOnlyLabel = new HTML("<font size=-1>(read-only)</font>");
            panel.add(readOnlyLabel);

            return;
        }

        panel.add(spacerLabel);
        panel.add(lengthLabel);

//        lengthField.setVisibleLength(7);
//
//        //Right now, we don't actually want to do updates, for now, that
//        //means I'm going to disable the length field
//        lengthField.setEnabled(false);
//        panel.add(lengthField);
//        panel.add(updateButton);
//        panel.add(revertButton);

        lengthField.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyUp(Widget widget, char c, int i) {
                checkDirty();
            }
        });

        updateButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                DBLinkDTO newDTO = dbLinkDTO.deepCopy();
                newDTO.setLength(Integer.valueOf(lengthField.getText()));
                parent.fireDBLinkUpdated(new DBLinkTableEvent(newDTO));

            }
        });


        revertButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                revert();
                checkDirty();
            }
        });

        checkDirty();
    }


    public void setDbLinkDTO(DBLinkDTO dbLinkDTO) {
        this.dbLinkDTO = dbLinkDTO;
        if (this.dbLinkDTO != null && this.dbLinkDTO.getLength() != null) {
            lengthField.setText(this.dbLinkDTO.getLength().toString());
        } else {
            lengthField.setText("");
        }
        checkDirty();
    }

    public DBLinkDTO getDbLinkDTO() {
        return dbLinkDTO;
    }

    public void checkDirty() {
        if (dbLinkDTO != null && lengthField.getText() != null && dbLinkDTO.getLength() != null) {
            updateButton.setEnabled(false == lengthField.getText().equals(dbLinkDTO.getLength().toString()));
            revertButton.setEnabled(false == lengthField.getText().equals(dbLinkDTO.getLength().toString()));
        } else if (dbLinkDTO == null || lengthField.getText() == null) {
            updateButton.setEnabled(false);
            revertButton.setEnabled(false);
        } else if (dbLinkDTO.getLength() == null && lengthField.getText().trim().length() == 0) {
            updateButton.setEnabled(false);
            revertButton.setEnabled(false);
        } else if (dbLinkDTO.getLength() == null && lengthField.getText().trim().length() > 0) {
            updateButton.setEnabled(true);
            revertButton.setEnabled(true);
        }
    }

    public void revert() {
        lengthField.setText(dbLinkDTO.getLength().toString());
    }
}
