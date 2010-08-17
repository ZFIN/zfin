package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;


public interface ImageRPCService extends RemoteService {

    public static class App {
        private static ImageRPCServiceAsync ourInstance = null;

        public static synchronized ImageRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (ImageRPCServiceAsync) GWT.create(ImageRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/imageservice");
            }
            return ourInstance;
        }
    }


    ImageDTO getImageForZdbID(String zdbID);
    TermDTO addTerm(String name, String imageZdbID);
    void removeTerm(String name, String imageZdbID);
    List<StageDTO> getStages();

    void setStages(String startStageZdbId, String endStageZdbId, String imageZdbID);
    


}
