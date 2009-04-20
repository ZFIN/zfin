package org.zfin.anatomy.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.framework.presentation.SectionVisibility;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.presentation.MorpholinoStatistics;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
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

    private List stages;
    private List<AnatomyItem> anatomyItems;
    private AnatomyItem anatomyItem;
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
    private int numberOfHighQualityProbes;
    private int markerCount;
    private SectionVisibility visibility = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);

    private List<Genotype> genotypes;
    private int genotypeCount;
    private int antibodyCount;
    private int expressedGeneCount;
    private int wildtypeMorpholinoCount;
    private int mutantMorpholinoCount;
    private int totalNumberOfFiguresPerAnatomyItem;
    private int totalNumberOfImagesPerAnatomyItem;
    private List<GenotypeStatistics> genoStats;
    private List<MorpholinoStatistics> allMorpholinos;
    private List<AntibodyStatistics> antibodyStatistics;
    private List<MorpholinoStatistics> nonWildtypeMorpholinos;
    private boolean wildtype;

    public List getStages() {
        return stages;
    }

    public void setStages(List stages) {
        this.stages = stages;
    }

    public Map<String, String> getDisplayStages() {
        Map<String, String> entries = new LinkedHashMap<String, String>();
        for (Object stage1 : stages) {
            DevelopmentStage stage = (DevelopmentStage) stage1;
            String labelString = StagePresentation.createDisplayEntry(stage);
            entries.put(stage.getZdbID(), labelString);
        }
        return entries;
    }

    public DevelopmentStage getStage() {
        if (stage == null) {
            stage = new DevelopmentStage();
        }
        return stage;
    }

    public void setStage(DevelopmentStage stage) {
        this.stage = stage;
    }

    public void setAnatomyItems(List<AnatomyItem> anatomyItems) {
        this.anatomyItems = anatomyItems;
    }

    public List<AnatomyItem> getAnatomyItems() {
        return anatomyItems;
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

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public AnatomyItem getAnatomyItem() {
        if (anatomyItem == null) {
            anatomyItem = new AnatomyItem();
        }
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }

    public void setAnatomyNamesAndSynonyms(List<String> names) {
        anatomyNamesAndSynonyms = names;
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
     * then the terms that contain the serach term. If no term is specified do not sort at all.
     *
     * @return list of anatomy statistics
     */
    public List<AnatomyStatistics> getSortedStatisticsItems() {
        if (searchTerm != null && !isStageSearch())
            Collections.sort(statisticItems, new SortAnatomyResults(searchTerm));
        return statisticItems;
    }

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(anatomyItem.getZdbID());
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

    public void setGenotypes(List<Genotype> genotypes) {
        this.genotypes = genotypes;
    }

    public List<Genotype> getGenotypes() {
        return genotypes;
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

    public void setGenotypeStatistics(List<GenotypeStatistics> genoStats) {
        this.genoStats = genoStats;
    }


    public List<GenotypeStatistics> getGenotypeStatistics() {
        return genoStats;
    }

    public void setAllMorpholinos(List<MorpholinoStatistics> morphs) {
        allMorpholinos = morphs;
    }


    public List<MorpholinoStatistics> getAllMorpholinos() {
        return allMorpholinos;
    }

    public List<AntibodyStatistics> getAntibodyStatistics() {
        return antibodyStatistics;
    }

    public void setAntibodyStatistics(List<AntibodyStatistics> antibodyStatistics) {
        this.antibodyStatistics = antibodyStatistics;
    }

    public List<MorpholinoStatistics> getNonWildtypeMorpholinos() {
        return nonWildtypeMorpholinos;
    }

    public void setNonWildtypeMorpholinos(List<MorpholinoStatistics> nonWildtypeMorpholinos) {
        this.nonWildtypeMorpholinos = nonWildtypeMorpholinos;
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
        Set<String> types = new HashSet<String>();
        List<AnatomyRelationship> relatedItems = anatomyItem.getRelatedItems();
        if (relatedItems != null) {
            for (AnatomyRelationship rel : relatedItems) {
                types.add(rel.getRelationship());
            }
        }
        List<String> uniqueTypes = new ArrayList<String>(types);
        Collections.sort(uniqueTypes, new RelationshipSorting());
        return AnatomyPresentation.createRelationshipPresentation(uniqueTypes, anatomyItem);

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
     * for a given AnatomyItem.
     *
     * @return String
     */
    public String getFormattedSynonymList() {
        return AnatomyPresentation.createFormattedSynonymList(anatomyItem);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NEWLINE);
        sb.append("AnatomySearchBean:");
        sb.append(NEWLINE);
        sb.append(statisticItems);

        return sb.toString();
    }

    public String getWelcomeInputSubject() {
        return anatomyItem.getName();
    }

    public String getWelcomeInputID() {
        return anatomyItem.getZdbID();
    }

    public int getGenotypeCount() {
        return genotypeCount;
    }

    public void setGenotypeCount(int genotypeCount) {
        this.genotypeCount = genotypeCount;
    }

    public int getWildtypeMorpholinoCount() {
        return wildtypeMorpholinoCount;
    }

    public void setWildtypeMorpholinoCount(int wildtypeMorpholinoCount) {
        this.wildtypeMorpholinoCount = wildtypeMorpholinoCount;
    }

    public int getAntibodyCount() {
        return antibodyCount;
    }

    public void setAntibodyCount(int antibodyCount) {
        this.antibodyCount = antibodyCount;
    }

    public int getMutantMorpholinoCount() {
        return mutantMorpholinoCount;
    }

    public void setMutantMorpholinoCount(int mutantMorpholinoCount) {
        this.mutantMorpholinoCount = mutantMorpholinoCount;
    }

    public int getExpressedGeneCount() {
        return expressedGeneCount;
    }

    public void setExpressedGeneCount(int expressedGeneCount) {
        this.expressedGeneCount = expressedGeneCount;
    }

    public String getExpressionSearchLink(boolean includeSubstructures) {
        URLCreator url = new URLCreator(ZfinProperties.getWebDriver());
        url.addNamevaluePair("MIval", "aa-xpatselect.apg");
        url.addNamevaluePair("query_results", "exist");
        url.addNamevaluePair("START", "0");
        url.addNamevaluePair("TA_selected_structures", getAnatomyItem().getName());
        url.addNamevaluePair("xpatsel_processed_selected_structures", getAnatomyItem().getName());
        if (includeSubstructures)
            url.addNamevaluePair("include_substructures", "checked");
        url.addNamevaluePair("structure_bool", "and");
        url.addNamevaluePair("xpatsel_jtypeDirect", "checked");
        url.addNamevaluePair("xpatsel_jtypePublished", "checked");
        url.addNamevaluePair("WINSIZE", "25");
        url.addNamevaluePair("xpatsel_calledBySelf", "true");
        url.addNamevaluePair("xpatsel_wtOnly", "checked");
        return url.getFullURL();
    }

    public String getMutantSearchLink(boolean includeSubstructures) {
        URLCreator url = new URLCreator(ZfinProperties.getWebDriver());
        url.addNamevaluePair("MIval", "aa-fishselect.apg");
        url.addNamevaluePair("query_results", "exist");
        url.addNamevaluePair("START", "1");
        url.addNamevaluePair("TA_selected_structures", getAnatomyItem().getName());
        url.addNamevaluePair("fsel_processed_selected_structures", getAnatomyItem().getName());
        if (includeSubstructures)
            url.addNamevaluePair("include_substructures", "checked");
        url.addNamevaluePair("structure_bool", "and");
        url.addNamevaluePair("mutagen", "any");
        url.addNamevaluePair("lg", "0");
        url.addNamevaluePair("WINSIZE", "20");
        url.addNamevaluePair("fishsel_calledBySelf", "true");
        url.addNamevaluePair("fselFilterValue", "all");
        url.addNamevaluePair("chrom_change", "any");
        url.addNamevaluePair("search", "SEARCH");
        url.addNamevaluePair("fsel_inputname", "");
        url.addNamevaluePair("compare", "contains");
        return url.getFullURL();
    }

    public boolean isWildtype() {
        return wildtype;
    }

    public void setWildtype(boolean wildtype) {
        this.wildtype = wildtype;
    }

    public String getExpressionSearchLink() {
        return getExpressionSearchLink(false);
    }

    public String getExpressionSearchLinkSubstructures() {
        return getExpressionSearchLink(true);
    }

    public String getMutantSearchLinkSubstructures() {
        return getMutantSearchLink(true);
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
        return genotypeCount <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isAllAntibodiesAreDisplayed() {
        return antibodyCount <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isAllProbesAreDisplayed() {
        return numberOfHighQualityProbes <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isAllWildtypeMorpholinosAreDisplayed() {
        return wildtypeMorpholinoCount <= MAX_NUMBER_GENOTYPES;
    }

    public boolean isAllMutantMorpholinosAreDisplayed() {
        return mutantMorpholinoCount <= MAX_NUMBER_GENOTYPES;
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

    public boolean isMorpholinoExist() {
        return !CollectionUtils.isEmpty(allMorpholinos);
    }

    public boolean isAntibodiesExist() {
        return !CollectionUtils.isEmpty(antibodyStatistics);
    }

    public boolean isNonWildtypeMorpholinoExist() {
        return !CollectionUtils.isEmpty(nonWildtypeMorpholinos);
    }

    public SectionVisibility getSectionVisibility() {
        return visibility;
    }

    public void setVisibility(SectionVisibility visibility) {
        this.visibility = visibility;
    }

    public static enum Section {
        ANATOMY_EXPRESSION,
        ANATOMY_PHENOTYPE;

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
