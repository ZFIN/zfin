package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.Species;
import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.presentation.GenomeBrowserFactory;
import org.zfin.genomebrowser.presentation.GenomeBrowserImageBuilder;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.mapping.MappingService;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.mapping.presentation.BrowserLink;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerNotFoundException;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;
import java.util.TreeSet;

@Controller
@RequestMapping("/marker")
public class CloneViewController {

	private Logger logger = LogManager.getLogger(CloneViewController.class);

	private ReferenceDatabase ensemblDatabase;

	@Autowired
	private ExpressionService expressionService;

	@Autowired
	private ExpressionSearchService expressionSearchService;

	@Autowired
	private MarkerRepository markerRepository;

	@Autowired
	private MarkerService markerService;

	@Autowired
	private GenomeBrowserFactory genomeBrowserFactory;

	public CloneViewController() {
		ensemblDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
			ForeignDB.AvailableName.ENSEMBL_CLONE,
			ForeignDBDataType.DataType.OTHER,
			ForeignDBDataType.SuperType.SUMMARY_PAGE,
			Species.Type.ZEBRAFISH
		);
		HibernateUtil.closeSession();
	}

	@RequestMapping(value = "/clone/view/{zdbID}")
	public String getCloneView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
		CanonicalLinkConfig.addCanonicalIfFound(model);
		// set base bean
		CloneBean cloneBean = new CloneBean();

		zdbID = markerService.getActiveMarkerID(zdbID);
		logger.info("zdbID: " + zdbID);
		Clone clone = markerRepository.getCloneById(zdbID);
		logger.info("clone: " + clone);
		cloneBean.setMarker(clone);

		MarkerService.createDefaultViewForMarker(cloneBean);

		// if it is a gene, also add any clones if related via a transcript
		MarkerService.pullGeneOntoCloneFromTranscript(cloneBean);

		List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(clone.getZdbID());
		cloneBean.setSuppliers(suppliers);

		if (clone.isRnaClone()) {
			cloneBean.setMarkerExpression(expressionService.getExpressionForRnaClone(clone));
		}

		// OTHER GENE / MARKER PAGES:
		cloneBean.addFakePubs(ensemblDatabase);

		// iterate through related marker list to add snps to it (if a dna clone)
		// this is technically a small list, so should be cheap
		if (!clone.isRnaClone() && RepositoryFactory.getMarkerRepository().cloneHasSnp(clone)) {
			List<MarkerRelationshipPresentation> markerRelationshipPresentationList = cloneBean.getMarkerRelationshipPresentationList();
			MarkerRelationshipPresentation snpPresentation = new SnpMarkerRelationshipPresentation();
			snpPresentation.setZdbId(zdbID);
			markerRelationshipPresentationList.add(snpPresentation);
		}

		// check whether we are a thisse probe
		cloneBean.setThisseProbe(expressionService.isThisseProbe(clone));

		List<MarkerGenomeLocation> cloneLocations = RepositoryFactory.getLinkageRepository().getGenomeLocationWithCoordinates(clone);
		if (cloneLocations.size() > 0) {
			GenomeBrowserImageBuilder imageBuilder = genomeBrowserFactory.getImageBuilder();
			imageBuilder.genomeBuild(GenomeBrowserBuild.GRCZ11);
			cloneBean.setImage(imageBuilder.buildForClone(clone));
			TreeSet<BrowserLink> locations = MappingService.getJBrowserBrowserLinksForClones(cloneBean.getImage());
			cloneBean.setLocations(locations);
		}
		model.addAttribute(LookupStrings.FORM_BEAN, cloneBean);
		model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getTitleString() + clone.getAbbreviation());

		return "marker/clone/clone-view";
	}

	@RequestMapping(value = "/clone/view/{zdbID}/expression")
	public String getCloneExpressionView(@PathVariable("zdbID") String zdbID) throws MarkerNotFoundException {
		return expressionSearchService.forwardToExpressionSearchForMarker(zdbID);
	}

	@RequestMapping(value = "/dbsnp", method = RequestMethod.GET)
	protected String getDbsnpView(
		Model model
		, @RequestParam("cloneId") String cloneId
		, @ModelAttribute("formBean") MarkerBean formBean
		, BindingResult result
	) {

		Marker clone = RepositoryFactory.getMarkerRepository().getMarkerByID(cloneId);

		formBean.setMarker(clone);

		String snpsString = RepositoryFactory.getMarkerRepository().getDbsnps(cloneId);
		model.addAttribute("dbsnps", snpsString);
		model.addAttribute(LookupStrings.FORM_BEAN, formBean);
		model.addAttribute(LookupStrings.DYNAMIC_TITLE, clone.getAbbreviation());

		return "marker/dbsnp-view";
	}
}
