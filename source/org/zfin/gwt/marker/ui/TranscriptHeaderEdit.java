package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.TranscriptDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.Collections;
import java.util.List;

/**
 */
public class TranscriptHeaderEdit extends AbstractHeaderEdit<TranscriptDTO>{

    // GUI name/type elements
    private final HTMLTable table = new Grid(4, 2);
    private final StringListBox typeListBox = new StringListBox(false);
    private final StringListBox statusListBox = new StringListBox(false);

    // listeners

    public TranscriptHeaderEdit() {
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(StandardDivNames.headerDiv).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(this);

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
        buttonPanel.add(saveButton);
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);
        panel.setStyleName("gwt-editbox");

        panel.add(new HTML("<br>")); // spacer

        errorLabel.setStyleName("error");
        panel.add(errorLabel);


    }


    protected void revertGUI() {
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + dto.getZdbID() + "</font>");
        nameBox.setText(dto.getName());
        typeListBox.setIndexForText(dto.getTranscriptType());
        statusListBox.setIndexForText(dto.getTranscriptStatus());
        handleDirty();
    }

    protected void sendUpdates() {

        // on success
        // set choices appropriates
        final TranscriptDTO newTranscriptDTO = new TranscriptDTO();
        newTranscriptDTO.setName(nameBox.getText());
        newTranscriptDTO.setTranscriptStatus(statusListBox.getSelected());
        newTranscriptDTO.setTranscriptType(typeListBox.getSelected());
        newTranscriptDTO.setZdbID(dto.getZdbID());



        if (isDirty()) {

            if(false == nameValidator.validate(nameBox.getText(),this)) return ;

            if(false == newTranscriptDTO.getName().equals(dto.getName())){
                if(false == publicationValidator.validate(publicationZdbID,this)) return ;
            }

            working();
            TranscriptRPCService.App.getInstance().changeTranscriptHeaders(newTranscriptDTO,
                    new MarkerEditCallBack<TranscriptDTO>("failed to change transcript properties: ", this) {
                        @Override
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof TranscriptTypeStatusMismatchException) {
                                TranscriptTypeStatusMismatchException e = (TranscriptTypeStatusMismatchException) throwable;
                                setError("Bad status for type.  Allowable types: " + e.getAllowableStatuses());
                                return ;
                            }

                            super.onFailure(throwable);
                            revertGUI();
                            notWorking();
                            setError(throwable.getMessage());
//                            TranscriptRPCService.App.getInstance().getTranscriptForZdbID(dto.getZdbID(),
//                                    new MarkerEditCallBack<TranscriptDTO>("failed to retrieve transcript: " ,handlesError){
//                                        @Override
//                                        public void onSuccess(TranscriptDTO result) {
//                                            dto.copyFrom(result);
//                                            DeferredCommand.addCommand(new CompareCommand());
//                                        }
//                                    });
                        }

                        @Override
                        public void onSuccess(TranscriptDTO returnedTranscriptDTO) {
                            String oldName = dto.getName();
                            handleChangeSuccess(returnedTranscriptDTO);
                            fireEventSuccess();
                            fireChangeEvent(new RelatedEntityEvent<TranscriptDTO>(dto,oldName));
                            notWorking();
                        }

                    });
        }
    }

    void handleChangeSuccess(TranscriptDTO transcriptDTO) {
        this.dto = transcriptDTO;
        DeferredCommand.addCommand(new CompareCommand());
    }

    public boolean isDirty() {
        boolean isDirty = false;
        isDirty = nameBox.isDirty(dto.getName()) || isDirty ;
        isDirty = typeListBox.isDirty(dto.getTranscriptType()) || isDirty ;
        isDirty = statusListBox.isDirty(dto.getTranscriptStatus()) || isDirty ;
        return isDirty;
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

    @Override
    public void working() {
        super.working();    //To change body of overridden methods use File | Settings | File Templates.
        typeListBox.setEnabled(false);
        statusListBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        super.notWorking();    //To change body of overridden methods use File | Settings | File Templates.
        typeListBox.setEnabled(true);
        statusListBox.setEnabled(true);
    }
}
