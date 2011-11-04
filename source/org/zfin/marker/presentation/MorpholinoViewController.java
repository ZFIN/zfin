package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Database;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Controller
public class MorpholinoViewController {

    private Logger logger = Logger.getLogger(MorpholinoViewController.class);

    @Autowired
    private MarkerRepository markerRepository ;

    //    private String ncbiBlastUrl ;
    private List<Database> databases;

    public MorpholinoViewController() {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository()
                .getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName.PUBRNA, ForeignDBDataType.DataType.RNA);
        databases = referenceDatabase.getRelatedBlastDbs();
    }

    @RequestMapping(value = "/morpholino/view/{zdbID}")
    public String getGeneView(
            Model model
            , @RequestParam("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        MorpholinoBean markerBean = new MorpholinoBean();

        logger.info("zdbID: " + zdbID);
        Marker morpholino = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + morpholino);

        markerBean.setMarker(morpholino);
        MarkerService.createDefaultViewForMarker(markerBean);

        // set targetGenes
//        Set<Marker> targetGenes = MarkerService.getRelatedMarker(morpholino, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
//        markerBean.setTargetGenes(targetGenes);
        List<MarkerRelationshipPresentation> knockdownRelationships = new ArrayList<MarkerRelationshipPresentation>();
        knockdownRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                morpholino, true
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE
        ));
        markerBean.setMarkerRelationshipPresentationList(knockdownRelationships);

        // PHENOTYPE
        markerBean.setPhenotypeOnMarkerBeans(MarkerService.getPhenotypeOnGene(morpholino));

        // add sequence
        markerBean.setSequences(markerRepository.getMarkerSequences(morpholino));

        // get sequence attribution
        if (markerBean.getSequence() != null) {
            List<RecordAttribution> attributions = RepositoryFactory.getInfrastructureRepository()
                    .getRecordAttributionsForType(markerBean.getSequence().getZdbID(), RecordAttribution.SourceType.STANDARD);
            // for this particular set, we only ever want the first one
            if (attributions.size() >= 1) {
                markerBean.setSequenceAttribution(PublicationPresentation.getLink(attributions.iterator().next().getSourceZdbID(), "1"));
            }
        }
        else{
            logger.warn("No sequence available for morpholino: "+markerBean.getZdbID());
        }

        markerBean.setDatabases(databases);

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Morpholino:" + morpholino.getAbbreviation());

        return "marker/morpholino-view.page";
    }
}