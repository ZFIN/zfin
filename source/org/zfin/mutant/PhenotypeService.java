package org.zfin.mutant;

import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyPhenotype;
import org.zfin.ontology.GoPhenotype;

import java.util.*;

/**
 * Service class that deals with Phenotype-related logic
 */
public class PhenotypeService {

    public static final String ANATOMY = "ANATOMY";

    /**
     * Return a map of phenotype descriptions, comma-delimited, and grouped by ontology.
     *
     * @param genotypeExperiment Genotype Experiment
     * @param anatomyItem        Anatomy Term
     * @return HashMap
     */
    public static Map<String, Set<String>> getPhenotypesGroupedByOntology(GenotypeExperiment genotypeExperiment, AnatomyItem anatomyItem) {
        if (genotypeExperiment == null)
            return null;
        if (anatomyItem == null)
            return null;
        if (genotypeExperiment.getPhenotypes() == null)
            return null;

        Map<String, Set<String>> map = new TreeMap<String, Set<String>>(new PhenotypeComparator());

        for (Phenotype phenotype : genotypeExperiment.getPhenotypes()) {
            if (StringUtils.equals(phenotype.getPatoSubTermzdbID(), anatomyItem.getZdbID()) ||
                    StringUtils.equals(phenotype.getPatoSuperTermzdbID(), anatomyItem.getZdbID())) {
                StringBuilder keyBuilder = new StringBuilder();
                if (phenotype.getPatoSubTermzdbID() != null) {
                    if (phenotype instanceof GoPhenotype) {
                        GoPhenotype goPheno = (GoPhenotype) phenotype;
                        keyBuilder.append(goPheno.getGoTerm().getName());
                    }
                    if (phenotype instanceof AnatomyPhenotype) {
                        AnatomyPhenotype aoPheno = (AnatomyPhenotype) phenotype;
                        AnatomyItem anatomyTerm = aoPheno.getAnatomyTerm();
                        if (anatomyTerm.isCellTerm()) {
                            keyBuilder.append(anatomyTerm.getName());
                        } else {
                            keyBuilder.append(ANATOMY);
                        }
                    }
                } else {
                    keyBuilder.append(ANATOMY);
                }

                String termName = phenotype.getTerm().getName();
                StringBuilder termNameBuilder = new StringBuilder();
                String tag = phenotype.getTag();
                if (termName.equals(Term.QUALITY) && tag.equals(Term.TAG_ABNORMAL))
                    termNameBuilder.append(Term.TAG_ABNORMAL);
                else if (tag != null && tag.equals(Term.TAG_NORMAL))
                    continue;
                else
                    termNameBuilder.append(termName);

                Set<String> phenos = map.get(keyBuilder.toString());
                if (phenos == null) {
                    phenos = new TreeSet<String>();
                }
                phenos.add(termNameBuilder.toString());
                map.put(keyBuilder.toString(), phenos);
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
}
