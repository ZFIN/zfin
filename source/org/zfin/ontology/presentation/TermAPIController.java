package org.zfin.ontology.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.*;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Fish;
import org.zfin.mutant.presentation.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

@RestController
@RequestMapping("/api/ontology")
public class TermAPIController {

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/antibodies", method = RequestMethod.GET)
    public JsonResultResponse<AntibodyStatistics> getLabeledAntibodies(@PathVariable String termID,
                                                                       @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                       @RequestParam(value = "filter.geneName", required = false) String filterGeneName,
                                                                       @RequestParam(value = "filter.antibodyName", required = false) String filterAntibodyName,
                                                                       @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                       @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<AntibodyStatistics> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

        if (term == null)
            return response;
        if (StringUtils.isNotEmpty(filterGeneName)) {
            pagination.addToFilterMap("gene.abbreviation", filterGeneName);
        }
        if (StringUtils.isNotEmpty(filterAntibodyName)) {
            pagination.addToFilterMap("antibody.abbreviation", filterAntibodyName);
        }
        if (StringUtils.isNotEmpty(filterTermName)) {
            pagination.addToFilterMap("subterm.termName", filterTermName);
        }
        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveAntibodyData(term, form, pagination, directAnnotation);
        response.setResults(form.getAntibodyStatistics());
        response.setTotal(form.getAntibodyCount());
        response.addSupplementalData("countDirect", form.getCountDirect());
        response.addSupplementalData("countIncludingChildren", form.getCountIncludingChildren());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/inSituProbes", method = RequestMethod.GET)
    public JsonResultResponse<HighQualityProbe> getInSituProbes(@PathVariable String termID,
                                                                @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                @RequestParam(value = "filter.geneName", required = false) String filterName,
                                                                @RequestParam(value = "filter.probeName", required = false) String filterProbeName,
                                                                @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<HighQualityProbe> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;
        if (StringUtils.isNotEmpty(filterName)) {
            pagination.addToFilterMap("gene.abbreviation", filterName);
        }
        if (StringUtils.isNotEmpty(filterProbeName)) {
            pagination.addToFilterMap("probe.abbreviation", filterProbeName);
        }
        if (StringUtils.isNotEmpty(filterTermName)) {
            pagination.addToFilterMap("subterm.termName", filterTermName);
        }
        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveHighQualityProbeData(term, form, pagination, directAnnotation);
        response.setResults(form.getHighQualityProbeGenes());
        response.setTotal(form.getNumberOfHighQualityProbes());
        response.addSupplementalData("countDirect", form.getCountDirect());
        response.addSupplementalData("countIncludingChildren", form.getCountIncludingChildren());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.ExpressedGeneAPI.class)
    @RequestMapping(value = "/{termID}/expressed-genes", method = RequestMethod.GET)
    public JsonResultResponse<ExpressedGeneDisplay> getExpressedGenes(@PathVariable String termID,
                                                                      @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                      @RequestParam(value = "filter.geneName", required = false) String filterGeneName,
                                                                      @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<ExpressedGeneDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

        if (StringUtils.isNotEmpty(filterGeneName)) {
            pagination.addToFilterMap("gene_mrkr_abbrev", filterGeneName);
        }

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveExpressedGenesData(term, form, pagination);
        response.setResults(form.getAllExpressedMarkers());
        response.setTotal(form.getTotalNumberOfExpressedGenes());
        response.addSupplementalData("countDirect", form.getTotalNumberOfExpressedGenes());
        response.addSupplementalData("countIncludingChildren", form.getTotalNumberOfExpressedGenes());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.ExpressedGeneAPI.class)
    @RequestMapping(value = "/{termID}/phenotype", method = RequestMethod.GET)
    public JsonResultResponse<FishStatistics> getPhenotypes(@PathVariable String termID,
                                                            @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                            @RequestParam(value = "includeNormalPhenotype", required = false, defaultValue = "false") boolean isIncludeNormalPhenotype,
                                                            @RequestParam(value = "filter.geneSymbol", required = false) String filterGeneSymbol,
                                                            @RequestParam(value = "filter.fishName", required = false) String filterFishName,
                                                            @RequestParam(value = "filter.phenotype", required = false) String filterPhenotype,
                                                            @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                            @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<FishStatistics> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

        if (StringUtils.isNotEmpty(filterGeneSymbol)) {
            pagination.addToFilterMap("fishStat.geneSymbolSearch", filterGeneSymbol);
        }
        if (StringUtils.isNotEmpty(filterFishName)) {
            pagination.addToFilterMap("fishStat.fish.name", filterFishName);
        }
        if (StringUtils.isNotEmpty(filterPhenotype)) {
            pagination.addToFilterMap("fishStat.phenotypeStatementSearch", filterPhenotype);
        }
        if (StringUtils.isNotEmpty(filterTermName)) {
            pagination.addToFilterMap("fishStat.term.termName", filterTermName);
        }

        PaginationResult<FishStatistics> phenotypeForDiseaseDirect = OntologyService.getPhenotypeForDisease(term, pagination, false, isIncludeNormalPhenotype);
        PaginationResult<FishStatistics> phenotypeForDiseaseAll = OntologyService.getPhenotypeForDisease(term, pagination, true, isIncludeNormalPhenotype);

        int totalCountDirect = phenotypeForDiseaseDirect.getTotalCount();
        response.addSupplementalData("countDirect", totalCountDirect);
        int totalCountAll = phenotypeForDiseaseAll.getTotalCount();
        response.addSupplementalData("countIncludingChildren", totalCountAll);

        List<FishStatistics> displayList;
        if (directAnnotation) {
            displayList = phenotypeForDiseaseDirect.getPopulatedResults();
            response.setTotal(totalCountDirect);
        } else {
            displayList = phenotypeForDiseaseAll.getPopulatedResults();
            response.setTotal(totalCountAll);
        }
        response.setResults(displayList);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/phenotype-chebi", method = RequestMethod.GET)
    public JsonResultResponse<ChebiPhenotypeDisplay> getPhenotypeChebi(@PathVariable String termID,
                                                                       @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                       @RequestParam(value = "isWildtype", required = false) Boolean wildType,
                                                                       @RequestParam(value = "isMultiChebiCondition", required = false) Boolean isMultiChebiCondition,
                                                                       @RequestParam(value = "isAmelioratedExacerbated", required = false) Boolean isAmelioratedExacerbated,
                                                                       @RequestParam(value = "filter.conditionName", required = false) String filterConditionName,
                                                                       @RequestParam(value = "filter.modification", required = false) String filterModification,
                                                                       @RequestParam(value = "filter.fishName", required = false) String filterFishName,
                                                                       @RequestParam(value = "filter.phenotype", required = false) String filterPhenotype,
                                                                       @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                       @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<ChebiPhenotypeDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

        if (isAmelioratedExacerbated != null) {
            pagination.addToNotNullFilterMap("chebiPhenotype.amelioratedExacerbatedPhenoSearch");
        }

        pagination.addToBooleanFilterMapIfNotNull("chebiPhenotype.fish.wildtype", wildType);
        pagination.addToBooleanFilterMapIfNotNull("chebiPhenotype.multiChebiCondition", isMultiChebiCondition);

        pagination.addToFilterMapIfNotEmpty("chebiPhenotype.conditionSearch", filterConditionName);
        pagination.addToFilterMapIfNotEmpty("chebiPhenotype.amelioratedExacerbatedPhenoSearch", filterModification);
        pagination.addToFilterMapIfNotEmpty("chebiPhenotype.fish.name", filterFishName);
        pagination.addToFilterMapIfNotEmpty("chebiPhenotype.phenotypeStatementSearch", filterPhenotype);
        pagination.addToFilterMapIfNotEmpty("chebiPhenotype.expConditionChebiSearch", filterTermName);

        PaginationResult<ChebiPhenotypeDisplay> genesInvolvedForDiseaseDirect = getDiseasePageRepository().getPhenotypeChebi(term, pagination, filterPhenotype, false);
        PaginationResult<ChebiPhenotypeDisplay> genesInvolvedForDiseaseAll = getDiseasePageRepository().getPhenotypeChebi(term, pagination, filterPhenotype, true);

        int totalCountDirect = genesInvolvedForDiseaseDirect.getTotalCount();
        response.addSupplementalData("countDirect", totalCountDirect);
        int totalCountAll = genesInvolvedForDiseaseAll.getTotalCount();
        response.addSupplementalData("countIncludingChildren", totalCountAll);

        List<ChebiPhenotypeDisplay> displayList;
        if (directAnnotation) {
            displayList = genesInvolvedForDiseaseDirect.getPopulatedResults();
            response.setTotal(totalCountDirect);
        } else {
            displayList = genesInvolvedForDiseaseAll.getPopulatedResults();
            response.setTotal(totalCountAll);
        }
        response.setResults(displayList);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/genes", method = RequestMethod.GET)
    public JsonResultResponse<OmimPhenotypeDisplay> getGenesInvolved(@PathVariable String termID,
                                                                     @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                     @RequestParam(value = "filter.humanGeneName", required = false) String filterHumanGeneName,
                                                                     @RequestParam(value = "filter.zfinGeneName", required = false) String filterZfinGeneName,
                                                                     @RequestParam(value = "filter.omimName", required = false) String filterOmimName,
                                                                     @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                     @RequestParam(value = "filter.omimAccessionID", required = false) String omimAccessionID,
                                                                     @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<OmimPhenotypeDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbIDOrOboId(termID);
        if (term == null)
            return response;

        if (StringUtils.isNotEmpty(filterHumanGeneName)) {
            pagination.addToFilterMap("omimPhenotype.homoSapiensGene.symbol", filterHumanGeneName);
        }
        if (StringUtils.isNotEmpty(filterZfinGeneName)) {
            pagination.addToFilterMap("zfinGene.abbreviation", filterZfinGeneName);
        }
        if (StringUtils.isNotEmpty(filterOmimName)) {
            pagination.addToFilterMap("omimPhenotype.name", filterOmimName);
        }
        if (StringUtils.isNotEmpty(filterTermName)) {
            pagination.addToFilterMap("omimPhenotype.disease.termName", filterTermName);
        }
        if (StringUtils.isNotEmpty(omimAccessionID)) {
            pagination.addToFilterMap("omimPhenotype.omimAccession", omimAccessionID);
        }

        PaginationResult<OmimPhenotypeDisplay> genesInvolvedForDiseaseDirect = OntologyService.getGenesInvolvedForDisease(term, pagination, false);
        PaginationResult<OmimPhenotypeDisplay> genesInvolvedForDiseaseAll = OntologyService.getGenesInvolvedForDisease(term, pagination, true);

        int totalCountDirect = genesInvolvedForDiseaseDirect.getTotalCount();
        response.addSupplementalData("countDirect", totalCountDirect);
        int totalCountAll = genesInvolvedForDiseaseAll.getTotalCount();
        response.addSupplementalData("countIncludingChildren", totalCountAll);

        List<OmimPhenotypeDisplay> displayList;
        if (directAnnotation) {
            displayList = genesInvolvedForDiseaseDirect.getPopulatedResults();
            ;
            response.setTotal(totalCountDirect);
        } else {
            displayList = genesInvolvedForDiseaseAll.getPopulatedResults();
            response.setTotal(totalCountAll);
        }

        response.setResults(displayList);
        HibernateUtil.flushAndCommitCurrentSession();

        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/chebi-zebrafish-models", method = RequestMethod.GET)
    public JsonResultResponse<ChebiFishModelDisplay> getChebiZebrafishModels(@PathVariable String termID,
                                                                             @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                             @RequestParam(value = "filter.fishName", required = false) String filterFishName,
                                                                             @RequestParam(value = "filter.diseaseName", required = false) String filterDiseaseName,
                                                                             @RequestParam(value = "filter.conditionName", required = false) String filterCondition,
                                                                             @RequestParam(value = "filter.evidenceCode", required = false) String filterEvidenceCode,
                                                                             @RequestParam(value = "filter.chebiName", required = false) String filterChebiName,
                                                                             @RequestParam(value = "filter.citation", required = false) String filterCitation,
                                                                             @Version Pagination pagination) {
        HibernateUtil.createTransaction();
        JsonResultResponse<ChebiFishModelDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbIDOrOboId(termID);
        if (term == null)
            return response;

        if (StringUtils.isNotEmpty(filterFishName)) {
            pagination.addFieldFilter(FieldFilter.FISH_NAME, filterFishName);
        }
        if (StringUtils.isNotEmpty(filterDiseaseName)) {
            pagination.addFieldFilter(FieldFilter.DISEASE_NAME, filterDiseaseName);
        }
        if (StringUtils.isNotEmpty(filterCondition)) {
            pagination.addFieldFilter(FieldFilter.CONDITION_NAME, filterCondition);
        }
        if (StringUtils.isNotEmpty(filterChebiName)) {
            pagination.addFieldFilter(FieldFilter.FILTER_TERM_NAME, filterChebiName);
        }
        if (StringUtils.isNotEmpty(filterEvidenceCode)) {
            pagination.addFieldFilter(FieldFilter.FILTER_EVIDENCE, filterEvidenceCode);
        }
        if (StringUtils.isNotEmpty(filterCitation)) {
            pagination.addFieldFilter(FieldFilter.FILTER_REF, filterCitation);
        }

        List<ChebiFishModelDisplay> chebiDirect = OntologyService.getAllChebiFishDiseaseModels(term, false);
        List<ChebiFishModelDisplay> chebiAllChildren = OntologyService.getAllChebiFishDiseaseModels(term, true);

        // filtering
        FilterService<ChebiFishModelDisplay> filterService = new FilterService<>(new ChebiFishModelDisplayFiltering());

        response.addSupplementalData("countDirect", chebiDirect.size());
        response.addSupplementalData("countIncludingChildren", chebiAllChildren.size());

        List<ChebiFishModelDisplay> filteredDisplay = null;
        if (directAnnotation) {
            List<ChebiFishModelDisplay> filteredChebiDirect = filterService.filterAnnotations(chebiDirect, pagination.getFieldFilterValueMap());
            filteredDisplay = filteredChebiDirect;
            response.setTotal(filteredChebiDirect.size());
        } else {
            List<ChebiFishModelDisplay> filteredAllChebi = filterService.filterAnnotations(chebiAllChildren, pagination.getFieldFilterValueMap());
            filteredDisplay = filteredAllChebi;
            response.setTotal(filteredAllChebi.size());
        }
        response.setResults(filteredDisplay.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList()));
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/zebrafish-models", method = RequestMethod.GET)
    public JsonResultResponse<FishModelDisplay> getZebrafishModels(@PathVariable String termID,
                                                                   @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                   @RequestParam(value = "filter.fishName", required = false) String filterFishName,
                                                                   @RequestParam(value = "filter.diseaseName", required = false) String filterDiseaseName,
                                                                   @RequestParam(value = "filter.conditionName", required = false) String filterCondition,
                                                                   @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<FishModelDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbIDOrOboId(termID);
        if (term == null)
            return response;

        if (StringUtils.isNotEmpty(filterFishName)) {
            pagination.addToFilterMap("fishModelDisplay.fish.displayName", filterFishName);
        }
        if (StringUtils.isNotEmpty(filterDiseaseName)) {
            pagination.addToFilterMap("fishModelDisplay.disease.termName", filterDiseaseName);
        }
        if (StringUtils.isNotEmpty(filterCondition)) {
            pagination.addToFilterMap("fishModelDisplay.conditionSearch", filterCondition);
        }

        PaginationResult<FishModelDisplay> genesInvolvedForDiseaseDirect = OntologyService.getFishDiseaseModels(term, pagination, false);
        PaginationResult<FishModelDisplay> genesInvolvedForDiseaseAll = OntologyService.getFishDiseaseModels(term, pagination, true);

        int totalCountDirect = genesInvolvedForDiseaseDirect.getTotalCount();
        response.addSupplementalData("countDirect", totalCountDirect);
        int totalCountAll = genesInvolvedForDiseaseAll.getTotalCount();
        response.addSupplementalData("countIncludingChildren", totalCountAll);

        List<FishModelDisplay> displayList;
        if (directAnnotation) {
            displayList = genesInvolvedForDiseaseDirect.getPopulatedResults();
            ;
            response.setTotal(totalCountDirect);
        } else {
            displayList = genesInvolvedForDiseaseAll.getPopulatedResults();
            response.setTotal(totalCountAll);
        }
        response.setResults(displayList);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/fish/{fishID}/zebrafish-models", method = RequestMethod.GET)
    public JsonResultResponse<FishModelDisplay> getZebrafishModelsByFish(@PathVariable String fishID,
                                                                         @RequestParam(value = "filter.diseaseName", required = false) String filterDiseaseName,
                                                                         @RequestParam(value = "filter.conditionName", required = false) String filterCondition,
                                                                         @RequestParam(value = "filter.fishName", required = false) String filterFishName,
                                                                         @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<FishModelDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        Fish fish = getMutantRepository().getFish(fishID);
        if (fish == null)
            return response;

        if (StringUtils.isNotEmpty(filterDiseaseName)) {
            pagination.addToFilterMap("diseaseModels", filterDiseaseName);
        }
        if (StringUtils.isNotEmpty(filterCondition)) {
            pagination.addToFilterMap("condition", filterCondition);
        }

        retrieveModelDataByFish(fish, response, pagination);
        HibernateUtil.flushAndCommitCurrentSession();

        return response;
    }

    private void retrieveModelDataByFish(Fish fish, JsonResultResponse<FishModelDisplay> response, Pagination pagination) {
        List<FishModelDisplay> diseaseModelsWithFishModel = OntologyService.getDiseaseModelsByFishModelsGrouped(fish, pagination);
        if (CollectionUtils.isNotEmpty(diseaseModelsWithFishModel)) {
            response.setResults(diseaseModelsWithFishModel.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
            response.setTotal(diseaseModelsWithFishModel.size());
        }
    }

    private void populateFormBeanForMutantList(GenericTerm ai, AnatomySearchBean form, PaginationResult<Fish> fishResult, boolean includeSubstructures) {
        form.setFishCount(fishResult.getTotalCount());
        form.setTotalRecords(fishResult.getTotalCount());
        form.setQueryString(request.getQueryString());
        form.setRequestUrl(new StringBuffer(request.getRequestURI()));

        List<Fish> fishList = fishResult.getPopulatedResults();
        form.setFish(fishList);
        List<FishStatistics> genoStats = createGenotypeStats(fishList, ai, includeSubstructures);
        form.setGenotypeStatistics(genoStats);
    }

    private List<FishStatistics> createGenotypeStats(List<Fish> fishList, GenericTerm ai, boolean includeSubstructures) {
        if (fishList == null || ai == null)
            return null;

        List<FishStatistics> stats = new ArrayList<>();
        for (Fish fish : fishList) {
            FishStatistics stat = new FishStatistics(fish, ai, includeSubstructures);
            stats.add(stat);
        }
        return stats;
    }

    private void retrieveAntibodyData(GenericTerm aoTerm, AnatomySearchBean form, Pagination pagi, boolean directAnnotation) {

        PaginationResult<org.zfin.mutant.presentation.AntibodyStatistics> antibodies = AnatomyService.getAntibodyStatistics(aoTerm, pagi, !directAnnotation);
        form.setAntibodyStatistics(antibodies.getPopulatedResults());
        form.setAntibodyCount(antibodies.getTotalCount());
        // if direct annotations are empty check for included ones
        if (directAnnotation) {
            int totalCount = RepositoryFactory.getAntibodyRepository().getAntibodyCount(aoTerm, true, pagi);
            form.setCountDirect(antibodies.getTotalCount());
            form.setCountIncludingChildren(totalCount);
        } else {
            int totalCount = RepositoryFactory.getAntibodyRepository().getAntibodyCount(aoTerm, false, pagi);
            form.setCountIncludingChildren(antibodies.getTotalCount());
            form.setCountDirect(totalCount);
        }
    }

    private void retrieveHighQualityProbeData(GenericTerm aoTerm, AnatomySearchBean form, Pagination pagi, boolean directAnnotation) {
        PaginationResult<HighQualityProbe> antibodies = AnatomyService.getHighQualityProbeStatistics(aoTerm, pagi, !directAnnotation);
        form.setHighQualityProbeGenes(antibodies.getPopulatedResults());
        form.setNumberOfHighQualityProbes(antibodies.getTotalCount());
        // if direct annotations are empty check for included ones
        if (directAnnotation) {
            int totalCount = RepositoryFactory.getAntibodyRepository().getProbeCount(aoTerm, true, pagi);
            form.setCountDirect(antibodies.getTotalCount());
            form.setCountIncludingChildren(totalCount);
        } else {
            int totalCount = RepositoryFactory.getAntibodyRepository().getProbeCount(aoTerm, false, pagi);
            form.setCountIncludingChildren(antibodies.getTotalCount());
            form.setCountDirect(totalCount);
        }
    }

    private void retrieveExpressedGenesData(GenericTerm anatomyTerm, AnatomySearchBean form, Pagination pagination) {

        PaginationResult<MarkerStatistic> expressionMarkersResult =
            getPublicationRepository().getAllExpressedMarkers(anatomyTerm, pagination);

        List<MarkerStatistic> markers = expressionMarkersResult.getPopulatedResults();
        form.setExpressedGeneCount(expressionMarkersResult.getTotalCount());
        List<ExpressedGeneDisplay> expressedGenes = new ArrayList<>();
        if (markers != null) {
            for (MarkerStatistic marker : markers) {
                ExpressedGeneDisplay expressedGene = new ExpressedGeneDisplay(marker);
                expressedGenes.add(expressedGene);
            }
        }

        form.setAllExpressedMarkers(expressedGenes);
        form.setTotalNumberOfExpressedGenes(expressionMarkersResult.getTotalCount());
    }


}

