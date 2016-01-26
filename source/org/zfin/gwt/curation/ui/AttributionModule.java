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
import org.zfin.gwt.curation.event.AddAttributeEvent;
import org.zfin.gwt.root.dto.DeAttributionException;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.StringUtils;

import java.util.HashMap;
import java.util.List;

/**
 */
public class AttributionModule extends AbstractRevertibleComposite<RelatedEntityDTO> {

    private HorizontalPanel container = new HorizontalPanel();
    private LookupComposite markerLookupComposite = new LookupComposite(false);
    private LookupComposite featureLookupComposite = new LookupComposite(false);
    private StringListBox removeListBox = new StringListBox(false);
    private HTML messageBox = new HTML("");
    private boolean working = false;
    private HashMap<String, RelatedEntityDTO> relatedEntityDTOs = new HashMap<>(); // entities in the Remove Attr drop-down list

    private MarkerRPCServiceAsync markerService = MarkerRPCService.App.getInstance();

    public enum RemoveHeader {
        MARKER,
        FEATURE,
        GENOTYPE,
        FISH;

        private static final String STARS = "**";

        public String toString() {
            return STARS + name();
        }

        public static boolean isHeader(String value) {
            return (value == null || value.startsWith(STARS));
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
        exposeAttributionMethodsToJavascript(this);
    }


    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    protected void revertGUI() {
        working();
        populateAttributeRemoval();
        markerLookupComposite.clearNote();
        markerLookupComposite.clearError();
        featureLookupComposite.clearNote();
        featureLookupComposite.clearError();
        clearMessage();
    }

    public void populateAttributeRemoval() {
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
                final String attributionToRemoveID = removeListBox.getSelectedValue();
                final String attributionToRemoveLabel = removeListBox.getSelectedText();

                if (RemoveHeader.isHeader(attributionToRemoveID)) {
                    return;
                }
                String cannotRemove = getCannotRemoveMessage(attributionToRemoveID, attributionToRemoveLabel);
                if (StringUtils.isNotEmpty(cannotRemove)) {
                    Window.alert(cannotRemove);
                    return;
                }

                if (!Window.confirm(getConfirmationMessage(attributionToRemoveID, attributionToRemoveLabel))) {
                    return;
                }

                working();

                markerService.checkDeattributionRules(attributionToRemoveID, dto.getPublicationZdbID(),
                        new DeAttributionRuleCallBack(attributionToRemoveID, handlesError));
            }
        });
    }

    private String getCannotRemoveMessage(String attributionToRemoveID, String attributionToRemoveLabel) {
        String message = null;

        // when a morpholino has a targeted gene attribution or an alias attribution in addition to the
        // direct attribution that is being removed.
        if (relatedEntityDTOs.get(attributionToRemoveID) instanceof MarkerDTO) {
            boolean stillAttributed = false;
            MarkerDTO markerDTO = (MarkerDTO) relatedEntityDTOs.get(attributionToRemoveID);
            if (markerDTO.getTargetedGeneAttributes() != null) {
                for (MarkerDTO targeted : markerDTO.getTargetedGeneAttributes()) {
                    if (targeted.getPublicationZdbID().equals(dto.getPublicationZdbID())) {
                        stillAttributed = true;
                        break;
                    }
                }
            }
            if (markerDTO.getAliasAttributes() != null) {
                for (RelatedEntityDTO alias : markerDTO.getAliasAttributes()) {
                    if (alias.getPublicationZdbID().equals(dto.getPublicationZdbID())) {
                        stillAttributed = true;
                        break;
                    }
                }
            }
            if (stillAttributed) {
                message = "This pub is still being used to attribute other data on " + attributionToRemoveLabel +
                        " such as target gene, sequence, or alias, so it cannot be removed from this pub. To" +
                        " dissociate this pub from " + attributionToRemoveLabel + ", you must first go to the " +
                        attributionToRemoveLabel + " page and remove those remaining attributions.";
            }
        }

        return message;
    }

    private String getConfirmationMessage(String attributionToRemoveID, String attributionToRemoveLabel) {
        // the default message
        String removeAttrPopupPrompt = "Are you sure you want to remove attribution for: " + attributionToRemoveLabel;
        return removeAttrPopupPrompt;
    }

    private void addMarkerAttribution(final String value) {
        // should cancel second submit (case 5943)
        if (isWorking()) return;
        if (checkAttributionExists(value)) return;
        working();
        markerService.addAttributionForMarkerName(value, dto.getPublicationZdbID(),
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
                        AppUtils.EVENT_BUS.fireEvent(new AddAttributeEvent());
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
        if (isWorking()) return;
        if (checkAttributionExists(value)) return;
        working();
        markerService.addAttributionForFeatureName(value, dto.getPublicationZdbID(),
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
        working = true;
        super.working();
        markerLookupComposite.working();
        featureLookupComposite.working();
        removeListBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        working = false;
        super.notWorking();
        markerLookupComposite.notWorking();
        featureLookupComposite.notWorking();
        removeListBox.setEnabled(true);
    }

    public boolean isWorking() {
        return working;
    }


    private native void exposeAttributionMethodsToJavascript(AttributionModule attributionModule)/*-{
        $wnd.refreshAttribution = function (pubID) {
            attributionModule.@org.zfin.gwt.curation.ui.AttributionModule::refreshAttribution()();
        };

    }-*/;

    public void refreshAttribution() {
        revertGUI();
    }


    class DeAttributionRuleCallBack extends ZfinAsyncCallback<Void> {

        private String entityID;
        private HandlesError errorLabel;

        public DeAttributionRuleCallBack(String entityID, HandlesError errorLabel) {
            super(entityID, null);
            this.entityID = entityID;
        }

        @Override
        public void onSuccess(Void result) {
            markerService.removeAttribution(entityID, dto.getPublicationZdbID(),
                    new MarkerEditCallBack<String>("Failed to remove attribution: ", errorLabel) {

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
                                AppUtils.EVENT_BUS.fireEvent(new RemoveAttributeEvent());
                                clearError();
                                setMessage("Removed attribution: ");
                                resetInput();
                            } else if (!message.isEmpty()) {
                                setError(message);
                                clearMessage();
                            }
                            notWorking();
                        }
                    });

        }

        @Override
        public void onFailure(Throwable throwable) {
            if (throwable instanceof DeAttributionException)
                Window.alert(throwable.getMessage());
            else
                super.onFailure(throwable);
            notWorking();
        }
    }

}
