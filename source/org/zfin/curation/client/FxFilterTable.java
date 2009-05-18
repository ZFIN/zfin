package org.zfin.curation.client;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import org.zfin.curation.dto.ExperimentDTO;
import org.zfin.curation.dto.StringUtils;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FxFilterTable extends Composite {

    // GUI elements
    public static final String FILTER_BAR = "curation-filter";
    public static final String FILTER_BAR_GENES = "curation-filter-genes";
    public static final String FILTER_BAR_FISH = "curation-filter-fish";
    public static final String ALL = "ALL";

    // filter bar (aka banana bar)
    private ListBoxWrapper geneList = new ListBoxWrapper(false);
    private ListBoxWrapper fishList = new ListBoxWrapper(false);

    // lookup
    public static final String LOOKUP_PUBLICATION_ID = "zdbID";
    public static final String LOOKUP_GENE_ID = "geneID";
    public static final String LOOKUP_FISH_ID = "fishID";
    public static final String CURATION_PROPERTIES = "curationProperties";
    public static final String DEBUG = "debug";

    private String geneID;
    private String fishID;

    private String publicationID;
    private DisplayExperimentTable experimentTable;

    public FxFilterTable(DisplayExperimentTable experimentTable) {
        super();
        this.experimentTable = experimentTable;
        initGUI();
        loadPublicationAndFilterElements();
        setInitialValues();
    }

    private void setInitialValues() {
        retrieveGenes();
        retrieveFish();
    }

    private void retrieveFish() {
        CurationExperimentRPC.App.getInstance().getFish(publicationID, new AsyncCallback<List<FishDTO>>() {
            public void onFailure(Throwable throwable) {
                if (throwable instanceof PublicationNotFoundException) {
                    Window.alert("" + throwable);
                } else {
                    Window.alert("Fatal exception: " + throwable);
                }
            }

            public void onSuccess(List<FishDTO> fish) {
//                Window.alert("brought back: " + genes.size() );
                fishList.addItem(ALL);
                for (FishDTO fishDTO : fish) {
                    fishList.addItem(fishDTO.getName(), fishDTO.getZdbID());
                }
                selectFishFilterElement();
            }
        });
    }

    private void retrieveGenes() {
        CurationExperimentRPC.App.getInstance().getGenes(publicationID, new AsyncCallback<List<MarkerDTO>>() {
            public void onFailure(Throwable throwable) {
                if (throwable instanceof PublicationNotFoundException) {
                    Window.alert("" + throwable);
                } else {
                    Window.alert("Fatal exception: " + throwable);
                }
            }

            public void onSuccess(List<MarkerDTO> genes) {
//                Window.alert("brought back: " + genes.size() );
                geneList.addItem(ALL);
                for (MarkerDTO gene : genes) {
                    geneList.addItem(gene.getAbbreviation(), gene.getZdbID());
                }
                selectGeneFilterElement();
            }
        });
    }

    private void initGUI() {
        RootPanel.get(FILTER_BAR_GENES).add(geneList);
        geneList.addChangeListener(new ChangeListener() {
            public void onChange(Widget widget) {
                applyExperimentFilter();
            }
        });

        RootPanel.get(FILTER_BAR_FISH).add(fishList);
        fishList.addChangeListener(new ChangeListener() {
            public void onChange(Widget widget) {
                applyExperimentFilter();
            }
        });

    }

    // Load properties from JavaScript.
    private void loadPublicationAndFilterElements() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary(CURATION_PROPERTIES);
            publicationID = transcriptDictionary.get(LOOKUP_PUBLICATION_ID);
            geneID = transcriptDictionary.get(LOOKUP_GENE_ID);
            fishID = transcriptDictionary.get(LOOKUP_FISH_ID);
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    /**
     * select the gene from filter that was used.
     *
     */
    private void selectGeneFilterElement() {
        if (StringUtils.isEmpty(geneID))
            return;

        int numberOfEntries = geneList.getItemCount();
        for (int row = 0; row < numberOfEntries; row++) {
            String gene = geneList.getValue(row);
            if (gene.equals(geneID)) {
                geneList.setSelectedIndex(row);
                break;
            }
        }
    }

    /**
     * select the fish from filter that was used.
     *
     */
    private void selectFishFilterElement() {
        if (StringUtils.isEmpty(fishID))
            return;

        int numberOfEntries = fishList.getItemCount();
        for (int row = 0; row < numberOfEntries; row++) {
            String gene = fishList.getValue(row);
            if (gene.equals(fishID)) {
                fishList.setSelectedIndex(row);
                break;
            }
        }
    }

    public class ExperimentSelectClickListener implements ClickListener {

        private ExperimentDTO experiment;

        public ExperimentSelectClickListener(ExperimentDTO experiment) {
            this.experiment = experiment;
        }

        public void onClick(Widget widget) {
            //Window.alert(experiment.getGeneName() + " | " + experiment.getExperimentZdbID());
            updateExpressionSection(experiment.getGeneName());
        }
    }

    public void applyExperimentFilter() {
        experimentTable.clearErrorMessages();
        String geneName = geneList.getItemText(geneList.getSelectedIndex());
        String geneID = geneList.getValue(geneList.getSelectedIndex());
        String fishName = fishList.getItemText(fishList.getSelectedIndex());
        String fishID = fishList.getValue(fishList.getSelectedIndex());
        //Window.alert("gene box changed to: " + geneName + "(" + geneID + ")");
        //Window.alert("fish box changed to: " + fishName );

        ExperimentDTO experimentFilter = new ExperimentDTO();
        if (!geneName.equals(ALL))
            experimentFilter.setGeneZdbID(geneID);
        if (!fishID.equals(ALL)){
            experimentFilter.setFishName(fishName);
            experimentFilter.setFishID(fishID);
        }
        experimentFilter.setPublicationID(publicationID);
        experimentTable.setExperimentFilter(experimentFilter);
        experimentTable.retrieveExperiments();
    }

    public native void updateExpressionSection(String experiment) /*-{
        $wnd.updateExpressionExpBox(experiment);
    }-*/;

}