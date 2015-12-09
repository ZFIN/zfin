package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinListBox;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

import java.util.*;

/**
 * Entry point for FX curation module.
 */
public class StructurePileModule extends Composite implements HandlesError {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("StructurePileModule.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, StructurePileModule> {
    }

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    Hyperlink showHidePile;
    @UiField
    Button UpdateStructuresTop;

    private ZfinListBox tagList;
    public static final String TAG_ABNORMAL = "abnormal";
    public static final String TAG_NORMAL = "normal";
    private String publicationID;
    private FxCurationPresenter fxCurationPresenter;

    private Map<EntityPart, TermEntry> termEntryUnitsMap = new HashMap<>(5);
    private Collection<TermEntry> termEntryUnits = new ArrayList<>(3);

    private final HandlerManager eventBus = new HandlerManager(null);

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public StructurePileModule() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setError(String message) {

    }

    @Override
    public void clearError() {

    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

}
