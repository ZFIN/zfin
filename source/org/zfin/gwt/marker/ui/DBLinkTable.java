package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.zfin.gwt.marker.event.DBLinkTableListener;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.IntegerTextBox;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.ArrayList;
import java.util.List;


/**
 * This copmonent handles DBLinks.
 */
public abstract class DBLinkTable extends AbstractRelatedEntityBox<DBLinkDTO> {


    // gui elements
    private String SPACER;
    private HTML dbNameLabel;
    private StringListBox referenceListBox;
    private HTML lengthLabel;
    private IntegerTextBox lengthField;
    private int MAX_LENGTH;

    private List<ReferenceDatabaseDTO> referenceDatabases = new ArrayList<ReferenceDatabaseDTO>();

    // listeners
    private final List<DBLinkTableListener> dbLinkTableListeners = new ArrayList<DBLinkTableListener>();

    protected void initGUI() {
        // has to be instantiated here because called from a sub-class initGUI;
        SPACER = "&nbsp;&nbsp;";
        dbNameLabel = new HTML(SPACER + "DbName:");
        referenceListBox = new StringListBox();
        lengthLabel = new HTML(SPACER + "Length:");
        lengthField = new IntegerTextBox();
        MAX_LENGTH = 6;

        lengthField.setMaxLength(MAX_LENGTH);
        lengthField.setVisibleLength(MAX_LENGTH);
        relatedEntityTable.setStyleName("relatedEntityTable");
        // init table
        panel.add(relatedEntityTable);

        // init new alias row
//        newAliasPanel.add(newAliasLabel);
        newRelatedEntityPanel.add(newRelatedEntityField);
        newRelatedEntityPanel.add(dbNameLabel);
        newRelatedEntityPanel.add(referenceListBox);
        newRelatedEntityPanel.add(lengthLabel);
        newRelatedEntityPanel.add(lengthField);
        newRelatedEntityPanel.add(addRelatedEntityButton);
        newRelatedEntityPanel.add(publicationLabel);
        panel.add(new HTML("&nbsp;"));
        panel.add(newRelatedEntityPanel);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);
        panel.setStyleName("gwt-editbox");

        // init current alias row
        publicationLabel.setStyleName("relatedEntityDefaultPub");
    }

    public void setReferenceDatabases(List<ReferenceDatabaseDTO> referenceDatabases) {
        this.referenceDatabases = referenceDatabases;
        refreshReferenceDatabases();
    }

    void refreshReferenceDatabases() {
        referenceListBox.clear();
        referenceListBox.addItem("-- lookup database --", "null");
        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabases) {
            referenceListBox.addItem(referenceDatabaseDTO.getNameAndType(), referenceDatabaseDTO.getZdbID());
        }

        if (referenceListBox.getItemCount() == 2) {
            referenceListBox.setItemSelected(1, true);
        }
    }

    List<ReferenceDatabaseDTO> getReferenceDatabases() {
        return referenceDatabases;
    }

    protected String validateNewAttribution(DBLinkDTO DBLinkDTO) {
        String pub = DBLinkDTO.getPublicationZdbID();
        String entityName = DBLinkDTO.getName();
        if (pub == null || pub.length() == 0) {
            return "Publication must be selected to add new reference.";
        }
        if (getRelatedEntityIndex(entityName) < 0) {
            return "DBLink does not exist [" + entityName + "]";
        }
        List<String> pubList = getRelatedEntityAttributionsForName(entityName);
        if (pubList.contains(pub)) {
            return "Publication [" + pub + "] exists for [" + entityName + "]";
        }

        return null;
    }

    protected DBLengthEntryField getDBLengthWidget(int row) {
        if (row < relatedEntityTable.getRowCount()) {
            return (DBLengthEntryField) relatedEntityTable.getWidget(row, 2);
        } else {
            return null;
        }
    }

    /**
     * @param relatedEntityName Related entity name.
     * @return A list of publications with this name
     */
    protected List<String> getRelatedEntityAttributionsForName(String relatedEntityName) {
        int rowCount = relatedEntityTable.getRowCount();
        List<String> attributionList = new ArrayList<String>();
        for (int i = 0; i < rowCount; ++i) {
            if (relatedEntityTable.getCellCount(i) > 1 && relatedEntityTable.getWidget(i, 1) != null) {
                PublicationAttributionLabel publicationAttributionLabel = ((PublicationAttributionLabel) relatedEntityTable.getWidget(i, 1));
                if (publicationAttributionLabel.getAssociatedName().equals(relatedEntityName)) {
                    attributionList.add(publicationAttributionLabel.getPublication());
                }
            }
        }
        return attributionList;
    }


    /**
     * This method returns an error string to be displayed.
     *
     * @param attributionName Name of the accession to add.
     * @return Error string to be displayed.
     */
    String validateNewDBLink(String attributionName) {
        if (getRelatedEntityIndex(attributionName) >= 0) {
            return "Attribution [" + attributionName + "] exists.";
        }

        return null;
    }

    public void addRelatedEntity(final String attributionName, final String pub) {
        // do client check
        String validationError = validateNewDBLink(attributionName);
        if (validationError != null) {
            setError(validationError);
            return;
        }
        DBLinkDTO dbLinkDTO = new DBLinkDTO();
        dbLinkDTO.setName(attributionName);
        dbLinkDTO.setPublicationZdbID(pub);

        Integer length = null;
        try {
            length = Integer.valueOf(lengthField.getText().trim());
        }
        catch (NumberFormatException nfe) {
            // nothing to catch here
        }
        dbLinkDTO.setLength(length);

        ReferenceDatabaseDTO referenceDatabaseDTO = null;
        String referenceDBString = referenceListBox.getItemText(referenceListBox.getSelectedIndex());
        String referenceDBZdbID = referenceListBox.getSelected();
        if (referenceDBZdbID != null) {
            referenceDatabaseDTO = new ReferenceDatabaseDTO();
            String[] refStrings = referenceDBString.split(" - ");
            referenceDatabaseDTO.setZdbID(referenceDBZdbID);
            referenceDatabaseDTO.setName(refStrings[0]);
            referenceDatabaseDTO.setType(refStrings[1]);
        }
        dbLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);
        fireRelatedEntityAdded(new RelatedEntityEvent(dbLinkDTO));
    }

    public boolean isEditable(RelatedEntityDTO relatedEntityDTO) {
        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabases) {
            if (referenceDatabaseDTO.getZdbID().equals(((DBLinkDTO) relatedEntityDTO).getReferenceDatabaseDTO().getZdbID())) {
                return true;
            }
        }
        return false;
    }


    public void addRelatedEntityToGUI(DBLinkDTO dbLinkDTO) {

        String pub = dbLinkDTO.getPublicationZdbID();
        if (pub == null) {
            pub = "";
        }

        dbLinkDTO.setEditable(isEditable(dbLinkDTO));

        // if this attribution already exists, just add a reference
        if (getRelatedEntityIndex(dbLinkDTO.getName()) >= 0) {
            addAttributionToGUI(dbLinkDTO);
        } else {
            // handle view
            int numRows = relatedEntityTable.getRowCount();
            relatedEntityTable.insertRow(numRows);
            // add namePanel
            RelatedEntityLabel<DBLinkDTO> relatedEntityLabel = new RelatedEntityLabel<DBLinkDTO>(this, dbLinkDTO.getName(), dbLinkDTO);
            relatedEntityLabel.setLinkableData(dbLinkDTO);
            relatedEntityTable.setWidget(numRows, 0, relatedEntityLabel);
            // add publication panel
            relatedEntityTable.setWidget(numRows, 1, new PublicationAttributionLabel<DBLinkDTO>(this, pub, dbLinkDTO.getName(), dbLinkDTO));
            relatedEntityTable.setWidget(numRows, 2, new DBLengthEntryField(this, dbLinkDTO));
            // add to current alias listBox
            resetRelatedEntityAdd();
        }
    }

    public void removeAttribution(final RelatedEntityDTO relatedEntityDTO) {
        DBLinkDTO dbLinkDTO = (DBLinkDTO) relatedEntityDTO;
        fireAttributionRemoved(new RelatedEntityEvent(dbLinkDTO));
    }


    void updatedLength(DBLinkDTO newDTO) {

        //drop this DTO into the correct length widget
        int rowcount = relatedEntityTable.getRowCount();
        for (int i = 0; i < rowcount; i++) {
            DBLengthEntryField dbLengthEntryField = (DBLengthEntryField) relatedEntityTable.getWidget(i, 2);

            if (dbLengthEntryField.getDbLinkDTO().getZdbID().equals(newDTO.getZdbID())) {
                dbLengthEntryField.setDbLinkDTO(newDTO);
                dbLengthEntryField.handleDirty();
            }
        }
    }

    protected List<String> getRelatedEntityNames() {
        int rowCount = relatedEntityTable.getRowCount();
        List<String> relatedEntityList = new ArrayList<String>();
        for (int i = 0; i < rowCount; ++i) {
            if (relatedEntityTable.getWidget(i, 0) == null) {
                Window.alert("Problem at row, contact dev: " + i);
                return null;
            } else {
                String name = ((RelatedEntityLabel<DBLinkDTO>) relatedEntityTable.getWidget(i, 0)).getName();
                relatedEntityList.add(name.substring(name.indexOf(":") + 1));
            }
        }
        return relatedEntityList;
    }

    void resetInput() {
        lengthField.setText("");
        referenceListBox.setItemSelected(0, true);
    }

    // This will be a list of type AttributeDomain objects

    public void setDBLinks(List<DBLinkDTO> dbLinkList) {
        reset();
        for (DBLinkDTO dbLinkDTO : dbLinkList) {
            addRelatedEntityToGUI(dbLinkDTO);
        }
    }

    public void addDBLinkTableListener(DBLinkTableListener dbLinkTableListener) {
        dbLinkTableListeners.add(dbLinkTableListener);
    }

    void fireDBLinkUpdated(RelatedEntityEvent<DBLinkDTO> tableEvent) {
        for (DBLinkTableListener dbLinkTableListener : dbLinkTableListeners) {
            dbLinkTableListener.dataChanged(tableEvent);
        }
    }

    void fireDBLinkAttributed(RelatedEntityEvent<DBLinkDTO> tableEvent) {
        for (DBLinkTableListener dbLinkTableListener : dbLinkTableListeners) {
            dbLinkTableListener.addAttribution(tableEvent);
        }
    }

}
