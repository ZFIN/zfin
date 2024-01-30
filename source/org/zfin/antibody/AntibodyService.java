package org.zfin.antibody;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.Species;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.*;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.framework.presentation.MatchingTextType;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.util.MatchType;
import org.zfin.util.MatchingService;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Class that contains various methods retrieving aggregated info from
 * antibodies.
 */
public class AntibodyService {

    private final Antibody antibody;
    private AntibodySearchCriteria antibodySearchCriteria;
    private MatchingService matchingService;

    private int numOfLabelings;

    private int numberOfPublications;

    private List<FigureSummaryDisplay> figureSummary;

    public AntibodyService(Antibody antibody) {
        if (antibody == null) {
            throw new RuntimeException("No antibody object provided");
        }
        this.antibody = antibody;
    }

    /**
     * Returns a list of distinct statements that this antibody labels.
     *
     * @return ist of distinct and sorted AO terms.
     */

    public String getRegistryID() {
        Marker abMrkr = getMarkerRepository().getMarkerByID(antibody.getZdbID());
        return getMarkerRepository().getABRegID(abMrkr.zdbID);
    }

    public static void setABRegistryID(Antibody antibody, String newRegistryID) {
        if (getMarkerRepository().getABRegID(antibody.getZdbID()) != null) {
            ReferenceDatabase refDB = getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.ABREGISTRY, ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.Type.ZEBRAFISH);
            MarkerDBLink mdb = getMarkerRepository().getDBLink(antibody, getMarkerRepository().getABRegID(antibody.getZdbID()), refDB);

            if (newRegistryID != null) {


                mdb.setAccessionNumber(newRegistryID);
                mdb.setAccessionNumberDisplay(newRegistryID);

                HibernateUtil.currentSession().save(mdb);
            } else {
                HibernateUtil.currentSession().delete(mdb);
            }
        } else {
            if (newRegistryID != null) {
                ReferenceDatabase refDB = getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.ABREGISTRY, ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.Type.ZEBRAFISH);
                MarkerDBLink mdb = new MarkerDBLink();
                mdb.setMarker(antibody);
                mdb.setAccessionNumber(newRegistryID);
                mdb.setAccessionNumberDisplay(newRegistryID);
                mdb.setReferenceDatabase(refDB);
                HibernateUtil.currentSession().save(mdb);
            }
        }
    }

    public static void setABRegistryIDs(Antibody antibody, String newRegistryIDs) {
        List<String> listOfIDs = List.of(newRegistryIDs.split("\\s*,\\s*"));
        List<String> existingIDs = getMarkerRepository().getABRegIDs(antibody.getZdbID());
        ReferenceDatabase refDB = getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.ABREGISTRY, ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.Type.ZEBRAFISH);

        List<String> toDelete = new ArrayList<>(CollectionUtils.subtract(existingIDs, listOfIDs));
        List<String> toAdd = new ArrayList<>(CollectionUtils.subtract(listOfIDs, existingIDs));

        int count = getMarkerRepository().deleteMarkerDBLinksByIDList(refDB, toDelete);

        for (String newRegistryID : toAdd) {
            MarkerDBLink mdb = new MarkerDBLink();
            mdb.setMarker(antibody);
            mdb.setAccessionNumber(newRegistryID);
            mdb.setAccessionNumberDisplay(newRegistryID);
            mdb.setReferenceDatabase(refDB);
            HibernateUtil.currentSession().save(mdb);
        }
    }

    /**
     * Returns the number of distinct composed terms where a composed term is composition of
     * superterm : subterm.
     * It goes through the list of all labelings for the antibody in question.
     *
     * @return ist of distinct and sorted AO terms.
     */
    public int getNumberOfDistinctComposedTerms() {
        Collection<String> distinctTerms = new HashSet<>();
        Set<ExpressionExperiment2> labelings = antibody.getAntibodyLabelings();
        if (labelings == null) {
            return 0;
        }
        for (ExpressionExperiment2 experiment : labelings) {

            Set<ExpressionResult2> results = getExpressionResult2s(experiment);

            Genotype geno = experiment.getFishExperiment().getFish().getGenotype();
            // need to get a FishExperiment object to check for standard environment; do nothing if not standard
            FishExperiment exp = experiment.getFishExperiment();

            if (!(geno.isWildtype() && exp.isStandardOrGenericControl())) {
                continue;
            }
            if (results != null) {
                for (ExpressionResult2 result : results) {
                    if (!result.isExpressionFound()) {
                        continue;
                    }
                    Term term = result.getSuperTerm();
                    Term subterm = result.getSubTerm();
                    String composedTermName = term.getZdbID();
                    if (subterm != null) {
                        composedTermName += subterm.getZdbID();
                    }
                    distinctTerms.add(composedTermName);
                }
            }
        }
        return distinctTerms.size();
    }

    private static Set<ExpressionResult2> getExpressionResult2s(ExpressionExperiment2 experiment) {
        if (CollectionUtils.isNotEmpty(experiment.getFigureStageSet())) {
            return experiment.getFigureStageSet().stream()
                .map(ExpressionFigureStage::getExpressionResultSet)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Returns a list of distinct GO terms that this antibody labels.
     * Only include wild-type fish and standard condition.
     *
     * @return ist of distinct and sorted GO terms.
     */
    public SortedSet<Term> getDistinctGoTermsWTAndStandard() {
        SortedSet<Term> distinctGoTerms = new TreeSet<>();
        Set<ExpressionExperiment2> labelings = antibody.getAntibodyLabelings();
        if (labelings == null || labelings.isEmpty()) {
            return null;
        }
        for (ExpressionExperiment2 experiment : labelings) {
            Set<ExpressionResult2> results = getExpressionResult2s(experiment);
            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = experiment.getFishExperiment().getFish().getGenotype();
            // need to get a FishExperiment object to check for standard environment; do nothing if not standard
            FishExperiment exp = experiment.getFishExperiment();

            if (!(geno.isWildtype() && exp.isStandardOrGenericControl())) {
                continue;
            }

            if (results != null && !results.isEmpty()) {
                for (ExpressionResult2 result : results) {
                    // only record if expression is found.
                    if (!result.isExpressionFound()) {
                        continue;
                    }

                    Term goTerm = result.getSubTerm();
                    if (goTerm != null && Ontology.isGoOntology(goTerm.getOntology())) {
                        if (!distinctGoTerms.contains(goTerm)) {
                            distinctGoTerms.add(goTerm);
                        }
                    }
                }
            }
        }
        return distinctGoTerms;
    }

    /**
     * Evaluate the earliest start stage among all expression results.
     * Only include wild type fish in standard environment
     *
     * @return start stage
     */
    public DevelopmentStage getEarliestStartStage() {
        DevelopmentStage stage = null;
        Set<ExpressionExperiment2> labelings = antibody.getAntibodyLabelings();
        if (labelings == null) {
            return null;
        }
        for (ExpressionExperiment2 experiment : labelings) {
            Set<ExpressionFigureStage> figureStageSet = experiment.getFigureStageSet();
            Genotype geno = experiment.getFishExperiment().getFish().getGenotype();
            // need to get a FishExperiment object to check for standard environment; do nothing if not standard
            FishExperiment exp = experiment.getFishExperiment();
            if (!(geno.isWildtype() && exp.isStandardOrGenericControl())) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(figureStageSet)) {
                for (ExpressionFigureStage figureStage : figureStageSet) {
                    DevelopmentStage testStage = figureStage.getStartStage();
                    if (stage == null || testStage.earlierThan(stage)) {
                        stage = testStage;
                    }
                }
            }
        }
        return stage;
    }

    /**
     * Evaluate the latest end stage among all expression results.
     * Only include wild type fish in standard environment
     *
     * @return start stage
     */
    public DevelopmentStage getLatestEndStage() {
        DevelopmentStage stage = null;
        Set<ExpressionExperiment2> labelings = antibody.getAntibodyLabelings();
        if (labelings == null) {
            return null;
        }
        for (ExpressionExperiment2 experiment : labelings) {
            Set<ExpressionFigureStage> figureStageSet = experiment.getFigureStageSet();
            Genotype geno = experiment.getFishExperiment().getFish().getGenotype();
            // need to get a FishExperiment object to check for standard environment; do nothing if not standard
            FishExperiment exp = experiment.getFishExperiment();
            if (!(geno.isWildtype() && exp.isStandardOrGenericControl())) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(figureStageSet)) {
                for (ExpressionFigureStage figureStage : figureStageSet) {
                    DevelopmentStage testStage = figureStage.getEndStage();
                    if (stage == null || !testStage.earlierThan(stage)) {
                        stage = testStage;
                    }
                }
            }
        }
        return stage;
    }

    /**
     * This method checks which of the criteria matched the retrieve antibody.
     * Currently, only antibody name and alias as well as Gene abbreviation and previous
     * name are supported.
     *
     * @return matching text collection
     */
    public Set<MatchingText> getMatchingText() {
        if (antibodySearchCriteria == null) {
            return null;
        }
        // return cached version.
        if (matchingService != null) {
            return matchingService.getMatchingTextList();
        }

        matchingService = new MatchingService();
        // check antibody name
        addMatchOnAntibody();

        // check antigen gene abbreviation, name and previous names
        addMatchingAntigenGene();

        // Check anatomy terms
        addMatchOnAnatomyTerm();

        return matchingService.getMatchingTextList();
    }

    private void addMatchOnAnatomyTerm() {
        String[] anatomyTerms = antibodySearchCriteria.getTermIDs();
        // do nothing if no search terms were entered
        if (anatomyTerms == null || anatomyTerms.length == 0) {
            return;
        }

        Set<Term> labelingTerms = getDistinctAoTerms();
        // check for direct matches
        for (String searchTermID : anatomyTerms) {
            if (matchingService.checkExactTermMatches(searchTermID, labelingTerms)) {
                continue;
            }
            matchingService.checkSubstructureTermMatches(searchTermID, labelingTerms);
        }

    }

    private void addMatchingAntigenGene() {
        String antigenNameFilterString = antibodySearchCriteria.getAntigenGeneName();
        if (antigenNameFilterString != null && antigenNameFilterString.trim().length() != 0) {
            List<Marker> genes = antibody.getAllRelatedMarker();
            // the loop exists for the first match as this is enough!
            for (Marker gene : genes) {
                if (!matchingService.addMatchingText(antigenNameFilterString, gene.getAbbreviation(), MatchingTextType.GENE_ABBREVIATION).equals(MatchType.NO_MATCH)) {
                    return;
                }
                if (!matchingService.addMatchingText(antigenNameFilterString, gene.getName(), MatchingTextType.GENE_NAME).equals(MatchType.NO_MATCH)) {
                    return;
                }
                Set<MarkerAlias> prevNames = gene.getAliases();
                if (prevNames != null) {
                    // loop until the first match is encountered
                    for (MarkerAlias prevName : prevNames) {
                        if (!matchingService.addMatchingText(antigenNameFilterString, prevName.getAlias(), MatchingTextType.GENE_ALIAS).equals(MatchType.NO_MATCH)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    protected void addMatchOnAntibody() {

        if (StringUtils.isEmpty(antibodySearchCriteria.getName())) {
            return;
        }

        String antibodyNameFilterString = antibodySearchCriteria.getName().trim();
        if (antibodyNameFilterString != null && antibodyNameFilterString.trim().length() != 0) {
            // if a match was found stop here
            if (!matchingService.addMatchingText(antibodyNameFilterString, antibody.getName(), MatchingTextType.ANTIBODY_NAME).equals(MatchType.NO_MATCH)) {
                return;
            }
            // check for aliases
            Set<MarkerAlias> aliases = antibody.getAliases();
            if (aliases != null) {
                for (MarkerAlias alias : aliases) {
                    if (!matchingService.addMatchingText(antibodyNameFilterString, alias.getAlias(), MatchingTextType.ANTIBODY_ALIAS).equals(MatchType.NO_MATCH)) {
                        return;
                    }
                }
            }
        }
    }

    public Set<Term> getDistinctAoTerms() {
        Set<Term> terms = new HashSet<>();
        Set<ExpressionExperiment2> experiments = antibody.getAntibodyLabelings();
        if (experiments == null) {
            return terms;
        }
        for (ExpressionExperiment2 experiment : experiments) {
            Genotype geno = experiment.getFishExperiment().getFish().getGenotype();

            // need to get an Experiment object to check for standard environment; do nothing if not standard
            FishExperiment exp = experiment.getFishExperiment();

            if (geno.isWildtype() && exp.isStandardOrGenericControl()) {
                Set<ExpressionResult2> results = getExpressionResult2s(experiment);
                if (results != null) {
                    for (ExpressionResult2 result : results) {
                        if (result.isExpressionFound()) {
                            terms.add(result.getSuperTerm());
                            GenericTerm subterm = result.getSubTerm();
                            if (subterm != null && subterm.getOntology().equals(Ontology.ANATOMY)) {
                                terms.add(subterm);
                            }
                        }
                    }
                }
            }
        }
        return terms;
    }

    public SortedSet<String> getDistinctAssayNames() {
        Set<ExpressionExperiment2> antibodyLabelings = antibody.getAntibodyLabelings();
        if (antibodyLabelings == null) {
            return new TreeSet<>();
        }
        SortedSet<String> assayNames = new TreeSet<>();
        for (ExpressionExperiment2 labeling : antibodyLabelings) {
            Set<ExpressionResult2> results = getExpressionResult2s(labeling);
            // exclude those assays with no expression result record
            if (results != null && !results.isEmpty()) {
                String assayName = labeling.getAssay().getName();
                if (assayName != null) {
                    assayNames.add(assayName);
                }
            }
        }
        return assayNames;
    }

    public Set<MarkerRelationship> getSortedAntigenRelationships() {
        Set<MarkerRelationship> relationships = antibody.getSecondMarkerRelationships();
        if (relationships == null) {
            return new TreeSet<>();
        }
        SortedSet<MarkerRelationship> antigenGenes = new TreeSet<>();
        for (MarkerRelationship mrkrRelation : relationships) {
            if (mrkrRelation != null && mrkrRelation.getType() == MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY) {
                antigenGenes.add(mrkrRelation);
            }
        }
        return antigenGenes;
    }

    public void setAntibodySearchCriteria(AntibodySearchCriteria antibodySearchCriteria) {
        this.antibodySearchCriteria = antibodySearchCriteria;
    }

    public Antibody getAntibody() {
        return antibody;
    }


    public List<AnatomyLabel> getAntibodyLabelings() {
        // a map of AOname-CCnames as keys and display objects as values
        Map<String, AnatomyLabel> map = new HashMap<>();

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment2> experiments = antibody.getAntibodyLabelings();

        // loop through the set of ExpressionExperiment objects to get the related data
        if (CollectionUtils.isNotEmpty(experiments)) {
            processExperiments(map, experiments);
        }

        List<AnatomyLabel> labelingDisplays = new ArrayList<>();

        if (map.values().size() > 0) {
            labelingDisplays.addAll(map.values());
        }

        Collections.sort(labelingDisplays);

        setNumOfLabelings(labelingDisplays.size());

        return labelingDisplays;
    }

    /**
     * Retrieve all distinct labeled expression statements
     *
     * @return list of expression statements
     */
    public List<ExpressionStatement> getAntibodyLabelingStatements() {

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment2> experiments = antibody.getAntibodyLabelings();
        List<ExpressionStatement> labelingDisplays = new ArrayList<>();

        // loop through the set of ExpressionExperiment objects to get the related data
        if (CollectionUtils.isNotEmpty(experiments)) {
            labelingDisplays = getDistinctExpressionStatements(experiments);
        }
        Collections.sort(labelingDisplays);
        setNumOfLabelings(labelingDisplays.size());
        return labelingDisplays;
    }

    private List<ExpressionStatement> getDistinctExpressionStatements(Set<ExpressionExperiment2> experiments) {
        if (experiments == null) {
            return null;
        }
        Set<ExpressionStatement> statementSet = new HashSet<>();

        for (ExpressionExperiment2 exp : experiments) {
            Genotype geno = exp.getFishExperiment().getFish().getGenotype();
            if (geno.isWildtype() && exp.getFishExperiment().isStandardOrGenericControl()) {
                Set<ExpressionResult2> results = null;
                if (CollectionUtils.isNotEmpty(exp.getFigureStageSet())) {
                    results = exp.getFigureStageSet().stream()
                        .map(ExpressionFigureStage::getExpressionResultSet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
                }
                if (results != null) {
                    for (ExpressionResult2 result : results) {
                        if (result.isExpressionFound()) {
                            ExpressionStatement statement = new ExpressionStatement();
                            statement.setEntity(result.getEntity());
                            statement.setExpressionFound(result.isExpressionFound());
                            statementSet.add(statement);
                        }
                    }
                }
            }
        }
        List<ExpressionStatement> statements = new ArrayList<>(statementSet.size());
        statements.addAll(statementSet);
        return statements;
    }

    private void processExperiments(Map<String, AnatomyLabel> map, Set<ExpressionExperiment2> experiments) {
        for (ExpressionExperiment2 exp : experiments) {

            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = exp.getFishExperiment().getFish().getGenotype();

            // need to get an Experiment object to check for standard environment; do nothing if not standard
            FishExperiment experiment = exp.getFishExperiment();

            if (geno.isWildtype() && experiment.isStandardOrGenericControl()) {

                // get a set of ExpressionResult objects
                Set<ExpressionResult2> results = getExpressionResult2s(exp);

                // loop through the set of ExpressionResult objects to get the related data
                for (ExpressionResult2 result : results) {
                    if (result.isExpressionFound()) {
                        Term ao = result.getSuperTerm();

                        Term cc = result.getSubTerm();
                        String ccName;
                        if (cc == null) {
                            ccName = "";
                        } else {
                            ccName = cc.getTermName();
                        }

                        // form the key
                        String key = ao.getTermName() + ccName;

                        AnatomyLabel labeling;

                        // if the ao is not a key in the map, instantiate a display object and add it to the map
                        // otherwise, get the display object from the map
                        if (!map.containsKey(key)) {
                            labeling = new AnatomyLabel(result);
                            map.put(key, labeling);
                        } else {
                            labeling = map.get(key);
                        }

                        /* decided not to display stage info in the Labeling section on AB details page
                    DevelopmentStage startStage = result.getStartStage();
                    DevelopmentStage AOstartSt = labeling.getStartStage();
                    // calculate and set the start stage
                    if (AOstartSt == null || startStage.earlierThan(AOstartSt))
                        labeling.setStartStage(startStage);

                    DevelopmentStage endStage = result.getEndStage();
                    DevelopmentStage AOendSt = labeling.getEndStage();
                    // calculate and set the end stage
                    if (AOendSt == null || AOendSt.earlierThan(endStage))
                        labeling.setEndStage(endStage);
                        */

                        Figure figure = result.getExpressionFigureStage().getFigure();

                        if (figure != null) {
                            labeling.getFigures().add(figure);
                            // if there is one figure with image, set the flag
                            if (figure.getImages() != null && figure.getImages().size() > 0) {
                                labeling.setFigureWithImage(true);
                                break;
                            }
                        }

                        Publication pub = exp.getPublication();
                        if (pub != null) {
                            labeling.getPublications().add(pub);
                        }

                        Set<Figure> allFigures = labeling.getFigures();
                        for (Figure fig : allFigures) {
                            if (fig.getType() == FigureType.FIGURE) {
                                labeling.setNotAllFiguresTextOnly(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public List<AnatomyLabel> getAntibodyDetailedLabelings() {
        // a map of AOname-CCname-startStageName-EndStageNames as keys and display objects as values
        Map<String, AnatomyLabel> map = new HashMap<>();

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment2> experiments = antibody.getAntibodyLabelings();

        // loop thru the set of ExpressionExperiment objects to get the related data
        for (ExpressionExperiment2 exp : experiments) {
            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = exp.getFishExperiment().getFish().getGenotype();

            if (geno.isWildtype() && exp.getFishExperiment().isStandardOrGenericControl()) {
                ExpressionAssay assay = exp.getAssay();
                Marker gene = exp.getGene();

                // get a set of ExpressionResult objects
                Set<ExpressionResult2> results = getExpressionResult2s(exp);

                // loop through the set of ExpressionResult objects to get the related data
                for (ExpressionResult2 result : results) {
                    if (result.isExpressionFound()) {

                        Term subterm = result.getSubTerm();
                        Term superterm = result.getSuperTerm();

                        DevelopmentStage startStage = result.getExpressionFigureStage().getStartStage();
                        String startStageName;

                        if (startStage == null) {
                            startStageName = "";
                        } else {
                            startStageName = startStage.getName();
                        }

                        DevelopmentStage endStage = result.getExpressionFigureStage().getEndStage();
                        String endStageName;
                        if (endStage == null) {
                            endStageName = "";
                        } else {
                            endStageName = endStage.getName();
                        }

                        // form the key
                        String key = superterm.getZdbID() + startStageName + endStageName;
                        if (subterm != null) {
                            key += subterm.getZdbID();
                        }

                        AnatomyLabel labeling;

                        // if the key is not in the map, instantiate a display (AnatomyLabel) object and add it to the map
                        // otherwise, just get the display object from the map
                        if (!map.containsKey(key)) {
                            labeling = new AnatomyLabel(result);
                            map.put(key, labeling);
                        } else {
                            labeling = map.get(key);
                        }

                        if (labeling.getAssays() == null) {
                            SortedSet<ExpressionAssay> assays = new TreeSet<>();
                            labeling.setAssays(assays);
                        }

                        if (assay != null) {
                            labeling.getAssays().add(assay);
                        }

                        if (labeling.getGenes() == null) {
                            SortedSet<Marker> genes = new TreeSet<>();
                            labeling.setGenes(genes);
                        }

                        if (gene != null) {
                            labeling.getGenes().add(gene);
                        }

                        // get the figures associated
                        Figure figure = result.getExpressionFigureStage().getFigure();

                        if (figure != null) {
                            labeling.getFigures().add(figure);
                        }

                        Publication pub = exp.getPublication();
                        if (pub != null) {
                            labeling.getPublications().add(pub);
                        }

                        Set<Figure> allFigures = labeling.getFigures();
                        for (Figure fig : allFigures) {
                            if (fig.getType() == FigureType.FIGURE) {
                                labeling.setNotAllFiguresTextOnly(true);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // use SortedSet to hold the values of the map so that the data could be displayed in order
        List<AnatomyLabel> labelingDisplays = new ArrayList<>();

        if (map.values().size() > 0) {
            labelingDisplays.addAll(map.values());
        }
        ////ToDo
        Collections.sort(labelingDisplays, new AntibodyLabelingDetailComparator());

        return labelingDisplays;
    }

    public ExpressionSummaryCriteria createExpressionSummaryCriteria(GenericTerm superterm, GenericTerm subterm, DevelopmentStage startStage, DevelopmentStage endStage, boolean withImgOnly) {
        ExpressionSummaryCriteria criteria = new ExpressionSummaryCriteria();

        //set the antibody
        criteria.setAntibody(antibody);

        //values used when called from the antibody page
        if (subterm != null) {
            PostComposedEntity entity = new PostComposedEntity();
            entity.setSuperterm(superterm);
            criteria.setEntity(entity);
            entity.setSubterm(subterm);
        } else {
            // assume we should be looking into super and sub term column
            criteria.setSingleTermEitherPosition(superterm);
        }

        if (startStage != null)
            criteria.setStart(startStage);
        if (endStage != null)
            criteria.setEnd(endStage);
        criteria.setWithImagesOnly(withImgOnly);

        //set the "assumed values"
        criteria.setWildtypeOnly(true);
        criteria.setStandardEnvironment(true);

        return criteria;
    }


    public void createFigureSummary(ExpressionSummaryCriteria criteria) {
        Set<Publication> publications = new HashSet<>();

        List<FigureSummaryDisplay> summaryRows = FigureService.createExpressionFigureSummary(criteria);

        for (FigureSummaryDisplay summaryRow : summaryRows) {
            publications.add(summaryRow.getPublication());
        }

        setNumberOfPublications(publications.size());
        setFigureSummary(summaryRows);
    }


    public int getNumOfLabelings() {
        return numOfLabelings;
    }

    public void setNumOfLabelings(int numOfLabelings) {
        this.numOfLabelings = numOfLabelings;
    }


    public String getNumberOfFiguresDisplay() {
        int numberOfFigures = figureSummary.size();
        return numberOfFigures + " " + AnatomyLabel.figureChoice.format(numberOfFigures);
    }

    public String getNumberOfPublicationsDisplay() {
        return numberOfPublications + " " + AnatomyLabel.publicationChoice.format(numberOfPublications);
    }

    public int getNumberOfPublications() {
        return numberOfPublications;
    }

    public void setNumberOfPublications(int numberOfPublications) {
        this.numberOfPublications = numberOfPublications;
    }

    public List<FigureSummaryDisplay> getFigureSummary() {
        return figureSummary;
    }

    public void setFigureSummary(List<FigureSummaryDisplay> figureSummary) {
        this.figureSummary = figureSummary;
    }

    /**
     * Add alias attribute. Same logic as MarkerRPCServiceImpl::addDataAliasRelatedEntity
     */
    public static DataAlias addDataAliasRelatedEntity(String markerID, String aliasName, String publicationID) {
        MarkerRepository markerRepository = getMarkerRepository();
        Marker marker = markerRepository.getMarkerByID(markerID);
        Publication publication = getPublicationRepository().getPublication(publicationID);

        DataAlias dataAlias = markerRepository.getSpecificDataAlias(marker, aliasName);
        if (dataAlias == null) {
            return markerRepository.addMarkerAlias(marker, aliasName, publication);

        } else if (publication == null) {
            //nothing to do. the alias already exists and we have no publication to attribute
        } else if (!publication.equals(dataAlias.getSinglePublication())) {
            //the alias already exists, but the attribution doesn't match our pub, so we add an attribution
            markerRepository.addDataAliasAttribution(dataAlias, publication, marker);
        }
        return dataAlias;
    }

    /**
     * For removing an alias name/publication from an antibody.  If the pair is unique, we remove the alias completely
     * If there are multiple publications for that alias for the antibody, we simply remove the attribution.
     *
     * @param alias
     * @param publicationId
     */
    public static void removeDataAliasAttributionAndAliasIfUnique(MarkerAlias alias, String publicationId) {
        List<RecordAttribution> attributions = getInfrastructureRepository().getRecordAttributions(alias.getZdbID());

        if (attributions.size() == 1 && StringUtils.equals(attributions.get(0).getSourceZdbID(), publicationId)) {
            removeDataAliasRelatedEntity(alias, publicationId);
        } else if (attributions.size() > 1) {
            removeDataAliasAttribution(alias, publicationId);
        }

    }

    /**
     * Delete a dataAlias
     * Same logic as MarkerRPCServiceImpl::removeDataAliasRelatedEntity
     */
    public static void removeDataAliasRelatedEntity(MarkerAlias alias, String publicationId) {
        Publication publication = null;
        if (StringUtils.isNotEmpty(publicationId)) {
            publication = RepositoryFactory.getPublicationRepository().getPublication(publicationId);
        }
        InfrastructureService.insertUpdate(alias.getMarker(), "Removed alias: " + alias.getAlias() + "' attributed to publication: '"
                                                              + (publication == null ? "null" : publication.getZdbID()) + "'");
        getMarkerRepository().deleteMarkerAlias(alias.getMarker(), alias);
    }

    public static void removeDataAliasAttribution(MarkerAlias alias, String publicationId) {
        getInfrastructureRepository().deleteRecordAttribution(alias.getZdbID(), publicationId);
        InfrastructureService.insertUpdate(alias.getMarker(), "Removed attribution: '" + publicationId + "' from alias: '" + alias.getAlias() + "'");
    }


}
