package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoMarkerAddBox extends GoInlineMarkerCloneBox {


    public GoMarkerAddBox(GoMarkerViewTable goViewTable) {
        this.parent = goViewTable;
        tabIndex = 10;
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }


    protected void initGUI() {
        super.initGUI();
        termInfoComposite.setWidth("400px");
        saveButton.setText("Create");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("<font color=red>Not Saved</font>");
    }

    @Override
    protected void setValues() {
        super.setValues();
        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.IMP.name());
        inferenceListBox.setDTO(createDTOFromGUI());
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);


        revertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                revertGUI();
                fireEventSuccess();
            }

        });
    }

    @Override
    public void onPublicationChanged(PublicationChangeEvent event) {
        if (event.isNotEmpty()) {
            dto.setPublicationZdbID(event.getPublication());
            super.onPublicationChanged(event);
            setValues();
            clearError();
        }
    }


    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI();
            // have to add marker
            try {
                GoEvidenceValidator.validate(goEvidenceDTO);
            } catch (ValidationException e) {
                setError(e.getMessage());
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().createMarkerGoTermEvidence(goEvidenceDTO,
                    new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:", this) {
                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            notWorking();
                            revertGUI();
                        }

                        @Override
                        public void onSuccess(final GoEvidenceDTO result) {
                            fireChangeEvent(new RelatedEntityEvent<GoEvidenceDTO>(result));
                            notWorking();
                            revertGUI();
                            parent.clearError();
//                            fireEventSuccess();
                        }
                    });
        }
    }

}