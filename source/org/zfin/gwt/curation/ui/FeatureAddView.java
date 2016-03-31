package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.TransgenicSuffix;
import org.zfin.gwt.root.ui.Revertible;
import org.zfin.gwt.root.ui.StringTextBox;

public class FeatureAddView extends AbstractFeatureView implements Revertible {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureAddView> {
    }

    private FeatureAddPresenter editPresenter;
    @UiField
    StringTextBox featureAliasBox;
    @UiField
    StringTextBox featureSequenceBox;
    @UiField
    TextArea curatorNoteBox;
    @UiField
    TextArea publicNoteBox;

    public FeatureAddView() {
        initWidget(uiBinder.createAndBindUi(this));
        featureTypeBox.clear();
        featureTypeBox.addNull();
        for (FeatureTypeEnum featureTypeEnum : FeatureTypeEnum.values()) {
            featureTypeBox.addItem(featureTypeEnum.getDisplay(), featureTypeEnum.name());
        }
        featureSuffixBox.addEnumValues(TransgenicSuffix.values());
        mutageeBox.addEnumValues(Mutagee.values());
    }

    @UiHandler("saveButton")
    void onClickSaveButton(@SuppressWarnings("unused") ClickEvent event) {
        editPresenter.createFeature();
    }

    private void handleChanges() {
        clearErrors();
        editPresenter.handleDirty();
    }

    @UiHandler("featureTypeBox")
    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        super.onChangeFeatureType(event);
        handleDirty();
    }

    public void resetInterface() {
        labOfOriginBox.setEnabled(false);
        labOfOriginBox.setSelectedIndex(0);
        labDesignationBox.setEnabled(false);
        labDesignationBox.clear();
        labDesignationBox.setSelectedIndex(0);
        lineNumberBox.setEnabled(false);
        lineNumberBox.clear();
        dominantCheckBox.setEnabled(false);
        dominantCheckBox.setValue(false);
        featureNameBox.setEnabled(false);
        featureNameBox.clear();
        featureAliasBox.setEnabled(false);
        featureAliasBox.clear();
        featureSequenceBox.setEnabled(false);
        featureSequenceBox.clear();
        mutageeBox.setEnabled(false);
        mutageeBox.setSelectedIndex(0);
        mutagenBox.setEnabled(false);
        mutagenBox.setSelectedIndex(0);
        publicNoteBox.setEnabled(false);
        publicNoteBox.setText("");
        curatorNoteBox.setEnabled(false);
        curatorNoteBox.setText("");
        knownInsertionCheckBox.setEnabled(false);
        knownInsertionCheckBox.setValue(false);
        featureDisplayName.clear();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean handleDirty() {
        return editPresenter.handleDirty();
    }



    public void setPresenter(FeatureAddPresenter presenter) {
        this.presenter = presenter;
        editPresenter = presenter;
    }

    public void clearErrors() {
        errorLabel.clearAllErrors();
    }

    public void working() {
        super.working();
        featureAliasBox.setEnabled(false);
        featureSequenceBox.setEnabled(false);
        publicNoteBox.setEnabled(false);
        curatorNoteBox.setEnabled(false);
    }

}
