package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class ImportGenotype extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);


    @UiTemplate("ImportGenotype.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ImportGenotype> {
    }

    @UiField
    Hyperlink showHideSection;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;
    @UiField
    VerticalPanel importGenotypePanel;
    @UiField
    ZfinFlexTable genotypeSearchResultTable;

    Button searchExistingGenotypes = new Button("Search");
    ListBox featureListBox = new ListBox();
    ListBox backgroundListBox = new ListBox();


    public ImportGenotype() {
        initWidget(binder.createAndBindUi(this));
        sextionVisibilityToggle = new ShowHideWidget(showHideSection, importGenotypePanel);
    }


    private void initConstructionGenotypeSearchResultRow(int row) {
        int col = 0;
        HorizontalPanel search = new HorizontalPanel();
        search.add(searchExistingGenotypes);
        genotypeSearchResultTable.setWidget(row, col, search);
        HorizontalPanel panel = new HorizontalPanel();
        InlineHTML featureHtml = new InlineHTML("Feature: ");
        featureHtml.setStyleName("bold");
        panel.add(featureHtml);
        panel.add(featureListBox);
        InlineHTML backgroundHtml = new InlineHTML("Background: ");
        backgroundHtml.setStyleName("bold");
        panel.add(backgroundHtml);
        panel.add(backgroundListBox);
        genotypeSearchResultTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeSearchResultTable.setWidget(row, col, panel);
        genotypeSearchResultTable.getCellFormatter().setStyleName(row, col, "bold");
        genotypeSearchResultTable.getFlexCellFormatter().setColSpan(row, col, 2);
        genotypeSearchResultTable.getRowFormatter().setStyleName(row, "table-header");
    }


    private void initGenotypeSearchResultTable() {
        int col = 0;
        genotypeSearchResultTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeSearchResultTable.setText(0, col++, "Add");
        genotypeSearchResultTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeSearchResultTable.setText(0, col++, "Display Name");
        genotypeSearchResultTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeSearchResultTable.setText(0, col, "Handle");
        genotypeSearchResultTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private Map<CheckBox, GenotypeDTO> genotypeCheckboxMap = new HashMap<>();

    public void updateExistingGenotypeListTableContent(List<GenotypeDTO> genotypeDTOList) {
        genotypeSearchResultTable.removeAllRows();
        if (genotypeDTOList == null || genotypeDTOList.size() == 0) {
            initGenotypeSearchResultTable();
            initConstructionGenotypeSearchResultRow(1);
            return;
        }

        genotypeSearchResultTable.setVisible(true);
        initGenotypeSearchResultTable();

        int groupIndex = 0;
        int rowIndex = 1;
        for (final GenotypeDTO genotype : genotypeDTOList) {
            int col = 0;
            CheckBox checkBox = new CheckBox();
            genotypeCheckboxMap.put(checkBox, genotype);
            genotypeSearchResultTable.setWidget(rowIndex, col++, checkBox);
            InlineHTML genoName = new InlineHTML(genotype.getNamePlusBackground());
            genotypeSearchResultTable.setWidget(rowIndex, col++, genoName);
            InlineHTML geno = new InlineHTML(genotype.getHandle());
            geno.setTitle(genotype.getZdbID());
            genotypeSearchResultTable.setWidget(rowIndex, col, geno);
            groupIndex = genotypeSearchResultTable.setRowStyle(rowIndex++, null, genotype.getZdbID(), groupIndex);
        }
        initConstructionGenotypeSearchResultRow(rowIndex);
    }

    private String publicationID;
    private ShowHideWidget sextionVisibilityToggle;

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public ShowHideWidget getSextionVisibilityToggle() {
        return sextionVisibilityToggle;
    }

    public Hyperlink getShowHideSection() {
        return showHideSection;
    }

    public ListBox getFeatureListBox() {
        return featureListBox;
    }

    public Button getSearchExistingGenotypes() {
        return searchExistingGenotypes;
    }

    public ListBox getBackgroundListBox() {
        return backgroundListBox;
    }

    public Map<CheckBox, GenotypeDTO> getGenotypeCheckboxMap() {
        return genotypeCheckboxMap;
    }
}
