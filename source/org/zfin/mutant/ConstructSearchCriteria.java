package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.presentation.ConstructSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.framework.search.AbstractSearchCriteria;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.framework.search.SortType;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to contain the search criteria for antibody searches.
 */
public class ConstructSearchCriteria extends AbstractSearchCriteria {

    SearchCriterion constructNameCriteria;
    SearchCriterion genePromoterCriteria;
    SearchCriterion expressedGeneCriteria;
    SearchCriterion engineeredRegionCriteria;
    SearchCriterion affectedGeneCriteria;
    SearchCriterion availabilityCriteria;

    public SearchCriterion getEtypeCriteria() {
        return etypeCriteria;
    }

    public SearchCriterion getTgtypeCriteria() {
        return tgtypeCriteria;
    }

    public void setTgtypeCriteria(SearchCriterion tgtypeCriteria) {
        this.tgtypeCriteria = tgtypeCriteria;
    }

    public void setEtypeCriteria(SearchCriterion etypeCriteria) {

        this.etypeCriteria = etypeCriteria;
    }

    public SearchCriterion getPtypeCriteria() {
        return ptypeCriteria;
    }

    public void setPtypeCriteria(SearchCriterion ptypeCriteria) {
        this.ptypeCriteria = ptypeCriteria;
    }

    public SearchCriterion getGtypeCriteria() {
        return gtypeCriteria;
    }

    public void setGtypeCriteria(SearchCriterion gtypeCriteria) {
        this.gtypeCriteria = gtypeCriteria;
    }

    SearchCriterion phenotypeAnatomyCriteria;
    SearchCriterion etypeCriteria;
    SearchCriterion ptypeCriteria;
    SearchCriterion gtypeCriteria;
    SearchCriterion tgtypeCriteria;

    public SearchCriterion getTypeCriteria() {
        return typeCriteria;
    }

    public void setTypeCriteria(SearchCriterion typeCriteria) {
        this.typeCriteria = typeCriteria;
    }

    SearchCriterion typeCriteria;
    private Marker gene;



    private GenotypeExperiment genotypeExperiment;
    private Genotype genotype;
    private List<Genotype> genos;
    private Antibody antibody;
    private PostComposedEntity entity;
    private Term singleTermEitherPosition;
    private DevelopmentStage start;
    private DevelopmentStage end;


    public List<Genotype> getGenos() {
        return genos;
    }

    public void setGenos(List<Genotype> genos) {
        this.genos = genos;
    }

    private Figure figure;

    private boolean isWildtypeOnly;
    private boolean withImagesOnly;
    private boolean isStandardEnvironment;
    private boolean isChemicalEnvironment;


    public ConstructSearchCriteria clone() {
        ConstructSearchCriteria criteria = new ConstructSearchCriteria();

       // criteria.setGene(gene);
      //  criteria.setGene(RepositoryFactory.getMarkerRepository().getMarkerByAbbreviationIgnoreCase(expressedGeneCriteria.getValue()));
        criteria.setGenotypeExperiment(genotypeExperiment);
        criteria.setGenotype(genotype);
        criteria.setAntibody(antibody);
        criteria.setEntity(entity);
        criteria.setSingleTermEitherPosition(singleTermEitherPosition);
        criteria.setGenos(genos);
        criteria.setFigure(figure);
        criteria.setWildtypeOnly(isWildtypeOnly);
        criteria.setWithImagesOnly(withImagesOnly);
        criteria.setStandardEnvironment(isStandardEnvironment);
        criteria.setChemicalEnvironment(isChemicalEnvironment);

        return criteria;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public boolean isWildtypeOnly() {
        return isWildtypeOnly;
    }

    public void setWildtypeOnly(boolean wildtypeOnly) {
        isWildtypeOnly = wildtypeOnly;
    }

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public Term getSingleTermEitherPosition() {
        return singleTermEitherPosition;
    }

    public void setSingleTermEitherPosition(Term singleTermEitherPosition) {
        this.singleTermEitherPosition = singleTermEitherPosition;
    }

    public boolean isWithImagesOnly() {
        return withImagesOnly;
    }

    public void setWithImagesOnly(boolean withImagesOnly) {
        this.withImagesOnly = withImagesOnly;
    }



    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public boolean isStandardEnvironment() {
        return isStandardEnvironment;
    }

    public void setStandardEnvironment(boolean standardEnvironment) {
        isStandardEnvironment = standardEnvironment;
    }

    public boolean isChemicalEnvironment() {
        return isChemicalEnvironment;
    }

    public void setChemicalEnvironment(boolean chemicalEnvironment) {
        isChemicalEnvironment = chemicalEnvironment;
    }


    private List<SearchCriterion> allCriteria;

    public ConstructSearchCriteria() {
    }

    public SearchCriterion getPhenotypeAnatomyCriteria() {
        return phenotypeAnatomyCriteria;
    }

    public void setPhenotypeAnatomyCriteria(SearchCriterion phenotypeAnatomyCriteria) {
        this.phenotypeAnatomyCriteria = phenotypeAnatomyCriteria;
    }

    //set defaults in constructor, all criteria to the list
    public ConstructSearchCriteria(ConstructSearchFormBean formBean) {
        allCriteria = new ArrayList<SearchCriterion>();
        sort = new ArrayList<SortType>();

        setStart(formBean.getFirstRecord());
        setRows(formBean.getMaxDisplayRecordsInteger());

        constructNameCriteria = new SearchCriterion(SearchCriterionType.CONSTRUCT_NAME, true);
        constructNameCriteria.setSeparator(SearchCriterion.WHITESPACE_SEPARATOR);
        constructNameCriteria.setValue(formBean.getConstruct());
        allCriteria.add(constructNameCriteria);

        genePromoterCriteria = new SearchCriterion(SearchCriterionType.PROMOTER_GENE, false);
        genePromoterCriteria.setSeparator(SearchCriterion.WHITESPACE_SEPARATOR);
        genePromoterCriteria.setValue(formBean.getPromoterOfGene());
        allCriteria.add(genePromoterCriteria);

        expressedGeneCriteria = new SearchCriterion(SearchCriterionType.EXPRESSED_GENE, false);
        expressedGeneCriteria.setSeparator(SearchCriterion.WHITESPACE_SEPARATOR);
        expressedGeneCriteria.setValue(formBean.getDrivesExpressionOfGene());
        allCriteria.add(expressedGeneCriteria);

        engineeredRegionCriteria = new SearchCriterion(SearchCriterionType.ENGINEERED_REGION, false);
        engineeredRegionCriteria.setSeparator(SearchCriterion.WHITESPACE_SEPARATOR);
        engineeredRegionCriteria.setValue(formBean.getHasEngineeredRegion());
        allCriteria.add(engineeredRegionCriteria);

        affectedGeneCriteria = new SearchCriterion(SearchCriterionType.AFFECTED_GENE, false);
        affectedGeneCriteria.setSeparator(SearchCriterion.WHITESPACE_SEPARATOR);
        affectedGeneCriteria.setValue(formBean.getAffectedGene());
        allCriteria.add(affectedGeneCriteria);

        phenotypeAnatomyCriteria = new SearchCriterion(SearchCriterionType.PHENOTYPE_ANATOMY_ID, true);
        phenotypeAnatomyCriteria.setValue(formBean.getAnatomyTermIDs());
        phenotypeAnatomyCriteria.setSeparator(",");
        phenotypeAnatomyCriteria.setNameSeparator("\\|");
        phenotypeAnatomyCriteria.setName(formBean.getAnatomyTermNames());
        allCriteria.add(phenotypeAnatomyCriteria);

        availabilityCriteria = new SearchCriterion(SearchCriterionType.LINE_AVAILABLE, "false", false);
        availabilityCriteria.setValue(formBean.isAvailableOnly() ? "true" : "false");
        allCriteria.add(availabilityCriteria);

        typeCriteria = new SearchCriterion(SearchCriterionType.CONSTRUCT_TYPE,"false",false);
        typeCriteria.setValue(formBean.isAllConstructs() ? "true" : "false");
        allCriteria.add(typeCriteria);

        etypeCriteria = new SearchCriterion(SearchCriterionType.CONSTRUCT_TYPE,"false",false);
        etypeCriteria.setValue(formBean.isEtConstruct() ? "true" : "false");
        /*typeCriteria.setValue(formBean.isGt() ? "true" : "false");
        typeCriteria.setValue(formBean.isPt() ? "true" : "false");*/
        allCriteria.add(etypeCriteria);

        ptypeCriteria = new SearchCriterion(SearchCriterionType.CONSTRUCT_TYPE,"false",false);
        ptypeCriteria.setValue(formBean.isPtConstruct()? "true" : "false" );
        allCriteria.add(ptypeCriteria);

        gtypeCriteria = new SearchCriterion(SearchCriterionType.CONSTRUCT_TYPE,"false",false);
        gtypeCriteria.setValue(formBean.isGtConstruct()? "true" : "false" );
        allCriteria.add(gtypeCriteria);
        tgtypeCriteria = new SearchCriterion(SearchCriterionType.CONSTRUCT_TYPE,"false",false);
        tgtypeCriteria.setValue(formBean.isTgConstruct()? "true" : "false" );
        allCriteria.add(tgtypeCriteria);


        /*if (formBean.getSortBy().equals(SortBy.FEATURES.toString())) {
            sort.add(SortType.FISH_PARTS_COUNT_ASC);
            sort.add(SortType.FEATURE_A_TO_Z);
            sort.add(SortType.GENE_COUNT_ASC);
            sort.add(SortType.GENE_A_TO_Z);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else if (formBean.getSortBy().equals(SortBy.FEATURES_REVERSE.toString())) {
            sort.add(SortType.FISH_PARTS_COUNT_DESC);
            sort.add(SortType.FEATURE_Z_TO_A);
            sort.add(SortType.GENE_COUNT_DESC);
            sort.add(SortType.GENE_Z_TO_A);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else if (formBean.getSortBy().equals(SortBy.GENES.toString())) {
            sort.add(SortType.GENE_COUNT_ASC);
            sort.add(SortType.GENE_A_TO_Z);
            sort.add(SortType.FISH_PARTS_COUNT_ASC);
            sort.add(SortType.FEATURE_A_TO_Z);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else if (formBean.getSortBy().equals(SortBy.GENES_REVERSE.toString())) {
            sort.add(SortType.GENE_COUNT_DESC);
            sort.add(SortType.GENE_Z_TO_A);
            sort.add(SortType.FISH_PARTS_COUNT_DESC);
            sort.add(SortType.FEATURE_Z_TO_A);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else { // best match
            sort.add(SortType.LUCENE_SIMPLE);
            sort.add(SortType.COMPLEXITY);
            sort.add(SortType.FEATURE_TYPE);
            sort.add(SortType.GENE_A_TO_Z);
            sort.add(SortType.FEATURE_A_TO_Z);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        }      */

    }


    public List<SearchCriterion> getAllCriteria() {
        return allCriteria;
    }


    public SearchCriterion getConstructNameCriteria() {
        return constructNameCriteria;
    }

    public void setConstructNameCriteria(SearchCriterion constructNameCriteria) {
        this.constructNameCriteria = constructNameCriteria;
    }

    public SearchCriterion getGenePromoterCriteria() {
        return genePromoterCriteria;
    }

    public void setGenePromoterCriteria(SearchCriterion genePromoterCriteria) {
        this.genePromoterCriteria = genePromoterCriteria;
    }

    public SearchCriterion getExpressedGeneCriteria() {
        return expressedGeneCriteria;
    }

    public void setExpressedGeneCriteria(SearchCriterion expressedGeneCriteria) {
        this.expressedGeneCriteria = expressedGeneCriteria;
    }

    public SearchCriterion getEngineeredRegionCriteria() {
        return engineeredRegionCriteria;
    }

    public void setEngineeredRegionCriteria(SearchCriterion engineeredRegionCriteria) {
        this.engineeredRegionCriteria = engineeredRegionCriteria;
    }

    public SearchCriterion getAffectedGeneCriteria() {
        return affectedGeneCriteria;
    }

    public void setAffectedGeneCriteria(SearchCriterion affectedGeneCriteria) {
        this.affectedGeneCriteria = affectedGeneCriteria;
    }

    public SearchCriterion getAvailabilityCriteria() {
        return availabilityCriteria;
    }

    public void setAvailabilityCriteria(SearchCriterion availabilityCriteria) {
        this.availabilityCriteria = availabilityCriteria;
    }
}
