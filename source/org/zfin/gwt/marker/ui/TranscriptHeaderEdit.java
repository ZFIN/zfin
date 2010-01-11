package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.TranscriptChangeEvent;
import org.zfin.gwt.marker.event.TranscriptChangeListener;
import org.zfin.gwt.root.dto.TranscriptDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class TranscriptHeaderEdit extends AbstractHeaderEdit{

    // GUI name/type elements
    protected HTMLTable table = new Grid(4, 2);
    private EasyListBox typeListBox = new EasyListBox(false);
    private EasyListBox statusListBox = new EasyListBox(false);

    // listeners
    private List<TranscriptChangeListener> transcriptChangeListeners = new ArrayList<TranscriptChangeListener>();

    // internal data
    private TranscriptDTO transcriptDTO;

    public TranscriptHeaderEdit(String div) {
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        addTranscriptListeners(new TranscriptChangeListener() {
            public void changeTranscriptProperties(final TranscriptChangeEvent transcriptChangeEvent) {
                final TranscriptDTO newTranscriptDTO = transcriptChangeEvent.getDTO();
                working();
                TranscriptRPCService.App.getInstance().changeTranscriptHeaders(newTranscriptDTO,
                        new MarkerEditCallBack<TranscriptDTO>("failed to change transcript properties: ", handlesError) {
                            public void onSuccess(TranscriptDTO newTranscriptDTO) {
                                if (transcriptChangeEvent.isNameChanged()) {
                                    if (null == previousNamesBox.validateNewRelatedEntity(transcriptChangeEvent.getDTO().getName())) {
                                        previousNamesBox.addRelatedEntity(transcriptChangeEvent.getPreviousName(), publicationZdbID);
                                    }
                                }

                                handleChangeSuccess(transcriptDTO);
                                fireEventSuccess();
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                if (throwable instanceof TranscriptTypeStatusMismatchException) {
                                    TranscriptTypeStatusMismatchException e = (TranscriptTypeStatusMismatchException) throwable;
                                    setError("Bad status for type.  Allowable types: " + e.getAllowableStatuses());
                                } else {
                                    super.onFailure(throwable);
                                }
                                TranscriptRPCService.App.getInstance().getTranscriptForZdbID(transcriptDTO.getZdbID(),
                                        new MarkerEditCallBack<TranscriptDTO>("failed to retrieve transcript: " ,handlesError){
                                            @Override
                                            public void onSuccess(TranscriptDTO result) {
                                                transcriptDTO = transcriptDTO.copyFrom(result);
                                                DeferredCommand.addCommand(new CompareCommand());
                                            }
                                        });
                            }
                        });
                notWorking();
            }
        });
    }


    public void setTranscriptDomain(TranscriptDTO transcriptDTO) {
        this.transcriptDTO = transcriptDTO;
        refreshGUI();
    }


    protected void initGUI() {
        table.setWidget(0, 0, zdbIDLabel);
        table.setWidget(0, 1, zdbIDHTML);
        table.setText(1, 0, "Name:");
        table.setWidget(1, 1, nameBox);
        table.setText(2, 0, "Type:");
        table.setWidget(2, 1, typeListBox);
        table.setText(3, 0, "Status:");
        table.setWidget(3, 1, statusListBox);

        panel.add(table);
        buttonPanel.add(updateButton);
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);

        panel.add(new HTML("<br>")); // spacer

        errorLabel.setStyleName("error");
        panel.add(errorLabel);


        typeListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        statusListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        nameBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
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


    protected void refreshGUI() {
        revert();
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + transcriptDTO.getZdbID() + "</font>");
    }

    public void revert() {
        nameBox.setText(transcriptDTO.getName());
        typeListBox.setIndexForValue(transcriptDTO.getTranscriptType());
        statusListBox.setIndexForValue(transcriptDTO.getTranscriptStatus());
        DeferredCommand.addCommand(new CompareCommand());
    }

    protected void sendUpdates() {

        // on success
        // set choices appropriates
        TranscriptDTO newTranscriptDTO = new TranscriptDTO();
        newTranscriptDTO.setName(nameBox.getText());
        newTranscriptDTO.setTranscriptStatus(statusListBox.getSelectedString());
        newTranscriptDTO.setTranscriptType(typeListBox.getSelectedString());
        newTranscriptDTO.setZdbID(this.transcriptDTO.getZdbID());
        if (isDirty() == true) {
            if ((publicationZdbID == null || publicationZdbID.length() < 16) && false == newTranscriptDTO.getName().equals(this.transcriptDTO.getName())) {
                setError("Need to attribute name changes.");
                return;
            }
            fireTranscriptChangeEvent(new TranscriptChangeEvent(newTranscriptDTO,transcriptDTO.getName()));
        }
    }

    public void handleChangeSuccess(TranscriptDTO transcriptDTO) {
        this.transcriptDTO = transcriptDTO;

        DeferredCommand.addCommand(new CompareCommand());
    }

    protected void fireTranscriptChangeEvent(TranscriptChangeEvent transcriptChangeEvent) {
        for (TranscriptChangeListener transcriptChangeListener : transcriptChangeListeners) {
            transcriptChangeListener.changeTranscriptProperties(transcriptChangeEvent);
        }
    }

    protected boolean isDirty() {
        boolean isDirty = false;

        // check names
        if (false == nameBox.getText().equals(transcriptDTO.getName())) {
            isDirty = true;
        }

        // check type box
        if (false == typeListBox.isFieldEqual(transcriptDTO.getTranscriptType())) {
            isDirty = true;
        }


        // check type box
        if (false == statusListBox.isFieldEqual(transcriptDTO.getTranscriptStatus())) {
            isDirty = true;
        }
//        Window.alert("is dirty 2: "+ isDirty + " chosenType: "+ chosenType + " typeListBox: "+typeListBox.getItemText(typeListBox.getSelectedIndex()));
        return isDirty;
    }


    public void addTranscriptListeners(TranscriptChangeListener transcriptChangeListener) {
        transcriptChangeListeners.add(transcriptChangeListener);
    }


    public void setTranscriptTypes(List<String> transcriptTypeList) {
        for (String transcriptType : transcriptTypeList) {
            typeListBox.addItem(transcriptType);
        }
    }


    public void setTranscriptStatuses(List<String> transcriptStatusList) {
        statusListBox.addItem("none", "null");
        Collections.sort(transcriptStatusList);
        for (String transcriptStatus : transcriptStatusList) {
            statusListBox.addItem(transcriptStatus);
        }
    }

}
