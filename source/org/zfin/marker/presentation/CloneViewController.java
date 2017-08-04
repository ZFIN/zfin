package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.Species;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;

@Controller
@RequestMapping("/marker")
public class CloneViewController {

    private Logger logger = Logger.getLogger(CloneViewController.class);

    private ReferenceDatabase ensemblDatabase = null;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

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

        // gbrowse image
        cloneBean.setImage(GBrowseImage.builder()
                .landmark("genomic_clone:" + clone.getZdbID())
                .highlight(clone.getAbbreviation())
                .tracks(GBrowseTrack.COMPLETE_CLONES, GBrowseTrack.GENES, GBrowseTrack.TRANSCRIPTS)
                .build()
        );

        model.addAttribute(LookupStrings.FORM_BEAN, cloneBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getTitleString() + clone.getAbbreviation());

        return "marker/clone-view.page";
    }

    @RequestMapping( value = "/dbsnp",method = RequestMethod.GET)
    protected String getDbsnpView(
            Model model
            ,@RequestParam("cloneId") String cloneId
            ,@ModelAttribute("formBean") MarkerBean formBean
            ,BindingResult result
    ) throws Exception {

        Marker clone = RepositoryFactory.getMarkerRepository().getMarkerByID(cloneId);

        formBean.setMarker(clone);

        String snpsString = RepositoryFactory.getMarkerRepository().getDbsnps(cloneId);
        model.addAttribute("dbsnps", snpsString);
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, clone.getAbbreviation());

        return "marker/dbsnp-view.page";
    }
}