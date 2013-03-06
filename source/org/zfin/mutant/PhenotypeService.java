package org.zfin.mutant;

import org.apache.commons.lang.StringUtils;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Service class that deals with Phenotype-related logic
 */
public class PhenotypeService {

    public static final String ANATOMY = "ANATOMY";
    public static final String GO = "GO";

    /**
     * Return a map of phenotype descriptions, comma-delimited, and grouped by ontology for a given
     * anatomy structure.
     *
     * @param genotypeExperiment Genotype Experiment
     * @param anatomyItem        Anatomy Term
     * @return HashMap
     */
    public static Map<String, Set<String>> getPhenotypesGroupedByOntology(GenotypeExperiment genotypeExperiment, GenericTerm anatomyItem) {
        if (genotypeExperiment == null)
            return null;
        if (anatomyItem == null)
            return null;
        if (genotypeExperiment.getPhenotypeExperiments() == null)
            return null;

        Map<String, Set<String>> map = new TreeMap<String, Set<String>>(new PhenotypeComparator());

        for (PhenotypeExperiment phenotype : genotypeExperiment.getPhenotypeExperiments()) {
            for (PhenotypeStatement phenoStatement : phenotype.getPhenotypeStatements()) {
                GenericTerm subTerm = phenoStatement.getEntity().getSubterm();
                if (StringUtils.equals(phenoStatement.getEntity().getSuperterm().getZdbID(), anatomyItem.getZdbID()) ||
                        (subTerm != null && StringUtils.equals(subTerm.getZdbID(), anatomyItem.getZdbID()))) {
                    StringBuilder keyBuilder = new StringBuilder(50);
                    if (subTerm != null) {
                        keyBuilder.append(phenoStatement.getEntity().getSubterm().getTermName());
                        Term anatomyTerm = phenoStatement.getEntity().getSuperterm();
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

                    String termName = phenoStatement.getQuality().getTermName();
                    StringBuilder termNameBuilder = new StringBuilder(50);
                    String tag = phenoStatement.getTag();
                    if (termName.equals(GenericTerm.QUALITY) && tag.equals(PhenotypeStatement.Tag.ABNORMAL.toString()))
                        termNameBuilder.append(PhenotypeStatement.Tag.ABNORMAL.toString());
                    else if (tag != null && tag.equals(PhenotypeStatement.Tag.NORMAL.toString()))
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
        }
        return map;
    }

    /**
     * Retrieve a list of phenotype statements that contain the given term
     * in any position (E1 or E2) in a given genotype experiment
     *
     * @param genoExperiment Genotype Experiment
     * @return list of phenotype statements
     */
    public static Set<PhenotypeStatement> getPhenotypeStatements(GenotypeExperiment genoExperiment) {
        return getPhenotypeStatements(genoExperiment, null);
    }

    /**
     * Retrieve a list of phenotype statements that contain the given term
     * in any position (E1 or E2) in a given genotype experiment
     *
     * @param genoExperiment Genotype Experiment
     * @param term           Term
     * @return list of phenotype statements
     */
    public static Set<PhenotypeStatement> getPhenotypeStatements(GenotypeExperiment genoExperiment, GenericTerm term) {
        if (genoExperiment == null)
            return null;

        boolean includeAll = term == null;
        Set<PhenotypeStatement> phenoStatements = new HashSet<PhenotypeStatement>(5);
        for (PhenotypeExperiment phenox : genoExperiment.getPhenotypeExperiments()) {
            for (PhenotypeStatement statement : phenox.getPhenotypeStatements()) {
                if (includeAll || statement.contains(term))
                    phenoStatements.add(statement);

            }
        }
        // since I do not want to change the equals() method to ignore the PK id
        // I have to create a distinct list myself.
        Set<PhenotypeStatement> distinctPhenoStatements = new HashSet<PhenotypeStatement>(phenoStatements.size());
        for (PhenotypeStatement statement : phenoStatements) {
            boolean recordFound = false;
            for (PhenotypeStatement distinctStatement : distinctPhenoStatements) {
                if (distinctStatement.equalsByPhenotype(statement)) {
                    recordFound = true;
                    break;
                }
            }
            if (!recordFound)
                distinctPhenoStatements.add(statement);
        }
        return distinctPhenoStatements;
    }

    /**
     * Retrieve a list of phenotype statements that contain the given term
     * in any position (E1 or E2) in a given genotype experiment
     *
     * @param genotype Genotype
     * @param term     Term
     * @return list of phenotype statements
     */
    public static Set<PhenotypeStatement> getPhenotypeStatements(Genotype genotype, GenericTerm term, boolean includSubstructures) {
        if (genotype == null)
            return null;

        List<PhenotypeStatement> phenotypeStatementList = getMutantRepository().getPhenotypeStatement(term, genotype, includSubstructures);
        // since I do not want to change the equals() method to ignore the PK id
        // I have to create a distinct list myself.
        Set<PhenotypeStatement> distinctPhenoStatements = new HashSet<PhenotypeStatement>(phenotypeStatementList.size());
        for (PhenotypeStatement statement : phenotypeStatementList) {
            boolean recordFound = false;
            for (PhenotypeStatement distinctStatement : distinctPhenoStatements) {
                if (distinctStatement.equalsByPhenotype(statement)) {
                    recordFound = true;
                    break;
                }
            }
            if (!recordFound)
                distinctPhenoStatements.add(statement);
        }
        return distinctPhenoStatements;
    }

    /**
     * Returns obsoleted terms from the possible 5-term combination.
     *
     * @param phenotypeStatement phenotype statement
     * @return list of obsoleted terms
     */
    public static Set<GenericTerm> getObsoleteTerm(PhenotypeStatement phenotypeStatement) {
        Set<GenericTerm> obsoletedTerms = new HashSet<GenericTerm>(3);
        if (phenotypeStatement.getEntity().getSuperterm().isObsolete()) {
            obsoletedTerms.add(phenotypeStatement.getEntity().getSuperterm());
        } else if (phenotypeStatement.getEntity().getSubterm() != null && phenotypeStatement.getEntity().getSubterm().isObsolete()) {
            obsoletedTerms.add(phenotypeStatement.getEntity().getSubterm());
        } else if (phenotypeStatement.getQuality() != null && phenotypeStatement.getQuality().isObsolete()) {
            obsoletedTerms.add(phenotypeStatement.getQuality());
        } else if (phenotypeStatement.getRelatedEntity() != null) {
            PostComposedEntity entity = phenotypeStatement.getRelatedEntity();
            if (entity.getSuperterm() != null && entity.getSuperterm().isObsolete())
                obsoletedTerms.add(entity.getSuperterm());
            if (entity.getSubterm() != null && entity.getSubterm().isObsolete())
                obsoletedTerms.add(entity.getSubterm());
        }
        return obsoletedTerms;
    }

    /**
     * Returns obsoleted terms from the possible 5-term combination.
     *
     * @param phenotypeStatement phenotype statement
     * @return list of obsoleted terms
     */
    public static Set<GenericTerm> getSecondaryTerm(PhenotypeStatement phenotypeStatement) {
        Set<GenericTerm> secondaryTerms = new HashSet<GenericTerm>(3);
        if (phenotypeStatement.getEntity().getSuperterm().isSecondary()) {
            secondaryTerms.add(phenotypeStatement.getEntity().getSuperterm());
        } else if (phenotypeStatement.getEntity().getSubterm() != null && phenotypeStatement.getEntity().getSubterm().isSecondary()) {
            secondaryTerms.add(phenotypeStatement.getEntity().getSubterm());
        } else if (phenotypeStatement.getQuality() != null && phenotypeStatement.getQuality().isSecondary()) {
            secondaryTerms.add(phenotypeStatement.getQuality());
        } else if (phenotypeStatement.getRelatedEntity() != null) {
            PostComposedEntity entity = phenotypeStatement.getRelatedEntity();
            if (entity.getSuperterm() != null && entity.getSuperterm().isSecondary())
                secondaryTerms.add(entity.getSuperterm());
            if (entity.getSubterm() != null && entity.getSubterm().isSecondary())
                secondaryTerms.add(entity.getSubterm());
        }
        return secondaryTerms;
    }

    public static String getSubstructureName(PhenotypeStatement phenotypeStatement, Term parentTerm) {
        PostComposedEntity entity = phenotypeStatement.getEntity();
        if (entity != null) {
            GenericTerm superterm = entity.getSuperterm();
            if (superterm != null)
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, superterm))
                    return superterm.getTermName();
            Term subterm = entity.getSubterm();
            if (subterm != null)
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, subterm))
                    return subterm.getTermName();
        }
        PostComposedEntity relatedEntity = phenotypeStatement.getRelatedEntity();
        if (relatedEntity != null) {
            GenericTerm superterm = relatedEntity.getSuperterm();
            if (superterm != null)
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, superterm))
                    return superterm.getTermName();
            Term subterm = relatedEntity.getSubterm();
            if (subterm != null)
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, subterm))
                    return subterm.getTermName();
        }
        return null;
    }

    public static boolean hasStructureOrSubstructure(GenericTerm anatomyItem, PhenotypeStatement statement) {
        PostComposedEntity entity = statement.getEntity();
        if (getOntologyRepository().isParentChildRelationshipExist(anatomyItem, entity.getSuperterm()))
            return true;
        if (entity.getSubterm() != null && getOntologyRepository().isParentChildRelationshipExist(anatomyItem, entity.getSubterm()))
            return true;

        PostComposedEntity relatedEntity = statement.getRelatedEntity();
        if (relatedEntity == null)
            return false;
        if (getOntologyRepository().isParentChildRelationshipExist(anatomyItem, relatedEntity.getSuperterm()))
            return true;
        if (entity.getSubterm() != null && getOntologyRepository().isParentChildRelationshipExist(anatomyItem, relatedEntity.getSubterm()))
            return true;

        return false;
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
     * 1) entity superterm = unspecified [AO]
     * 2) entity subterm = null
     * 3) Quality = quality
     * 4) related superterm = null
     * 5) related subterm = null
     * 6) Tag = abnormal
     *
     * @param phenoExperiment PhenotypeExperiment
     * @return PhenotypeStatement
     */
/*
    public static PhenotypeStatement getDefaultPhenotypeStatement(PhenotypeExperiment phenoExperiment) {
        PhenotypeStatement defaultStatement = new PhenotypeStatement();
        defaultStatement.setPhenotypeExperiment(phenoExperiment);
        defaultStatement.setTag(PhenotypeStatement.Tag.TAG_ABNORMAL.toString());
        defaultStatement.setQuality(OntologyManager.getInstance().getTermByName(Ontology.QUALITY, "quality"));
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED));
        defaultStatement.setEntity(entity);
        return defaultStatement;
    }
*/

    /**
     * retrieve all distinct Anatomy terms that are referenced in the entity or related entity for a given
     * phenotype statement.
     *
     * @param phenotypeStatement phenotype statement
     * @return list of terms
     */
    public static Set<Term> getAllAnatomyTerms(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null)
            return null;
        // at most 4 entries
        Set<Term> termList = new HashSet<Term>(2);
        PostComposedEntity entity = phenotypeStatement.getEntity();
        if (entity != null) {
            addIfAnatomyTerm(termList, entity.getSuperterm());
            addIfAnatomyTerm(termList, entity.getSubterm());
        }
        PostComposedEntity relatedEntity = phenotypeStatement.getRelatedEntity();
        if (relatedEntity != null) {
            addIfAnatomyTerm(termList, relatedEntity.getSuperterm());
            addIfAnatomyTerm(termList, relatedEntity.getSubterm());
        }
        return termList;
    }

    public static Set<Term> getAllGOTerms(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null)
            return null;
        // at most 4 entries
        Set<Term> termList = new HashSet<Term>(2);
        PostComposedEntity entity = phenotypeStatement.getEntity();
        if (entity != null) {
            addIfGOTerm(termList, entity.getSuperterm());
            addIfGOTerm(termList, entity.getSubterm());
        }
        PostComposedEntity relatedEntity = phenotypeStatement.getRelatedEntity();
        if (relatedEntity != null) {
            addIfGOTerm(termList, relatedEntity.getSuperterm());
            addIfGOTerm(termList, relatedEntity.getSubterm());
        }
        return termList;
    }

    private static void addIfAnatomyTerm(Set<Term> termList, GenericTerm superterm) {
        if (superterm != null)
            if (superterm.getOntology().equals(Ontology.ANATOMY))
                termList.add(superterm);
    }

    private static void addIfGOTerm(Set<Term> termList, GenericTerm superterm) {
        if (superterm != null)
            if (superterm.getOntology().equals(Ontology.GO_BP)||(superterm.getOntology().equals(Ontology.GO_MF))||(superterm.getOntology().equals(Ontology.GO_CC)))
                termList.add(superterm);
           }
}


