package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.CloneChangeEvent;
import org.zfin.gwt.marker.event.CloneChangeListener;
import org.zfin.gwt.root.dto.CloneDTO;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class CloneHeaderEdit extends AbstractHeaderEdit{

    // GUI name/type elements
    private HTMLTable table = new Grid(5, 2);
    private HTML typeLabel = new HTML("");
    private EasyListBox problemTypeListBox = new EasyListBox();

    // listeners
    private List<CloneChangeListener> cloneChangeListeners = new ArrayList<CloneChangeListener>();

    // internal data
    private CloneDTO cloneDTO;

    public CloneHeaderEdit(String div) {
        super();
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        addMarkerChangeListener(new CloneChangeListener() {
            public void changeCloneProperties(final CloneChangeEvent cloneChangeEvent) {
                final CloneDTO markerDTO = cloneChangeEvent.getDTO();
                CloneRPCService.App.getInstance().updateCloneHeaders(markerDTO,
                        new MarkerEditCallBack<Void>("failed to change clone name and type: ",handlesError) {
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                revert();
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public void onSuccess(Void o) {
                                handleChangeSuccess(markerDTO);
//                                fireCloneChangeEvent(new CloneChangeEvent(markerDTO,cloneDTO.getName()));
                                fireEventSuccess();
                                //To change body of implemented methods use File | Settings | File Templates.
//                        aliasRelatedEntities.addNewRelatedEntityToGUI(oldTranscriptName,publication);
                            }
                        });
            }
        });
    }

    public void setDomain(CloneDTO markerDTO) {
        this.cloneDTO = markerDTO;
        refreshGUI();
    }

    public void refreshGUI() {
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + cloneDTO.getZdbID() + "</font>");
        nameBox.setText(cloneDTO.getName());
        typeLabel.setHTML("<div class=\"attributionDefaultPub\">" + cloneDTO.getMarkerType() + "</font>");
        problemTypeListBox.addNullAndItems(cloneDTO.getProblemTypes()) ;
        problemTypeListBox.setIndexForValue(cloneDTO.getProblemType());
    }


    protected void initGUI() {

        table.setWidget(0, 0, zdbIDLabel);
        table.setWidget(0, 1, zdbIDHTML);
        table.setText(1, 0, "Marker Name:");
        table.setWidget(1, 1, nameBox);
        table.setText(2, 0, "Marker Type:");
        table.setWidget(2, 1, typeLabel);
        table.setText(3, 0, "Problem Type:");
        table.setWidget(3, 1, problemTypeListBox);
        table.setWidget(4, 1, errorLabel);
        panel.add(table);

        errorLabel.setStyleName("error");

        buttonPanel.add(updateButton);
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);
        panel.add(new HTML("<br>")); // spacer


        nameBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        problemTypeListBox.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });


        updateButton.setEnabled(false);
        updateButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sendUpdates();
            }
        });

        revertButton.setEnabled(false);
        revertButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                revert();
            }
        });
    }


    public void revert() {
        nameBox.setText(cloneDTO.getName());
        problemTypeListBox.setIndexForValue(cloneDTO.getProblemType());
        DeferredCommand.addCommand(new CompareCommand());
    }

    protected void sendUpdates() {
        // on success
        // set choices appropriates
        if (isDirty() == true) {
            CloneDTO newCloneDTO = new CloneDTO();
            newCloneDTO.setZdbID(cloneDTO.getZdbID());
            newCloneDTO.setName(nameBox.getText());
            newCloneDTO.setProblemType(problemTypeListBox.getSelectedString());


            if (newCloneDTO.getProblemType() != null &&
                    ((false == cloneDTO.getZdbID().startsWith("ZDB-EST"))
                            &&
                            (false == cloneDTO.getZdbID().startsWith("ZDB-CDNA")))
                    ) {
                setError("Only EST's and CDNA's can have problem types.");
                return;
            }

            if ((publicationZdbID == null || publicationZdbID.length() < 16) && false == newCloneDTO.getName().equals(this.cloneDTO.getName())) {
                setError("Need to attribute name changes.");
                return;
            }
            fireCloneChangeEvent(new CloneChangeEvent(newCloneDTO,cloneDTO.getName()));
        }
    }

    // the only thing that we chan change, I think.

    public void handleChangeSuccess(CloneDTO dto) {
        cloneDTO = dto ;
//        cloneDTO.setName(dto.getName());
//        cloneDTO.setProblemType(dto.getProblemType());
        DeferredCommand.addCommand(new CompareCommand());
    }

    public void fireCloneChangeEvent(CloneChangeEvent cloneChangeEvent) {
        for (CloneChangeListener cloneChangeListener : cloneChangeListeners) {
            cloneChangeListener.changeCloneProperties(cloneChangeEvent);
        }
    }

    protected boolean isDirty() {
        // check names
        if (false == nameBox.getText().equals(cloneDTO.getName())) return true ;
        if (false == problemTypeListBox.isFieldEqual(cloneDTO.getProblemType())) return true;

        return false;
    }

    public void addMarkerChangeListener(CloneChangeListener cloneChangeListener) {
        cloneChangeListeners.add(cloneChangeListener);
    }

    public String getZdbID() {
        return cloneDTO.getZdbID();
    }

}
