package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.RelatedEntityDTO;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class AbstractRelatedEntityBox<U extends RelatedEntityDTO> extends AbstractRelatedEntityContainer<U> {


    // gui componenents
    protected VerticalPanel panel = new VerticalPanel();

    // table componenents
    protected FlexTable relatedEntityTable = new FlexTable();

    // first row components
    protected HorizontalPanel newRelatedEntityPanel = new HorizontalPanel();
    protected TextBox newRelatedEntityField = new TextBox();
    protected Button addRelatedEntityButton = new Button("Add");


    // listeners


    // abstract method

    public abstract void addRelatedEntity(final String name, final String pubZdbID);

    public AbstractRelatedEntityBox() {
        initGui();
        addHandlers();
        initWidget(panel);
    }

    protected void initGui() {
        relatedEntityTable.setStyleName("relatedEntityTable");

        // init table
        panel.add(relatedEntityTable);

        newRelatedEntityPanel.add(newRelatedEntityField);
        newRelatedEntityPanel.add(addRelatedEntityButton);
        newRelatedEntityPanel.add(publicationLabel);
        panel.add(new HTML("&nbsp;"));
        panel.add(newRelatedEntityPanel);

        errorLabel.setStyleName("error");
        panel.add(errorLabel);


        publicationLabel.setStyleName("relatedEntityDefaultPub");

    }

    protected void addHandlers() {
        addRelatedEntityButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                if (false == attributionIsValid()) return;
                if (newRelatedEntityField.getText().trim().length() > 0) {
                    addRelatedEntity(newRelatedEntityField.getText().trim(),
                            getPublication());
                } else {
                    setError("Nothing to attribute.");
                }
            }
        });
    }

    /**
     * @param relatedEntityName Related entity name.
     * @return A list of publications with this name
     */
    protected List<String> getRelatedEntityAttributionsForName(String relatedEntityName) {
        int rowCount = relatedEntityTable.getRowCount();
        List<String> attributionList = new ArrayList<String>();
        for (int i = 0; i < rowCount; ++i) {
            if (relatedEntityTable.getCellCount(i) == 2 && relatedEntityTable.getWidget(i, 1) != null) {
                PublicationAttributionLabel publicationAttributionLabel = ((PublicationAttributionLabel) relatedEntityTable.getWidget(i, 1));
                if (publicationAttributionLabel.getAssociatedName().equals(relatedEntityName)) {
                    attributionList.add(publicationAttributionLabel.getPublication());
                }
            }
        }
        return attributionList;
    }

    protected PublicationAttributionLabel getPublicationWidget(int row) {
        if (row < relatedEntityTable.getRowCount()) {
            return (PublicationAttributionLabel) relatedEntityTable.getWidget(row, 1);
        } else {
            return null;
        }
    }

    protected RelatedEntityLabel<U> getNameWidget(int row) {
        if (row < relatedEntityTable.getRowCount()) {
            return (RelatedEntityLabel<U>) relatedEntityTable.getWidget(row, 0);
        } else {
            return null;
        }
    }


    public void addAttributionToGUI(U relatedEntityDTO) {
        String aliasName = relatedEntityDTO.getName();
        String pub = relatedEntityDTO.getPublicationZdbID();

        relatedEntityDTO.setEditable(isEditable(relatedEntityDTO));

        // this may need to be inserted after the last pub, but for now I don't see a problem with it
        int index = getRelatedEntityIndex(aliasName);
        if (getPublicationWidget(index) == null) {
            relatedEntityTable.setWidget(index, 1, new PublicationAttributionLabel<U>(this, pub, aliasName, relatedEntityDTO));
        } else if (getPublicationWidget(index).isClear()) {
            getPublicationWidget(index).setPublication(pub);
            relatedEntityTable.setWidget(index, 1, new PublicationAttributionLabel<U>(this, pub, aliasName, relatedEntityDTO));
        } else {
            relatedEntityTable.insertRow(index + 1);
            relatedEntityTable.setWidget(index + 1, 0, new RelatedEntityLabel<U>(this, aliasName, false, relatedEntityDTO));
            relatedEntityTable.setWidget(index + 1, 1, new PublicationAttributionLabel<U>(this, pub, aliasName, relatedEntityDTO));
        }
        resetRelatedEntityAdd();
    }


    protected List<String> getRelatedEntityNames() {
        int rowCount = relatedEntityTable.getRowCount();
        List<String> relatedEntityList = new ArrayList<String>();
        for (int i = 0; i < rowCount; ++i) {
            if (relatedEntityTable.getWidget(i, 0) == null) {
                Window.alert("Problem at row, contact dev: " + i);
                return null;
            } else {
                relatedEntityList.add(((RelatedEntityLabel<U>) relatedEntityTable.getWidget(i, 0)).getName());
            }
        }
        return relatedEntityList;
    }

    protected List<U> getRelatedEntityDTOs() {
        List<U> entities = new ArrayList<U>();
        int rowCount = relatedEntityTable.getRowCount();

        for (int i = 0; i < rowCount; ++i) {
            if (relatedEntityTable.getWidget(i, 0) == null) {
                Window.alert("Problem at row, contact dev: " + i);
                return null;
            } else {
                entities.add(((RelatedEntityLabel<U>) relatedEntityTable.getWidget(i, 0)).getRelatedEntityDTO());
            }
        }

        return entities;
    }

    public void addRelatedEntityToGUI(U relatedEntityDTO) {
        String publicationZdbID = relatedEntityDTO.getPublicationZdbID();
        String attributionName = relatedEntityDTO.getName();

        relatedEntityDTO.setEditable(isEditable(relatedEntityDTO));

        if (publicationZdbID == null) {
            publicationZdbID = "";
        }
        // if this attribution already exists, just add a reference
        if (getRelatedEntityIndex(attributionName) >= 0) {
            addAttributionToGUI(relatedEntityDTO);
            return;
        }

        // handle view
        int numRows = relatedEntityTable.getRowCount();
        relatedEntityTable.insertRow(numRows);
        // add namePanel
        relatedEntityTable.setWidget(numRows, 0, new RelatedEntityLabel<U>(this, attributionName, relatedEntityDTO));
        // add publication panel
        relatedEntityTable.setWidget(numRows, 1, new PublicationAttributionLabel<U>(this, publicationZdbID, attributionName, relatedEntityDTO));
        // add to current alias listBox
        resetRelatedEntityAdd();
    }


    public void removeRelatedEntityFromGUI(U relatedEntityDTO) {
        String attributionName = relatedEntityDTO.getName();
        int aliasIndex = getRelatedEntityIndex(attributionName);
        int numPubsForAttribution = getRelatedEntityAttributionsForName(attributionName).size();
        if (numPubsForAttribution == 0) {
            relatedEntityTable.removeRow(aliasIndex);
        } else {
            for (int i = numPubsForAttribution - 1; i >= 0; i--) {
                // do databasey stuff
                relatedEntityTable.removeRow(i + aliasIndex);
            }
        }
    }

    protected int getAttributionIndex(String aliasName, String publication) {
        int rowCount = relatedEntityTable.getRowCount();
        for (int i = 0; i < rowCount; ++i) {
            if (relatedEntityTable.getWidget(i, 1) != null) {
                PublicationAttributionLabel pubWidget = ((PublicationAttributionLabel) relatedEntityTable.getWidget(i, 1));
                if (pubWidget.getAssociatedName().equals(aliasName)
                        &&
                        pubWidget.getPublication().equals(publication)
                        ) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void removeAttributionFromGUI(U relatedEntityDTO) {
        String aliasName = relatedEntityDTO.getName();
        String publicationZdbID = relatedEntityDTO.getPublicationZdbID();
        int pubIndex = getAttributionIndex(aliasName, publicationZdbID);
        int aliasIndex = getRelatedEntityIndex(aliasName);
        int pubIndexForName = aliasIndex - pubIndex;
        int numPubsForName = getRelatedEntityAttributionsForName(aliasName).size();
        if (pubIndex < 0) {
            return;
        }
        // do database stuff

        // case 1, not the first row, just delete
        if (((RelatedEntityLabel<U>) relatedEntityTable.getWidget(pubIndex, 0)).isVisibleName() == false) {
            relatedEntityTable.removeRow(pubIndex);
        }
        // case 2, the first row, but the only elemnt
        else if (pubIndexForName == 0 && numPubsForName == 1) {
            ((PublicationAttributionLabel) relatedEntityTable.getWidget(pubIndex, 1)).clearPublication();
        }
        // case 3, the first row, but many elements, so need to shift up
        else if (pubIndexForName == 0 && numPubsForName > 1) {
            for (int i = 0; i < numPubsForName - 1; i++) {
                getPublicationWidget(i + pubIndex).setPublication(getPublicationWidget(i + pubIndex + 1).getPublication());
            }

            getNameWidget(aliasIndex).setVisibleName(true);
            relatedEntityTable.removeRow(pubIndex + 1);
        }

    }


    protected void resetRelatedEntityAdd() {
        newRelatedEntityField.setText("");
    }

    public String getNewRelatedEntityField() {
        return newRelatedEntityField.getText().trim();
    }
}
