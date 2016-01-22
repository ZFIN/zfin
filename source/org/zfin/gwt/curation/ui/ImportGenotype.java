package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.ShowHideWidget;

/**
 * Table of associated genotypes
 */
public class ImportGenotype extends SingleGridBaseComposite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);

    @UiTemplate("ImportGenotype.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ImportGenotype> {
    }

    @UiField
    VerticalPanel importGenotypePanel;
    @UiField
    ZfinFlexTable genotypeSearchResultTable;
    @UiField
    Button searchExistingGenotypes;
    @UiField
    ListBox featureListBox;
    @UiField
    ListBox backgroundListBox;
    @UiField
    HorizontalPanel featureBackgroundLists;
    @UiField
    SimpleErrorElement messageLabel;

    private ImportGenotypePresenter presenter;

    public ImportGenotype() {
        initWidget(binder.createAndBindUi(this));
        sectionVisibilityToggle = new ShowHideWidget(showHideSection, importGenotypePanel);
    }

    @UiHandler("searchExistingGenotypes")
    void onSearchEvent(@SuppressWarnings("unused") ClickEvent event) {
        presenter.searchForGenotypes();
    }

    @UiHandler("featureListBox")
    void onChangeFeatureSelection(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.searchForGenotypes();
    }

    @UiHandler("backgroundListBox")
    void onChangeBackgroundSelection(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.searchForGenotypes();
    }

    @UiHandler("showHideSection")
    void onShowHideClick(@SuppressWarnings("unused") ClickEvent event) {
        sectionVisibilityToggle.toggleVisibility();
    }

    protected void addGenotype(GenotypeDTO genotype, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (genotype == null) {
            dataTable.setText(row, 0, "");
            return;
        }
        InlineHTML genoName = new InlineHTML(genotype.getNamePlusBackground());
        dataTable.setWidget(row, 1, genoName);
        dataTable.setText(row, 2, genotype.getHandle());
    }

    protected void addCheckBox(int elementIndex, ClickHandler event) {
        int row = elementIndex + 1;
        CheckBox checkBox = new CheckBox();
        checkBox.addClickHandler(event);
        checkBox.setTitle("Import Genotype");
        dataTable.setWidget(row, 0, checkBox);
    }

    protected void createLastTableRow() {
        int lastRow = getLastRow();
        int col = 0;
        dataTable.setWidget(lastRow, col++, searchExistingGenotypes);
        dataTable.setWidget(lastRow, col, featureBackgroundLists);
        dataTable.getRowFormatter().setStyleName(lastRow, "table-header");
    }

    public void setMessage(String message) {
        messageLabel.setError(message);
    }

    public void resetGUI() {
        errorLabel.setError("");
        messageLabel.setError("");
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public ListBox getFeatureListBox() {
        return featureListBox;
    }

    public ListBox getBackgroundListBox() {
        return backgroundListBox;
    }

    public void setPresenter(ImportGenotypePresenter presenter) {
        this.presenter = presenter;
    }
}
