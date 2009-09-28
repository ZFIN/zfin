package org.zfin.marker.presentation.client;

import org.zfin.framework.presentation.client.LookupService;
import org.zfin.marker.presentation.dto.MarkerDTO;
import org.zfin.marker.presentation.event.RelatedEntityEvent;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;

/**
 * This class is the complete implementation, but it also positions itself and contains all of its handlers.
 *
 */
public class RelatedGeneLookupBox extends RelatedMarkerBox {

    private SuggestBox suggestBox ;
    private RelatedGeneOracle oracle ;

    public RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack type, boolean zdbIDThenAbbrev,String div){
        super(type,zdbIDThenAbbrev,div) ;
    }

    protected void initGui(){
        relatedEntityTable.setStyleName("relatedEntityTable");

        // init table
        panel.add(relatedEntityTable);

        newRelatedEntityPanel.add(newRelatedEntityField);
        oracle = new RelatedGeneOracle() ;
        suggestBox = new SuggestBox(oracle,newRelatedEntityField) ;
        newRelatedEntityPanel.add(suggestBox);
        newRelatedEntityPanel.add(addRelatedEntityButton);
        newRelatedEntityPanel.add(publicationLabel);
        panel.add(new HTML("&nbsp;"));
        panel.add(newRelatedEntityPanel);

        errorLabel.setStyleName("error");
        panel.add(errorLabel) ;

        publicationLabel.setStyleName("relatedEntityDefaultPub");
    }

    @Override
    public void addRelatedEntity(final String name,final String pubZdbID) {
        // do client check
        String validationError = validateNewRelatedEntity(name);
        if(validationError!=null){
            Window.alert(validationError);
            return ;
        }
        final MarkerDTO dto = new MarkerDTO() ;
        dto.setZdbID(getZdbID()) ;
        dto.setName(name);
        dto.setPublicationZdbID(pubZdbID);

        if(getRelatedEntityNames().size()>0){
            // if the type of the transcript is not miRNA, then give warning and proceed only if approved
            TranscriptRPCService.App.getInstance().getTranscriptTypeForZdbID(getZdbID(),new MarkerEditCallBack<String>("Failed to get transcript type"){
                public void onSuccess(String transcriptType) {
                    if(false==transcriptType.equals("miRNA")){
                        boolean confirmed = Window.confirm("Are you sure you want to associate more than one gene to this transcript?") ;
                        if(true==confirmed){
                            fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
                        }
                    }
                    else{
                        fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
                    }
                }
            });
        }
        else{
            fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
        }
    }

    private class RelatedGeneOracle extends SuggestOracle{

        @Override
        public boolean isDisplayStringHTML() {
            return true ;
        }

        public void requestSuggestions(final Request request, final Callback callback) {
            String query = request.getQuery() ;
            if(query.length()>=3){
                LookupService.App.getInstance().getGenedomAndEFGSuggestions(request,true, new AsyncCallback<Response>(){
                    public void onFailure(Throwable throwable) {
                        callback.onSuggestionsReady(request,new Response());
                    }

                    public void onSuccess(Response response) {
                        callback.onSuggestionsReady(request, response);
                    }
                });
            }
        }
    }

}