package org.zfin.gwt.root.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.zfin.datatransfer.webservice.EBIFetch;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;
import org.zfin.gwt.root.ui.PublicationSessionKey;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
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

import java.util.*;

/**
 */
public class MarkerGoEvidenceRPCServiceImpl extends RemoteServiceServlet implements MarkerGoEvidenceRPCService {

    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private transient MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private final static transient Logger logger = Logger.getLogger(MarkerGoEvidenceRPCServiceImpl.class);


    @Override
    public GoEvidenceDTO getMarkerGoTermEvidenceDTO(String goTermEvidenceZdbID) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class);
        criteria.add(Restrictions.eq("zdbID", goTermEvidenceZdbID));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();

        return DTOConversionService.convertToGoEvidenceDTO(markerGoTermEvidence);
    }


    @Override
    public GoEvidenceDTO editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO) throws DuplicateEntryException{
        // retrieve
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class);
        criteria.add(Restrictions.eq("zdbID", goEvidenceDTO.getZdbID()));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();
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

        Term goTerm = (Term) HibernateUtil.currentSession().get(GenericTerm.class, goEvidenceDTO.getGoTerm().getZdbID());
        markerGoTermEvidence.setGoTerm(goTerm);
//
        if (person != null) {
            markerGoTermEvidence.setModifiedBy(person.getName());
        }
        markerGoTermEvidence.setModifiedWhen(new Date());


        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(goEvidenceDTO.getPublicationZdbID());
        markerGoTermEvidence.setSource(publication);

        GoEvidenceCode goEvidenceCode = (GoEvidenceCode) HibernateUtil.currentSession().createCriteria(GoEvidenceCode.class).add(Restrictions.eq("code", goEvidenceDTO.getEvidenceCode().name())).uniqueResult();
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

        infrastructureRepository.insertUpdatesTable(markerGoTermEvidence.getZdbID(), "MarkerGoTermEvidence", oldValueString , markerGoTermEvidence.toString(), "Updated MarkerGoTermEvidence record");

        // already saved, sadly, so we just check to see that there is more than one.
        // fogbugz 5656
        if(mutantRepository.getNumberMarkerGoTermEvidences(markerGoTermEvidence)>1 ){
            logger.warn("Duplicate marker go evidence attempted: "+ markerGoTermEvidence);
            throw new DuplicateEntryException("GO annotation not saved because it is identical to an existing annotation.") ;
        }

        HibernateUtil.flushAndCommitCurrentSession();

        try {
            PublicationService.addRecentPublications(getServletContext(),publication,PublicationSessionKey.GOCURATION) ;
        } catch (Exception e) {
            logger.debug("Unable to add a recent pub, should only be a problem in test mode",e);
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
                ) {
            return NCBIEfetch.validateAccession(accession, true);
        } else if (inferenceCategory.equals(InferenceCategory.GENPEPT.name())) {
            return NCBIEfetch.validateAccession(accession, false);
        } else if (inferenceCategory.equals(InferenceCategory.UNIPROTKB.name())) {
            return EBIFetch.validateAccession(accession);
        }
        return true;  //To change body of created methods use File | Settings | File Templates.
    }


    @Override
    public List<GoEvidenceDTO> getGOTermsForPubAndMarker(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<GoEvidenceDTO> goEvidenceDTOs = new ArrayList<GoEvidenceDTO>();

        if (publication != null) {
            // get genes
            Marker marker = markerRepository.getMarkerByID(dto.getMarkerDTO().getZdbID());
            List<Term> goTerms = mutantRepository.getGoTermsByMarkerAndPublication(marker, publication);
            for (Term goTerm : goTerms) {
                GoEvidenceDTO relatedEntityDTO = new GoEvidenceDTO();
                relatedEntityDTO.setName(goTerm.getTermName());
                relatedEntityDTO.setZdbID(goTerm.getID());
                relatedEntityDTO.setDataZdbID(goTerm.getID());
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
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class);
        criteria.add(Restrictions.eq("marker.zdbID", zdbID));
        List<MarkerGoTermEvidence> markerGoTermEvidenceList = criteria.list();

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

        Marker marker;
        if (StringUtils.isNotEmpty(goEvidenceDTO.getMarkerDTO().getZdbID())) {
            marker = markerRepository.getMarkerByID(goEvidenceDTO.getMarkerDTO().getZdbID());
        } else if (StringUtils.isNotEmpty(goEvidenceDTO.getMarkerDTO().getName())) {
            marker = markerRepository.getMarkerByAbbreviation(goEvidenceDTO.getMarkerDTO().getName());
        } else {
            throw new RuntimeException("Failed to create marker go term.  Bad MarkerDTO passed in.");
        }

        markerGoTermEvidence.setMarker(marker);
        Term goTerm = (Term) HibernateUtil.currentSession().get(GenericTerm.class, goEvidenceDTO.getGoTerm().getZdbID());
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

        GoEvidenceCode goEvidenceCode = (GoEvidenceCode) HibernateUtil.currentSession().createCriteria(GoEvidenceCode.class).add(Restrictions.eq("code", goEvidenceDTO.getEvidenceCode().name())).uniqueResult();
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        markerGoTermEvidence.setFlag(goEvidenceDTO.getFlag());
        markerGoTermEvidence.setNote(goEvidenceDTO.getNote());


        if (person != null) {
            markerGoTermEvidence.setModifiedBy(person.getName());
            markerGoTermEvidence.setCreatedBy(person.getName());
        }

        Date rightNow = new Date() ;
        markerGoTermEvidence.setModifiedWhen(rightNow);
        markerGoTermEvidence.setCreatedWhen(rightNow);

        // implies that the ID is set here
        HibernateUtil.currentSession().save(markerGoTermEvidence);

        // have to do this after we add inferences
        Set<String> newInferenceStrings = new TreeSet<String>(goEvidenceDTO.getInferredFrom());
        for (String inference : newInferenceStrings) {
            mutantRepository.addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, inference);
        }

        infrastructureRepository.insertUpdatesTable(markerGoTermEvidence.getZdbID(), "MarkerGoTermEvidence", markerGoTermEvidence.toString(), "Created new MarkerGoTermEvidence record");


        // already saved, sadly, so we just check to see that there is more than one.
        // fogbugz 5656
        if(mutantRepository.getNumberMarkerGoTermEvidences(markerGoTermEvidence) >1){
            logger.warn("Duplicate marker go evidence attempted: "+ markerGoTermEvidence);
            throw new DuplicateEntryException("GO annotation not saved because it is identical to an existing annotation.") ;
        }


        HibernateUtil.flushAndCommitCurrentSession();

        try{
            PublicationService.addRecentPublications(getServletContext(),publication,PublicationSessionKey.GOCURATION) ;
        } catch (Exception e) {
            logger.debug("Unable to add a recent pub, should only be a problem in test mode",e);
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
        Person person = Person.getCurrentSecurityUser();
        infrastructureRepository.insertUpdatesTable(zdbID, "MarkerGoTermEvidence",zdbID, "",  "delete this annotation via GO gwt");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    public TermDTO getGOTermByName(String value) {
        // going to be faster to go to the DB
        Term goTerm = null ;
        if(value.startsWith("ZDB-TERM")){
            goTerm = (Term) HibernateUtil.currentSession().createCriteria(GenericTerm.class).
                    add(Restrictions.eq("ID", value)).uniqueResult();
        }
        Ontology ontology ;
        for(Iterator<Ontology> iterator = Ontology.GO.getIndividualOntologies().iterator()  ;
            iterator.hasNext() && goTerm==null  ; ){
            ontology = iterator.next() ;
            goTerm = OntologyManager.getInstance().getTermByName(ontology,value,true) ;
        }
        if (goTerm == null) {
            throw new RuntimeException("Failed to find GO term[" + value + "]");
        } else {
            return DTOConversionService.convertToTermDTO(goTerm);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GoEvidenceDTO> getMarkerGoTermEvidencesForPub(String publicationID) {
        HibernateUtil.currentSession();
        List<MarkerGoTermEvidence> evidences = (List<MarkerGoTermEvidence>)
                HibernateUtil.currentSession().createQuery(" " +
                        " from MarkerGoTermEvidence ev where ev.source = :pubZdbID " +
                        " order by ev.marker.abbreviation , ev.goTerm.termName " +
                        "")
                        .setString("pubZdbID", publicationID)
                        .list();

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
        List<MarkerGoTermEvidence> evidences = (List<MarkerGoTermEvidence>)
                HibernateUtil.currentSession().createQuery(" " +
                        " from MarkerGoTermEvidence ev where ev.marker.zdbID = :markerZdbID " +
                        " order by ev.goTerm.ontology , marker.abbreviation , ev.goTerm.termName " +
                        "")
                        .setString("markerZdbID", markerID)
                        .list();

        List<GoEvidenceDTO> goEvidenceDTOs = new ArrayList<GoEvidenceDTO>();
        for (MarkerGoTermEvidence markerGoTermEvidence : evidences) {
            goEvidenceDTOs.add(DTOConversionService.convertToGoEvidenceDTO(markerGoTermEvidence));
        }

        return goEvidenceDTOs;
    }

    @Override
    public List<MarkerDTO> getGenesForPub(String publicationID) {
        List<MarkerDTO> markerDTOs = new ArrayList<MarkerDTO>();
        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkerForAttribution(publicationID);
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
