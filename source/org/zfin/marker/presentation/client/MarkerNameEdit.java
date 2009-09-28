package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;

import java.util.List;
import java.util.ArrayList;

import org.zfin.marker.presentation.dto.MarkerDTO;
import org.zfin.marker.presentation.event.*;

/**
 */
public class MarkerNameEdit extends Composite {

    // GUI
    private final String TEXT_WORKING = "working..." ;
    private final String TEXT_UPDATE = "update" ;

    // GUI elements
    private VerticalPanel panel = new VerticalPanel() ;

    // GUI name/type elements
    private HTMLTable table = new Grid(3,2) ;
    private Label zdbIDLabel = new Label("Marker ZdbID: ") ;
    private HTML zdbIDHTML = new HTML() ;
    private TextBox nameBox = new TextBox() ;
    private HTML typeLabel = new HTML("") ;
    private HorizontalPanel buttonPanel = new HorizontalPanel() ;
    private Button updateButton = new Button(TEXT_UPDATE) ;
    private Button revertButton = new Button("revert") ;


    // listeners
    private List<MarkerChangeListener> markerChangeListeners = new ArrayList<MarkerChangeListener>() ;

    // internal data
    private MarkerDTO markerDTO;

    public MarkerNameEdit(String div){
        super() ;
        initGui() ;
        initWidget(panel);
        addInternalListeners();
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(){
        addMarkerChangeListener(new MarkerChangeListener(){
            public void changeMarkerProperties(final MarkerChangeEvent changeEvent) {
                final MarkerDTO markerDTO = changeEvent.getMarkerDTO() ;
                MarkerRPCService.App.getInstance().updateMarkerName(markerDTO,
                        new MarkerEditCallBack<Void>("failed to change clone name and type: "){
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                revert();
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public void onSuccess(Void o) {
                                handleChangeSuccess(markerDTO);
                                fireMarkerChangeEvent(new MarkerChangeEvent(markerDTO));
                                //To change body of implemented methods use File | Settings | File Templates.
//                        aliasRelatedEntities.addNewRelatedEntityToGUI(oldTranscriptName,publication);
                            }
                        });
            }
        });
    }

    public void setDomain(MarkerDTO markerDTO){
        this.markerDTO = markerDTO;
        refreshGUI();
    }

    public void refreshGUI(){
        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">"+markerDTO.getZdbID()+"</font>");
        nameBox.setText(markerDTO.getName());
        typeLabel.setHTML("<div class=\"attributionDefaultPub\">"+markerDTO.getMarkerType()+"</font>");
    }



    private void initGui(){

        table.setWidget(0,0,zdbIDLabel);
        table.setWidget(0,1,zdbIDHTML);
        table.setText(1,0,"Marker Name:");
        table.setWidget(1,1,nameBox);
        table.setText(2,0,"Marker Type:");
        table.setWidget(2,1, typeLabel);
        panel.add(table);

        buttonPanel.add(updateButton) ;
        buttonPanel.add(new HTML("&nbsp;")) ;
        buttonPanel.add(revertButton) ;
        panel.add(buttonPanel);
        panel.add(new HTML("<br>")); // spacer


        nameBox.addKeyboardListener(new KeyboardListenerAdapter(){
            public void onKeyPress(Widget widget, char c, int i) {
                super.onKeyPress(widget, c, i);
                DeferredCommand.addCommand(new CompareCommand());
            }
        });


        updateButton.setEnabled(false);
        updateButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                updateMarker() ;
            }
        });

        revertButton.setEnabled(false);
        revertButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                revert();
            }
        });
    }


    public void working(){
        updateButton.setText(TEXT_WORKING);
        updateButton.setEnabled(false);
        revertButton.setEnabled(false);
    }

    public void notWorking(){
        updateButton.setText(TEXT_UPDATE);
    }


    public void revert(){
        nameBox.setText(markerDTO.getName());
        DeferredCommand.addCommand(new CompareCommand());
    }

    private void updateMarker(){

        // on success
        // set choices appropriates
        MarkerDTO sendMarkerDTO = new MarkerDTO() ;
        sendMarkerDTO.setZdbID(markerDTO.getZdbID());
        sendMarkerDTO.setName(nameBox.getText());
        if(isDirty()==true) {
            fireMarkerChangeEvent(new MarkerChangeEvent(sendMarkerDTO)) ;
        }
    }

    // the only thing that we chan change, I think.
    public void handleChangeSuccess(MarkerDTO dto){
        markerDTO.setName(dto.getName()) ;

        DeferredCommand.addCommand(new CompareCommand());
    }

    public void fireMarkerChangeEvent(MarkerChangeEvent markerChangeEvent){
        for(MarkerChangeListener markerChangeListener: markerChangeListeners){
            markerChangeListener.changeMarkerProperties(markerChangeEvent);
        }
    }

    protected  boolean isDirty(){
        boolean isDirty = false ;

        // check names
        if(false == nameBox.getText().equals(markerDTO.getName())){
            isDirty = true ;
        }

        return isDirty ;
    }

    private class CompareCommand implements Command{
        public void execute() {
            boolean isDirty = isDirty() ;

            if(true==isDirty){
                updateButton.setEnabled(true);
                revertButton.setEnabled(true);
            }
            else{
                updateButton.setEnabled(false);
                revertButton.setEnabled(false);
            }
        }
    }


    public void addMarkerChangeListener(MarkerChangeListener markerChangeListener){
        markerChangeListeners.add(markerChangeListener) ;
    }

    public String getZdbID() {
        return markerDTO.getZdbID();
    }
}
