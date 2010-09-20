package org.zfin.gwt.marker.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;


/**
 * A GWT Controller class for editing image information
 *
 * Initially it will only be used to create relationships to anatomy,
 * eventually it should replace all of the update functionality of
 * imageview.apg.
 */
public class ImageEditController extends Composite {

    public static final String LOOKUP_ZDBID = "zdbID";    
    private static final String ANATOMY_CONTAINER_DIV = "imageEditDiv" ;
    private static final String STAGE_CONTAINER_DIV = "imageStageEditDiv";
    private ImageDTO dto;

    private ImageAnatomyBox imageAnatomyBox;
    private ImageEditStageSelector stageSelector;

    public void initGUI() {
    
        Dictionary dictionary = Dictionary.getDictionary("MarkerProperties") ;

        imageAnatomyBox = new ImageAnatomyBox(dictionary.get(ANATOMY_CONTAINER_DIV));
        stageSelector = new ImageEditStageSelector();
        stageSelector.hideMultiSelect();
        //hide the stage selector, only show it after the DTO is set,
        //since it sometimes fails to get set properly, when it's broken
        //it will at least be hidden.
        RootPanel.get(dictionary.get(STAGE_CONTAINER_DIV)).add(stageSelector);

        setValues();

        // load transcript
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                loadDTO();
            }
        });
    }

    protected void setValues() {
        ImageRPCService.App.getInstance().getStages(new RetrieveStageListCallback());

    }

    protected void loadDTO() {
        try {
            Dictionary dictionary = Dictionary.getDictionary("MarkerProperties") ;
            String zdbID = dictionary.get(LOOKUP_ZDBID);

            ImageRPCService.App.getInstance().getImageForZdbID(zdbID,
                    new MarkerEditCallBack<ImageDTO>("failed to load image: ", null) {
                        @Override
                        public void onSuccess(ImageDTO result) {
                            dto = result;
                            imageAnatomyBox.setDTO(dto);
                            stageSelector.setImageDTO(dto);
                        }
                    });

        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }



    /**
     * Callback for reading all stages.
     */
    public class RetrieveStageListCallback extends ZfinAsyncCallback<List<StageDTO>> {

        public RetrieveStageListCallback() {
            //todo: define error element?
//            super("Error while reading Figure Filters", errorElement);
            super("Error while loading stages", null);

        }

        public void onSuccess(List<StageDTO> stages) {
            stageSelector.setStageList(stages);
            stageSelector.setImageDTO(dto);

            //loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            //loadingImage.setVisible(true);
        }
    }



}
