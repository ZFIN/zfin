package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.List;

/**
 * This class shows stemloops for a specified gene.
 */
@Controller
public class StemLoopController {

    private static Logger logger = Logger.getLogger(MiniGeneController.class);
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private ReferenceDatabase referenceDatabase ;

    public StemLoopController(){
        // should be only Loaded MiRNA Stem Loop
        referenceDatabase = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.MIRBASE_STEM_LOOP,
                ForeignDBDataType.DataType.OTHER,
                ForeignDBDataType.SuperType.SUMMARY_PAGE,
                Species.ZEBRAFISH
        );
    }

    @RequestMapping(value="/stemloop-sequence")
    public String getStemLoopInfo(Model model
            ,@RequestParam("zdbID") String zdbID
    ) throws Exception {
        logger.debug("zdbID: " + zdbID);
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.debug("marker: " + marker);

        // should be only Loaded MiRNA Stem Loop
        List<Sequence> sequences = MountedWublastBlastService.getInstance()
                .getSequencesForMarker(marker, referenceDatabase);

        // if there are none there, then load the curated microRNA stem loops
        if (CollectionUtils.isEmpty(sequences)) {
            referenceDatabase = sequenceRepository.getZebrafishSequenceReferenceDatabase(
                    ForeignDB.AvailableName.CURATED_MIRNA_STEM_LOOP, ForeignDBDataType.DataType.RNA);
            sequences = MountedWublastBlastService.getInstance().getSequencesForMarker(marker, referenceDatabase);
        }

        model.addAttribute(LookupStrings.FORM_BEAN,sequences);
        return "marker/stemloop-sequence.insert";
    }
}
