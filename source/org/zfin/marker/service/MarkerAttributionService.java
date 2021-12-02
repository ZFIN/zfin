package org.zfin.marker.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class MarkerAttributionService {
    private static transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static transient Logger logger = LogManager.getLogger(MarkerAttributionService.class);

    public static void addAttributionForMarkerName(String markerAbbrev, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(markerAbbrev);
        if (m == null) {
            throw new TermNotFoundException(markerAbbrev, "Marker");
        }
        String markerZdbID = m.getZdbID();
        if (infrastructureRepository.getRecordAttribution(markerZdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            throw new DuplicateEntryException(m.getAbbreviation() + " is already attributed.");
        }

        infrastructureRepository.insertRecordAttribution(markerZdbID, pubZdbID);
        infrastructureRepository.insertUpdatesTable(markerZdbID, "record attribution", "", pubZdbID, "Added direct attribution");

        List<Marker> targetedGenes = getTargetedGenes(m);
        for(Marker gene : targetedGenes) {
            if (infrastructureRepository.getRecordAttribution(gene.zdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) == null) {
                infrastructureRepository.insertRecordAttribution(gene.getZdbID(), pubZdbID);
                infrastructureRepository.insertUpdatesTable(gene.getZdbID(), "record attribution", "", pubZdbID, "Added inferred gene attribution");
            }
        }
    }

    public static List<Marker> getTargetedGenes(Marker m) {
        ArrayList<Marker> results = new ArrayList<Marker>();
        SequenceTargetingReagent str = getMarkerRepository().getSequenceTargetingReagent(m.zdbID);

        if (str == null) {
            return results;
        }

        ActiveData.Type activeDataType = ActiveData.validateID(str.getZdbID());
        if ( !(activeDataType == ActiveData.Type.MRPHLNO ||
                activeDataType == ActiveData.Type.CRISPR ||
                activeDataType == ActiveData.Type.TALEN)) {
            return results;
        }

        return str.getTargetGenes();
    }

}
