package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

import java.util.*;

/**
 * Entry point for FX curation module.
 */
public class StructurePileView extends Composite implements HandlesError {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("StructurePileView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, StructurePileView> {
    }

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    Button updateStructuresTop;
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
    @UiField
    Image loadingImage;
    @UiField
    Button updateStructuresBottom;

    private ZfinListBox tagList;
    public static final String TAG_ABNORMAL = "abnormal";
    public static final String TAG_NORMAL = "normal";
    private String publicationID;
    private StructurePilePresenter structurePilePresenter;

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public StructurePileView() {
        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        structurePilePresenter.retrieveStructurePile();
    }

    @UiHandler("updateStructuresTop")
    void onClickUpdateButton(@SuppressWarnings("unused") ClickEvent event) {
        alternateStructurePanel.setVisible(false);
        loadingImage.setVisible(true);
        structurePilePresenter.updateStructures();
    }

    @UiHandler("updateStructuresBottom")
    void onClickUpdateButtonBottoms(@SuppressWarnings("unused") ClickEvent event) {
        onClickUpdateButton(event);
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

    public Image getLoadingImage() {
        return loadingImage;
    }

    public VerticalPanel getStructurePile() {
        return structurePile;
    }

    public Hyperlink getReCreatePile() {
        return reCreatePile;
    }
}
