package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;

/**
 */
@Controller
public class CloneViewController {

    private Logger logger = Logger.getLogger(CloneViewController.class);

    private ReferenceDatabase ensemblDatabase = null ;

    @Autowired
    private ExpressionService expressionService ;

    @Autowired
    private MarkerRepository markerRepository ;

    public CloneViewController(){
        ensemblDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.ENSEMBL_CLONE
                ,ForeignDBDataType.DataType.OTHER
                ,ForeignDBDataType.SuperType.SUMMARY_PAGE
                , Species.ZEBRAFISH
        );
    }

    @RequestMapping(value = "/clone/view/{zdbID}")
    public String getCloneView(Model model
            ,@PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        CloneBean cloneBean = new CloneBean();

        logger.info("zdbID: " + zdbID);
        Clone clone = markerRepository.getCloneById(zdbID);
        logger.info("clone: " + clone);
        cloneBean.setMarker(clone);

        MarkerService.createDefaultViewForMarker(cloneBean);

        // if it is a gene, also add any clones if related via a transcript
        MarkerService.pullGeneOntoCloneFromTranscript(cloneBean);

        List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(clone.getZdbID());
        cloneBean.setSuppliers(suppliers);

        if(clone.isRnaClone()){
//            String geneLookupSymbol ;
//            List<MarkerRelationshipPresentation> markerRelationshipPresentationList = cloneBean.getMarkerRelationshipPresentationList();
//            for(MarkerRelationshipPresentation markerRelationshipPresentation : markerRelationshipPresentationList){
//                if( markerRelationshipPresentation.getMarkerType().equals("Gene") ){
//                    geneLookupSymbol = markerRelationshipPresentation.getAbbreviation();
//                }
//            }
            cloneBean.setMarkerExpression(expressionService.getExpressionForRnaClone(clone));
        }

        // OTHER GENE / MARKER PAGES:
        cloneBean.addFakePubs(ensemblDatabase);



        // iterate through related marker list to add snps to it (if a dna clone)
        // this is technically a small list, so should be cheap
        if(false==clone.isRnaClone() && RepositoryFactory.getMarkerRepository().cloneHasSnp(clone)){
            List<MarkerRelationshipPresentation> markerRelationshipPresentationList = cloneBean.getMarkerRelationshipPresentationList();
            MarkerRelationshipPresentation snpPresentation = new SnpMarkerRelationshipPresentation();
            snpPresentation.setZdbId(zdbID);
            markerRelationshipPresentationList.add(snpPresentation);
        }


        // check whether we are a thisse probe
        cloneBean.setThisseProbe(expressionService.isThisseProbe(clone));

        // MAPPING INFO:
        cloneBean.setMappedMarkerBean(MarkerService.getMappedMarkers(clone));

        model.addAttribute(LookupStrings.FORM_BEAN, cloneBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getTitleString() + clone.getAbbreviation());

        return "marker/clone-view.page";
    }
}