package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.mutant.Genotype;

import java.util.*;

/**
 */
public class AttributionModule extends AbstractRevertibleComposite<RelatedEntityDTO> {

    private HorizontalPanel container = new HorizontalPanel();
    private LookupComposite markerLookupComposite = new LookupComposite();
    private LookupComposite featureLookupComposite = new LookupComposite();
    private ListBoxWrapper removeListBox = new ListBoxWrapper(false);
    private HTML messageBox = new HTML("");
    private boolean working = false ;
    private String removeAttrPopupPrompt = "";
    private HashMap<String, RelatedEntityDTO> relatedEntityDTOs = new HashMap<String, RelatedEntityDTO>(); // entities in the Remove Attr drop-down list

    public static enum RemoveHeader {
        MARKER,
        FEATURE,
        GENOTYPE,;

        private static final String stars = "**";

        public String toString() {
            return stars + name();
        }

        public static boolean isHeader(String value) {
            return (value.startsWith(stars));
        }
    }

    public AttributionModule() {
        this(StandardDivNames.directAttributionDiv);
    }

    public AttributionModule(String div) {
        initWidget(panel);
        initGUI();
        setValues();
        addInternalListeners(this);
        if (div != null) {
            RootPanel.get(div).add(this);
        }
    }


    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    protected void revertGUI() {
        working();
        LookupRPCService.App.getInstance().getAttributionsForPub(dto.getPublicationZdbID(),
                new MarkerEditCallBack<List<RelatedEntityDTO>>("Failed to return attributions for: " + dto, this) {
                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(List<RelatedEntityDTO> result) {

                        removeListBox.clear();
                        for (RelatedEntityDTO relatedEntityDTO : result) {
                            removeListBox.addItem(relatedEntityDTO.getName(), relatedEntityDTO.getZdbID());
                            relatedEntityDTOs.put(relatedEntityDTO.getZdbID(), relatedEntityDTO);
                        }
                        notWorking();
                    }
                });
        markerLookupComposite.clearNote();
        markerLookupComposite.clearError();
        featureLookupComposite.clearNote();
        featureLookupComposite.clearError();
        clearMessage();
    }

    @Override
    protected void initGUI() {
        markerLookupComposite.setType(LookupComposite.MARKER_LOOKUP_AND_TYPE);
        markerLookupComposite.setButtonText("Attr Marker");
        markerLookupComposite.setWildCard(false);
        markerLookupComposite.setSubmitOnEnter(true);
        markerLookupComposite.setAction(new SubmitAction() {
            @Override
            public void doSubmit(String value) {
                addMarkerAttribution(value);
            }
        });
        markerLookupComposite.initGui();
        container.add(markerLookupComposite);
        container.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));

        featureLookupComposite.setType(LookupComposite.FEATURE_LOOKUP);
        featureLookupComposite.setButtonText("Attr Feature");
        featureLookupComposite.setWildCard(false);
        featureLookupComposite.setSubmitOnEnter(true);
        featureLookupComposite.setAction(new SubmitAction() {
            @Override
            public void doSubmit(String value) {
                addFeatureAttribution(value);
            }
        });
        featureLookupComposite.initGui();
        container.add(featureLookupComposite);
        container.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));

        container.add(new Label("Remove Attr"));
        container.add(removeListBox);
        panel.add(container);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);

        messageBox.setStyleName("clickable");
        panel.add(messageBox);
    }

    public void setMessage(String message) {
        messageBox.setHTML(message + " [close]");
    }

    public void clearMessage() {
        messageBox.setHTML("");
    }

    @Override
    protected void setValues() {

    }

    @Override
    protected void addInternalListeners(final HandlesError handlesError) {

        messageBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearMessage();
            }
        });

        removeListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                final String attributionToRemoveID = removeListBox.getSelectedStringValue();
                final String attributionToRemoveLabel = removeListBox.getSelectedText();

                if (RemoveHeader.isHeader(attributionToRemoveID)) {
                    return;
                }

                if (checkGenotypeHasOnlyOneCitation(attributionToRemoveID, attributionToRemoveLabel)) {
                    return;
                }

                if(false==Window.confirm(removeAttrPopupPrompt)) {
                    return ;
                }

                working();
                MarkerRPCService.App.getInstance().removeAttribution(attributionToRemoveID, dto.getPublicationZdbID(),
                        new MarkerEditCallBack<String>("Failed to remove attribution: " + attributionToRemoveLabel, handlesError) {

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                notWorking();
                            }

                            @Override
                            public void onSuccess(String message) {
                                notWorking();
                                if (message == null) {
                                    fireEventSuccess();
                                    clearError();
                                    setMessage("Removed attribution: " + attributionToRemoveLabel);
                                    resetInput();
                                } else if (message != null && false == message.isEmpty()) {
                                    setError(message);
                                    clearMessage();
                                }
                            }
                        });
            }
        });
    }

    private boolean checkGenotypeHasOnlyOneCitation(String attributionToRemoveID, String attributionToRemoveLabel) {
        // when the related entity is a genotype and there is only one publication associated with it
        if (attributionToRemoveID.startsWith("ZDB-GENO-") && relatedEntityDTOs.get(attributionToRemoveID).getAssociatedPublications().size()==1) {
            StringBuilder sb = new StringBuilder();
            sb.append("This is the last publication associated with ").append(attributionToRemoveLabel).append(".\nTo delete ")
                    .append(attributionToRemoveLabel).append(", click Cancel and use the interface on the Genotype tab. To remove pub from genotype, click OK.");
            removeAttrPopupPrompt = sb.toString();
        } else {
            removeAttrPopupPrompt = "Are you sure you want to delete: " + attributionToRemoveLabel;
        }
        return false;

    }

    private void addMarkerAttribution(final String value) {
        // should cancel second submit (case 5943)
        if (isWorking()) return ;
        if (checkAttributionExists(value)) return;
        working();
        MarkerRPCService.App.getInstance().addAttributionForMarkerName(value, dto.getPublicationZdbID(),
                new MarkerEditCallBack<Void>("Failed to add attribution for: " + value, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        notWorking();
                        fireEventSuccess();
                        clearError();
                        setMessage("Marker attribution added: " + value);
                        resetInput();
                    }
                });
    }

    private boolean checkAttributionExists(String attribution) {
        if (removeListBox.setIndexForValue(attribution) >= 0) {
            setError("'" + attribution + "' is already attributed.");
            return true;
        }
        return false;
    }

    private void addFeatureAttribution(final String value) {
        // should cancel second submit (case 5943)
        if (isWorking()) return ;
        if (checkAttributionExists(value)) return;
        working();
        MarkerRPCService.App.getInstance().addAttributionForFeatureName(value, dto.getPublicationZdbID(),
                new MarkerEditCallBack<Void>("Failed to add attribution for: " + value, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        notWorking();
                        fireEventSuccess();
                        clearError();
                        setMessage("Feature attribution added:" + value);
                        resetInput();
                    }
                });
    }

    public void setPublication(String publicationID) {
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        setDTO(relatedEntityDTO);
    }

    private void resetInput() {
        markerLookupComposite.setText("");
        featureLookupComposite.setText("");
    }

    @Override
    public void working() {
        working = true ;
        super.working();
        markerLookupComposite.working();
        featureLookupComposite.working();
        removeListBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        working = false ;
        super.notWorking();
        markerLookupComposite.notWorking();
        featureLookupComposite.notWorking();
        removeListBox.setEnabled(true);
    }

    public boolean isWorking() {
        return working;
    }
}
