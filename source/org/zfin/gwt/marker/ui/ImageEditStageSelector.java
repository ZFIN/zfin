package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.StageSelector;

/**
 * Stage selector specifically for the ImageEditController
 */
public class ImageEditStageSelector extends StageSelector {

    protected ImageDTO imageDTO;

    public ImageEditStageSelector() {
        super();
        panelTitle.setVisible(false);
        this.setStyleName("gwt-editbox");
    }

    public void addHandlers() {
        startStageList.addChangeHandler(new StartStageChangeHandler());
        endStageList.addChangeHandler(new EndStageChangeHandler());

    }

    public ImageDTO getImageDTO() {
        return imageDTO;
    }

    public void setImageDTO(ImageDTO imageDTO) {
        this.imageDTO = imageDTO;

        if (imageDTO == null)
            return;
        if (imageDTO.getStart() != null)
            selectStartStage(imageDTO.getStart().getZdbID());
        if (imageDTO.getEnd() != null)
            selectEndStage(imageDTO.getEnd().getZdbID());

    }

    private class StartStageChangeHandler implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            int startStageIndex = startStageList.getSelectedIndex();
            // always set end stage = start stage when changing start stage.
            endStageList.setSelectedIndex(startStageIndex);
            String startStageZdbId = startStageList.getValue(startStageList.getSelectedIndex());
            String endStageZdbId = endStageList.getValue(endStageList.getSelectedIndex());

            ImageRPCService.App.getInstance().setStages(startStageZdbId, endStageZdbId, imageDTO.getZdbID(),
                    new MarkerEditCallBack<Void>("failed to set stages: ") {
                @Override
                public void onSuccess(Void o) {
                    //nothing to do.. might be good to have a spinner to turn off or something..
                }
            });

        }

    }


    private class EndStageChangeHandler implements ChangeHandler {

        public void onChange(ChangeEvent event) {

            String startStageZdbId = startStageList.getValue(startStageList.getSelectedIndex());
            String endStageZdbId = endStageList.getValue(endStageList.getSelectedIndex());

            ImageRPCService.App.getInstance().setStages(startStageZdbId, endStageZdbId, imageDTO.getZdbID(),
                    new MarkerEditCallBack<Void>("failed to set stages: ") {
                @Override
                public void onSuccess(Void o) {
                    //nothing to do.. might be good to have a spinner to turn off or something..
                }
            });

        }

    }



}





