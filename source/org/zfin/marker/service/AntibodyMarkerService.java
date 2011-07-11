package org.zfin.marker.service;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyLabelingDetailComparator;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.*;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

//import org.zfin.ontology.OntologyManager;

/**
 * Class that contains various methods retrieving aggregated info from
 * antibodies.
 */
public class AntibodyMarkerService {

    private AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    /**
     * Returns a list of distinct anatomy terms that this antibody labels.
     *
     * @return ist of distinct and sorted AO terms.
     */
    public static List<Term> getDistinctAnatomyTerms(Antibody antibody) {
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
                    Term term = result.getSuperTerm();
                    if (!distinctAoTerms.contains(term))
                        distinctAoTerms.add(term);
                    // add the secondary term if available
                    Term subterm = result.getSubTerm();
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
    public static int getNumberOfDistinctComposedTerms(Antibody antibody) {
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
                    Term term = result.getSuperTerm();
                    Term subterm = result.getSubTerm();
                    String composedTermName = term.getZdbID();
                    if (subterm != null)
                        composedTermName += subterm.getZdbID();
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
    public static SortedSet<Term> getDistinctGoTermsWTAndStandard(Antibody antibody) {
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

                    Term goTerm = result.getSubTerm();
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
    public static DevelopmentStage getEarliestStartStage(Antibody antibody) {
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
    public static DevelopmentStage getLatestEndStage(Antibody antibody) {
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



    public static Set<GenericTerm> getDistinctAoTerms(Antibody antibody) {
        Set<GenericTerm> terms = new HashSet<GenericTerm>();
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

    public static Set<String> getDistinctAssayNames(Antibody antibody) {
        Set<ExpressionExperiment> antibodyLabelings = antibody.getAntibodyLabelings();
        if (antibodyLabelings == null) {
            return new TreeSet<String>();
        }
        Set<String> assayNames = new TreeSet<String>();
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

    public static List<AnatomyLabel> getAntibodyLabelings(Antibody antibody) {
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

        return labelingDisplays;
    }

    private static void processExperiments(Map<String, AnatomyLabel> map, Set<ExpressionExperiment> experiments) {
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

    public static List<AnatomyLabel> getAntibodyDetailedLabelings(Antibody antibody) {
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

                        Term subterm = result.getSubTerm();
                        Term superterm = result.getSuperTerm();

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
                        String key = superterm.getZdbID() + startStageName + endStageName;
                        if (subterm != null)
                            key += subterm.getZdbID();

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

                        if (figures != null && !figures.isEmpty()) {
                            labeling.getFigures().addAll(figures);
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


}
