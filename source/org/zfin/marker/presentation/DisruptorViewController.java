package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
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
public class DisruptorViewController {

    private Logger logger = Logger.getLogger(DisruptorViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    //    private String ncbiBlastUrl ;
    private List<Database> databases;

    public DisruptorViewController() {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository()
                .getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName.PUBRNA, ForeignDBDataType.DataType.RNA);
        databases = referenceDatabase.getRelatedBlastDbs();
    }

    @RequestMapping(value = "/disruptor/view/{zdbID}")
    public String getView(
            Model model
            , @RequestParam("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        DisruptorBean disruptorBean = new DisruptorBean();

        logger.info("zdbID: " + zdbID);
        SequenceTargetingReagent disruptor = markerRepository.getSequenceTargetingReagent(zdbID);
        logger.info("disruptor: " + disruptor);

        disruptorBean.setMarker(disruptor);
        model.addAttribute("disruptor", disruptor);

        MarkerService.createDefaultViewForMarker(disruptorBean);

        // set targetGenes
//        Set<Marker> targetGenes = MarkerService.getRelatedMarker(morpholino, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
//        markerBean.setTargetGenes(targetGenes);
        List<MarkerRelationshipPresentation> knockdownRelationships = new ArrayList<MarkerRelationshipPresentation>();
        knockdownRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                disruptor, true
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE
        ));
        disruptorBean.setMarkerRelationshipPresentationList(knockdownRelationships);

        // PHENOTYPE
        disruptorBean.setPhenotypeOnMarkerBeans(MarkerService.getPhenotypeOnGene(disruptor));

        // GENOTYPE (for CRISPR and TALEN only at this time)
        if (disruptorBean.isTALEN() || disruptorBean.isCRISPR()) {
            disruptorBean.setGenotypes(markerRepository.getTALENorCRISPRcreatedGenotypes(zdbID));
        }

        // add sequence
        //disruptorBean.setSequences(markerRepository.getMarkerSequences(disruptor));

        // get sequence attribution
        if (disruptor.getSequence() != null) {
            List<RecordAttribution> attributions = RepositoryFactory.getInfrastructureRepository()
                    .getRecordAttributionsForType(disruptor.getZdbID(), RecordAttribution.SourceType.SEQUENCE);
            // for this particular set, we only ever want the first one
            if (attributions.size() >= 1) {
                disruptorBean.setSequenceAttribution(PublicationPresentation.getLink(attributions.iterator().next().getSourceZdbID(), "1"));
            }
        } else {
            logger.warn("No sequence available for disruptor: " + disruptorBean.getZdbID());
        }

        disruptorBean.setDatabases(databases);
        String disruptorType = disruptor.getType().toString();

        // Todo: there should be a better place to store the display name for the different STR entities
        if (disruptorType.equals("MRPHLNO")) {
            disruptorType = "Morpholino";
        }

        // set source
        disruptorBean.setSuppliers(markerRepository.getSuppliersForMarker(disruptor.getZdbID()));
        model.addAttribute(LookupStrings.FORM_BEAN, disruptorBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, disruptorType + ": " + disruptor.getAbbreviation());

        return "marker/disruptor-view.page";
    }
}


