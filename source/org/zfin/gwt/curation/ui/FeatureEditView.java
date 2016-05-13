package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.TransgenicSuffix;
import org.zfin.gwt.root.ui.Revertible;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.StringUtils;

public class FeatureEditView extends AbstractFeatureView implements Revertible {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureEditView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureEditView> {
    }

    FeatureEditPresenter editPresenter;
    @UiField
    StringListBox featureEditList;
    @UiField
    FeatureAliasList featureAliasList;
    @UiField
    FeatureSequenceList featureSequenceList;
    @UiField
    DeleteImage removeFeatureLink;
    @UiField
    Button revertButton;
    @UiField
    FeatureNotesView featureNotesView;

    public FeatureEditView() {
        initWidget(uiBinder.createAndBindUi(this));
        featureTypeBox.addNull();
        for (FeatureTypeEnum featureTypeEnum : FeatureTypeEnum.values()) {
            featureTypeBox.addItem(featureTypeEnum.getDisplay(), featureTypeEnum.name());
        }
        featureSuffixBox.addEnumValues(TransgenicSuffix.values());
        mutageeBox.addEnumValues(Mutagee.values());
    }

    @UiHandler("featureEditList")
    void onChangeFeature(@SuppressWarnings("unused") final ChangeEvent event) {
        final String featureID = featureEditList.getValue(featureEditList.getSelectedIndex());
        mutationDetailDnaView.resetGUI();
        mutationDetailProteinView.resetGUI();
        mutationDetailTranscriptView.resetGUI();
        if (StringUtils.isEmpty(featureID)) {
            setNote("No feature selected");
            resetGUI();
            removeFeatureLink.setVisible(false);
            editPresenter.dto = null;
            editPresenter.revertGUI();
        } else {
            editPresenter.onFeatureSelectionChange(featureEditList.getSelected());
            removeFeatureLink.setVisible(true);
        }
    }

    @UiHandler("featureTypeBox")
    void onChangeFeatureType(@SuppressWarnings("unused") ChangeEvent event) {
        super.onChangeFeatureType(event);
        handleDirty();
    }

    @UiHandler("mutageeBox")
    void onChangeMutagee(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("mutagenBox")
    void onChangeMutagen(@SuppressWarnings("unused") ChangeEvent event) {
        handleChanges();
    }

    @UiHandler("saveButton")
    void onClickSaveButton(@SuppressWarnings("unused") ClickEvent event) {
        super.onclickSaveButton(event);
        editPresenter.updateFeature();
    }

    @UiHandler("revertButton")
    void onClickRevertButton(@SuppressWarnings("unused") ClickEvent event) {
        editPresenter.revertGUI();
        editPresenter.handleDirty();
        mutationDetailProteinView.resetMessages();
        mutationDetailDnaView.resetMessages();
    }

    public void setPresenter(FeatureEditPresenter presenter) {
        this.presenter = presenter;
        this.editPresenter = presenter;
    }

    public void resetGUI() {
        super.resetGUI();
        featureEditList.setSelectedIndex(0);
        featureAliasList.revertGUI();
        featureNotesView.resetGUI();
        hideMutationDetail();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean handleDirty() {
        return false;
    }

    public void working() {
        ///super.working();
        revertButton.setEnabled(false);
        featureEditList.setEnabled(false);
        removeFeatureLink.setVisible(false);
    }

    public void notWorking() {
//        super.notWorking();
        saveButton.setText(TEXT_SAVE);
        featureEditList.setEnabled(true);
        removeFeatureLink.setVisible(true);
    }


}
