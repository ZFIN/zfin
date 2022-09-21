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
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

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
		retrieveExpressedGenesData(term, form);
		response.setResults(form.getAllExpressedMarkers());
		response.setTotal(form.getTotalNumberOfExpressedGenes());
		response.addSupplementalData("countDirect", form.getTotalNumberOfExpressedGenes());
		response.addSupplementalData("countIncludingChildren", form.getTotalNumberOfExpressedGenes());
		HibernateUtil.flushAndCommitCurrentSession();
		return response;
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

	private void retrieveExpressedGenesData(GenericTerm anatomyTerm, AnatomySearchBean form) {

		PaginationResult<MarkerStatistic> expressionMarkersResult =
			getPublicationRepository().getAllExpressedMarkers(anatomyTerm, 0, AnatomySearchBean.MAX_NUMBER_EPRESSED_GENES);

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

