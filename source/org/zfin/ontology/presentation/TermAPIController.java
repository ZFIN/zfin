package org.zfin.ontology.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Fish;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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
                                                                       @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<AntibodyStatistics> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

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
                                                                @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<HighQualityProbe> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

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
                                                                      @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<ExpressedGeneDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

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
                                                            @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<FishStatistics> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        form.setMaxDisplayRecords(pagination.getLimit());
        form.setPageInteger(pagination.getPage());
        retrieveMutantData(term, form, !directAnnotation);
        response.setResults(form.getGenotypeStatistics());
        response.setTotal(form.getTotalRecords());
        if (directAnnotation) {
            response.addSupplementalData("countDirect", form.getTotalRecords());
            response.addSupplementalData("countIncludingChildren", form.getTotalNumberOfExpressedGenes());
        } else {
            response.addSupplementalData("countIncludingChildren", form.getTotalRecords());
            response.addSupplementalData("countDirect", form.getTotalNumberOfExpressedGenes());
        }
        HibernateUtil.flushAndCommitCurrentSession();

        return response;
    }

    private void retrieveMutantData(GenericTerm ai, AnatomySearchBean form, boolean includeSubstructures) {
        PaginationBean bean = new PaginationBean();
        bean.setPageInteger(1);
        bean.setFirstPageRecord(1);
        bean.setMaxDisplayRecords(1);
        PaginationResult<Fish> genotypeResult;
        if (includeSubstructures) {
            genotypeResult = getMutantRepository().getFishByAnatomyTermIncludingSubstructures(ai, false, form);
            PaginationResult<Fish> genotypeResultDirectAnno = getMutantRepository().getFishByAnatomyTerm(ai, false, bean);
            form.setTotalNumberOfExpressedGenes(genotypeResultDirectAnno.getTotalCount());
        } else {
            genotypeResult = getMutantRepository().getFishByAnatomyTerm(ai, false, form);
            PaginationResult<Fish> genotypeResultIncludedChildren = getMutantRepository().getFishByAnatomyTermIncludingSubstructures(ai, false, bean);
            form.setTotalNumberOfExpressedGenes(genotypeResultIncludedChildren.getTotalCount());
        }
        populateFormBeanForMutantList(ai, form, genotypeResult, includeSubstructures);
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

        AnatomyStatistics statistics = getAnatomyRepository().getAnatomyStatisticsForMutants(ai.getZdbID());
        form.setAnatomyStatisticsMutant(statistics);
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

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(pagi.getLimit());
        pagination.setPageInteger(pagi.getPage());
        PaginationResult<org.zfin.mutant.presentation.AntibodyStatistics> antibodies = AnatomyService.getAntibodyStatistics(aoTerm, pagination, !directAnnotation);
        form.setAntibodyStatistics(antibodies.getPopulatedResults());
        form.setAntibodyCount(antibodies.getTotalCount());
        // if direct annotations are empty check for included ones
        if (directAnnotation) {
            int totalCount = RepositoryFactory.getAntibodyRepository().getAntibodyCount(aoTerm, true);
            form.setCountDirect(antibodies.getTotalCount());
            form.setCountIncludingChildren(totalCount);
        } else {
            int totalCount = RepositoryFactory.getAntibodyRepository().getAntibodyCount(aoTerm, false);
            form.setCountIncludingChildren(antibodies.getTotalCount());
            form.setCountDirect(totalCount);
        }
    }

    private void retrieveHighQualityProbeData(GenericTerm aoTerm, AnatomySearchBean form, Pagination pagi, boolean directAnnotation) {
        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(pagi.getLimit());
        pagination.setPageInteger(pagi.getPage());
        PaginationResult<HighQualityProbe> antibodies = AnatomyService.getHighQualityProbeStatistics(aoTerm, pagination, !directAnnotation);
        form.setHighQualityProbeGenes(antibodies.getPopulatedResults());
        form.setNumberOfHighQualityProbes(antibodies.getTotalCount());
        // if direct annotations are empty check for included ones
        if (directAnnotation) {
            int totalCount = RepositoryFactory.getAntibodyRepository().getProbeCount(aoTerm, true);
            form.setCountDirect(antibodies.getTotalCount());
            form.setCountIncludingChildren(totalCount);
        } else {
            int totalCount = RepositoryFactory.getAntibodyRepository().getProbeCount(aoTerm, false);
            form.setCountIncludingChildren(antibodies.getTotalCount());
            form.setCountDirect(totalCount);
        }
    }

    private void retrieveExpressedGenesData(GenericTerm anatomyTerm, AnatomySearchBean form, Pagination pagination) {

        PaginationResult<MarkerStatistic> expressionMarkersResult =
            getPublicationRepository().getAllExpressedMarkers(anatomyTerm, pagination.getStart(), pagination.getLimit());

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
        // todo: could we get this as part of our statistic?
        form.setTotalNumberOfFiguresPerAnatomyItem(getPublicationRepository().getTotalNumberOfFiguresPerAnatomyItem(anatomyTerm));
        // maybe used later?
        form.setTotalNumberOfExpressedGenes(expressionMarkersResult.getTotalCount());

        AnatomyStatistics statistics = getAnatomyRepository().getAnatomyStatistics(anatomyTerm.getZdbID());
        form.setAnatomyStatistics(statistics);
    }


}

