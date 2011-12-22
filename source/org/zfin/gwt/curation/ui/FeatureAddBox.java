package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class FeatureAddBox extends AbstractFeatureBox  {

    public FeatureAddBox() {
        initGUI();
        setValues();
        addInternalListeners(this);
        initWidget(panel);
    }


    public void initGUI() {
        super.initGUI();
        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.add(new HTML("<b>Create New Feature:</b>")) ;
        table.setWidget(FeatureTableLayout.TITLE.row(),0,titlePanel);

        table.setWidget(FeatureTableLayout.ALIAS.row(),1,featureAliasBox);

        Grid notePanel = new Grid(2,2);
        table.setHTML(FeatureTableLayout.NOTE.row(), 0, "<b>Notes</b>");

        notePanel.setWidget(0,0,new HTML("<b>Public:</b>"));
        notePanel.setWidget(0,1,publicNoteBox);
        notePanel.setWidget(1,0,new HTML("<b>Curator:</b>"));
        notePanel.setWidget(1,1,curatorNoteBox);


        publicNoteBox.setWidth("400px");
        curatorNoteBox.setWidth("400px");
        table.setWidget(FeatureTableLayout.NOTE.row(),1,notePanel);
    }

    protected void sendUpdates() {
        FeatureDTO featureDTO = createDTOFromGUI();

        String errorMessage = FeatureValidationService.isValidToSave(featureDTO) ;
        if(errorMessage!=null){
            setError(errorMessage);
            return ;
        }
//        saveButton.setEnabled(false);
        working();
        FeatureRPCService.App.getInstance().createFeature(featureDTO, new FeatureEditCallBack<FeatureDTO>("Failed to create feature:",this) {

            @Override
            public void onFailure(Throwable throwable) {
                super.onFailure(throwable);
                notWorking();
//                saveButton.setEnabled(true);
            }

            @Override
            public void onSuccess(final FeatureDTO result) {
                //setDTO(result);
                fireEventSuccess();
                //notWorking();
                // saveButton.setEnabled(false);
//                    Window.alert("Feature successfully created");
                featureTypeBox.setSelectedIndex(0);
                revertGUI();
                setNote("Feature created: "+ result.getName() + " ["+result.getZdbID()+"]");
                notWorking();
                saveButton.setEnabled(false);
            }
        });
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);


        featureTypeBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                revertGUI();
            }
        });
    }

    @Override
    protected void updateForFeatureType() {
        final FeatureTypeEnum featureTypeSelected = FeatureTypeEnum.getTypeForDisplay(featureTypeBox.getSelectedText());
        if(featureTypeSelected==null){
            resetInterface();
        }
        else{
            super.updateForFeatureType();
        }
    }

    public FeatureDTO createDTOFromGUI() {
        FeatureDTO featureDTO = super.createDTOFromGUI();
        if(StringUtils.isNotEmptyTrim(featureAliasBox.getText())){
            featureDTO.setAlias(featureAliasBox.getText());
        }
        if(StringUtils.isNotEmptyTrim(publicNoteBox.getText())){
            NoteDTO publicNoteDTO = new NoteDTO() ;
            publicNoteDTO.setNoteData(publicNoteBox.getText());
            featureDTO.setPublicNote(publicNoteDTO);
        }

        if(StringUtils.isNotEmptyTrim(curatorNoteBox.getText())){
            List<NoteDTO> curatorNoteDTOs = new ArrayList<NoteDTO>() ;
            NoteDTO noteDTO = new NoteDTO() ;
            noteDTO.setNoteData(curatorNoteBox.getText());
            curatorNoteDTOs.add(noteDTO) ;
            featureDTO.setCuratorNotes(curatorNoteDTOs);
        }

        return featureDTO;
    }

    protected void revertGUI() {
        updateForFeatureType();
        clearError();
    }

    public boolean handleDirty(){
        featureDisplayName.setText(FeatureValidationService.generateFeatureDisplayName(createDTOFromGUI()));
        saveButton.setEnabled(FeatureValidationService.isFeatureSaveable(createDTOFromGUI())) ;
        return isDirty();
    }

    @Override
    public boolean isDirty() {
        return true ;
    }
}
