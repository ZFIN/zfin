package org.zfin.gwt.curation.ui;

import org.zfin.gwt.curation.event.AddNewFeatureEvent;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeaturePrefixDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAddPresenter   implements HandlesError {

    private ExperimentAddView view;

    private String publicationID;


    public ExperimentAddPresenter(ExperimentAddView view, String publicationID) {
       // super(view, publicationID);
        this.publicationID = publicationID;
        this.view = view;
        //dto = new FeatureDTO();
        //dto.setPublicationZdbID(publicationID);

    }

    public void go() {
      //  super.go();

    }

    @Override
    public void setError(String message) {
        view.errorLabel.setError(message);
    }

    @Override
    public void clearError() {
      //  view.message.setText("");
        view.errorLabel.setError("");
    }

    @Override
    public void fireEventSuccess() {

    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {

    }



    /*public FeatureDTO createDTOFromGUI() {
        FeatureDTO featureDTO = super.createDTOFromGUI(view);

        featureDTO.setFeatureSequence(view.featureSequenceBox.getText());

        if (StringUtils.isNotEmptyTrim(view.featureAliasBox.getText())) {
            featureDTO.setAlias(view.featureAliasBox.getText());
        }
        if (StringUtils.isNotEmptyTrim(view.publicNoteBox.getText())) {
            NoteDTO publicNoteDTO = new NoteDTO();
            publicNoteDTO.setNoteData(view.publicNoteBox.getText());
            publicNoteDTO.setPublicationZdbID(publicationID);
            featureDTO.addPublicNote(publicNoteDTO);
        }

        if (StringUtils.isNotEmptyTrim(view.curatorNoteBox.getText())) {
            List<CuratorNoteDTO> curatorNoteDTOs = new ArrayList<>();
            CuratorNoteDTO noteDTO = new CuratorNoteDTO();
            noteDTO.setNoteData(view.curatorNoteBox.getText());
            curatorNoteDTOs.add(noteDTO);
            featureDTO.setCuratorNotes(curatorNoteDTOs);
        }
        if (view.hasMutationDetails()) {
            featureDTO.setDnaChangeDTO(view.mutationDetailDnaView.getDto());
            featureDTO.setProteinChangeDTO(view.mutationDetailProteinView.getDto());
            featureDTO.setTranscriptChangeDTOSet(view.mutationDetailTranscriptView.getPresenter().getDtoSet());
        }

        return featureDTO;
    }



    public void createFeature() {
        final FeatureDTO featureDTO = createDTOFromGUI();

        String errorMessage = FeatureValidationService.isValidToSave(featureDTO);
        if (errorMessage != null) {
            setError(errorMessage);
            return;
        }
        if (view.mutationDetailProteinView.hasAASelected() && view.mutationDetailProteinView.hasPlusMinusUsed()) {
            view.setError("Cannot select Amino Acids and defines plus / minus fields");
            return;
        }
        view.working();
        FeatureRPCService.App.getInstance().createFeature(featureDTO, new FeatureEditCallBack<FeatureDTO>("Failed to create feature:", this) {

            @Override
            public void onFailure(Throwable throwable) {
                super.onFailure(throwable);
                view.notWorking();
                handleDirty();
            }

            @Override
            public void onSuccess(final FeatureDTO result) {
                fireEventSuccess();
                //Window.alert("Feature successfully created");
                view.featureTypeBox.setSelectedIndex(0);
                view.message.setText("Feature created: " + result.getName() + " [" + result.getZdbID() + "]");
                view.notWorking();
                view.saveButton.setEnabled(false);
                view.clearErrors();
                AddNewFeatureEvent event = new AddNewFeatureEvent(result);
                AppUtils.EVENT_BUS.fireEvent(event);
                view.resetInterface();
                view.hideMutationDetail();
            }
        });

    }
*/

}
