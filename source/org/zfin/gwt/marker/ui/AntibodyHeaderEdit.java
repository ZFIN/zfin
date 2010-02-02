package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityChangeListener;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.ui.HandlesError;

/**
 */
public class AntibodyHeaderEdit extends AbstractHeaderEdit<AntibodyDTO>{

    // GUI name/type elements
    private final HTMLTable table = new Grid(2, 2);

    public AntibodyHeaderEdit() {
        super();
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
        RootPanel.get(StandardMarkerDivNames.headerDiv).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        addChangeListener(new RelatedEntityChangeListener<AntibodyDTO>(){
            @Override
            public void dataChanged(RelatedEntityEvent<AntibodyDTO> antibodyDTODataChangedEvent) {
            }
        } );
    }

    protected void revertGUI() {
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + dto.getZdbID() + "</font>");
        nameBox.setText(dto.getName());
        DeferredCommand.addCommand(new CompareCommand());
    }


    protected void initGUI() {

        table.setWidget(0, 0, zdbIDLabel);
        table.setWidget(0, 1, zdbIDHTML);
        table.setText(1, 0, "Marker Name:");
        table.setWidget(1, 1, nameBox);
        panel.add(table);
        panel.setStyleName("gwt-editbox");


        buttonPanel.add(saveButton);
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);
        panel.add(new HTML("<br>")); // spacer

        saveButton.setEnabled(false);
        revertButton.setEnabled(false);

        errorLabel.setStyleName("error");
        panel.add(errorLabel);

    }


    protected void sendUpdates() {
        // on success
        // set choices appropriates
        if (isDirty()) {
            final AntibodyDTO antibodyDTO = new AntibodyDTO();
            antibodyDTO.setZdbID(this.dto.getZdbID());
            antibodyDTO.setName(nameBox.getText());

            if(nameBox.getText()==null || nameBox.getText().length()<3){
                setError("Name should be at least 3 characters long.");
                return ; 
            }

            if (publicationZdbID == null || publicationZdbID.trim().length() < 16)  {
                setError("Need to attribute name changes.");
                return;
            }

            working();
            AntibodyRPCService.App.getInstance().updateAntibodyHeaders(antibodyDTO,
                    new MarkerEditCallBack<Void>("failed to change clone name and type: ",this) {
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            notWorking();
                            revertGUI();
                        }

                        public void onSuccess(Void o) {
                            DeferredCommand.addCommand(new CompareCommand());
                            fireEventSuccess();
                            notWorking();
                            fireChangeEvent(new RelatedEntityEvent<AntibodyDTO>(antibodyDTO,dto.getName()));
                        }
                    });
        }
    }

    public boolean isDirty() {
        // check names
//        if (false == nameBox.getText().equals(dto.getName())) return true ;
        return nameBox.isDirty(dto.getName());
    }

}