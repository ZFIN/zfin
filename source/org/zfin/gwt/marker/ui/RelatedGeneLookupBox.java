package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SuggestBox;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.MarkerRelationshipEnumTypeGWTHack;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.LookupCallback;
import org.zfin.gwt.root.ui.LookupOracle;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.util.LookupRPCService;

/**
 * This class is the complete implementation, but it also positions itself and contains all of its handlers.
 */
public class RelatedGeneLookupBox extends RelatedMarkerBox {

    private SuggestBox suggestBox;
    private RelatedGeneOracle oracle;

    public RelatedGeneLookupBox(MarkerRelationshipEnumTypeGWTHack type, boolean zdbIDThenAbbrev, String div) {
        super(type, zdbIDThenAbbrev, div);
        initGUI();
    }

    protected void initGUI() {
        relatedEntityTable.setStyleName("relatedEntityTable");

        // init table
        panel.add(relatedEntityTable);

        newRelatedEntityPanel.add(newRelatedEntityField);
        oracle = new RelatedGeneOracle();
        suggestBox = new SuggestBox(oracle, newRelatedEntityField);
        newRelatedEntityPanel.add(suggestBox);
        newRelatedEntityPanel.add(addRelatedEntityButton);
        newRelatedEntityPanel.add(publicationLabel);
        panel.add(new HTML("&nbsp;"));
        panel.add(newRelatedEntityPanel);

        errorLabel.setStyleName("error");
        panel.add(errorLabel);
        panel.setStyleName("gwt-editbox");

        publicationLabel.setStyleName("relatedEntityDefaultPub");
    }

    @Override
    public void addRelatedEntity(final String name, final String pubZdbID) {
        // do client check
        String validationError = validateNewRelatedEntity(name);
        if (validationError != null) {
            Window.alert(validationError);
            return;
        }
        final MarkerDTO dto = new MarkerDTO();
        dto.setZdbID(getZdbID());
        dto.setName(name);
        dto.setPublicationZdbID(pubZdbID);

        if (getRelatedEntityNames().size() > 0 && type == MarkerRelationshipEnumTypeGWTHack.GENE_PRODUCES_TRANSCRIPT) {
            // if the type of the transcript is not miRNA, then give warning and proceed only if approved
            TranscriptRPCService.App.getInstance().getTranscriptTypeForZdbID(getZdbID(), new MarkerEditCallBack<String>("Failed to get transcript type") {
                public void onSuccess(String transcriptType) {
                    if (false == transcriptType.equals("miRNA")) {
                        boolean confirmed = Window.confirm("Are you sure you want to associate more than one gene to this transcript?");
                        if (confirmed) {
                            fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
                        }
                    } else {
                        fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
                    }
                }
            });
        } else {
            fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
        }
    }

    /**
     */
    public static class RelatedGeneOracle extends LookupOracle {

        @Override
        public void doLookup(final Request request, final Callback callback) {
            LookupRPCService.App.getInstance().getGenedomSuggestions(request,
                    new LookupCallback(request, callback));
        }
    }
}