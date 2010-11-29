package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This class shows stemloops for a specified gene.
 */
public class StemLoopController extends AbstractController {

    private static Logger logger = Logger.getLogger(MiniGeneController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        logger.info("zdbID: " + zdbID);
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("marker: " + marker);
        SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

        // should be only Loaded MiRNA Stem Loop
        ReferenceDatabase referenceDatabase = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.MIRBASE_STEM_LOOP,
                ForeignDBDataType.DataType.OTHER,
                ForeignDBDataType.SuperType.SUMMARY_PAGE,
                Species.ZEBRAFISH
        );
        List<Sequence> sequences = MountedWublastBlastService.getInstance().getSequencesForMarker(marker, referenceDatabase);

        // if there are none there, then load the curated microRNA stem loops
        if (CollectionUtils.isEmpty(sequences)) {
            referenceDatabase = sequenceRepository.getZebrafishSequenceReferenceDatabase(
                    ForeignDB.AvailableName.CURATED_MIRNA_STEM_LOOP, ForeignDBDataType.DataType.RNA);
            sequences = MountedWublastBlastService.getInstance().getSequencesForMarker(marker, referenceDatabase);
        }

        ModelAndView modelAndView = new ModelAndView("marker/stemloop-sequence.insert", LookupStrings.FORM_BEAN, sequences);
        return modelAndView;
    }
}
