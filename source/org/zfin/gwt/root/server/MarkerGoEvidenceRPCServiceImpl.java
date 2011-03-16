package org.zfin.gwt.root.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;
import org.zfin.gwt.root.ui.PublicationSessionKey;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.orthology.Species;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.service.UniprotService;

import java.util.*;

/**
 */
public class MarkerGoEvidenceRPCServiceImpl extends RemoteServiceServlet implements MarkerGoEvidenceRPCService {

    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private transient MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private transient MarkerGoTermEvidenceRepository markerGoTermEvidenceRepository = RepositoryFactory.getMarkerGoTermEvidenceRepository();
    private transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private final static transient Logger logger = Logger.getLogger(MarkerGoEvidenceRPCServiceImpl.class);
    private transient UniprotService uniprotService = new UniprotService();

    private transient GafOrganization zfinGafOrganization;

    public MarkerGoEvidenceRPCServiceImpl() {
        zfinGafOrganization = markerGoTermEvidenceRepository.getGafOrganization(GafOrganization.OrganizationEnum.ZFIN);
    }

    @Override
    public GoEvidenceDTO getMarkerGoTermEvidenceDTO(String goTermEvidenceZdbID) {
        MarkerGoTermEvidence markerGoTermEvidence = markerGoTermEvidenceRepository.getMarkerGoTermEvidenceByZdbID(goTermEvidenceZdbID);
        return DTOConversionService.convertToGoEvidenceDTO(markerGoTermEvidence);
    }


    @Override
    public GoEvidenceDTO editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO) throws DuplicateEntryException {
        // retrieve
        MarkerGoTermEvidence markerGoTermEvidence = markerGoTermEvidenceRepository.getMarkerGoTermEvidenceByZdbID(goEvidenceDTO.getZdbID());
        String oldValueString = markerGoTermEvidence.toString();

        HibernateUtil.createTransaction();
        // set modified by
        Person person = Person.getCurrentSecurityUser();

        Marker marker;
        if (StringUtils.isNotEmpty(goEvidenceDTO.getMarkerDTO().getZdbID())) {
            marker = markerRepository.getMarkerByID(goEvidenceDTO.getMarkerDTO().getZdbID());
        } else if (StringUtils.isNotEmpty(goEvidenceDTO.getMarkerDTO().getName())) {
            marker = markerRepository.getMarkerByAbbreviation(goEvidenceDTO.getMarkerDTO().getName());
        } else {
            throw new RuntimeException("Failed to create marker go term.  Bad MarkerDTO passed in.");
        }
        markerGoTermEvidence.setMarker(marker);

        GenericTerm goTerm = (GenericTerm) HibernateUtil.currentSession().get(GenericTerm.class, goEvidenceDTO.getGoTerm().getZdbID());
        markerGoTermEvidence.setGoTerm(goTerm);
//
        if (person != null) {
            markerGoTermEvidence.setModifiedBy(person);
        }
        markerGoTermEvidence.setModifiedWhen(new Date());


        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(goEvidenceDTO.getPublicationZdbID());
        markerGoTermEvidence.setSource(publication);

        GoEvidenceCode goEvidenceCode = markerGoTermEvidenceRepository.getGoEvidenceCode(goEvidenceDTO.getEvidenceCode().name());
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        markerGoTermEvidence.setFlag(goEvidenceDTO.getFlag());

        markerGoTermEvidence.setNote(goEvidenceDTO.getNote());

        // add or remove inferences depending on sets returned
        Set<InferenceGroupMember> existingInferenceGroupMembers = markerGoTermEvidence.getInferredFrom();

        Set<String> existingInferenceStrings = new TreeSet<String>();
        for (InferenceGroupMember inferenceGroupMember : existingInferenceGroupMembers) {
            existingInferenceStrings.add(inferenceGroupMember.getInferredFrom());
        }
        Set<String> newInferenceStrings = new TreeSet<String>(goEvidenceDTO.getInferredFrom());

        Collection<String> inferencesToAdd = CollectionUtils.subtract(newInferenceStrings, existingInferenceStrings);
        Collection<String> inferencesToRemove = CollectionUtils.subtract(existingInferenceStrings, newInferenceStrings);


        for (String inference : inferencesToAdd) {
            mutantRepository.addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, inference);
        }

        for (String inference : inferencesToRemove) {
            mutantRepository.removeInferenceToGoMarkerTermEvidence(markerGoTermEvidence, inference);
        }

        infrastructureRepository.insertUpdatesTable(markerGoTermEvidence.getZdbID(), "MarkerGoTermEvidence", oldValueString, markerGoTermEvidence.toString(), "Updated MarkerGoTermEvidence record");

        // already saved, sadly, so we just check to see that there is more than one.
        // fogbugz 5656
        if (mutantRepository.getNumberMarkerGoTermEvidences(markerGoTermEvidence) > 1) {
            logger.warn("Duplicate marker go evidence attempted: " + markerGoTermEvidence);
            throw new DuplicateEntryException("GO annotation not saved because it is identical to an existing annotation.");
        }

        HibernateUtil.flushAndCommitCurrentSession();

        try {
            PublicationService.addRecentPublications(getServletContext(), publication, PublicationSessionKey.GOCURATION);
        } catch (Exception e) {
            logger.debug("Unable to add a recent pub, should only be a problem in test mode", e);
        }

        // do a safe return this way
        return getMarkerGoTermEvidenceDTO(goEvidenceDTO.getZdbID());
    }

    @Override
    public List<MarkerDTO> getGenesForGOAttributions(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<MarkerDTO> relatedEntityDTOs = new ArrayList<MarkerDTO>();

        if (publication != null) {
            List<Marker> genes = markerRepository.getMarkersForStandardAttributionAndType(publication, "GENE");
            if (CollectionUtils.isNotEmpty(genes)) {
                for (Marker gene : genes) {
                    relatedEntityDTOs.add(DTOConversionService.convertToMarkerDTO(gene));
                }
            }
        }
        return relatedEntityDTOs;

    }

    @Override
    public List<RelatedEntityDTO> getGenotypesAndMorpholinosForGOAttributions(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<RelatedEntityDTO> relatedEntityDTOs = new ArrayList<RelatedEntityDTO>();

        if (publication != null) {
            List<Marker> morpholinos = markerRepository.getMarkersForStandardAttributionAndType(publication, "MRPHLNO");
            if (CollectionUtils.isNotEmpty(morpholinos)) {
                RelatedEntityDTO morpholinoLabelDTO = new RelatedEntityDTO();
                morpholinoLabelDTO.setName("Morpholinos:");
                relatedEntityDTOs.add(morpholinoLabelDTO);
                for (Marker gene : morpholinos) {
                    relatedEntityDTOs.add(DTOConversionService.convertToMarkerDTO(gene));
                }
            }

            // get genotypes
            List<Genotype> genotypes = mutantRepository.getGenotypesForStandardAttribution(publication);
            if (CollectionUtils.isNotEmpty(genotypes)) {
                RelatedEntityDTO genotypeLabelDTO = new RelatedEntityDTO();
                genotypeLabelDTO.setName("Genotypes:");
                relatedEntityDTOs.add(genotypeLabelDTO);
                for (Genotype genotype : genotypes) {
                    relatedEntityDTOs.add(DTOConversionService.convertToGenotypeDTO(genotype));
                }
            }
        }

        return relatedEntityDTOs;
    }


    @Override
    public boolean validateAccession(String accession, String inferenceCategory) {
        if (inferenceCategory.equals(InferenceCategory.GENBANK.name())
                ||
                inferenceCategory.equals(InferenceCategory.REFSEQ.name())
                ||
                inferenceCategory.equals(InferenceCategory.GENPEPT.name())
                ) {
            return NCBIEfetch.validateAccession(accession);
        } else if (inferenceCategory.equals(InferenceCategory.UNIPROTKB.name())) {
            return uniprotService.validateAccession(accession);
        }
        return true;
    }


    @Override
    public List<GoEvidenceDTO> getGOTermsForPubAndMarker(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<GoEvidenceDTO> goEvidenceDTOs = new ArrayList<GoEvidenceDTO>();

        if (publication != null) {
            // get genes
            Marker marker = markerRepository.getMarkerByID(dto.getMarkerDTO().getZdbID());
            List<GenericTerm> goTerms = mutantRepository.getGoTermsByMarkerAndPublication(marker, publication);
            for (GenericTerm goTerm : goTerms) {
                GoEvidenceDTO relatedEntityDTO = new GoEvidenceDTO();
                relatedEntityDTO.setName(goTerm.getTermName());
                relatedEntityDTO.setZdbID(goTerm.getZdbID());
                relatedEntityDTO.setDataZdbID(goTerm.getZdbID());
                relatedEntityDTO.setGoTerm(DTOConversionService.convertToTermDTO(goTerm));
                relatedEntityDTO.setPublicationZdbID(dto.getPublicationZdbID());
                goEvidenceDTOs.add(relatedEntityDTO);
            }
        }

        return goEvidenceDTOs;
    }


    /**
     * Returns inferences already inferred from a marker.
     *
     * @param zdbID             Marker zdbID.
     * @param inferenceCategory Category of inferences.  All inferences must start with this category.
     * @return Set of inferences.
     */
    @SuppressWarnings("unchecked")
    protected Set<String> getInferencesFromPreviousInferences(String zdbID, String inferenceCategory) {
        Set<String> inferredFromSet = new TreeSet<String>();

        // for interpro and EC, these should come form db_links and for SP_KW, from previous inferences
        List<MarkerGoTermEvidence> markerGoTermEvidenceList = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbID(zdbID);

        for (MarkerGoTermEvidence markerGoTermEvidence : markerGoTermEvidenceList) {
            for (InferenceGroupMember inferenceGroupMember : markerGoTermEvidence.getInferredFrom()) {
                if (inferenceGroupMember.getInferredFrom().startsWith(inferenceCategory)) {
                    inferredFromSet.add(inferenceGroupMember.getInferredFrom());
                }
            }
        }
        return inferredFromSet;

    }

    @Override
    public Set<String> getInferencesByMarkerAndType(GoEvidenceDTO dto, String inferenceCategory) {
        // treeset to sort by string order
        Set<String> inferredFromSet = new TreeSet<String>();
        // for interpro and EC, these should come form db_links and for SP_KW, from previous inferences

        if (inferenceCategory.startsWith(InferenceCategory.EC.prefix())) {
            ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.EC,
                    ForeignDBDataType.DataType.DOMAIN,
                    ForeignDBDataType.SuperType.PROTEIN,
                    Species.ZEBRAFISH
            );
            inferredFromSet.addAll(getInferencesByDBLink(dto.getMarkerDTO().getZdbID(), referenceDatabase));
        } else if (inferenceCategory.startsWith(InferenceCategory.INTERPRO.prefix())) {
            ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.INTERPRO,
                    ForeignDBDataType.DataType.DOMAIN,
                    ForeignDBDataType.SuperType.PROTEIN,
                    Species.ZEBRAFISH
            );
            inferredFromSet.addAll(getInferencesByDBLink(dto.getMarkerDTO().getZdbID(), referenceDatabase));
        }

        inferredFromSet.addAll(getInferencesFromPreviousInferences(dto.getMarkerDTO().getZdbID(), inferenceCategory));

        return inferredFromSet;
    }

    protected Set<String> getInferencesByDBLink(String zdbID, ReferenceDatabase referenceDatabase) {
        Marker marker = (Marker) HibernateUtil.currentSession().get(Marker.class, zdbID);
        List<MarkerDBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinksForMarker(marker, referenceDatabase);
        Set<String> inferredFromSet = new TreeSet<String>();
        for (MarkerDBLink dbLink : dbLinks) {
            inferredFromSet.add(referenceDatabase.getForeignDB().getDbName() + ":" + dbLink.getAccessionNumber());
        }

        return inferredFromSet;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public GoEvidenceDTO createMarkerGoTermEvidence(GoEvidenceDTO goEvidenceDTO) throws DuplicateEntryException {

        HibernateUtil.createTransaction();
        // set modified by
        Person person = Person.getCurrentSecurityUser();
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();

        markerGoTermEvidence.setExternalLoadDate(null);
        markerGoTermEvidence.setGafOrganization(zfinGafOrganization);
        markerGoTermEvidence.setOrganizationCreatedBy(GafOrganization.OrganizationEnum.ZFIN.name());

        Marker marker;
        if (StringUtils.isNotEmpty(goEvidenceDTO.getMarkerDTO().getZdbID())) {
            marker = markerRepository.getMarkerByID(goEvidenceDTO.getMarkerDTO().getZdbID());
        } else if (StringUtils.isNotEmpty(goEvidenceDTO.getMarkerDTO().getName())) {
            marker = markerRepository.getMarkerByAbbreviation(goEvidenceDTO.getMarkerDTO().getName());
        } else {
            throw new RuntimeException("Failed to create marker go term.  Bad MarkerDTO passed in.");
        }

        markerGoTermEvidence.setMarker(marker);
        GenericTerm goTerm = (GenericTerm) HibernateUtil.currentSession().get(GenericTerm.class, goEvidenceDTO.getGoTerm().getZdbID());
        markerGoTermEvidence.setGoTerm(goTerm);

//

        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(goEvidenceDTO.getPublicationZdbID());
        markerGoTermEvidence.setSource(publication);

        try {
            PublicationService.addRecentPublications(getServletContext(), publication, PublicationSessionKey.GOCURATION);
        } catch (NullPointerException npe) {
            // this is normal for test mode
        }

        GoEvidenceCode goEvidenceCode = markerGoTermEvidenceRepository.getGoEvidenceCode(goEvidenceDTO.getEvidenceCode().name());
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        markerGoTermEvidence.setFlag(goEvidenceDTO.getFlag());
        markerGoTermEvidence.setNote(goEvidenceDTO.getNote());


        if (person != null) {
            markerGoTermEvidence.setModifiedBy(person);
            markerGoTermEvidence.setCreatedBy(person);
        }

        Date rightNow = new Date();
        markerGoTermEvidence.setModifiedWhen(rightNow);
        markerGoTermEvidence.setCreatedWhen(rightNow);

        // implies that the ID is set here
        markerGoTermEvidenceRepository.addEvidence(markerGoTermEvidence);

        // have to do this after we add inferences
        Set<String> newInferenceStrings = new TreeSet<String>(goEvidenceDTO.getInferredFrom());
        for (String inference : newInferenceStrings) {
            mutantRepository.addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, inference);
        }

        infrastructureRepository.insertUpdatesTable(markerGoTermEvidence.getZdbID(), "MarkerGoTermEvidence", markerGoTermEvidence.toString(), "Created new MarkerGoTermEvidence record");


        // already saved, sadly, so we just check to see that there is more than one.
        // fogbugz 5656
        if (mutantRepository.getNumberMarkerGoTermEvidences(markerGoTermEvidence) > 1) {
            logger.warn("Duplicate marker go evidence attempted: " + markerGoTermEvidence);
            throw new DuplicateEntryException("GO annotation not saved because it is identical to an existing annotation.");
        }


        HibernateUtil.flushAndCommitCurrentSession();

        try {
            PublicationService.addRecentPublications(getServletContext(), publication, PublicationSessionKey.GOCURATION);
        } catch (Exception e) {
            logger.debug("Unable to add a recent pub, should only be a problem in test mode", e);
        }
        // do a safe return this way
        return getMarkerGoTermEvidenceDTO(markerGoTermEvidence.getZdbID());
    }

    /**
     * delete from zdb_active_data where zactvd_zdb_id='$OID';
     * execute procedure p_drop_go_unknown_attribution('$mrkrzdbid'); # NOT FOUND
     * insert into updates (submitter_id,submitter_name, rec_id, old_value,field_name, comments,when)
     * values ('$ZDB_ident','$ZDB_name','$mrkrzdbid', '$OID','go annotation','delete this annotation',current);
     *
     * @param zdbID Marker go evidence ZdbID: ZDB-MRKGOEV-%
     */
    @Override
    public void deleteMarkerGoTermEvidence(String zdbID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteActiveDataByZdbID(zdbID);
        infrastructureRepository.insertUpdatesTable(zdbID, "MarkerGoTermEvidence", zdbID, "", "delete this annotation via GO gwt");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GoEvidenceDTO> getMarkerGoTermEvidencesForPub(String publicationID) {
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForPubZdbID(publicationID);

        List<GoEvidenceDTO> goEvidenceDTOs = new ArrayList<GoEvidenceDTO>();
        for (MarkerGoTermEvidence markerGoTermEvidence : evidences) {
            goEvidenceDTOs.add(DTOConversionService.convertToGoEvidenceDTO(markerGoTermEvidence));
        }

        return goEvidenceDTOs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GoEvidenceDTO> getMarkerGoTermEvidencesForMarker(String markerID) {
        HibernateUtil.currentSession();
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbIDOrdered(markerID);

        List<GoEvidenceDTO> goEvidenceDTOs = new ArrayList<GoEvidenceDTO>();
        for (MarkerGoTermEvidence markerGoTermEvidence : evidences) {
            goEvidenceDTOs.add(DTOConversionService.convertToGoEvidenceDTO(markerGoTermEvidence));
        }

        return goEvidenceDTOs;
    }

    @Override
    public List<MarkerDTO> getGenesForPub(String publicationID) {
        List<MarkerDTO> markerDTOs = new ArrayList<MarkerDTO>();
        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkersForAttribution(publicationID);
        if (CollectionUtils.isNotEmpty(markers)) {
            for (Marker m : markers) {
                if (m.getZdbID().startsWith("ZDB-GENE-")) {
                    markerDTOs.add(DTOConversionService.convertToMarkerDTO(m));
                }
            }
        }

        return markerDTOs;
    }

    @Override
    public String createInferenceLink(String inference) {
        return MarkerGoEvidencePresentation.generateInferenceLink(inference);
    }


}
