package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.SimpleErrorElement;

import java.util.ArrayList;
import java.util.List;

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
    @UiField
    SimpleErrorElement errorElementTop;

    private StructurePilePresenter structurePilePresenter;

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

    @UiHandler("errorElement")
    void onChangeOfErrorMessage(@SuppressWarnings("unused") ChangeEvent event) {
        errorElementTop.setError(errorElement.getText());
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

    public void addErrorMessage(String message) {
        errorElement.setError(message);
        errorElementTop.setError(message);
    }

    public void clearErrorMessage() {
        errorElement.clearAllErrors();
        errorElementTop.clearAllErrors();
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
