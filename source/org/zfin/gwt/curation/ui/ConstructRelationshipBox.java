package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.event.FilterChangeEvent;
import org.zfin.gwt.curation.event.FilterChangeListener;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.marker.MarkerType;

import java.util.Collections;
import java.util.List;

/**
 */
public class ConstructRelationshipBox extends AbstractComposite<ConstructDTO>{


    private VerticalPanel panel = new VerticalPanel();
    private FlexTable constructTable = new FlexTable() ;
    private HTMLTable.RowFormatter rowFormatter = constructTable.getRowFormatter() ;
   // private FeatureFilterModule constructFilterModule = new MarkerFilterModule();
    private Button addButton = new Button("Add") ;

    // for add row
    private StringListBox constructToAddList = new StringListBox() ;
    private StringListBox constructToAddRelationship = new StringListBox() ;
    private StringListBox constructToAddTarget = new StringListBox() ;
    private Label constructToAddType = new Label() ;

    // internal data
    private List<ConstructRelationshipDTO> constructMarkerRelationshipDTOs ;

    private MarkerDTO markerDTOs;
    private List<ConstructDTO> constructDTOs;
    private String lastSelectedConstructZdbId = null ;

    public ConstructRelationshipBox(){
        initGUI() ;
        setValues() ;
        addInternalListeners(this) ;
        exposeConstructRelationshipsToJavascript(this);
    }

    protected void initGUI() {
        initWidget(panel);

        panel.add(new HTML("<br>"));
  //      panel.add(constructFilterModule);
        panel.add(new HTML("<br>"));
        panel.setWidth("100%");


        constructToAddList.setName("constructToAddList");
        constructTable.setStyleName(CssStyles.SEARCHRESULTS + " " + CssStyles.GROUPSTRIPES_HOVER);
        constructTable.setWidth("100%");
        panel.add(constructTable);
        addButton.setEnabled(false);
        constructToAddRelationship.setEnabled(false);
        constructToAddTarget.setEnabled(false);
        panel.add(addButton);
        panel.add(errorLabel);
        errorLabel.setStyleName("error");
    }



    protected void setValues() {
        if(dto!=null && dto.getPublicationZdbID()!=null){
            MarkerRPCService.App.getInstance().getConstructMarkerRelationshipsForPub(dto.getPublicationZdbID(),
                    new MarkerEditCallBack<List<ConstructRelationshipDTO>>(
                            "Failed to find construct marker relationships for this pub: "
                                    + dto.getPublicationZdbID(),this){
                        @Override
                        public void onSuccess(List<ConstructRelationshipDTO> constructMarkerRelationshipDTOList) {
                            constructMarkerRelationshipDTOs = constructMarkerRelationshipDTOList;
                            redrawTable();
                        }
                    });

            MarkerRPCService.App.getInstance().getConstructsForPub(dto.getPublicationZdbID(),
                    new MarkerEditCallBack<List<ConstructDTO>>("Problem finding constructs for pub: " + dto.getPublicationZdbID()+ " ",this) {

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            constructToAddList.setEnabled(false);
                        }

                        @Override
                        public void onSuccess(List<ConstructDTO> constructs) {
                            constructDTOs = constructs ;
                            if(constructDTOs!=null){
                                constructToAddList.clear();
                                constructToAddList.addItem("-----------");
                                for(ConstructDTO constructDTO : constructDTOs){
                                    constructToAddList.addItem(constructDTO.getName(),constructDTO.getZdbID());
                                }
                                constructToAddList.setEnabled(true);
                            }

                            if(lastSelectedConstructZdbId!=null){
                                constructToAddList.setIndexForValue(lastSelectedConstructZdbId);
                                lastSelectedConstructZdbId = null ;
                                constructAddListChanged();
                            }
                        }
                    });
        }
    }




    /**
     * assume that 0 is the header and numRows-1 is the add widget
     */
    private void redrawTable(){
        // remove the rows
        while(constructTable.getRowCount()>0){
            constructTable.removeRow(0);
        }
        // creates the header
        constructTable.setText(0,0,"Construct");
        constructTable.setText(0,1,"Type");
        constructTable.setText(0,2,"Relationship");
        constructTable.setText(0,3,"Target");
        constructTable.setText(0,4,"Delete");
        rowFormatter.setStyleName(0, CssStyles.TABLE_HEADER.toString());


        // creates the add row
        constructTable.setWidget(1,0,constructToAddList);
        constructTable.setWidget(1,1, constructToAddType);
        constructTable.setWidget(1,2, constructToAddRelationship);
        constructTable.setWidget(1,3,constructToAddTarget);
        rowFormatter.setStyleName(1, CssStyles.EXPERIMENT_ROW.toString());


        if(constructMarkerRelationshipDTOs!=null && constructMarkerRelationshipDTOs.size()>0){
            String lastFeatureName = constructMarkerRelationshipDTOs.get(0).getName();
            CssStyles lastStyle = CssStyles.ODDGROUP;
            String newGroupStyle= "";
            for(ConstructRelationshipDTO relationshipDTO : constructMarkerRelationshipDTOs){
                constructTable.insertRow(constructTable.getRowCount()-1) ;
                int lastRow = constructTable.getRowCount()-2 ;
                constructTable.setWidget(lastRow,1, new Label(relationshipDTO.getConstructDTO().getConstructType()));
                constructTable.setWidget(lastRow,2, new Label(relationshipDTO.getRelationshipType()));
                constructTable.setWidget(lastRow,3,new HTML(relationshipDTO.getMarkerDTO().getLink())) ;
                if(relationshipDTO.getRelationshipType().equals("contains engineered region")){
                constructTable.setWidget(lastRow,4,new DeleteConstructMarkerRelationshipButton(relationshipDTO,this) );
                }

                // switch style if name changes
                if(!relationshipDTO.getConstructDTO().getName().equals(lastFeatureName)){
                    lastStyle  =  (  lastStyle==CssStyles.EVENGROUP ? CssStyles.ODDGROUP : CssStyles.EVENGROUP  ) ;
                    newGroupStyle = CssStyles.NEWGROUP + " " ;
                   constructTable.setWidget(lastRow,0,new HTML(relationshipDTO.getConstructDTO().getLink()));
                }
                else{
                    newGroupStyle = "" ;
                }
                lastFeatureName =  relationshipDTO.getConstructDTO().getName() ;
                rowFormatter.setStyleName(lastRow, newGroupStyle +  lastStyle + " "+ CssStyles.EXPERIMENT_ROW);
            }
        }

    }


    private void filterRowsByText(String name ,int column){
        if(name==null) {
            return ;
        }
        for(int i = 1 ; i < constructTable.getRowCount()-1 ; i++){
            String text = ((Label) (constructTable.getWidget(i,column))).getText() ;
            if(  !text.equals(name)){
                rowFormatter.setVisible(i,false);
            }
        }
    }


    private void showAllRows(){
        for(int i = 0 ; i < constructTable.getRowCount() ; i++){
            rowFormatter.setVisible(i,true);
        }
    }


    @Override
    protected void revertGUI() {
//        constructFilterModule.setDTO(getDTO());
        constructToAddType.setText("");
        constructToAddRelationship.clear();
        constructToAddTarget.clear();
        setValues();
//        constructFilterModule.refilter();
    }


    @Override
    protected void addInternalListeners(final HandlesError handlesError) {
       /* constructFilterModule.addFilterChangeListener(new FilterChangeListener(){
            @Override
            public void changed(FilterChangeEvent event) {

                showAllRows();

                if(event.isEmpty()) return ;

                if(event.containsKey(FeatureFilterModule.FILTER_FEATURE_NAME)){
                    filterRowsByText(event.get(FeatureFilterModule.FILTER_FEATURE_NAME),0);
                }

                if(event.containsKey(FeatureFilterModule.FILTER_FEATURE_TYPE)){
                    filterRowsByText(event.get(FeatureFilterModule.FILTER_FEATURE_TYPE),1);
                }

            }
        });*/

        constructToAddList.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                constructAddListChanged() ;
            }
        });

        constructToAddRelationship.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                updateTargets();
            }
        });

        addButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ConstructRelationshipDTO constructRelationshipDTO = getConstructMarkerRelationshipFromGui();
                /*try {
                    //FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(featureMarkerRelationshipDTO,featureMarkerRelationshipDTOs) ;
                } catch (ValidationException e) {
                    setError(e.getMessage());
                    return;
                }*/
                addButton.setEnabled(false);
                constructToAddTarget.setEnabled(false);
                constructToAddList.setEnabled(false);
                constructToAddRelationship.setEnabled(false);
                lastSelectedConstructZdbId = constructRelationshipDTO.getConstructDTO().getZdbID();
                MarkerRPCService.App.getInstance().addConstructMarkerRelationShip(constructRelationshipDTO
                        ,new MarkerEditCallBack<Void>("Failed to create constructMarkerRelation: "+constructRelationshipDTO,handlesError){

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        addButton.setEnabled(true);
                        constructToAddTarget.setEnabled(true);
                        constructToAddList.setEnabled(true);
                        constructToAddRelationship.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        revertGUI();
                        clearError();
                    }
                } );
            }
        });
    }




    private void constructAddListChanged() {
        constructToAddRelationship.setEnabled(false);
        constructToAddRelationship.clear();
        constructToAddTarget.setEnabled(false);
        constructToAddTarget.clear();

        final ConstructDTO constructDTO = getConstructDTOForName(constructToAddList.getSelectedText());
        if(constructDTO!=null && constructDTO.getConstructType()!=null){
            constructToAddType.setText(constructDTO.getConstructType());
        }
        /*else{
            setError("C type was null");
            return ;
        }*/
        MarkerRPCService.App.getInstance().getEditableRelationshipTypesForConstruct(new MarkerEditCallBack<String>("Failed to return construct relationships for type: " + constructDTO.getConstructType(), this) {
                    @Override
                    public void onSuccess(String result) {
                        if (result != null) {

                                // this is probably correct so we don't need to screen it
                            constructToAddRelationship.addItem("---");
                                constructToAddRelationship.addItem(result);
                            }

                      constructToAddRelationship.setEnabled(true);


                updateTargets();
        }
    });
    }





    private ConstructRelationshipDTO getConstructMarkerRelationshipFromGui() {
        ConstructRelationshipDTO constructMarkerRelationshipDTO = new ConstructRelationshipDTO();
        ConstructDTO constructDTO= new ConstructDTO() ;
        constructDTO.setZdbID(constructToAddList.getSelected());
        constructDTO.setName(constructToAddList.getSelectedText());
        constructDTO.setConstructType(constructToAddType.getText());
        constructMarkerRelationshipDTO.setConstructDTO(constructDTO);
        constructMarkerRelationshipDTO.setPublicationZdbID(dto.getPublicationZdbID());

        constructMarkerRelationshipDTO.setRelationshipType(constructToAddRelationship.getSelectedText());

        MarkerDTO markerDTO = new MarkerDTO() ;
        markerDTO.setZdbID(constructToAddTarget.getSelected());
        markerDTO.setName(constructToAddTarget.getSelectedText());

        constructMarkerRelationshipDTO.setMarkerDTO(markerDTO);

        return constructMarkerRelationshipDTO ;
    }

    private void updateTargets(){
        addButton.setEnabled(false);
        constructToAddTarget.setEnabled(false);

        MarkerRPCService.App.getInstance().getMarkersForRelation(constructToAddRelationship.getSelectedText(), dto.getPublicationZdbID(),
                new MarkerEditCallBack<List<MarkerDTO>>("Failed to find markers for type[" + constructToAddType.getText() + "] and pub: " +
                        (dto != null ? dto.getPublicationZdbID() : dto), this) {
                    @Override
                    public void onSuccess(List<MarkerDTO> markers) {
                        constructToAddTarget.clear();
                        Collections.sort(markers);
                        if (markers != null & markers.size() > 0) {
                            for (MarkerDTO m : markers) {

                                constructToAddTarget.addItem(m.getName(), m.getZdbID());

                            }
                            addButton.setEnabled(true);
                            constructToAddTarget.setEnabled(true);
                        }
                    }
                });
    }

    public ConstructDTO getConstructDTOForName(String name){
        for(ConstructDTO constructDTO : constructDTOs){
            if(constructDTO.getName().equals(name)){
              return constructDTO;
            }
        }
        return null ;
    }



   public void setPublication(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID ;
        ConstructDTO constructDTO = new ConstructDTO();
        constructDTO.setPublicationZdbID(publicationZdbID);
        setDTO(constructDTO);
    }



private class DeleteConstructMarkerRelationshipButton extends Button{
    private ConstructRelationshipDTO constructMarkerRelationshipDTO  ;

    public DeleteConstructMarkerRelationshipButton(ConstructRelationshipDTO relationshipDTO,final HandlesError handlesError) {
        super("X") ;
        this.constructMarkerRelationshipDTO = relationshipDTO;

        addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                setEnabled(false);
                MarkerRPCService.App.getInstance().deleteConstructMarkerRelationship(constructMarkerRelationshipDTO,
                        new MarkerEditCallBack<Void>("Unable to remove  marker relationship: "+constructMarkerRelationshipDTO,handlesError){

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                setEnabled(true);
                            }

                            @Override
                             public void onSuccess(Void result) {
                                setValues();
                            }
                        });

            }
        });
    }
 }
    private native void exposeConstructRelationshipsToJavascript(ConstructRelationshipBox constructRelationship)/*-{
        $wnd.refreshRelationship = function (pubID) {
            constructRelationship.@org.zfin.gwt.curation.ui.ConstructRelationshipBox::refreshRelationship(Ljava/lang/String;)(pubID);
        };

    }-*/;

    public void refreshRelationship(String pubID) {
        revertGUI();
    }
}

