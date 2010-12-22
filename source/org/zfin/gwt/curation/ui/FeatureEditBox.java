package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class FeatureEditBox extends AbstractFeatureBox {


    protected final StringListBox featureEditList = new StringListBox(false);
    protected final HTML zdbIdHTML = new HTML() ;

    private final String imageURL = "/images/";
    private final Image removeRelatedEntityButton = new Image(imageURL + "delete-button.png");
    private final Button revertButton = new Button(TEXT_REVERT);
    protected final String TEXT_SAVE = "Save";

    // listeners
    private final List<RelatedEntityChangeListener<FeatureDTO>> changeListeners = new ArrayList<RelatedEntityChangeListener<FeatureDTO>>();


    public FeatureEditBox() {
        initGUI();
        setValues();
        addInternalListeners(this);
        initWidget(panel);
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(handlesError);


        featureEditList.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                final String featureID = featureEditList.getValue(featureEditList.getSelectedIndex());
                if (StringUtils.isEmpty(featureID)) {
                    setError("Empty ID");
                    resetGUI();
                }
                else{
                    FeatureRPCService.App.getInstance().getFeature(featureID, new FeatureEditCallBack<FeatureDTO>("Failed to remove attribution: ",handlesError) {
                        public void onSuccess(FeatureDTO result) {
                            result.setPublicationZdbID(dto.getPublicationZdbID());
                            setDTO(result);
                            updateForFeatureType();
//                            setNote("Feature loaded: "+ result.getName() + " ["+result.getZdbID()+"]");
                        }
                    });
                }
            }
        });


        removeRelatedEntityButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final String featureID = featureEditList.getSelected();
                final String featureName = featureEditList.getSelectedText();
                if (false == Window.confirm("Delete Feature ["+featureName+"]?")) {
                    return;
                }
                if (StringUtils.isEmpty(featureID)) {
                    setError("Empty ID");
                    resetGUI();
                    return ;
                }

                Window.open("/cgi-bin/webdriver?MIval=aa-delete_record.apg&OID="+featureID+"&rtype=feature",
                        "_self", "");

//                else{
//                    working();
//                    FeatureRPCService.App.getInstance().deleteFeature(featureID, new FeatureEditCallBack<Void>("Failed to delete feature: " + featureID,handlesError) {
//                        @Override
//                        public void onFailure(Throwable throwable) {
//                            super.onFailure(throwable);
//                            notWorking();
//                        }
//
//                        public void onSuccess(Void result) {
//                            setPublication(dto.getPublicationZdbID());
//                            fireEventSuccess();
//                            notWorking();
////                            mutageeBox.setDirty(false);
////                            mutagenBox.setDirty(false);
////                            labDesigBox.setDirty(false);
//                            setNote("Feature deleted ["+featureName+"]-["+featureID+"]");
//                        }
//
//
//                    });
//                }
            }
        });


        featureTypeBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                updateForFeatureType() ;
                handleDirty();
            }
        });

        addChangeListener(new RelatedEntityChangeListener<FeatureDTO>(){
            @Override
            public void dataChanged(RelatedEntityEvent changeEvent) {
                if (false == changeEvent.getDTO().getName().equals(dto.getName())) {
                    if(changeEvent.isNameChanged()){
                        String message = featureAliasList.validateNewRelatedEntity(dto.getName());
                        if(message==null){
                            featureAliasList.addAlias(changeEvent.getPreviousName());
                            // set to the new one
                        }
                        else{
                            setError("Unable to add previous name ["+changeEvent.getPreviousName()+"] as alias to feature ["+dto.getName()+"]");
                        }
                    }
                }

            }
        });


        revertButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                revertGUI();
                handleDirty();
            }
        });

    }

    @Override
    public void setDTO(FeatureDTO dto) {
        super.setDTO(dto);
        loadFeaturesForPub();
    }

    public boolean isDirty(){
        boolean isDirty = false ;
        // this displays most changes
        // alias and notes are done automatically?
        isDirty = (featureDisplayName.isDirty(dto.getName()) || isDirty)  ;
        isDirty = ( featureTypeBox.getSelected()!=null && dto.getFeatureType()==null
                || featureTypeBox.isDirty(dto.getFeatureType().name())
                || isDirty)  ;
        isDirty = (mutageeBox.isDirty(dto.getMutagee()) || isDirty)  ;
        isDirty = (mutagenBox.isDirty(dto.getMutagen()) || isDirty)  ;
        isDirty = (labDesignationBox.isDirty(dto.getLabPrefix()) || isDirty)  ;
        isDirty = (lineNumberBox.isDirty(dto.getLineNumber()) || isDirty)  ;
        isDirty = (labOfOriginBox.isDirty(dto.getLabOfOrigin()) || isDirty)  ;
        return isDirty ;
    }

    @Override
    public boolean handleDirty(){
        featureDisplayName.setText(FeatureValidationService.generateFeatureDisplayName(createDTOFromGUI()));
        boolean isDirty = isDirty() ;
        saveButton.setEnabled(isDirty && FeatureValidationService.isFeatureSaveable(createDTOFromGUI())) ;
        revertButton.setEnabled(isDirty);
        return isDirty ;
    }

    @Override
    public void working() {
        super.working();
        revertButton.setEnabled(false);
        featureEditList.setEnabled(false);
        removeRelatedEntityButton.setVisible(false);
    }

    @Override
    public void notWorking() {
        super.notWorking();
        saveButton.setText(TEXT_SAVE);
        featureEditList.setEnabled(true);
        removeRelatedEntityButton.setVisible(true);
    }

    protected void sendUpdates() {
        FeatureDTO featureDTO = createDTOFromGUI();

        String errorMessage = FeatureValidationService.isValidToSave(featureDTO) ;
        if(errorMessage!=null){
            setError(errorMessage);
            return ;
        }

        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        if (isDirty() && FeatureValidationService.isFeatureSaveable(featureDTO)) {
            working();
            FeatureRPCService.App.getInstance().editFeatureDTO(featureDTO,
                    new FeatureEditCallBack<FeatureDTO>("Failed to create feature:", this) {

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            // TODO: we need to use the FeatureAddBox function and the isDirty() to enable this
                            notWorking();
                        }

                        @Override
                        public void onSuccess(final FeatureDTO result) {
                            result.setPublicationZdbID(dto.getPublicationZdbID());
                            setDTO(result);
                            fireEventSuccess();
                            // TODO: we need to use the FeatureAddBox function and the isDirty() to enable this
                            notWorking();
                            setNote("Saved Feature ["+ result.getName()+"]");
                        }
                    });

        }
    }


    protected void revertGUI() {
        if(dto.getZdbID()==null){
            resetGUI();
        }
        else{
            removeRelatedEntityButton.setVisible(true);
            featureEditList.setIndexForValue(dto.getZdbID()) ;
        }
        featureAliasList.setDTO(dto);
        labDesignationBox.clear();
        lineNumberBox.setValue(dto.getLineNumber());
        mutagenBox.setIndexForText(dto.getMutagen());
        mutageeBox.setIndexForText(dto.getMutagee());
        if(dto.getFeatureType()!=null){
            featureTypeBox.setIndexForText(dto.getFeatureType().getDisplay());
        }
        featureNoteBox.setDTO(dto);
        featureNoteBox.revertGUI();
        featureNameBox.setText(FeatureValidationService.getNameFromFullName(dto));
        constructTextBox.setText(FeatureValidationService.getNameFromFullName(dto));
        labOfOriginBox.setIndexForValue(dto.getLabOfOrigin());
        updateLabsDesignations();
        knownInsertionCheckBox.setValue(dto.getKnownInsertionSite());
        dominantCheckBox.setValue(dto.getDominant());
        featureDisplayName.setValue(dto.getName());
        zdbIdHTML.setText(dto.getZdbID());
        featureSuffixBox.setIndexForText(dto.getTransgenicSuffix());
    }


    public void initGUI() {
        super.initGUI();

        table.setHTML(FeatureTableLayout.TITLE.row(), 0, "<b>Feature Name/Abbrev:</b>");
        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.add(featureEditList) ;
        removeRelatedEntityButton.setStyleName("relatedEntityPubLink");
        removeRelatedEntityButton.setTitle("Delete this Feature from ZFIN");
        removeRelatedEntityButton.setVisible(false);
        titlePanel.add(removeRelatedEntityButton) ;
        table.setWidget(FeatureTableLayout.TITLE.row(),1,titlePanel);
        table.setWidget(FeatureTableLayout.ALIAS.row(),1,featureAliasList);
        table.setHTML(FeatureTableLayout.NOTE.row(), 0, "<b>Notes:</b>");
        table.setWidget(FeatureTableLayout.NOTE.row(),1,featureNoteBox);
        saveButton.setText(TEXT_SAVE);
        panel.add(zdbIdHTML);
        revertButton.setEnabled(false);
        buttonPanel.add(revertButton);
    }

    public void loadFeaturesForPub() {
        if (dto.getPublicationZdbID() != null) {
            FeatureRPCService.App.getInstance().getFeaturesForPub(dto.getPublicationZdbID(),
                    new FeatureEditCallBack<List<FeatureDTO>>("Failed to find features for pub: " + "ZDB-PUB-071210-28",this) {

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            featureEditList.setEnabled(true);
                        }

                        @Override
                        public void onSuccess(List<FeatureDTO> results) {
                            featureEditList.clear();
                            featureEditList.addItem("");
                            for (FeatureDTO dto : results) {
                                if (featureEditList.setIndexForValue(dto.getZdbID()) < 0) {
                                    featureEditList.addItem(dto.getName(), dto.getZdbID());
                                }
                            }

                            featureEditList.setIndexForValue(dto.getZdbID());
                            featureEditList.setEnabled(true);
                            featureAliasList.setDTO(dto);
                            revertGUI();
                        }
                    });
        }
    }

    @Override
    public FeatureDTO createDTOFromGUI() {
        FeatureDTO featureDTO = super.createDTOFromGUI();
        featureDTO.setZdbID(dto.getZdbID());
        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        return featureDTO ;
    }

    protected void fireChangeEvent(RelatedEntityEvent<FeatureDTO> relatedEntityEvent) {
        for (RelatedEntityChangeListener<FeatureDTO> changeListener : changeListeners) {
            changeListener.dataChanged(relatedEntityEvent);
        }
    }

    public void addChangeListener(RelatedEntityChangeListener<FeatureDTO> relatedEntityChangeListener) {
        changeListeners.add(relatedEntityChangeListener);
    }

    private void deleteReset(String publicationZdbId){
        dto.setPublicationZdbID(publicationZdbId);
        setDTO(dto);
        loadFeaturesForPub();
    }

    public void setPublication(String publicationZdbId) {
        super.setPublication(publicationZdbId);
        loadFeaturesForPub();
    }

    public void resetGUI() {
//        dto = new FeatureDTO();
//        dto.setPublicationZdbID(publicationZdbID);
        removeRelatedEntityButton.setVisible(false);
        featureEditList.setSelectedIndex(0);
        featureTypeBox.setSelectedIndex(0);
        labOfOriginBox.setSelectedIndex(0);
        labOfOriginBox.setDirty(false);
        labDesignationBox.clear();
        labDesignationBox.setDirty(false) ;
        mutageeBox.setDirty(false);
        mutagenBox.setDirty(false);
        lineNumberBox.setDirty(false);
        featureDisplayName.setDirty(false) ;

//        revertGUI();
    }
}



