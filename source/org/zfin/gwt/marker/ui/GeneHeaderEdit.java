package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.HandlesError;

/**
 */
public class GeneHeaderEdit extends AbstractHeaderEdit<MarkerDTO>{

    // GUI name/type elements
    private HTMLTable table = new Grid(4, 2);
    private HTML typeLabel = new HTML("");

    public GeneHeaderEdit(String div) {
        super();
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        // TODO: add gene name or abbrev here

    }

    public void revertGUI() {
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + dto.getZdbID() + "</font>");
        nameBox.setText(dto.getName());
        typeLabel.setHTML("<div class=\"attributionDefaultPub\">" + dto.getMarkerType() + "</font>");
        handleDirty();
    }


    protected void initGUI() {

        table.setWidget(0, 0, zdbIDLabel);
        table.setWidget(0, 1, zdbIDHTML);
        table.setText(1, 0, "Marker Name:");
        table.setWidget(1, 1, nameBox);
        table.setText(2, 0, "Marker Type:");
        table.setWidget(2, 1, typeLabel);
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
            final MarkerDTO markerDTO = dto.deepCopy();
            markerDTO.setName(nameBox.getText());

            if(false == nameValidator.validate(nameBox.getText(),this)) return ;
            if(false==publicationValidator.validate(publicationZdbID,this)) return ;

            working();
            MarkerRPCService.App.getInstance().updateMarkerHeaders(markerDTO,
                    new MarkerEditCallBack<Void>("failed to change clone name and type: ",this) {
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            revertGUI();
                            notWorking();
                        }

                        public void onSuccess(Void o) {
                            handleChangeSuccess(markerDTO);
                            fireEventSuccess();
                            fireChangeEvent(new RelatedEntityEvent<MarkerDTO>(markerDTO,dto.getName()));
                            notWorking();
                        }
                    });

        }
    }

    // the only thing that we chan change, I think.

    public void handleChangeSuccess(MarkerDTO markerDTO) {
        dto = markerDTO ;
//        dto.setName(dto.getName());
//        dto.setProblemType(dto.getProblemType());
        DeferredCommand.addCommand(new CompareCommand());
    }

    public boolean isDirty() {
        if (nameBox.isDirty(dto.getName())) return true ;
        return false;
    }

}