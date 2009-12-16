package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.TranscriptDTO;
import org.zfin.marker.presentation.event.PublicationChangeEvent;
import org.zfin.marker.presentation.event.PublicationChangeListener;
import org.zfin.marker.presentation.event.TranscriptChangeEvent;
import org.zfin.marker.presentation.event.TranscriptChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class TranscriptHeaderEdit extends Composite implements HandlesError, PublicationChangeListener {

    // GUI
    private final String TEXT_WORKING = "working...";
    private final String TEXT_UPDATE = "update";

    // GUI elements
    private VerticalPanel panel = new VerticalPanel();

    // GUI name/type elements
    private HTMLTable table = new Grid(4, 2);
    private Label zdbIDLabel = new Label("ZdbID: ");
    private HTML zdbIDHTML = new HTML();
    private TextBox nameBox = new TextBox();
    private EasyListBox typeListBox = new EasyListBox(false);
    private EasyListBox statusListBox = new EasyListBox(false);
    private HorizontalPanel buttonPanel = new HorizontalPanel();
    private Button updateButton = new Button(TEXT_UPDATE);
    private Button revertButton = new Button("revert");

    private Label errorLabel = new Label();

    // listeners
    private List<TranscriptChangeListener> transcriptChangeListeners = new ArrayList<TranscriptChangeListener>();
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    // internal data
    private TranscriptDTO transcriptDTO;
    private String publicationZdbID;

    public TranscriptHeaderEdit(String div) {
        initGui();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        addTranscriptListeners(new TranscriptChangeListener() {
            public void changeTranscriptProperties(final TranscriptChangeEvent transcriptChangeEvent) {
                final TranscriptDTO newTranscriptDTO = transcriptChangeEvent.getTranscriptDTO();
                working();
                TranscriptRPCService.App.getInstance().changeTranscriptHeaders(newTranscriptDTO,
                        new MarkerEditCallBack<TranscriptDTO>("failed to change transcript properties: ", handlesError) {
                            public void onSuccess(TranscriptDTO newTranscriptDTO) {
                                handleChangeSuccess(transcriptDTO);
                                transcriptDTO.copyFrom(newTranscriptDTO);
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


    protected void initGui() {
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


        typeListBox.addChangeListener(new ChangeListener() {
            public void onChange(Widget widget) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        statusListBox.addChangeListener(new ChangeListener() {
            public void onChange(Widget widget) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        nameBox.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget widget, char c, int i) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });


        updateButton.setEnabled(false);
        updateButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                sendUpdates();
            }
        });

        revertButton.setEnabled(false);
        revertButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                revert();
            }
        });
    }

    public void working() {
        updateButton.setText(TEXT_WORKING);
        updateButton.setEnabled(false);
        revertButton.setEnabled(false);
    }

    public void notWorking() {
        updateButton.setText(TEXT_UPDATE);
    }

    public int getIndexForType(String type) {
        for (int i = 0; i < typeListBox.getItemCount(); i++) {
            if (typeListBox.getItemText(i).equals(type)) {
                return i;
            }
        }
        return -1;
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
//        transcriptDTO.setMarkerType(this.transcriptDTO.getMarkerType()); // should not change
        newTranscriptDTO.setZdbID(this.transcriptDTO.getZdbID());
//        if(false==transcriptName.equals(transcript.getName())) {
        if (isDirty() == true) {
            if ((publicationZdbID == null || publicationZdbID.length() < 16) && false == newTranscriptDTO.getName().equals(this.transcriptDTO.getName())) {
                setError("Need to attribute name changes.");
                return;
            }
            fireTranscriptChangeEvent(new TranscriptChangeEvent(newTranscriptDTO));
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

    protected class CompareCommand implements Command {
        public void execute() {
            boolean isDirty = isDirty();

            if (true == isDirty) {
                updateButton.setEnabled(true);
                revertButton.setEnabled(true);
            } else {
                updateButton.setEnabled(false);
                revertButton.setEnabled(false);
            }
        }
    }


    public void addTranscriptListeners(TranscriptChangeListener transcriptChangeListener) {
        transcriptChangeListeners.add(transcriptChangeListener);
    }

    public void removeTranscriptChangeListeners(TranscriptChangeListener transcriptChangeListener) {
        transcriptChangeListeners.remove(transcriptChangeListener);
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


    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public void publicationChanged(PublicationChangeEvent event) {
        publicationZdbID = event.getPublication();
    }

    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }
}
