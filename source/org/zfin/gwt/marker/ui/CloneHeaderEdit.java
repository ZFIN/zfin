package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.StringListBox;

/**
 */
public class CloneHeaderEdit extends AbstractHeaderEdit<CloneDTO>{

    // GUI name/type elements
    private HTMLTable table = new Grid(4, 2);
    private HTML typeLabel = new HTML("");
    private StringListBox problemTypeListBox = new StringListBox();

    public CloneHeaderEdit(String div) {
        super();
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {

        super.addInternalListeners(handlesError);

        problemTypeListBox.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

    }

    public void revertGUI() {
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + dto.getZdbID() + "</font>");
        nameBox.setText(dto.getName());
        typeLabel.setHTML("<div class=\"attributionDefaultPub\">" + dto.getMarkerType() + "</font>");
        problemTypeListBox.addNullAndItems(dto.getProblemTypes()) ;
        problemTypeListBox.setIndexForValue(dto.getProblemType());
        handleDirty();
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
        panel.add(table);

        panel.setStyleName("gwt-editbox");

        errorLabel.setStyleName("error");

        buttonPanel.add(saveButton);
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);
        panel.add(errorLabel);

        saveButton.setEnabled(false);
        revertButton.setEnabled(false);

    }

    protected void sendUpdates() {
        // on success
        // set choices appropriates
        if (isDirty()) {
            final CloneDTO newCloneDTO = dto.deepCopy();
            newCloneDTO.setName(nameBox.getText());
            newCloneDTO.setProblemType(problemTypeListBox.getSelected());


            if (newCloneDTO.getProblemType() != null &&
                    ((false == dto.getZdbID().startsWith("ZDB-EST"))
                            &&
                            (false == dto.getZdbID().startsWith("ZDB-CDNA")))
                    ) {
                setError("Only EST's and CDNA's can have problem types.");
                return;
            }

            if(false == nameValidator.validate(nameBox.getText(),this)) return ;

            if(false == newCloneDTO.getName().equals(dto.getName())){
                if(false == publicationValidator.validate(publicationZdbID,this)) return ;
            }

            working();
            CloneRPCService.App.getInstance().updateCloneHeaders(newCloneDTO,
                    new MarkerEditCallBack<Void>("failed to change clone name and type: ",this) {
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            revertGUI();
                            notWorking();
                            setError(throwable.getMessage());
                        }

                        public void onSuccess(Void o) {
                            handleChangeSuccess(newCloneDTO);
                            fireEventSuccess();
                            fireChangeEvent(new RelatedEntityEvent<CloneDTO>(newCloneDTO,dto.getName()));
                            notWorking();
                        }
                    });
        }
    }

    // the only thing that we chan change, I think.

    public void handleChangeSuccess(CloneDTO cloneDTO) {
        this.dto= cloneDTO;
//        cloneDTO.setName(dto.getName());
//        cloneDTO.setProblemType(dto.getProblemType());
        DeferredCommand.addCommand(new CompareCommand());
    }

    public boolean isDirty() {
        if (nameBox.isDirty(dto.getName())) return true ;
        if (problemTypeListBox.isDirty(dto.getProblemType())) return true;

        return false;
    }

}
