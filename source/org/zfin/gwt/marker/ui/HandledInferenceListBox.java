package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityAdapter;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.GoEvidenceDTO;

/**
 * A list of inferences.
 * In the first box, can be a set of inferences and the "entry" box is open text field
 * In the second box, will be the entry field and only visible if not ZFIN.
 * In the third box, will be a drop-down and only visible IF ZFIN.
 *
 * Everything available will be related to that pub.
 * However, if free-text can also be entered and it will be associated with whatever is selected in the drop-down.
 *
 */
public class HandledInferenceListBox extends AbstractInferenceListBox{


    public HandledInferenceListBox(String div){
        super(div);
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    public HandledInferenceListBox() {
        super();
    }


    @Override
    public void sendUpdates() {
        super.sendUpdates();
        sendUpdateToServer(dto,valueToSend,prefixToSend) ;
    }

    private void sendUpdateToServer(GoEvidenceDTO dto, final String value, String prefix) {
        TermRPCService.App.getInstance().addInference(dto, value, prefix,
                new MarkerEditCallBack<GoEvidenceDTO>("Failed to add inference"){

                    @Override
                    public void onFailure(Throwable t) {
                        super.onFailure(t);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(GoEvidenceDTO result) {
                        if(result!=null){
//                            addToGUI(fullAccession);
                            fireEventSuccess();
                            fireDataChanged(new RelatedEntityEvent(result));
                        }
                        else{
                            setError("Accession is invalid: "+ value);
                        }
                        notWorking();
                    }
                });
    }

    protected void addToGUI(String name) {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setName(name);
        goEvidenceDTO.setZdbID(dto.getZdbID());
        goEvidenceDTO.setDataZdbID(dto.getDataZdbID());
        StackComposite<GoEvidenceDTO> stackComposite = new StackComposite<GoEvidenceDTO>(goEvidenceDTO) ;
        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<GoEvidenceDTO>(){
            @Override
            public void removeRelatedEntity(final RelatedEntityEvent<GoEvidenceDTO> event) {

                TermRPCService.App.getInstance().removeInference(event.getDTO(),
                        new MarkerEditCallBack<GoEvidenceDTO>("Failed to remove inference"){

                            @Override
                            public void onFailure(Throwable t) {
                                super.onFailure(t);
                                notWorking();
                            }

                            @Override
                            public void onSuccess(GoEvidenceDTO result) {
                                removeFromGUI(result.getName());
                                notWorking();
                                fireEventSuccess();
                                fireDataChanged(new RelatedEntityEvent(result));
                            }
                        });
            }
        });
        int rowCount = stackTable.getRowCount();
        stackTable.setWidget(rowCount, 0, stackComposite);
        resetInput();
    }

}