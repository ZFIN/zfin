package org.zfin.indexer.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.api.View;
import org.zfin.framework.dao.IndexerRunDAO;
import org.zfin.indexer.IndexerInfo;
import org.zfin.indexer.IndexerRun;
import org.zfin.indexer.IndexerTask;
import org.zfin.indexer.UiIndexerConfig;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/indexer")
public class IndexerAPIController {

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/runs", method = RequestMethod.GET)
    public JsonResultResponse<IndexerRun> getIndexerRuns(@RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                         @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<IndexerRun> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        pagination.setSortBy("startDate");
        IndexerRunDAO dao = new IndexerRunDAO();
        SearchResponse<IndexerRun> runs = dao.findAll(pagination);
/*
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
*/
        List<IndexerRun> results = runs.getResults();
        results.sort(Comparator.comparing(IndexerRun::getStartDate).reversed());
        results.stream().findFirst()
            .ifPresent(run -> run.setIsRunning(run.getEndDate() == null));
        results.stream().skip(1)
            .forEach(run -> run.setIsRunning(false));

        response.setResults(results);
        response.setTotal(runs.getTotalResults());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public JsonResultResponse<String> getIndexerRunConfig() {

        JsonResultResponse<String> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);

        response.setResults(UiIndexerConfig.getAllIndexerSorted().stream().map(UiIndexerConfig::getTypeName).toList());
        response.setTotal(UiIndexerConfig.getAllIndexerSorted().size());
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/run/{ID}", method = RequestMethod.GET)
    public JsonResultResponse<IndexerInfo> getIndexerRunInfo(@PathVariable String ID,
                                                             @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                             @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<IndexerInfo> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);

        IndexerRunDAO dao = new IndexerRunDAO();
        IndexerRun run = dao.find(Long.valueOf(ID));
/*
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
*/
        List<IndexerInfo> infos = new ArrayList<>(run.getIndexerInfos());
        infos.sort(Comparator.comparing(IndexerInfo::getStartDate));
        infos.stream().filter(indexerInfo -> indexerInfo.getCurrentDuration() != null).skip(1)
            .forEach(indexerInfo -> indexerInfo.setRunning(true));
        response.setResults(run.getIndexerInfos());
        response.setTotal(run.getIndexerInfos().size());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/run/{ID}/info/{infoID}", method = RequestMethod.GET)
    public JsonResultResponse<IndexerTask> getIndexerRunTask(@PathVariable String ID,
                                                             @PathVariable String infoID,
                                                             @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                             @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<IndexerTask> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);

        IndexerRunDAO dao = new IndexerRunDAO();
        IndexerRun run = dao.find(Long.valueOf(ID));
        Set<IndexerInfo> indexerInfos = run.getIndexerInfos().stream().filter(indexerInfo -> indexerInfo.getId() == Long.parseLong(infoID)).collect(Collectors.toSet());
        List<IndexerTask> infos = new ArrayList<>(indexerInfos.iterator().next().getIndexerTasks());
        infos.sort(Comparator.comparing(IndexerTask::getStartDate));
        infos.stream().filter(indexerInfo -> indexerInfo.getDuration() == null)
            .forEach(indexerInfo -> indexerInfo.setRunning(true));

        response.setResults(infos);
        response.setTotal(infos.size());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

}

