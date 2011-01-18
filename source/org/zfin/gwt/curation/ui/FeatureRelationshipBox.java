package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.event.FilterChangeEvent;
import org.zfin.gwt.curation.event.FilterChangeListener;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureMarkerRelationshipDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.*;

import java.util.Collections;
import java.util.List;

/**
 */
public class FeatureRelationshipBox extends AbstractComposite<FeatureDTO>{


    private VerticalPanel panel = new VerticalPanel();
    private FlexTable featureTable = new FlexTable() ;
    private HTMLTable.RowFormatter rowFormatter = featureTable.getRowFormatter() ;
    private FeatureFilterModule featureFilterModule = new FeatureFilterModule();
    private Button addButton = new Button("Add") ;

    // for add row
    private StringListBox featureToAddList = new StringListBox() ;
    private StringListBox featureToAddRelationship = new StringListBox() ;
    private StringListBox featureToAddTarget = new StringListBox() ;
    private Label featureToAddType = new Label() ;

    // internal data
    private List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipDTOs ;
    private List<FeatureDTO> featureDTOs;
    private String lastSelectedFeatureZdbId = null ;

    public FeatureRelationshipBox(){
        initGUI() ;
        setValues() ;
        addInternalListeners(this) ;
    }

    protected void initGUI() {
        initWidget(panel);

        panel.add(new HTML("<br>"));
        panel.add(featureFilterModule);
        panel.add(new HTML("<br>"));
        panel.setWidth("100%");


        featureToAddList.setName("featureToAddList");
        featureTable.setStyleName(CssStyles.SEARCHRESULTS + " " + CssStyles.GROUPSTRIPES_HOVER);
        featureTable.setWidth("100%");
        panel.add(featureTable);
        addButton.setEnabled(false);
        featureToAddRelationship.setEnabled(false);
        featureToAddTarget.setEnabled(false);
        panel.add(addButton);
        panel.add(errorLabel);
        errorLabel.setStyleName("error");
    }



    protected void setValues() {
        if(dto!=null && dto.getPublicationZdbID()!=null){
            FeatureRPCService.App.getInstance().getFeaturesMarkerRelationshipsForPub(dto.getPublicationZdbID(),
                    new FeatureEditCallBack<List<FeatureMarkerRelationshipDTO>>(
                            "Failed to find feature marker relationships for this pub: "
                                    + dto.getPublicationZdbID(),this){
                        @Override
                        public void onSuccess(List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipDTOList) {
                            featureMarkerRelationshipDTOs = featureMarkerRelationshipDTOList;
                            redrawTable();
                        }
                    });

            FeatureRPCService.App.getInstance().getFeaturesForPub(dto.getPublicationZdbID(),
                    new FeatureEditCallBack<List<FeatureDTO>>("Problem finding features for pub: " + dto.getPublicationZdbID()+ " ",this) {

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            featureToAddList.setEnabled(false);
                        }

                        @Override
                        public void onSuccess(List<FeatureDTO> features) {
                            featureDTOs = features ;
                            if(featureDTOs!=null){
                                featureToAddList.clear();
                                featureToAddList.addItem("-----------");
                                for(FeatureDTO featureDTO : featureDTOs){
                                    featureToAddList.addItem(featureDTO.getName(),featureDTO.getZdbID());
                                }
                                featureToAddList.setEnabled(true);
                            }

                            if(lastSelectedFeatureZdbId!=null){
                                featureToAddList.setIndexForValue(lastSelectedFeatureZdbId);
                                lastSelectedFeatureZdbId = null ;
                                featureAddListChanged();
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
        while(featureTable.getRowCount()>0){
            featureTable.removeRow(0);
        }
        // creates the header
        featureTable.setText(0,0,"Feature/Marker");
        featureTable.setText(0,1,"Type");
        featureTable.setText(0,2,"Relationship");
        featureTable.setText(0,3,"Target");
        featureTable.setText(0,4,"Delete");
        rowFormatter.setStyleName(0, CssStyles.TABLE_HEADER.toString());


        // creates the add row
        featureTable.setWidget(1,0,featureToAddList);
        featureTable.setWidget(1,1, featureToAddType);
        featureTable.setWidget(1,2, featureToAddRelationship);
        featureTable.setWidget(1,3,featureToAddTarget);
        rowFormatter.setStyleName(1, CssStyles.EXPERIMENT_ROW.toString());


        if(featureMarkerRelationshipDTOs!=null && featureMarkerRelationshipDTOs.size()>0){
            String lastFeatureName = featureMarkerRelationshipDTOs.get(0).getName();
            CssStyles lastStyle = CssStyles.ODDGROUP;
            String newGroupStyle= "";
            for(FeatureMarkerRelationshipDTO relationshipDTO : featureMarkerRelationshipDTOs){
                featureTable.insertRow(featureTable.getRowCount()-1) ;
                int lastRow = featureTable.getRowCount()-2 ;
                featureTable.setWidget(lastRow,1, new Label(relationshipDTO.getFeatureDTO().getFeatureType().getDisplay()));
                featureTable.setWidget(lastRow,2, new Label(relationshipDTO.getRelationshipType()));
                featureTable.setWidget(lastRow,3,new HTML(relationshipDTO.getMarkerDTO().getLink())) ;
                featureTable.setWidget(lastRow,4,new DeleteFeatureMarkerRelationshipButton(relationshipDTO,this) );

                // switch style if name changes
                if(!relationshipDTO.getFeatureDTO().getName().equals(lastFeatureName)){
                    lastStyle  =  (  lastStyle==CssStyles.EVENGROUP ? CssStyles.ODDGROUP : CssStyles.EVENGROUP  ) ;
                    newGroupStyle = CssStyles.NEWGROUP + " " ;
                    featureTable.setWidget(lastRow,0,new HTML(relationshipDTO.getFeatureDTO().getLink()));
                }
                else{
                    newGroupStyle = "" ;
                }
                lastFeatureName =  relationshipDTO.getFeatureDTO().getName() ;
                rowFormatter.setStyleName(lastRow, newGroupStyle +  lastStyle + " "+ CssStyles.EXPERIMENT_ROW);
            }
        }

    }


    private void filterRowsByText(String name ,int column){
        if(name==null) {
            return ;
        }
        for(int i = 1 ; i < featureTable.getRowCount()-1 ; i++){
            String text = ((Label) (featureTable.getWidget(i,column))).getText() ;
            if(  !text.equals(name)){
                rowFormatter.setVisible(i,false);
            }
        }
    }


    private void showAllRows(){
        for(int i = 0 ; i < featureTable.getRowCount() ; i++){
            rowFormatter.setVisible(i,true);
        }
    }


    @Override
    protected void revertGUI() {
        featureFilterModule.setDTO(getDTO());
        featureToAddType.setText("");
        featureToAddRelationship.clear();
        featureToAddTarget.clear();
        setValues();
        featureFilterModule.refilter();
    }


    @Override
    protected void addInternalListeners(final HandlesError handlesError) {
        featureFilterModule.addFilterChangeListener(new FilterChangeListener(){
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
        });

        featureToAddList.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                featureAddListChanged() ;
            }
        });

        featureToAddRelationship.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                updateTargets();
            }
        });

        addButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = getFeatureMarkerRelationshipFromGui();
                try {
                    FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(featureMarkerRelationshipDTO,featureMarkerRelationshipDTOs) ;
                } catch (ValidationException e) {
                    setError(e.getMessage());
                    return;
                }
                addButton.setEnabled(false);
                featureToAddTarget.setEnabled(false);
                featureToAddList.setEnabled(false);
                featureToAddRelationship.setEnabled(false);
                lastSelectedFeatureZdbId = featureMarkerRelationshipDTO.getFeatureDTO().getZdbID();
                FeatureRPCService.App.getInstance().addFeatureMarkerRelationShip(featureMarkerRelationshipDTO
                        ,new FeatureEditCallBack<Void>("Failed to create FeatureMarkerRelation: "+featureMarkerRelationshipDTO,handlesError){

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                addButton.setEnabled(true);
                                featureToAddTarget.setEnabled(true);
                                featureToAddList.setEnabled(true);
                                featureToAddRelationship.setEnabled(true);
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

    private void featureAddListChanged() {
        featureToAddRelationship.setEnabled(false);
        featureToAddRelationship.clear();
        featureToAddTarget.setEnabled(false);
        featureToAddTarget.clear();

        final FeatureDTO featureDTO = getFeatureDTOForName(featureToAddList.getSelectedText());
        if(featureDTO!=null && featureDTO.getFeatureType()!=null){
            featureToAddType.setText(featureDTO.getFeatureType().getDisplay());
        }
        else{
            setError("Feature type was null");
            return ;
        }
        FeatureRPCService.App.getInstance().getRelationshipTypesForFeatureType(featureDTO.getFeatureType(),
                new FeatureEditCallBack<List<String>>("Failed to return feature relationships for type: " + featureDTO.getFeatureType().getDisplay(), this) {
                    @Override
                    public void onSuccess(List<String> result) {
                        if (result != null && result.size() > 0) {
                            if (result.size() == 1) {
                                // this is probably correct so we don't need to screen it
                                featureToAddRelationship.addItem(result.get(0));
                            } else {
                                featureToAddRelationship.addItem("-------");
                                for (String rel : result) {
                                    // see case 6337
                                    // is_allele relationship should only be available for transgenic insertions where the known insertion site box is checked
                                    // unspecified transgenic will never have known insertion sites
                                    if ((featureDTO.getFeatureType() == FeatureTypeEnum.TRANSGENIC_INSERTION
                                            || featureDTO.getFeatureType() == FeatureTypeEnum.TRANSGENIC_UNSPECIFIED)
                                            && rel.startsWith("is allele of")) {
                                        if (featureDTO.getKnownInsertionSite()) {
                                            featureToAddRelationship.addItem(rel);
                                        }
                                    } else {
                                        featureToAddRelationship.addItem(rel);
                                    }
                                }
                            }
                            featureToAddRelationship.setEnabled(true);
                        }
                        updateTargets();
                    }
                }
        );
    }



    private FeatureMarkerRelationshipDTO getFeatureMarkerRelationshipFromGui() {
        FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = new FeatureMarkerRelationshipDTO();
        FeatureDTO featureDTO= new FeatureDTO() ;
        featureDTO.setZdbID(featureToAddList.getSelected());
        featureDTO.setName(featureToAddList.getSelectedText());
        featureDTO.setFeatureType(FeatureTypeEnum.getTypeForDisplay(featureToAddType.getText()));
        featureMarkerRelationshipDTO.setFeatureDTO(featureDTO);
        featureMarkerRelationshipDTO.setPublicationZdbID(dto.getPublicationZdbID());

        featureMarkerRelationshipDTO.setRelationshipType(featureToAddRelationship.getSelectedText());

        MarkerDTO markerDTO = new MarkerDTO() ;
        markerDTO.setZdbID(featureToAddTarget.getSelected());
        markerDTO.setName(featureToAddTarget.getSelectedText());

        featureMarkerRelationshipDTO.setMarkerDTO(markerDTO);

        return featureMarkerRelationshipDTO ;
    }

    private void updateTargets(){
        addButton.setEnabled(false);
        featureToAddTarget.setEnabled(false);
        FeatureRPCService.App.getInstance().getMarkersForFeatureRelationAndSource(featureToAddRelationship.getSelectedText(), dto.getPublicationZdbID(),
                new FeatureEditCallBack<List<MarkerDTO>>("Failed to find markers for type[" + featureToAddType.getText() + "] and pub: " +
                        (dto != null ? dto.getPublicationZdbID() : dto), this) {
                    @Override
                    public void onSuccess(List<MarkerDTO> markers) {
                        featureToAddTarget.clear();
                        Collections.sort(markers);
                        if (markers != null & markers.size() > 0) {
                            for (MarkerDTO m : markers) {
                                featureToAddTarget.addItem(m.getName(), m.getZdbID());
                            }
                            addButton.setEnabled(true);
                            featureToAddTarget.setEnabled(true);
                        }
                    }
                });
    }

    public FeatureDTO getFeatureDTOForName(String name){
        for(FeatureDTO featureDTO : featureDTOs){
            if(featureDTO.getName().equals(name)){
                return featureDTO ;
            }
        }
        return null ;
    }

    public void setPublication(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID ;
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setPublicationZdbID(publicationZdbID);
        setDTO(featureDTO);
    }


    private class DeleteFeatureMarkerRelationshipButton extends Button{
        private FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO  ;

        public DeleteFeatureMarkerRelationshipButton(FeatureMarkerRelationshipDTO relationshipDTO,final HandlesError handlesError) {
            super("X") ;
            this.featureMarkerRelationshipDTO = relationshipDTO;

            addClickHandler(new ClickHandler(){
                @Override
                public void onClick(ClickEvent event) {
                    setEnabled(false);
                    FeatureRPCService.App.getInstance().deleteFeatureMarkerRelationship(featureMarkerRelationshipDTO,
                            new FeatureEditCallBack<Void>("Unable to remove feature marker relationship: "+featureMarkerRelationshipDTO,handlesError){

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
}
