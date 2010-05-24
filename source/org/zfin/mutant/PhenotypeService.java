package org.zfin.mutant;

import org.apache.commons.lang.StringUtils;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

import java.util.*;

/**
 * Service class that deals with Phenotype-related logic
 */
public class PhenotypeService {

    public static final String ANATOMY = "ANATOMY";

    /**
     * Return a map of phenotype descriptions, comma-delimited, and grouped by ontology for a given
     * anatomy structure.
     *
     * @param genotypeExperiment Genotype Experiment
     * @param anatomyItem        Anatomy Term
     * @return HashMap
     */
    public static Map<String, Set<String>> getPhenotypesGroupedByOntology(GenotypeExperiment genotypeExperiment, Term anatomyItem) {
        if (genotypeExperiment == null)
            return null;
        if (anatomyItem == null)
            return null;
        if (genotypeExperiment.getPhenotypes() == null)
            return null;

        Map<String, Set<String>> map = new TreeMap<String, Set<String>>(new PhenotypeComparator());

        for (Phenotype phenotype : genotypeExperiment.getPhenotypes()) {
            Term subTerm = phenotype.getSubterm();
            if (StringUtils.equals(phenotype.getSuperterm().getID(), anatomyItem.getID()) ||
                    (subTerm != null && StringUtils.equals(subTerm.getID(), anatomyItem.getID()))) {
                StringBuilder keyBuilder = new StringBuilder(50);
                if (subTerm != null) {
                    keyBuilder.append(phenotype.getSubterm().getTermName());
                    Term anatomyTerm = phenotype.getSuperterm();
                    keyBuilder.append(":");
                    keyBuilder.append(anatomyTerm.getTermName());
                    ////TODO
/*
                    if (anatomyTerm.isCellTerm()) {
                        keyBuilder.append(anatomyTerm.getName());
                    } else {
                        keyBuilder.append(ANATOMY);
                    }
*/
                } else {
                    keyBuilder.append(ANATOMY);
                }

                String termName = phenotype.getTerm().getTermName();
                StringBuilder termNameBuilder = new StringBuilder(50);
                String tag = phenotype.getTag();
                if (termName.equals(GenericTerm.QUALITY) && tag.equals(Phenotype.Tag.ABNORMAL.toString()))
                    termNameBuilder.append(Phenotype.Tag.ABNORMAL.toString());
                else if (tag != null && tag.equals(Phenotype.Tag.NORMAL.toString()))
                    continue;
                else
                    termNameBuilder.append(termName);

                Set<String> phenotypes = map.get(keyBuilder.toString());
                if (phenotypes == null) {
                    phenotypes = new TreeSet<String>();
                }
                phenotypes.add(termNameBuilder.toString());
                map.put(keyBuilder.toString(), phenotypes);
            }
        }
        return map;
    }

    private static class PhenotypeComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            if (o1 == null)
                return -1;
            if (o2 == null)
                return +1;
            if (o1.equals(ANATOMY) && !o2.equals(ANATOMY))
                return -1;
            if (o2.equals(ANATOMY) && !o1.equals(ANATOMY))
                return +1;
            return o1.compareTo(o2);
        }
    }


    /**
     * Return the default phenotype if it exists:
     * check:
     * 1) AO superterm = unspecified
     * 2) Quality = quality
     * 3) Tag = abnormal
     * 4) stages the same
     * 5) publication the same
     *
     * @param mfs
     * @return
     */
    public static Phenotype getDefaultPhenotype(MutantFigureStage mfs) {
        GenotypeExperiment genotypeExperiment = mfs.getGenotypeExperiment();
        Phenotype defaultPhenotype = null;
        if (genotypeExperiment.getPhenotypes() != null) {
            for (Phenotype phenotype : genotypeExperiment.getPhenotypes()) {
                if (phenotype.getSuperterm() != null && phenotype.getSuperterm().getTermName().equals(Term.UNSPECIFIED))
                    if (phenotype.getSubterm() == null)
                        if (phenotype.getTerm().getTermName().equals("quality"))
                            if (phenotype.getTag().equals(Phenotype.Tag.ABNORMAL.toString())) {
                                if (phenotype.getStartStage().equals(mfs.getStart()) && phenotype.getEndStage().equals(mfs.getEnd()))
                                    if (phenotype.getPublication().equals(mfs.getPublication()))
                                        defaultPhenotype = phenotype;
                            }
            }
        }
        return defaultPhenotype;
    }


}
