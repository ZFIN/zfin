package org.zfin.antibody;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.*;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.*;

/**
 * Class that contains various methods retrieving aggregated info from
 * antibodies.
 */
public class AntibodyService {

    private Antibody antibody;
    private AntibodySearchCriteria antibodySearchCriteria;

    private int numOfLabelings;

    private int numberOfPublications;

    private List<FigureSummaryDisplay> figureSummary;

    public AntibodyService(Antibody antibody) {
        if (antibody == null)
            throw new RuntimeException("No antibody object provided");
        this.antibody = antibody;
    }

    /**
     * Returns a list of distinct anatomy terms that this antibody labels.
     *
     * @return ist of distinct and sorted AO terms.
     */
    public List<Term> getDistinctAnatomyTerms() {
        List<Term> distinctAoTerms = new ArrayList<Term>();
        Set<ExpressionExperiment> labelings = antibody.getAntibodyLabelings();
        if (labelings == null)
            return null;
        for (ExpressionExperiment experiment : labelings) {
            Set<ExpressionResult> results = experiment.getExpressionResults();
            Genotype geno = experiment.getGenotypeExperiment().getGenotype();
            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment exp = experiment.getGenotypeExperiment().getExperiment();

            if (!(geno.isWildtype() && exp.isStandard()))
                continue;
            if (results != null) {
                for (ExpressionResult result : results) {
                    if (!result.isExpressionFound())
                        continue;
                    Term term = result.getSuperterm();
                    if (!distinctAoTerms.contains(term))
                        distinctAoTerms.add(term);
                    // add the secondary term if available
                    Term subterm = result.getSubterm();
                    if (subterm != null)
                        if (!distinctAoTerms.contains(subterm))
                            distinctAoTerms.add(subterm);
                }
            }
        }
        Collections.sort(distinctAoTerms);
        return distinctAoTerms;
    }

    /**
     * Returns the number of distinct composed terms where a composed term is composition of
     * superterm : subterm.
     * It goes through the list of all labelings for the antibody in question.
     *
     * @return ist of distinct and sorted AO terms.
     */
    public int getNumberOfDistinctComposedTerms() {
        Collection<String> distinctTerms = new HashSet<String>();
        Set<ExpressionExperiment> labelings = antibody.getAntibodyLabelings();
        if (labelings == null)
            return 0;
        for (ExpressionExperiment experiment : labelings) {
            Set<ExpressionResult> results = experiment.getExpressionResults();
            Genotype geno = experiment.getGenotypeExperiment().getGenotype();
            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment exp = experiment.getGenotypeExperiment().getExperiment();

            if (!(geno.isWildtype() && exp.isStandard()))
                continue;
            if (results != null) {
                for (ExpressionResult result : results) {
                    if (!result.isExpressionFound())
                        continue;
                    Term term = result.getSuperterm();
                    Term subterm = result.getSubterm();
                    String composedTermName = term.getID();
                    if (subterm != null)
                        composedTermName += subterm.getID();
                    distinctTerms.add(composedTermName);
                }
            }
        }
        return distinctTerms.size();
    }

    /**
     * Returns a list of distinct GO terms that this antibody labels.
     * Only include wild-type fish and standard condition.
     *
     * @return ist of distinct and sorted GO terms.
     */
    public SortedSet<Term> getDistinctGoTermsWTAndStandard() {
        SortedSet<Term> distinctGoTerms = new TreeSet<Term>();
        Set<ExpressionExperiment> labelings = antibody.getAntibodyLabelings();
        if (labelings == null || labelings.isEmpty())
            return null;
        for (ExpressionExperiment experiment : labelings) {
            Set<ExpressionResult> results = experiment.getExpressionResults();
            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = experiment.getGenotypeExperiment().getGenotype();
            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment exp = experiment.getGenotypeExperiment().getExperiment();

            if (!(geno.isWildtype() && exp.isStandard()))
                continue;

            if (results != null && !results.isEmpty()) {
                for (ExpressionResult result : results) {
                    // only record if expression is found.
                    if (!result.isExpressionFound())
                        continue;

                    Term goTerm = result.getSubterm();
                    if (goTerm != null && Ontology.isGoOntology(goTerm.getOntology())) {
                        if (!distinctGoTerms.contains(goTerm))
                            distinctGoTerms.add(goTerm);
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
        Set<ExpressionExperiment> labelings = antibody.getAntibodyLabelings();
        if (labelings == null)
            return null;
        for (ExpressionExperiment experiment : labelings) {
            Set<ExpressionResult> results = experiment.getExpressionResults();
            Genotype geno = experiment.getGenotypeExperiment().getGenotype();
            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment exp = experiment.getGenotypeExperiment().getExperiment();
            if (!(geno.isWildtype() && exp.isStandard()))
                continue;
            if (results != null) {
                for (ExpressionResult result : results) {
                    DevelopmentStage testStage = result.getStartStage();
                    if (stage == null || testStage.earlierThan(stage))
                        stage = testStage;
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
        Set<ExpressionExperiment> labelings = antibody.getAntibodyLabelings();
        if (labelings == null)
            return null;
        for (ExpressionExperiment experiment : labelings) {
            Set<ExpressionResult> results = experiment.getExpressionResults();
            Genotype geno = experiment.getGenotypeExperiment().getGenotype();
            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment exp = experiment.getGenotypeExperiment().getExperiment();
            if (!(geno.isWildtype() && exp.isStandard()))
                continue;
            if (results != null) {
                for (ExpressionResult result : results) {
                    DevelopmentStage testStage = result.getEndStage();
                    if (stage == null || !testStage.earlierThan(stage))
                        stage = testStage;
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
    public List<MatchingText> getMatchingText() {
        if (antibodySearchCriteria == null)
            return null;
        List<MatchingText> matchingTexts = new ArrayList<MatchingText>();

        // check antibody name
        addMatchingAntibodyName(matchingTexts);

        // check antigen gene abbreviation, name and previous names
        addMatchingAntigenGene(matchingTexts);

        // Check anatomy terms
        addMatchingAnatomyTerms(matchingTexts);

        return matchingTexts;
    }

    private void addMatchingAnatomyTerms(List<MatchingText> matchingTexts) {
        String[] anatomyTerms = antibodySearchCriteria.getTermIDs();
        // do nothing if no search terms were entered
        if (anatomyTerms == null || anatomyTerms.length == 0)
            return;

        Set<Term> labelingTerms = getDistinctAoTerms();
        // check for direct matches
        MatchingText match = new MatchingText(MatchingText.Type.AO_TERM);
        List<String> directMatchesFound = new ArrayList<String>();
        for (String searchTermID : anatomyTerms) {
            Term item = OntologyManager.getInstance().getTermByID(Ontology.ANATOMY, searchTermID);
            String termName = item.getTermName();
            match.addMatchedString(termName);
            for (Term label : labelingTerms) {
                if (label.getID().equals(item.getID())) {
                    match.addMatchingTerm(label.getTermName());
                    directMatchesFound.add(termName);
                }
            }
        }
        if (match.getMatchingTerms() != null)
            matchingTexts.add(match);
        // if all terms are matched in a 'every term' filter do not proceed with substructure matching
        if (antibodySearchCriteria.isAnatomyEveryTerm() && directMatchesFound.size() == anatomyTerms.length)
            return;
        // if at least one term is found in a 'any term' filter do not proceed with substructure matching.
        if (!antibodySearchCriteria.isAnatomyEveryTerm() && directMatchesFound.size() >= 1)
            return;


        if (match.getMatchingTerms() == null || (match.getMatchingTerms() != null && match.getMatchingTerms().size() < anatomyTerms.length)) {
            match = new MatchingText(MatchingText.Type.AO_TERM);

            // check for substructure matches
            for (String searchTermID : anatomyTerms) {
                // omit terms that were already matched
                if (directMatchesFound.contains(searchTermID))
                    continue;
                Term item = OntologyManager.getInstance().getTermByID(Ontology.ANATOMY, searchTermID);
                String termName = item.getTermName();
                match.addMatchedString(termName);
                for (Term label : labelingTerms) {
                    if (OntologyManager.getInstance().isSubstructureOf(label, item)) {
                        match.setAppendix("(" + label.getTermName() + ") ");
                        match.addMatchingTerm(termName);
                        break;
                        //match.setAppendix(searchTermName);
                    }
                }
            }
            matchingTexts.add(match);
        }
    }

    private void addMatchingAntigenGene(List<MatchingText> matchingTexts) {
        String antigenNameFilterString = antibodySearchCriteria.getAntigenGeneName();
        if (antigenNameFilterString != null && antigenNameFilterString.trim().length() != 0) {
            List<Marker> genes = antibody.getAllRelatedMarker();
            // the loop exists for the first match as this is enough!
            for (Marker gene : genes) {
                if (gene.getAbbreviation().indexOf(antigenNameFilterString.toLowerCase().trim()) > -1) {
                    MatchingText match = new MatchingText(MatchingText.Type.GENE_NAME);
                    match.addMatchedString(antigenNameFilterString);
                    match.addMatchingTerm(gene.getAbbreviation());
                    matchingTexts.add(match);
                    break;
                }
                if (gene.getName().indexOf(antigenNameFilterString.toLowerCase().trim()) > -1) {
                    MatchingText match = new MatchingText(MatchingText.Type.GENE_NAME);
                    match.addMatchedString(antigenNameFilterString);
                    match.addMatchingTerm(gene.getName());
                    matchingTexts.add(match);
                    break;
                }
                Set<MarkerAlias> prevNames = gene.getAliases();
                MatchingText match = new MatchingText(MatchingText.Type.GENE_ALIAS);
                if (prevNames != null) {
                    // loop until the first match is encountered
                    for (MarkerAlias prevName : prevNames) {
                        if (prevName.getAlias().indexOf(antigenNameFilterString.toLowerCase().trim()) > -1) {
                            match.addMatchedString(antigenNameFilterString);
                            match.addMatchingTerm(prevName.getAlias());
                            matchingTexts.add(match);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void addMatchingAntibodyName(List<MatchingText> matchingTexts) {

        if (StringUtils.isEmpty(antibodySearchCriteria.getName()))
            return;

        String antibodyNameFilterString = antibodySearchCriteria.getName().trim();
        if (antibodyNameFilterString != null && antibodyNameFilterString.trim().length() != 0) {
            String antibodyName = antibody.getName();
            boolean hasNameMatch = false;
            if (antibodyName.toLowerCase().indexOf(antibodyNameFilterString.toLowerCase()) > -1) {
                MatchingText match = new MatchingText(MatchingText.Type.ANTIBODY_NAME);
                match.addMatchedString(antibodyNameFilterString);
                match.addMatchingTerm(antibody.getName());
                matchingTexts.add(match);
                hasNameMatch = true;
            }
            if (!hasNameMatch) {
                Set<MarkerAlias> aliases = antibody.getAliases();
                if (aliases != null) {
                    MatchingText match = new MatchingText(MatchingText.Type.ANTIBODY_ALIAS);
                    for (MarkerAlias alias : aliases) {
                        if (alias.getAlias().toLowerCase().indexOf(antibodyNameFilterString.toLowerCase()) > -1) {
                            match.addMatchedString(antibodyNameFilterString);
                            match.addMatchingTerm(alias.getAlias());
                        }
                    }
                    if (match.getMatchingTerms() != null)
                        matchingTexts.add(match);
                }
            }
        }
    }

    public Set<Term> getDistinctAoTerms() {
        Set<Term> terms = new HashSet<Term>();
        Set<ExpressionExperiment> experiments = antibody.getAntibodyLabelings();
        if (experiments == null)
            return terms;
        for (ExpressionExperiment experiment : experiments) {
            Genotype geno = experiment.getGenotypeExperiment().getGenotype();

            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment exp = experiment.getGenotypeExperiment().getExperiment();

            if (geno.isWildtype() && exp.isStandard()) {
                Set<ExpressionResult> results = experiment.getExpressionResults();
                if (results != null) {
                    for (ExpressionResult result : results) {
                        if (result.isExpressionFound()) {
                            terms.add(result.getSuperterm());
                            Term subterm = result.getSubterm();
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
        Set<ExpressionExperiment> antibodyLabelings = antibody.getAntibodyLabelings();
        if (antibodyLabelings == null) {
            return new TreeSet<String>();
        }
        SortedSet<String> assayNames = new TreeSet<String>();
        for (ExpressionExperiment labeling : antibodyLabelings) {
            Set<ExpressionResult> results = labeling.getExpressionResults();
            // exclude those assays with no expression result record
            if (results != null && !results.isEmpty()) {
                String assayName = labeling.getAssay().getName();
                if (assayName != null)
                    assayNames.add(assayName);
            }
        }
        return assayNames;
    }

    public SortedSet<MarkerRelationship> getSortedAntigenRelationships() {
        Set<MarkerRelationship> relationships = antibody.getSecondMarkerRelationships();
        if (relationships == null) {
            return new TreeSet<MarkerRelationship>();
        }
        SortedSet<MarkerRelationship> antigenGenes = new TreeSet<MarkerRelationship>();
        for (MarkerRelationship mrkrRelation : relationships) {
            if (mrkrRelation != null && mrkrRelation.getType() == MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY)
                antigenGenes.add(mrkrRelation);
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
        Map<String, AnatomyLabel> map = new HashMap<String, AnatomyLabel>();

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment> experiments = antibody.getAntibodyLabelings();

        // loop through the set of ExpressionExperiment objects to get the related data
        if (CollectionUtils.isNotEmpty(experiments)) {
            processExperiments(map, experiments);
        }

        List<AnatomyLabel> labelingDisplays = new ArrayList<AnatomyLabel>();

        if (map.values().size() > 0)
            labelingDisplays.addAll(map.values());

        Collections.sort(labelingDisplays);

        setNumOfLabelings(labelingDisplays.size());

        return labelingDisplays;
    }

    private void processExperiments(Map<String, AnatomyLabel> map, Set<ExpressionExperiment> experiments) {
        for (ExpressionExperiment exp : experiments) {

            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = exp.getGenotypeExperiment().getGenotype();

            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment experiment = exp.getGenotypeExperiment().getExperiment();

            if (geno.isWildtype() && experiment.isStandard()) {

                // get a set of ExpressionResult objects
                Set<ExpressionResult> results = exp.getExpressionResults();

                // loop thru the set of ExpressionResult objects to get the related data
                for (ExpressionResult result : results) {
                    if (result.isExpressionFound()) {
                        Term ao = result.getSuperterm();

                        Term cc = result.getSubterm();
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
                            labeling = new AnatomyLabel(ao, cc, null, null);
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

                        Set<Figure> figures = result.getFigures();

                        if (figures != null && !figures.isEmpty()) {
                            labeling.getFigures().addAll(figures);
                            // if there is one figure with image, set the flag
                            for (Figure fig : figures) {
                                if (fig.getImages() != null && fig.getImages().size() > 0) {
                                    labeling.setFigureWithImage(true);
                                    break;
                                }
                            }
                        }

                        Publication pub = exp.getPublication();
                        if (pub != null)
                            labeling.getPublications().add(pub);

                        Set<Figure> allFigures = labeling.getFigures();
                        for (Figure fig : allFigures) {
                            if (fig.getType() == Figure.Type.FIGURE) {
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
        Map<String, AnatomyLabel> map = new HashMap<String, AnatomyLabel>();

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment> experiments = antibody.getAntibodyLabelings();

        // loop thru the set of ExpressionExperiment objects to get the related data
        for (ExpressionExperiment exp : experiments) {
            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = exp.getGenotypeExperiment().getGenotype();

            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment experiment = exp.getGenotypeExperiment().getExperiment();

            if (geno.isWildtype() && experiment.isStandard()) {
                ExpressionAssay assay = exp.getAssay();
                Marker gene = exp.getGene();

                // get a set of ExpressionResult objects
                Set<ExpressionResult> results = exp.getExpressionResults();

                // loop through the set of ExpressionResult objects to get the related data
                for (ExpressionResult result : results) {
                    if (result.isExpressionFound()) {

                        Term subterm = result.getSubterm();
                        Term superterm = result.getSuperterm();

                        DevelopmentStage startStage = result.getStartStage();
                        String startStageName;

                        if (startStage == null)
                            startStageName = "";
                        else
                            startStageName = startStage.getName();

                        DevelopmentStage endStage = result.getEndStage();
                        String endStageName;
                        if (endStage == null)
                            endStageName = "";
                        else
                            endStageName = endStage.getName();

                        // form the key
                        String key = superterm.getID() + startStageName + endStageName;
                        if (subterm != null)
                            key += subterm.getID();

                        AnatomyLabel labeling;

                        // if the key is not in the map, instantiate a display (AnatomyLabel) object and add it to the map
                        // otherwise, just get the display object from the map
                        if (!map.containsKey(key)) {
                            labeling = new AnatomyLabel(superterm, subterm, startStage, endStage);
                            map.put(key, labeling);
                        } else {
                            labeling = map.get(key);
                        }

                        if (labeling.getAssays() == null) {
                            SortedSet<ExpressionAssay> assays = new TreeSet<ExpressionAssay>();
                            labeling.setAssays(assays);
                        }

                        if (assay != null)
                            labeling.getAssays().add(assay);

                        if (labeling.getGenes() == null) {
                            SortedSet<Marker> genes = new TreeSet<Marker>();
                            labeling.setGenes(genes);
                        }

                        if (gene != null)
                            labeling.getGenes().add(gene);

                        // get the figures associated
                        Set<Figure> figures = result.getFigures();

                        labeling.setFigureWithImage(false);
                        if (figures != null && !figures.isEmpty()) {
                            labeling.getFigures().addAll(figures);
                            // if there is one figure with image, set the flag
                            for (Figure fig : figures) {
                                if (fig.getImages() != null && fig.getImages().size() > 0) {
                                    labeling.setFigureWithImage(true);
                                    break;
                                }
                            }
                        }

                        Publication pub = exp.getPublication();
                        if (pub != null)
                            labeling.getPublications().add(pub);

                        Set<Figure> allFigures = labeling.getFigures();
                        for (Figure fig : allFigures) {
                            if (fig.getType() == Figure.Type.FIGURE) {
                                labeling.setNotAllFiguresTextOnly(true);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // use SortedSet to hold the values of the map so that the data could be displayed in order
        List<AnatomyLabel> labelingDisplays = new ArrayList<AnatomyLabel>();

        if (map.values().size() > 0)
            labelingDisplays.addAll(map.values());
        ////ToDo
        Collections.sort(labelingDisplays, new AntibodyLabelingDetailComparator());

        return labelingDisplays;
    }

    public void createFigureSummary(Term superTermComp, Term subtermComp, DevelopmentStage startStage, DevelopmentStage endStage, boolean withImgOnly) {
        Set<Publication> publications = new HashSet<Publication>();

        // a map of publicationID-FigureID as keys and figure summary display objects as values
        Map<String, FigureSummaryDisplay> map = new HashMap<String, FigureSummaryDisplay>();

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment> experiments = antibody.getAntibodyLabelings();

        // loop through the set of ExpressionExperiment objects to get the related data
        for (ExpressionExperiment exp : experiments) {
            // need to get a Genotype object to check for wildtype; do nothing if not wildtype
            Genotype geno = exp.getGenotypeExperiment().getGenotype();

            // need to get an Experiment object to check for standard environment; do nothing if not standard
            Experiment experiment = exp.getGenotypeExperiment().getExperiment();

            if (geno.isWildtype() && experiment.isStandard()) {
                // get a set of ExpressionResult objects
                Set<ExpressionResult> results = exp.getExpressionResults();

                // loop through the set of ExpressionResult objects to get the related data
                for (ExpressionResult result : results) {
                    if (result.isExpressionFound()) {
                        // there are 2 cases: from antibody details page (stage IDs are null) and from antibody labeling details page
                        if (result.getSuperterm().getID().equals(superTermComp.getID()) && (
                                (startStage.getZdbID() == null && endStage.getZdbID() == null)
                                        || (result.getStartStage().equals(startStage) && result.getEndStage().equals(endStage)))) {

                            Term subterm = result.getSubterm();
                            if ((subterm == null && subtermComp != null) || (subterm != null && subtermComp == null))
                                continue;
                            if (subterm != null && subtermComp != null && !subterm.getID().equals(subtermComp.getID()))
                                continue;
                            // come here if the term IDs are equal or both terms are null
                            // get the figures associated
                            Set<Figure> figures = result.getFigures();

                            if (figures != null && !figures.isEmpty()) {
                                // loop through the figures to get the figure data to be displayed
                                for (Figure f : figures) {
                                    Set<Image> imgs = f.getImages();
                                    if (withImgOnly && imgs.isEmpty())
                                        continue;
                                    Publication pub = f.getPublication();
                                    String key = pub.getZdbID() + f.getZdbID();

                                    // if the key is not in the map, instantiate a display object and add it to the map
                                    // otherwise, get the display object from the map
                                    if (!map.containsKey(key)) {
                                        FigureSummaryDisplay figureData = new FigureSummaryDisplay();
                                        figureData.setPublication(pub);
                                        publications.add(pub);
                                        figureData.setFigure(f);
                                        List<Term> anatomyItems = new ArrayList<Term>();
                                        for (ExpressionResult er : f.getExpressionResults()) {
                                            Term aOstructure = er.getSuperterm();
                                            if (!anatomyItems.contains(aOstructure))
                                                anatomyItems.add(aOstructure);
                                        }
                                        Collections.sort(anatomyItems);
                                        figureData.setTerms(anatomyItems);
                                        for (Image img : f.getImages()) {
                                            if (figureData.getThumbnail() == null)
                                                figureData.setThumbnail(img.getThumbnail());
                                        }

                                        map.put(key, figureData);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<FigureSummaryDisplay> summaryRows = new ArrayList<FigureSummaryDisplay>();
        if (map.values().size() > 0) {
            summaryRows.addAll(map.values());
        }
        Collections.sort(summaryRows);

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
}
