package org.zfin.marker.service;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationType;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class MarkerAttributionService {
    private static transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static transient Logger logger = LogManager.getLogger(MarkerAttributionService.class);

    public static RecordAttribution addAttributionForMarkerName(String markerAbbrev, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(markerAbbrev);
        if (m == null) {
            throw new TermNotFoundException(markerAbbrev, "Marker");
        }
        return addAttributionForMarker( m, pubZdbID);
    }

    public static RecordAttribution addAttributionForMarkerID(String markerZdbID, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        Marker m = RepositoryFactory.getMarkerRepository().getMarker(markerZdbID);
        if (m == null) {
            throw new TermNotFoundException(markerZdbID, "Marker");
        }
        return addAttributionForMarker( m, pubZdbID);
    }

    public static RecordAttribution addAttributionForMarker(Marker marker, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        String markerZdbID = marker.getZdbID();
        if (infrastructureRepository.getRecordAttribution(markerZdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            throw new DuplicateEntryException(marker.getAbbreviation() + " is already attributed.");
        }

        RecordAttribution result = infrastructureRepository.insertRecordAttribution(markerZdbID, pubZdbID);
        infrastructureRepository.insertUpdatesTable(markerZdbID, "record attribution", "", pubZdbID, "Added direct attribution");

        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubZdbID);
        if (publication.getType() != PublicationType.JOURNAL) {
            return result;
        }

        List<Marker> targetedGenes = getTargetedGenes(marker);
        for (Marker gene : targetedGenes) {
            infrastructureRepository.insertRecordAttribution(gene.getZdbID(), pubZdbID);
            infrastructureRepository.insertUpdatesTable(gene.getZdbID(), "record attribution", "", pubZdbID, "Added inferred gene attribution");
        }

        return result;
    }

    public static List<Marker> getTargetedGenes(Marker m) {
        ArrayList<Marker> results = new ArrayList<>();
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

    public static String deleteRecordAttribution(String markerZdbID, String pubZdbID) {
        String returnMessage = createRemoveAttributionMessage(markerZdbID, pubZdbID);
        if (StringUtils.isNotEmpty(returnMessage)) {
            return returnMessage;
        }
        infrastructureRepository.deleteRecordAttribution(markerZdbID, pubZdbID);
        DataAlias dAlias = infrastructureRepository.getDataAliasByID(markerZdbID);
        if (dAlias != null) {
            infrastructureRepository.deleteRecordAttribution(dAlias.getZdbID(), pubZdbID);
        }

        infrastructureRepository.insertUpdatesTable(markerZdbID, "record attribution", pubZdbID, "removed", "Removed direct attribution");
        return null;
    }

    /**
     * @param zdbID    ZdbID to check against.
     * @param pubZdbID Pub to check against.
     * @return A message if there is a problem.  Otherwise null.
     */
    private static String createRemoveAttributionMessage(String zdbID, String pubZdbID) {

        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubZdbID);
        if (publication == null) {
            return "Invalid pub: " + pubZdbID;
        }


        // always check db-links first
        if (infrastructureRepository.getDBLinkAttributions(zdbID, pubZdbID) > 0) {
            return createMessage(zdbID, "is associated via a dblink that is");
        }

        // if used in inference of markergoentry
        // if there is a pub with a marker go entry on it that has inferences

        // if feature
        if (zdbID.startsWith("ZDB-ALT-")) {
            Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbID);
            return createRemoveAttributionMessageForFeature(feature, publication);
        }

        // if feature
        if (zdbID.startsWith("ZDB-GENO-")) {
            Genotype genotype = getMutantRepository().getGenotypeByID(zdbID);
            return createRemoveAttributionMessageForGenotype(genotype, publication);
        }

        // if a marker
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (marker != null) {
            // handle marker case
            return createRemoveAttributionMessageForMarker(marker, publication);
        }

        // if a fish
        List<DiseaseAnnotation> diseaseAnnotationList = getMutantRepository().getDiseaseModel(zdbID, pubZdbID);
        if (diseaseAnnotationList != null) {
            for (DiseaseAnnotation model : diseaseAnnotationList) {
               /* if (model.getDiseaseAnnotationModel() != null)
                    return createMessage(model.getEntityName(), "has a fishmodel");*/
            }
            return null;
        }

        // if anything else
        // direct association
        if (!zdbID.startsWith("ZDB-GENO") && infrastructureRepository.
                getRecordAttribution(zdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            return createMessage(zdbID, "is directly ");
        }
//            if (infrastructureRepository.getDataAliasesAttributions(zdbID, pubZdbID) > 0) {
//                return createMessage(zdbID,"has data aliases") ;
//            }

        // if marker
        return null;
    }

    private static String createMessage(String name, String message) {
        return "Can't remove " + name + ": It " + message + " attributed to this pub.";
    }

    private static String createRemoveAttributionMessageForGenotype(Genotype genotype, Publication publication) {
        if (getMutantRepository().getZFINInferences(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is inferred as GO evidence that is");
        }

        if (infrastructureRepository.getGenotypeExperimentRecordAttributions(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is used in an experiment that is");
        }

        if (infrastructureRepository.getGenotypeExpressionExperimentRecordAttributions(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is used in expression that is");
        }

        if (infrastructureRepository.getGenotypePhenotypeRecordAttributions(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is used in phenotype that is");
        }

        return null;
    }

    private static String createRemoveAttributionMessageForMarker(Marker marker, Publication publication) {

        if (getMutantRepository().getZFINInferences(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "is inferred as GO evidence that is ");
        }

        if (infrastructureRepository.getGoRecordAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has GO annotations ");
        }

        if (infrastructureRepository.getOrthologRecordAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has been annotated with orthologs ");
        }

        if (infrastructureRepository.getMarkerFeatureRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has a related feature ");
        }

        if (infrastructureRepository.getMarkerGenotypeFeatureRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has a relationship to a feature and genotype is ");
        }

        if (infrastructureRepository.getDBLinkAssociatedToGeneAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "is related to a dblink that is ");
        }

        // see fogbugz 5872
        // we can not remove a gene if it has related markers that are attributed to this pub
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            if (infrastructureRepository.getFirstMarkerRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
                return createMessage(marker.getAbbreviation(), "is related to a marker (in the second position) that is ");
            }

            if (infrastructureRepository.getSecondMarkerRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
                return createMessage(marker.getAbbreviation(), "is related a marker (in the first position) that is ");
            }
        }

        if (infrastructureRepository.getExpressionExperimentMarkerAttributions(marker, publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has expression data ");
        }

        if (infrastructureRepository.getSequenceTargetingReagentEnvironmentAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "is present in an environment");
        }

        return null;
    }

    private static String createRemoveAttributionMessageForFeature(Feature feature, Publication publication) {

        if (infrastructureRepository.getFeatureGenotypeAttributions(feature.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(feature.getAbbreviation(), "used in a genotype");
        }

        return null;
    }


}
