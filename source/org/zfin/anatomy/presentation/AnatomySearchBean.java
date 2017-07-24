package org.zfin.anatomy.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.SectionVisibility;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Fish;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.*;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.util.URLCreator;

import java.util.*;

/**
 * This Bean is used for most of the anatomy pages and contains all objects that
 * need to be displayed on any of the pages.
 */
public class AnatomySearchBean extends PaginationBean {

    public static final int MAX_NUMBER_GENOTYPES = 5;
    public static final int MAX_NUMBER_EPRESSED_GENES = 5;
    public static final int MAX_NUMBER_PROBES = 5;

    private String id;
    private List<AnatomyStatistics> statisticItems;
    private DevelopmentStage stage;
    private String action;
    private String searchTerm;
    private int numberOfPublications;
    private String highlightText;
    private List<String> anatomyNamesAndSynonyms;
    private List<Publication> qualityProbePublications;
    private List<HighQualityProbe> highQualityProbeGenes;
    private List<ExpressedGeneDisplay> allExpressedMarkers;
    private AnatomyStatistics anatomyStatistics;
    private AnatomyStatistics anatomyStatisticsMutant;
    private AnatomyStatistics anatomyStatisticsProbe;
    private AnatomyStatistics anatomyStatisticsAntibodies;
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String TAB = "\t";
    private int numberOfHighQualityProbes;
    private int markerCount;
    private SectionVisibility visibility = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);

    private List<Fish> fish;
    private int fishCount;
    private int antibodyCount;
    private int expressedGeneCount;
    private int wildtypeSTRcount;
    private int mutantSTRcount;
    private int totalNumberOfFiguresPerAnatomyItem;
    private int totalNumberOfImagesPerAnatomyItem;
    private List<FishStatistics> genoStats;
    private List<AntibodyStatistics> antibodyStatistics;
    private GenericTerm aoTerm;
    private String ontologyName = Ontology.ANATOMY.getOntologyName();

    private Map<String, String> stageListDisplay;
    private List<TermDTO> terms;

    public Map<String, String> getDisplayStages() {
        if (stageListDisplay != null)
            return stageListDisplay;

        stageListDisplay = AnatomyService.getDisplayStages();
        return stageListDisplay;
    }

    static List<Ontology> ontologyBrowsingList = new ArrayList<>(5);

    static {
        ontologyBrowsingList.add(Ontology.ANATOMY);
        ontologyBrowsingList.add(Ontology.GO_CC);
        ontologyBrowsingList.add(Ontology.GO_BP);
        ontologyBrowsingList.add(Ontology.GO_MF);
    }

    public Map<String, String> getOntologyList() {
        LinkedHashMap<String, String> ontologyList = new LinkedHashMap<>(5);
        for (Ontology stage : ontologyBrowsingList) {
            ontologyList.put(stage.getOntologyName(), stage.getCommonName());
        }
        return ontologyList;
    }

    public DevelopmentStage getStage() {
        if (stage == null) {
            stage = new DevelopmentStage();
        }
        return stage;
    }

    public Term getAoTerm() {
        if (aoTerm == null)
            aoTerm = new GenericTerm();
        return aoTerm;
    }

    public void setAoTerm(GenericTerm aoTerm) {
        this.aoTerm = aoTerm;
    }

    public void setStage(DevelopmentStage stage) {
        this.stage = stage;
    }

    public List<AnatomyStatistics> getStatisticItems() {
        return statisticItems;
    }

    public void setStatisticItems(List<AnatomyStatistics> statisticItems) {
        this.statisticItems = statisticItems;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isTermSearch() {
        return action != null && action.equals(AnatomySearchBean.Action.TERM_SEARCH.toString());
    }

    public boolean isCompleteSearch() {
        return action != null && action.equals(AnatomySearchBean.Action.COMPLETE_SEARCH.toString());
    }

    public boolean isStageSearch() {
        return action != null && action.equals(AnatomySearchBean.Action.STAGE_SEARCH.toString());
    }

    // remove wild-card term

    public String getSearchTerm() {
        return (isWildCard() ? searchTerm.substring(0, searchTerm.length() - 1) : searchTerm);
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    /**
     * Retrieve a comma-delimited string of the names, no space and each item enclosed in quotes.
     *
     * @return String
     */
    public String getAnatomyNamesAndSynonymsString() {
        return AnatomyPresentation.createFormattedList(anatomyNamesAndSynonyms);
    }

    public void setHighQualityProbeGenes(List<HighQualityProbe> hqp) {
        highQualityProbeGenes = hqp;
    }

    public List<HighQualityProbe> getHighQualityProbeGenes() {
        return highQualityProbeGenes;
    }

    public void setAllExpressedMarkers(List<ExpressedGeneDisplay> markers) {
        allExpressedMarkers = markers;
    }

    public List<ExpressedGeneDisplay> getAllExpressedMarkers() {
        return allExpressedMarkers;
    }

    public void setAnatomyStatistics(AnatomyStatistics statistics) {
        anatomyStatistics = statistics;
    }

    public AnatomyStatistics getAnatomyStatistics() {
        return anatomyStatistics;
    }

    /**
     * Sort the result list according to: first the items that begin with the search term and
     * then the terms that contain the search term. If no term is specified do not sort at all.
     *
     * @return list of anatomy statistics
     */
    public List<AnatomyStatistics> getSortedStatisticsItems() {
        if (searchTerm != null && !isStageSearch())
            Collections.sort(statisticItems, new SortAnatomyResults(searchTerm));
        return statisticItems;
    }

    public void setNumberOfHighQualityProbes(int numberOfHighQualityProbes) {
        this.numberOfHighQualityProbes = numberOfHighQualityProbes;
    }

    public int getNumberOfHighQualityProbes() {
        return numberOfHighQualityProbes;
    }

    public void setTotalNumberOfExpressedGenes(int markerCount) {
        this.markerCount = markerCount;
    }

    public int getTotalNumberOfExpressedGenes() {
        return markerCount;
    }

    public List<Fish> getFish() {
        return fish;
    }

    public void setFish(List<Fish> fish) {
        this.fish = fish;
    }

    public void setTotalNumberOfFiguresPerAnatomyItem(int totalNumberOfFiguresPerAnatomyItem) {
        this.totalNumberOfFiguresPerAnatomyItem = totalNumberOfFiguresPerAnatomyItem;
    }


    public int getTotalNumberOfFiguresPerAnatomyItem() {
        return totalNumberOfFiguresPerAnatomyItem;
    }

    public int getTotalNumberOfImagesPerAnatomyItem() {
        return totalNumberOfImagesPerAnatomyItem;
    }


    public void setTotalNumberOfImagesPerAnatomyItem(int totalNumberOfImagesPerAnatomyItem) {
        this.totalNumberOfImagesPerAnatomyItem = totalNumberOfImagesPerAnatomyItem;
    }

    public void setGenotypeStatistics(List<FishStatistics> genoStats) {
        this.genoStats = genoStats;
    }


    public List<FishStatistics> getGenotypeStatistics() {
        return genoStats;
    }

    public List<AntibodyStatistics> getAntibodyStatistics() {
        return antibodyStatistics;
    }

    public void setAntibodyStatistics(List<AntibodyStatistics> antibodyStatistics) {
        this.antibodyStatistics = antibodyStatistics;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public boolean isWildCard() {
        if (searchTerm == null)
            return false;
        return (searchTerm.endsWith("*"));
    }

    public static enum Action {
        TERM_SEARCH("term-search"),
        COMPLETE_SEARCH("complete-search"),
        STAGE_SEARCH("term-by-stage-search");

        private final String value;

        private Action(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    public List<RelationshipPresentation> getRelations() {
        Set<String> types = new HashSet<>();
        List<GenericTermRelationship> relatedItems = aoTerm.getAllDirectlyRelatedTerms();
        if (relatedItems != null) {
            for (TermRelationship rel : relatedItems) {
                types.add(rel.getType());
            }
        }
        List<String> uniqueTypes = new ArrayList<>(types);
        Collections.sort(uniqueTypes, new RelationshipSorting());
        return AnatomyPresentation.createRelationshipPresentation(uniqueTypes, aoTerm);

    }

    public int getNumberOfPublications() {
        return numberOfPublications;
    }

    public void setNumberOfPublications(int numberOfPublications) {
        this.numberOfPublications = numberOfPublications;
    }

    public String getCurrentDisplayStageString() {
        return StagePresentation.createDisplayEntry(stage);
    }

    public String getHighlightText() {
        return highlightText;
    }

    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
    }

    public List<Publication> getQualityProbePublications() {
        return qualityProbePublications;
    }

    public void setQualityProbePublications(List<Publication> qualityProbePublications) {
        this.qualityProbePublications = qualityProbePublications;
    }

    /**
     * Create a formatted (comma-delimited list) list for all synonyms
     * for a given term list.
     *
     * @return String
     */
    public String getFormattedSynonymList() {
        return AnatomyPresentation.createFormattedSynonymList(aoTerm);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NEWLINE);
        sb.append("AnatomySearchBean:");
        sb.append(NEWLINE);
        sb.append(statisticItems);
        sb.append(NEWLINE);
        sb.append("id: ");
        sb.append(TAB);
        sb.append(id);
        sb.append(NEWLINE);
        sb.append("Anatomy Item: ");
        sb.append(TAB);
        if (aoTerm == null) {
            sb.append("No Anatomy item");
        } else {
            sb.append(aoTerm.getZdbID());
            sb.append(TAB);
            sb.append(aoTerm.getOboID());
        }


        return sb.toString();
    }

    public List<TermDTO> getTerms() {
        return terms;
    }

    public void setTerms(List<TermDTO> terms) {
        this.terms = terms;
    }

    public List<TermDTO> getOrderedTerms() {
        List<TermDTO> termList = new ArrayList<>(terms);
        Collections.sort(termList);
        return termList;
    }

    public String getWelcomeInputSubject() {
        return aoTerm.getTermName();
    }

    public String getWelcomeInputID() {
        return aoTerm.getZdbID();
    }

    public int getFishCount() {
        return fishCount;
    }

    public void setFishCount(int fishCount) {
        this.fishCount = fishCount;
    }

    public int getWildtypeSTRcount() {
        return wildtypeSTRcount;
    }

    public void setWildtypeSTRcount(int wildtypeSTRcount) {
        this.wildtypeSTRcount = wildtypeSTRcount;
    }

    public int getAntibodyCount() {
        return antibodyCount;
    }

    public void setAntibodyCount(int antibodyCount) {
        this.antibodyCount = antibodyCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMutantSTRcount() {
        return mutantSTRcount;
    }

    public void setMutantSTRcount(int mutantSTRcount) {
        this.mutantSTRcount = mutantSTRcount;
    }

    public int getExpressedGeneCount() {
        return expressedGeneCount;
    }

    public void setExpressedGeneCount(int expressedGeneCount) {
        this.expressedGeneCount = expressedGeneCount;
    }

    public String getExpressionSearchLink(boolean includeSubstructures) {
        URLCreator url = new URLCreator(ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value());
        url.addNameValuePair("MIval", "aa-xpatselect.apg");
        url.addNameValuePair("query_results", "exist");
        url.addNameValuePair("START", "0");
        url.addNameValuePair("TA_selected_structures", getAoTerm().getTermName());
        url.addNameValuePair("xpatsel_processed_selected_structures", getAoTerm().getZdbID());
        url.addNameValuePair("xpatsel_processed_selected_structures_names", getAoTerm().getTermName());
        if (includeSubstructures)
            url.addNameValuePair("include_substructures", "checked");
        url.addNameValuePair("structure_bool", "and");
        url.addNameValuePair("xpatsel_jtypeDirect", "checked");
        url.addNameValuePair("xpatsel_jtypePublished", "checked");
        url.addNameValuePair("WINSIZE", "25");
        url.addNameValuePair("xpatsel_calledBySelf", "true");
        url.addNameValuePair("xpatsel_wtOnly", "checked");
        return url.getURL();
    }

    public String getExpressionSearchLink() {
        return getExpressionSearchLink(false);
    }

    public String getExpressionSearchLinkSubstructures() {
        return getExpressionSearchLink(true);
    }

    public AnatomyStatistics getAnatomyStatisticsMutant() {
        return anatomyStatisticsMutant;
    }

    public void setAnatomyStatisticsMutant(AnatomyStatistics anatomyStatisticsMutant) {
        this.anatomyStatisticsMutant = anatomyStatisticsMutant;
    }

    public AnatomyStatistics getAnatomyStatisticsProbe() {
        return anatomyStatisticsProbe;
    }

    public void setAnatomyStatisticsProbe(AnatomyStatistics anatomyStatisticsProbe) {
        this.anatomyStatisticsProbe = anatomyStatisticsProbe;
    }

    public AnatomyStatistics getAnatomyStatisticsAntibodies() {
        return anatomyStatisticsAntibodies;
    }

    public void setAnatomyStatisticsAntibodies(AnatomyStatistics anatomyStatisticsAntibodies) {
        this.anatomyStatisticsAntibodies = anatomyStatisticsAntibodies;
    }

    public boolean isAllExpressedGenesAreDisplayed() {
        return expressedGeneCount <= MAX_NUMBER_EPRESSED_GENES;
    }

    public boolean isAllGenotypesAreDisplayed() {
        return fishCount <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isAllAntibodiesAreDisplayed() {
        return antibodyCount <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isAllProbesAreDisplayed() {
        return numberOfHighQualityProbes <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isExpressedGenesExist() {
        return !CollectionUtils.isEmpty(allExpressedMarkers);
    }

    public boolean isMutantsExist() {
        return !CollectionUtils.isEmpty(genoStats);
    }

    public boolean isInSituProbesExist() {
        return !CollectionUtils.isEmpty(highQualityProbeGenes);
    }

    public boolean isAntibodiesExist() {
        return !CollectionUtils.isEmpty(antibodyStatistics);
    }

    public SectionVisibility getSectionVisibility() {
        return visibility;
    }

    public void setVisibility(SectionVisibility visibility) {
        this.visibility = visibility;
    }

    public static enum Section {
        EXPRESSION,
        PHENOTYPE;

        public static String[] getValues() {
            String[] values = new String[values().length];
            int index = 0;
            for (Section section : values()) {
                values[index++] = section.toString();
            }
            return values;
        }


    }

}
