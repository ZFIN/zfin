package org.zfin.gwt.marker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.zfin.datatransfer.webservice.EBIFetch;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.TermRPCService;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GoTerm;
import org.zfin.orthology.Species;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.util.*;

/**
 */
public class TermRPCServiceImpl extends RemoteServiceServlet implements TermRPCService {

    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private transient MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private transient Logger logger = Logger.getLogger(TermRPCServiceImpl.class);


    @Override
    public GoEvidenceDTO getMarkerGoTermEvidenceDTO(String goTermEvidenceZdbID) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class) ;
        criteria.add(Restrictions.eq("zdbID",goTermEvidenceZdbID));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();

        GoEvidenceDTO returnDTO = new GoEvidenceDTO();
        returnDTO.setZdbID(markerGoTermEvidence.getZdbID());
        returnDTO.setDataZdbID(markerGoTermEvidence.getZdbID());
        returnDTO.setGoTerm(DTOService.createGoTermDTOFromGoTerm(markerGoTermEvidence.getGoTerm()));

        returnDTO.setEvidenceCode(GoEvidenceCodeEnum.valueOf(markerGoTermEvidence.getEvidenceCode().getCode()));
        returnDTO.setFlag( markerGoTermEvidence.getFlag()==null ? null : markerGoTermEvidence.getFlag() );
        returnDTO.setMarkerDTO(DTOService.createMarkerDTOFromMarker(markerGoTermEvidence.getMarker()));

        // set name to the go term name
        returnDTO.setName(markerGoTermEvidence.getGoTerm().getName());
        Set<String> inferredFromSet = new HashSet<String>() ;
        if(markerGoTermEvidence.getInferredFrom()!=null){
            for(InferenceGroupMember inferenceGroupMember : markerGoTermEvidence.getInferredFrom()){
                inferredFromSet.add(inferenceGroupMember.getInferredFrom());
            }
        }
        returnDTO.setInferredFrom(inferredFromSet);
        returnDTO.setNote(markerGoTermEvidence.getNote());


        returnDTO.setPublicationZdbID(markerGoTermEvidence.getSource().getZdbID());
//        returnDTO.setCreatedDate(markerGoTermEvidence.getCreatedWhen());
//        returnDTO.setModifiedDate(markerGoTermEvidence.getCreatedWhen());
//        returnDTO.setCreatedPersonName(markerGoTermEvidence.getCreatedBy());
//        returnDTO.setModifiedPersonName(markerGoTermEvidence.getModifiedBy());


        return returnDTO;
    }

    @Override
    public GoEvidenceDTO editMarkerHeaderGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO) {

        // retrieve
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class) ;
        criteria.add(Restrictions.eq("zdbID",goEvidenceDTO.getZdbID()));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();

        HibernateUtil.createTransaction();
        // set modified by
        Person person = Person.getCurrentSecurityUser();



        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(goEvidenceDTO.getPublicationZdbID());
        markerGoTermEvidence.setSource(publication);

        PublicationService.addRecentPublications(getServletContext(),publication) ;

        GoEvidenceCode goEvidenceCode = (GoEvidenceCode) HibernateUtil.currentSession().createCriteria(GoEvidenceCode.class).add(Restrictions.eq("code",goEvidenceDTO.getEvidenceCode().name())).uniqueResult();
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        markerGoTermEvidence.setFlag(goEvidenceDTO.getFlag());

        markerGoTermEvidence.setNote(goEvidenceDTO.getNote());

        if(person!=null){
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(markerGoTermEvidence.getZdbID(),"MarkerGoTermEvidence",markerGoTermEvidence.toString(),"Updated MarkerGoTermEvidence record",person);
            markerGoTermEvidence.setModifiedBy(person.getName());
        }
        markerGoTermEvidence.setModifiedWhen(goEvidenceDTO.getModifiedDate());

        HibernateUtil.flushAndCommitCurrentSession();

        return goEvidenceDTO ;
    }

    @Override
    public GoEvidenceDTO editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO) {
        // retrieve
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class) ;
        criteria.add(Restrictions.eq("zdbID",goEvidenceDTO.getZdbID()));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();

        HibernateUtil.createTransaction();
        // set modified by
        Person person = Person.getCurrentSecurityUser();

        GoTerm goTerm = (GoTerm) HibernateUtil.currentSession().get(GoTerm.class,goEvidenceDTO.getGoTerm().getZdbID()) ;
        markerGoTermEvidence.setGoTerm(goTerm) ;
//
//        if(person==null){
//            person = RepositoryFactory.getProfileRepository().getPersonB(goEvidenceDTO.getModifiedPersonName());
//        }
        if(person!=null){
            markerGoTermEvidence.setModifiedBy(person.getName());
        }
        markerGoTermEvidence.setModifiedWhen(goEvidenceDTO.getModifiedDate());



        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(goEvidenceDTO.getPublicationZdbID());
        markerGoTermEvidence.setSource(publication);

        GoEvidenceCode goEvidenceCode = (GoEvidenceCode) HibernateUtil.currentSession().createCriteria(GoEvidenceCode.class).add(Restrictions.eq("code",goEvidenceDTO.getEvidenceCode().name())).uniqueResult();
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        markerGoTermEvidence.setFlag(goEvidenceDTO.getFlag());

        markerGoTermEvidence.setNote(goEvidenceDTO.getNote());

        // add or remove inferences depending on sets returned
        Set<InferenceGroupMember> existingInferenceGroupMembers = markerGoTermEvidence.getInferredFrom();

        Set<String> existingInferenceStrings = new TreeSet<String>() ;
        for(InferenceGroupMember inferenceGroupMember: existingInferenceGroupMembers){
            existingInferenceStrings.add(inferenceGroupMember.getInferredFrom()) ;
        }
        Set<String> newInferenceStrings = new TreeSet<String>(goEvidenceDTO.getInferredFrom());

        Collection<String> inferencesToAdd = CollectionUtils.subtract(newInferenceStrings,existingInferenceStrings) ;
        Collection<String> inferencesToRemove = CollectionUtils.subtract(existingInferenceStrings,newInferenceStrings) ;


        for(String inference: inferencesToAdd){
            mutantRepository.addInferenceToGoMarkerTermEvidence(markerGoTermEvidence,inference) ;
        }

        for(String inference: inferencesToRemove){
            mutantRepository.removeInferenceToGoMarkerTermEvidence(markerGoTermEvidence,inference) ;
        }

//        markerGoTermEvidence.setInferredFrom(goEvidenceDTO.getInferredFrom());

        HibernateUtil.flushAndCommitCurrentSession();
        // do a safe return this way
        return getMarkerGoTermEvidenceDTO(goEvidenceDTO.getZdbID());
    }

    public List<MarkerDTO> getGenesForGOAttributions(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<MarkerDTO> relatedEntityDTOs = new ArrayList<MarkerDTO>() ;

        if(publication!=null){
            List<Marker> genes = markerRepository.getMarkersForStandardAttributionAndType(publication,"GENE");
            if(CollectionUtils.isNotEmpty(genes)){
                for(Marker gene: genes){
                    relatedEntityDTOs.add(DTOService.createMarkerDTOFromMarker(gene));
                }
            }
        }
        return relatedEntityDTOs ;

    }

    @Override
    public List<RelatedEntityDTO> getGenoTypesAndMorpholinosForGOAttributions(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<RelatedEntityDTO> relatedEntityDTOs = new ArrayList<RelatedEntityDTO>() ;

        if(publication!=null){
            List<Marker> morpholinos = markerRepository.getMarkersForStandardAttributionAndType(publication,"MRPHLNO");
            if(CollectionUtils.isNotEmpty(morpholinos)){
                RelatedEntityDTO morpholinoLabelDTO = new RelatedEntityDTO();
                morpholinoLabelDTO.setName("Morpholinos:");
                relatedEntityDTOs.add(morpholinoLabelDTO);
                for(Marker gene: morpholinos){
                    relatedEntityDTOs.add(DTOService.createMarkerDTOFromMarker(gene));
                }
            }
//
//            // get features
//            List<Feature> features = RepositoryFactory.getMutantRepository().getFeaturesForStandardAttribution(publication);
//            if(CollectionUtils.isNotEmpty(features)){
//                RelatedEntityDTO featureLabelDTO = new RelatedEntityDTO();
//                featureLabelDTO.setName("Features:");
//                relatedEntityDTOs.add(featureLabelDTO);
//                for(Feature feature: features){
//                    relatedEntityDTOs.add(DTOService.createFeatureDTOFromFeature(feature));
//                }
//            }

            // get genotypes
            List<Genotype> genotypes = mutantRepository.getGenotypesForStandardAttribution(publication);
            if(CollectionUtils.isNotEmpty(genotypes)){
                RelatedEntityDTO genotypeLabelDTO = new RelatedEntityDTO();
                genotypeLabelDTO.setName("Genotypes:");
                relatedEntityDTOs.add(genotypeLabelDTO);
                for(Genotype genotype: genotypes){
                    relatedEntityDTOs.add(DTOService.createGenotypeDTOFromGenotype(genotype));
                }
            }
        }

        return relatedEntityDTOs ;
    }


    /**
     *
     * @param dto Thing to add to.
     * @param accession The inference String value.
     * @param inferenceCategory The Inference Category.
     * @return Returns false if invalid accession and nothing done.
     */
    @Override
    public GoEvidenceDTO addInference(GoEvidenceDTO dto, String accession,String inferenceCategory) {

        if(false==validateAccession(accession,inferenceCategory)){
            return null ;
        }

        String fullAccession = inferenceCategory + accession;
        HibernateUtil.createTransaction();
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class) ;
        criteria.add(Restrictions.eq("zdbID",dto.getZdbID()));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();

        mutantRepository.addInferenceToGoMarkerTermEvidence(markerGoTermEvidence,fullAccession) ;

        HibernateUtil.flushAndCommitCurrentSession();
        return getMarkerGoTermEvidenceDTO(dto.getDataZdbID());
    }

    private boolean validateAccession(String accession, String inferenceCategory) {
//        InferenceCategory.
        if( inferenceCategory.equals(InferenceCategory.GENBANK.name())
                ||
                inferenceCategory.equals(InferenceCategory.REFSEQ.name())
                ){
            return NCBIEfetch.validateAccession(accession,true) ;
        }
        else
        if( inferenceCategory.equals(InferenceCategory.GENPEPT.name()) ){
            return NCBIEfetch.validateAccession(accession,false) ;
        }
        else
        if( inferenceCategory.equals(InferenceCategory.UNIPROTKB.name()) ){
            return EBIFetch.validateAccession(accession) ;
        }
        return true;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public GoEvidenceDTO removeInference(RelatedEntityDTO dto) {
        HibernateUtil.createTransaction();

        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class) ;
        criteria.add(Restrictions.eq("zdbID",dto.getDataZdbID()));
        criteria.setMaxResults(1);
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) criteria.uniqueResult();

        mutantRepository.removeInferenceToGoMarkerTermEvidence(markerGoTermEvidence,dto.getName());

        HibernateUtil.flushAndCommitCurrentSession();
        return getMarkerGoTermEvidenceDTO(dto.getDataZdbID());
    }

    @Override
    public List<GoEvidenceDTO> getGOTermsForPubAndMarker(GoEvidenceDTO dto) {
        Publication publication = publicationRepository.getPublication(dto.getPublicationZdbID());
        List<GoEvidenceDTO> goEvidenceDTOs = new ArrayList<GoEvidenceDTO>() ;

        if(publication!=null){
            // get genes
            Marker marker = markerRepository.getMarkerByID(dto.getMarkerDTO().getZdbID());
            List<GoTerm> goTerms = mutantRepository.getGoTermsByMarkerAndPublication(marker,publication) ;
            for(GoTerm goTerm: goTerms){
                GoEvidenceDTO relatedEntityDTO = new GoEvidenceDTO() ;
                relatedEntityDTO.setName(goTerm.getName());
                relatedEntityDTO.setZdbID(goTerm.getZdbID());
                relatedEntityDTO.setDataZdbID(goTerm.getZdbID());
                relatedEntityDTO.setGoTerm(DTOService.createGoTermDTOFromGoTerm(goTerm));
                relatedEntityDTO.setPublicationZdbID(dto.getPublicationZdbID());
                goEvidenceDTOs.add(relatedEntityDTO) ;
            }
//
//            GoEvidenceDTO dividerDTO = new GoEvidenceDTO();
//            dividerDTO.setName("----");
//            dividerDTO.setDataZdbID("----");
//            goEvidenceDTOs.add(dividerDTO) ;
//
//            goTerms = RepositoryFactory.getMutantRepository().getGoTermsByPhenotypeAndPublication(publication) ;
//            for(GoTerm goTerm: goTerms){
//                GoEvidenceDTO relatedEntityDTO = new GoEvidenceDTO();
//                relatedEntityDTO.setName(goTerm.getName());
//                relatedEntityDTO.setZdbID(goTerm.getZdbID());
//                relatedEntityDTO.setDataZdbID(goTerm.getZdbID());
//                relatedEntityDTO.setGoTermID(goTerm.getGoID());
//                relatedEntityDTO.setPublicationZdbID(dto.getPublicationZdbID());
//                goEvidenceDTOs.add(relatedEntityDTO) ;
//            }
        }

        return goEvidenceDTOs ;
    }


    protected Set<String> getInferencesFromPreviousInferences(String zdbID,String inferenceCategory){
        Set<String> inferredFromSet = new TreeSet<String>() ;
        // for interpro and EC, these should come form db_links and for SP_KW, from previous inferences

        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class) ;
        criteria.add(Restrictions.eq("marker.zdbID",zdbID));
        List<MarkerGoTermEvidence> markerGoTermEvidenceList = criteria.list();

        for(MarkerGoTermEvidence markerGoTermEvidence: markerGoTermEvidenceList){
            for(InferenceGroupMember inferenceGroupMember : markerGoTermEvidence.getInferredFrom()){
                if(inferenceGroupMember.getInferredFrom().startsWith(inferenceCategory)){
                    inferredFromSet.add(inferenceGroupMember.getInferredFrom());
                }
            }
        }
        return inferredFromSet ;

    }

    @Override
    public Set<String> getInferencesByMarkerAndType(GoEvidenceDTO dto, String inferenceCategory) {
        // treeset to sort by string order
        Set<String> inferredFromSet = new TreeSet<String>() ;
        // for interpro and EC, these should come form db_links and for SP_KW, from previous inferences

        if(inferenceCategory.startsWith(InferenceCategory.EC.prefix())){
            ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.EC,
                    ForeignDBDataType.DataType.DOMAIN,
                    ForeignDBDataType.SuperType.PROTEIN,
                    Species.ZEBRAFISH
            );
            inferredFromSet.addAll(getInferencesByDBLink(dto.getMarkerDTO().getZdbID(),referenceDatabase));
        }
        else
        if(inferenceCategory.startsWith(InferenceCategory.INTERPRO.prefix())){
            ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.INTERPRO,
                    ForeignDBDataType.DataType.DOMAIN,
                    ForeignDBDataType.SuperType.PROTEIN,
                    Species.ZEBRAFISH
            );
            inferredFromSet.addAll(getInferencesByDBLink(dto.getMarkerDTO().getZdbID(),referenceDatabase));
        }

        inferredFromSet.addAll(getInferencesFromPreviousInferences(dto.getMarkerDTO().getZdbID(),inferenceCategory));

        return inferredFromSet ;
    }

    protected Set<String> getInferencesByDBLink(String zdbID, ReferenceDatabase referenceDatabase) {
        Marker marker = (Marker) HibernateUtil.currentSession().get(Marker.class,zdbID) ;
        List<MarkerDBLink> dbLinks= RepositoryFactory.getSequenceRepository().getDBLinksForMarker(marker,referenceDatabase);
        Set<String> inferredFromSet = new TreeSet<String>() ;
        for(MarkerDBLink dbLink : dbLinks){
            inferredFromSet.add(referenceDatabase.getForeignDB().getDbName()+":"+dbLink.getAccessionNumber()) ;
        }

        return inferredFromSet;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public GoEvidenceDTO createMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO) {

        HibernateUtil.createTransaction();
        // set modified by
        Person person = Person.getCurrentSecurityUser();
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();

        Marker marker = markerRepository.getMarkerByID(goEvidenceDTO.getMarkerDTO().getZdbID()) ;
        markerGoTermEvidence.setMarker(marker);
        GoTerm goTerm = (GoTerm) HibernateUtil.currentSession().get(GoTerm.class,goEvidenceDTO.getGoTerm().getZdbID()) ;
        markerGoTermEvidence.setGoTerm(goTerm) ;

//

        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(goEvidenceDTO.getPublicationZdbID());
        markerGoTermEvidence.setSource(publication);

        try{
            PublicationService.addRecentPublications(getServletContext(),publication) ;
        }catch(NullPointerException npe){
            // this is normal for test mode
        }

        GoEvidenceCode goEvidenceCode = (GoEvidenceCode) HibernateUtil.currentSession().createCriteria(GoEvidenceCode.class).add(Restrictions.eq("code",goEvidenceDTO.getEvidenceCode().name())).uniqueResult();
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        markerGoTermEvidence.setFlag(goEvidenceDTO.getFlag());
        markerGoTermEvidence.setNote(goEvidenceDTO.getNote());

        // implies that the ID is set here
        HibernateUtil.currentSession().save(markerGoTermEvidence) ;

        // have to do this after we add inferences
        Set<String> newInferenceStrings = new TreeSet<String>(goEvidenceDTO.getInferredFrom());
        for(String inference: newInferenceStrings){
            mutantRepository.addInferenceToGoMarkerTermEvidence(markerGoTermEvidence,inference) ;
        }

        if(person!=null){
            markerGoTermEvidence.setModifiedBy(person.getName());
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(markerGoTermEvidence.getZdbID(),"MarkerGoTermEvidence",markerGoTermEvidence.toString(),"Created new MarkerGoTermEvidence record",person);
        }
        markerGoTermEvidence.setModifiedWhen(goEvidenceDTO.getModifiedDate());

        HibernateUtil.flushAndCommitCurrentSession();
        // do a safe return this way
        return getMarkerGoTermEvidenceDTO(markerGoTermEvidence.getZdbID());
    }

    @Override
    public GoTermDTO getGOTermByName(String value) {
        GoTerm goTerm = (GoTerm) HibernateUtil.currentSession().createCriteria(GoTerm.class).
                add(Restrictions.eq("name",value)).uniqueResult() ;
        if(goTerm==null) {
            throw new RuntimeException("Failed to find termp"+value+"]") ;
        }
        else{
            return DTOService.createGoTermDTOFromGoTerm(goTerm) ;
        }
    }

}