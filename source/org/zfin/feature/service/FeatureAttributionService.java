package org.zfin.feature.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerAttributionService;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationType;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class FeatureAttributionService {
    private static transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static transient Logger logger = LogManager.getLogger(FeatureAttributionService.class);

    public static void addFeatureAttribution(String featureAbbrev, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        Feature f = RepositoryFactory.getFeatureRepository().getFeatureByAbbreviation(featureAbbrev);
        if (f == null) {
            throw new TermNotFoundException(featureAbbrev, "Feature");
        }

        String featureZdbID = f.getZdbID();
        if (infrastructureRepository.getRecordAttribution(featureZdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            throw new DuplicateEntryException(f.getAbbreviation() + " is already attributed as " + f.getName());
        }
        infrastructureRepository.insertRecordAttribution(featureZdbID, pubZdbID);
        infrastructureRepository.insertUpdatesTable(featureZdbID, "record attribution", pubZdbID, "Added direct attribution");

        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubZdbID);
        if (publication.getType() != PublicationType.JOURNAL) {
            return;
        }

        if (f.getType().equals(FeatureTypeEnum.TRANSGENIC_INSERTION)) {
            List<Marker> m = RepositoryFactory.getFeatureRepository().getConstructsByFeature(f);
            for (Marker mrkr : m) {
                if (infrastructureRepository.getRecordAttribution(mrkr.zdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) == null) {
                    infrastructureRepository.insertRecordAttribution(mrkr.zdbID, pubZdbID);
                    infrastructureRepository.insertUpdatesTable(mrkr.zdbID, "record attribution", pubZdbID, "Added direct attribution to related construct");
                }
                List<Marker> codingSeq = RepositoryFactory.getMarkerRepository().getCodingSequence(mrkr);
                for (Marker codingGene : codingSeq) {
                    if (infrastructureRepository.getRecordAttribution(codingGene.zdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) == null) {
                        infrastructureRepository.insertRecordAttribution(codingGene.zdbID, pubZdbID);
                        infrastructureRepository.insertUpdatesTable(codingGene.zdbID, "record attribution", pubZdbID, "Added direct attribution to related construct");
                    }
                }
            }
        }
        Set<Marker> genes = f.getAffectedGenes();
        for(Marker gene : genes) {
            if (infrastructureRepository.getRecordAttribution(gene.zdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) == null) {
                infrastructureRepository.insertRecordAttribution(gene.zdbID, pubZdbID);
                infrastructureRepository.insertUpdatesTable(gene.zdbID, "record attribution", pubZdbID, "Added direct attribution to gene related to feature");
            }
        }
    }
}
