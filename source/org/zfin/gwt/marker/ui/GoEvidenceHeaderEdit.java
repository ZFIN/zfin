package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.GoEvidenceDTO;

/**
 * This code header houses 4 things
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoEvidenceHeaderEdit extends AbstractGoEvidenceHeader{

    public GoEvidenceHeaderEdit() {
        this(StandardDivNames.headerDiv) ;
    }

    public GoEvidenceHeaderEdit(String div) {

        initGUI();
        setValues() ;
        addInternalListeners(this);
        initWidget(panel);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI() ;
            if(false==GoEvidenceValidator.validate(this,createDTOFromGUI())){
                return ;
            }
            working();
            TermRPCService.App.getInstance().editMarkerGoTermEvidenceDTO(goEvidenceDTO,new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:"){
                @Override
                public void onFailure(Throwable throwable) {
                    super.onFailure(throwable);
                    notWorking();
                    revertGUI();
                }

                @Override
                public void onSuccess(GoEvidenceDTO result) {
                    setDTO(result);
                    fireEventSuccess();
                    notWorking();
                    fireChangeEvent(new RelatedEntityEvent<GoEvidenceDTO>(result));
                    DeferredCommand.addCommand(new CompareCommand());
                }
            });
        }
    }

//    public GoEvidenceDTO createDTOFromGUI() {
//        GoEvidenceDTO goEvidenceDTO ;
//        if(dto!=null){
//            goEvidenceDTO = dto.deepCopy();
//        }
//        else{
//            goEvidenceDTO = new GoEvidenceDTO();
//        }
//        goEvidenceDTO.setFlag(evidenceFlagBox.getItemCount()==0 || evidenceFlagBox.getSelected()==null  ? null : GoFlagEnum.getType(evidenceFlagBox.getSelected()));
//        goEvidenceDTO.setPublicationZdbID(pubLabel.getBoxValue());
//        goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.valueOf(evidenceCodeBox.getSelected()));
//        goEvidenceDTO.setNote(noteBox.getText());
//        goEvidenceDTO.setModifiedDate(new Date());
//
//        if(inferenceListBox.createDTOFromGUI()!=null){
//            goEvidenceDTO.setInferredFrom(inferenceListBox.createDTOFromGUI().getInferredFrom());
//        }
//
//        return goEvidenceDTO;
//    }


//    public boolean isDirty() {
//        if(dto==null) return false ;
//        boolean isDirty = false;
//        isDirty = nameBox.isDirty(dto.getName()) || isDirty ;
//        isDirty = evidenceCodeBox.isDirty(dto.getEvidenceCode().name()) || isDirty ;
//        isDirty = pubLabel.isDirty(dto.getPublicationZdbID()) || isDirty ;
//        isDirty = evidenceFlagBox.isDirty( (dto.getFlag()==null ? null : dto.getFlag().toString())) || isDirty ;
//        isDirty = noteBox.isDirty( (dto.getNote()==null ? null : dto.getNote())) || isDirty ;
//        isDirty = inferenceListBox.isDirty() || isDirty ;
//        return isDirty;
//    }




    @Override
    public void working() {
        super.working();    //To change body of overridden methods use File | Settings | File Templates.
        evidenceCodeBox.setEnabled(false);
        pubLabel.setEnabled(false);
        evidenceFlagBox.setEnabled(false);
        inferenceListBox.working();
    }

    @Override
    public void notWorking() {
        super.notWorking();    //To change body of overridden methods use File | Settings | File Templates.
        evidenceCodeBox.setEnabled(true);
        evidenceFlagBox.setEnabled(true);
        nameBox.setEnabled(false);
        inferenceListBox.notWorking();
    }

}