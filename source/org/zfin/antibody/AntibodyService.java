package org.zfin.antibody;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.*;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GoTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * Class that contains various methods retrieving aggregated info from
 * antibodies.
 */
public class AntibodyService {

    private Antibody antibody;
    private AntibodySearchCriteria antibodySerachCriteria;

    private int numOfLabelings;
    private int numberOfPublications;
    private List<FigureSummaryDisplay> figureSummary;
    // for caching purposed
    private List<MatchingText> matchingTexts;

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
    public List<AnatomyItem> getDistinctAnatomyTerms() {
        List<AnatomyItem> distinctAoTerms = new ArrayList<AnatomyItem>();
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
                    AnatomyItem term = result.getAnatomyTerm();
                    if (!distinctAoTerms.contains(term))
                        distinctAoTerms.add(term);
                    // add the secondary term if available
                    if (result instanceof AnatomyExpressionResult) {
                        AnatomyExpressionResult goResult = (AnatomyExpressionResult) result;
                        AnatomyItem goTerm = goResult.getSubterm();
                        if (goTerm != null) {
                            if (!distinctAoTerms.contains(goTerm))
                                distinctAoTerms.add(goTerm);
                        }
                    }
                }
            }
        }
        Collections.sort(distinctAoTerms);
        return distinctAoTerms;
    }

    /**
     * Returns a list of distinct GO terms that this antibody labels.
     * ONly include wild-type fish and standard condition.
     *
     * @return ist of distinct and sorted GO terms.
     */
    public SortedSet<GoTerm> getDistinctGoTermsWTAndStandard() {
        SortedSet<GoTerm> distinctGoTerms = new TreeSet<GoTerm>();
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

                    if (result instanceof GoTermExpressionResult) {
                        GoTermExpressionResult goResult = (GoTermExpressionResult) result;
                        GoTerm goTerm = goResult.getSubterm();
                        if (goTerm != null) {
                            if (!distinctGoTerms.contains(goTerm))
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
     * Only inlcude wild type fish in standard environment
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
     * Only inlcude wild type fish in standard environment
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
        if (matchingTexts != null)
            return matchingTexts;

        if (antibodySerachCriteria == null)
            return null;
        List<MatchingText> matchedTerms = new ArrayList<MatchingText>();
        // check antibody name
        addMatchingAntibodyName(matchedTerms);

        // check antigen gene abbreviation, name and previous names
        addMatchingAntigenGene(matchedTerms);

        // Check anatomy terms
        addMatchingAnatomyTerms(matchedTerms);
        matchingTexts = matchedTerms;
        return matchingTexts;
    }

    private void addMatchingAnatomyTerms(List<MatchingText> matchingTexts) {
        String[] anatomyTerms = antibodySerachCriteria.getAnatomyTerms();
        // do nothing if no search terms were entered
        if (anatomyTerms == null || anatomyTerms.length == 0)
            return;

        Set<AnatomyItem> labelingTerms = getDistinctAoTerms();
        // check for direct matches
        AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
        MatchingText match = new MatchingText(MatchingText.Type.AO_TERM);
        List<String> directMatchesFound = new ArrayList<String>();
        for (String searchTermName : anatomyTerms) {
            match.addMatchedString(searchTermName);
            AnatomyItem item = anatomyRepository.getAnatomyItem(searchTermName);
            for (AnatomyItem label : labelingTerms) {
                if (label.getZdbID().equals(item.getZdbID())) {
                    match.addMatchingTerm(label.getName());
                    directMatchesFound.add(searchTermName);
                }
            }
        }
        if (match.getMatchingTerms() != null)
            matchingTexts.add(match);
        // if all terms are matched in a 'every term' filter do not proceed with substructure matching
        if (antibodySerachCriteria.isAnatomyEveryTerm() && directMatchesFound.size() == anatomyTerms.length)
            return;
        // if at least one term is found in a 'any term' filter do not proceed with substructure matching.
        if (!antibodySerachCriteria.isAnatomyEveryTerm() && directMatchesFound.size() >= 1)
            return;


        if (match.getMatchingTerms() == null || (match.getMatchingTerms() != null && match.getMatchingTerms().size() < anatomyTerms.length)) {
            match = new MatchingText(MatchingText.Type.AO_TERM);

            // check for substructure matches
            for (String searchTermName : anatomyTerms) {
                // omit terms that were already matched
                if (directMatchesFound.contains(searchTermName))
                    continue;
                match.addMatchedString(searchTermName);
                AnatomyItem item = RepositoryFactory.getAnatomyRepository().getAnatomyItem(searchTermName);
                for (AnatomyItem label : labelingTerms) {
                    if (anatomyRepository.isSubstructureOf(label, item)) {
                        match.setAppendix("(" + label.getName() + ") ");
                        match.addMatchingTerm(searchTermName);
                        break;
                        //match.setAppendix(searchTermName);
                    }
                }
            }
            matchingTexts.add(match);
        }
    }

    private void addMatchingAntigenGene(List<MatchingText> matchingTexts) {
        String antigenNamefilterString = antibodySerachCriteria.getAntigenGeneName();
        if (antigenNamefilterString != null && antigenNamefilterString.trim().length() != 0) {
            List<Marker> genes = antibody.getAllRelatedMarker();
            // the loop exists for the first match as this is enough!
            for (Marker gene : genes) {
                if (gene.getAbbreviation().indexOf(antigenNamefilterString.toLowerCase().trim()) > -1) {
                    MatchingText match = new MatchingText(MatchingText.Type.GENE_NAME);
                    match.addMatchedString(antigenNamefilterString);
                    match.addMatchingTerm(gene.getAbbreviation());
                    matchingTexts.add(match);
                    break;
                }
                if (gene.getName().indexOf(antigenNamefilterString.toLowerCase().trim()) > -1) {
                    MatchingText match = new MatchingText(MatchingText.Type.GENE_NAME);
                    match.addMatchedString(antigenNamefilterString);
                    match.addMatchingTerm(gene.getName());
                    matchingTexts.add(match);
                    break;
                }
                Set<MarkerAlias> prevNames = gene.getAliases();
                MatchingText match = new MatchingText(MatchingText.Type.GENE_ALIAS);
                if (prevNames != null) {
                    // loop until the first match is encountered
                    for (MarkerAlias prevName : prevNames) {
                        if (prevName.getAlias().indexOf(antigenNamefilterString.toLowerCase().trim()) > -1) {
                            match.addMatchedString(antigenNamefilterString);
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
        String name = antibodySerachCriteria.getName();
        String antibodyNamefilterString = name != null ? name.trim() : "";
        if (antibodyNamefilterString != null && antibodyNamefilterString.trim().length() != 0) {
            String antibodyName = antibody.getName();
            boolean hasNameMatch = false;
            if (antibodyName.toLowerCase().indexOf(antibodyNamefilterString.toLowerCase()) > -1) {
                MatchingText match = new MatchingText(MatchingText.Type.ANTIBODY_NAME);
                match.addMatchedString(antibodyNamefilterString);
                match.addMatchingTerm(antibody.getName());
                matchingTexts.add(match);
                hasNameMatch = true;
            }
            if (!hasNameMatch) {
                Set<MarkerAlias> aliases = antibody.getAliases();
                if (aliases != null) {
                    MatchingText match = new MatchingText(MatchingText.Type.ANTIBODY_ALIAS);
                    for (MarkerAlias alias : aliases) {
                        if (alias.getAlias().toLowerCase().indexOf(antibodyNamefilterString.toLowerCase()) > -1) {
                            match.addMatchedString(antibodyNamefilterString);
                            match.addMatchingTerm(alias.getAlias());
                        }
                    }
                    if (match.getMatchingTerms() != null)
                        matchingTexts.add(match);
                }
            }
        }
    }

    public Set<AnatomyItem> getDistinctAoTerms() {
        Set<AnatomyItem> terms = new HashSet<AnatomyItem>();
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
                            terms.add(result.getAnatomyTerm());
                            if (result instanceof AnatomyExpressionResult) {
                                AnatomyExpressionResult anatResult = (AnatomyExpressionResult) result;
                                AnatomyItem secondaryTerm = anatResult.getSubterm();
                                if (secondaryTerm != null)
                                    terms.add(secondaryTerm);
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

    public void setAntibodySerachCriteria(AntibodySearchCriteria antibodySerachCriteria) {
        this.antibodySerachCriteria = antibodySerachCriteria;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public List<AnatomyLabel> getAntibodyLabelings() {
        // a map of AOname-CCnames as keys and display obejects as values
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

                // get a set of ExpressionResult objects
                Set<ExpressionResult> results = exp.getExpressionResults();

                // loop thru the set of ExpressionResult objects to get the related data
                for (ExpressionResult result : results) {
                    if (result.isExpressionFound()) {
                        AnatomyItem ao = result.getAnatomyTerm();

                        GoTerm cc = null;
                        if (result instanceof GoTermExpressionResult) {
                            GoTermExpressionResult goResult = (GoTermExpressionResult) result;
                            cc = goResult.getSubterm();
                        }
                        String ccName;
                        if (cc == null)
                            ccName = "";
                        else
                            ccName = cc.getTermName();

                        // form the key
                        String key = ao.getName() + ccName;

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

        List<AnatomyLabel> labelingDisplays = new ArrayList<AnatomyLabel>();

        if (map.values().size() > 0)
            labelingDisplays.addAll(map.values());

        Collections.sort(labelingDisplays);

        setNumOfLabelings(labelingDisplays.size());

        return labelingDisplays;
    }

    public List<AnatomyLabel> getAntibodyDetailedLabelings() {
        // a map of AOname-CCname-startStageName-EndStageNames as keys and display obejects as values
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
                Marker gene = exp.getMarker();

                // get a set of ExpressionResult objects
                Set<ExpressionResult> results = exp.getExpressionResults();

                // loop thru the set of ExpressionResult objects to get the related data
                for (ExpressionResult result : results) {
                    if (result.isExpressionFound()) {
                        AnatomyItem ao = result.getAnatomyTerm();

                        GoTerm cc = null;
                        if (result instanceof GoTermExpressionResult) {
                            GoTermExpressionResult goResult = (GoTermExpressionResult) result;
                            cc = goResult.getSubterm();
                        }
                        AnatomyItem secondaryAoTerm = null;
                        if (result instanceof AnatomyExpressionResult) {
                            AnatomyExpressionResult anatResult = (AnatomyExpressionResult) result;
                            secondaryAoTerm = anatResult.getSubterm();
                        }
                        String termZdbID = "";
                        if (cc != null)
                            termZdbID = cc.getZdbID();
                        else if (secondaryAoTerm != null)
                            termZdbID = secondaryAoTerm.getZdbID();

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
                        String key = ao.getName() + termZdbID + startStageName + endStageName;

                        AnatomyLabel labeling;

                        // if the key is not in the map, instantiate a display (AnatomyLabel) object and add it to the map
                        // otherwise, just get the display object from the map
                        if (!map.containsKey(key)) {
                            if (cc != null)
                                labeling = new AnatomyLabel(ao, cc, startStage, endStage);
                            else
                                labeling = new AnatomyLabel(ao, secondaryAoTerm, startStage, endStage);
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

        Collections.sort(labelingDisplays, new AntibodyLabelingDetailComparator());

        return labelingDisplays;
    }

    public void createFigureSummary(AnatomyItem ao, DevelopmentStage startStage, DevelopmentStage endStage, boolean withImgOnly) {
        Set<Publication> publications = new HashSet<Publication>();

        // a map of publicationID-FigureID as keys and figure summary display obejects as values
        Map<String, FigureSummaryDisplay> map = new HashMap<String, FigureSummaryDisplay>();

        // get a set of ExpressionExperiment objects associated with the antibody
        Set<ExpressionExperiment> experiments = antibody.getAntibodyLabelings();

        // loop thru the set of ExpressionExperiment objects to get the related data
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
                        // there are 2 cases: from antibody details page (stage IDs are null) and from antibody labeling details page
                        if (result.getAnatomyTerm().equals(ao) && (
                                (startStage.getZdbID() == null && endStage.getZdbID() == null)
                                        || (result.getStartStage().equals(startStage) && result.getEndStage().equals(endStage)))) {

                            // get the figures associated
                            Set<Figure> figures = result.getFigures();

                            if (figures != null && !figures.isEmpty()) {
                                // loop thru the figures to get the figure data to be displayed
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
                                        List<AnatomyItem> anatomyItems = new ArrayList<AnatomyItem>();
                                        for (ExpressionResult er : f.getExpressionResults()) {
                                            AnatomyItem AOstructure = er.getAnatomyTerm();
                                            if (!anatomyItems.contains(AOstructure))
                                                anatomyItems.add(AOstructure);
                                        }
                                        Collections.sort(anatomyItems);
                                        figureData.setAnatomyItems(anatomyItems);
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
