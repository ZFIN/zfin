package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.RootPanel;

import org.zfin.marker.presentation.dto.RelatedEntityDTO;
import org.zfin.marker.presentation.event.RelatedEntityListener;
import org.zfin.marker.presentation.event.RelatedEntityEvent;

/**
 */
public class PreviousNamesBox extends RelatedEntityBox  {


    public PreviousNamesBox(String div){
        super() ;
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }


    public void addInternalListeners(final HandlesError handlesError){
        addRelatedEntityCompositeListener(new RelatedEntityListener<RelatedEntityDTO>() {
            public void addRelatedEntity(RelatedEntityEvent<RelatedEntityDTO> event) {
                MarkerRPCService.App.getInstance().addDataAliasRelatedEntity(event.getRelatedEntityDTO(),
                        new MarkerEditCallBack<RelatedEntityDTO>("Failed to add name: ",handlesError) {
                            public void onSuccess(RelatedEntityDTO relatedEntityDTO) {
                                addRelatedEntityToGUI(relatedEntityDTO);
                            }
                        });

            }

            public void addAttribution(RelatedEntityEvent<RelatedEntityDTO> event) {
                MarkerRPCService.App.getInstance().addDataAliasAttribution(event.getRelatedEntityDTO(),
                        new MarkerEditCallBack<RelatedEntityDTO>("failed to add attribution: ",handlesError) {
                            public void onSuccess(RelatedEntityDTO relatedEntityDTO) {
                                addAttributionToGUI(relatedEntityDTO);
                            }
                        });

            }

            public void removeRelatedEntity(RelatedEntityEvent<RelatedEntityDTO> event) {
                final RelatedEntityDTO dto = event.getRelatedEntityDTO();
                MarkerRPCService.App.getInstance().removeDataAliasRelatedEntity(event.getRelatedEntityDTO(),
                        new MarkerEditCallBack<Void>("failed to remove name: ",handlesError) {
                            public void onSuccess(Void o) {
                                removeRelatedEntityFromGUI(dto);
                            }
                        });
            }

            public void removeAttribution(RelatedEntityEvent<RelatedEntityDTO> event) {
                final RelatedEntityDTO dto = event.getRelatedEntityDTO();
                MarkerRPCService.App.getInstance().removeDataAliasAttribution(event.getRelatedEntityDTO(),
                        new MarkerEditCallBack<Void>("failed to remove attribution: ",handlesError) {
                            public void onSuccess(Void o) {
                                removeAttributionFromGUI(dto);
                            }
                        });
            }
        });

    }


}