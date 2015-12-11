package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.ui.*;
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
    ShowHideToggle showHideToggle;
    @UiField
    Button UpdateStructuresTop;
    @UiField
    StructureAlternateComposite alternateStructurePanel;
    @UiField
    SimpleErrorElement errorElement;
    @UiField
    StructurePileTable structurePileTable;
    @UiField
    Hyperlink reCreatePile;
    @UiField
    VerticalPanel structurePile;

    private ZfinListBox tagList;
    public static final String TAG_ABNORMAL = "abnormal";
    public static final String TAG_NORMAL = "normal";
    private String publicationID;
    private StructurePilePresenter structurePilePresenter;

    private Map<EntityPart, TermEntry> termEntryUnitsMap = new HashMap<>(5);
    private Collection<TermEntry> termEntryUnits = new ArrayList<>(3);

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public StructurePileModule() {
        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        structurePilePresenter.retrieveStructurePile();
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

    public SimpleErrorElement getErrorElement() {
        return errorElement;
    }

    public void setErrorElement(SimpleErrorElement errorElement) {
        this.errorElement = errorElement;
    }

    public void setStructurePilePresenter(StructurePilePresenter structurePilePresenter) {
        this.structurePilePresenter = structurePilePresenter;
    }

    public StructurePileTable getStructurePileTable() {
        return structurePileTable;
    }

    public StructureAlternateComposite getAlternateStructurePanel() {
        return alternateStructurePanel;
    }
}
