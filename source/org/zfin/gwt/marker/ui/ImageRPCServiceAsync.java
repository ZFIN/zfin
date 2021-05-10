package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.List;

public interface ImageRPCServiceAsync {

    void getImageForZdbID(String zdbID, AsyncCallback<ImageDTO> async);

    void addTerm(String name, String imageZdbID, AsyncCallback<TermDTO> async);

    void removeTerm(String name, String imageZdbID, AsyncCallback<Void> async);

    void getStages(AsyncCallback<List<StageDTO>> async);

    void setStages(String startStageZdbId, String endStageZdbId, String imageZdbID, AsyncCallback<Void> async);

    void addConstruct(String name, String imageZdbID, AsyncCallback<MarkerDTO> async);

    void removeConstruct(String name, String imageZdbID, AsyncCallback<Void> async);
}
