package org.zfin.gwt.marker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.Isotype;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.AntibodyRPCService;
import org.zfin.gwt.marker.ui.NoteBox;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.MarkerSupplier;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.AntibodyWikiWebService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class AntibodyRPCServiceImpl extends RemoteServiceServlet implements AntibodyRPCService {

    private transient Logger logger = Logger.getLogger(AntibodyRPCServiceImpl.class);
    private final MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private final AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();


    @Override
    public AntibodyDTO getAntibodyForZdbID(String zdbID) {
        Antibody antibody = antibodyRepository.getAntibodyByID(zdbID);
        AntibodyDTO antibodyDTO = new AntibodyDTO();
        antibodyDTO.setZdbID(antibody.getZdbID());
        antibodyDTO.setName(antibody.getName());
        antibodyDTO.setHostOrganism(antibody.getHostSpecies());
        antibodyDTO.setImmunogenOrganism(antibody.getImmunogenSpecies());
        antibodyDTO.setHeavyChain(antibody.getHeavyChainIsotype());
        antibodyDTO.setLightChain(antibody.getLightChainIsotype());
        antibodyDTO.setType(antibody.getClonalType());


        Set<MarkerSupplier> markerSuppliers = antibody.getSuppliers();
        List<String> supplierList = new ArrayList<String>();
        for (MarkerSupplier markerSupplier : markerSuppliers) {
            supplierList.add(markerSupplier.getOrganization().getName());
        }
        antibodyDTO.setSuppliers(supplierList);


        // get direct attributions
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(zdbID);
        List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        List<String> attributions = new ArrayList<String>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution.getSourceZdbID());
        }
        antibodyDTO.setRecordAttributions(attributions);

        // get notes
        List<NoteDTO> curatorNotes = new ArrayList<NoteDTO>();
        Set<DataNote> dataNotes = antibody.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            NoteDTO noteDTO = new NoteDTO();
            noteDTO.setNoteData(dataNote.getNote());
            noteDTO.setZdbID(dataNote.getZdbID());
//            noteDTO.setDataZdbID(dataNote.getDataZdbID());
            noteDTO.setDataZdbID(antibody.getZdbID());
            noteDTO.setEditMode(NoteBox.EditMode.PRIVATE.name());
            curatorNotes.add(noteDTO);
        }
        antibodyDTO.setCuratorNotes(curatorNotes);

        NoteDTO publicNoteDTO = new NoteDTO();
        publicNoteDTO.setNoteData(antibody.getPublicComments());
        publicNoteDTO.setZdbID(antibody.getZdbID());
        publicNoteDTO.setDataZdbID(antibody.getZdbID());
        publicNoteDTO.setEditMode(NoteBox.EditMode.PUBLIC.name());
        antibodyDTO.setPublicNote(publicNoteDTO);


        List<NoteDTO> externalNotes = new ArrayList<NoteDTO>();
        for (AntibodyExternalNote antibodyExternalNote: antibody.getExternalNotes()) {
            NoteDTO antibodyExternalNoteDTO = new NoteDTO();
            antibodyExternalNoteDTO.setZdbID(antibodyExternalNote.getZdbID());
            antibodyExternalNoteDTO.setEditMode(NoteBox.EditMode.EXTERNAL.name());
            antibodyExternalNoteDTO.setDataZdbID(antibody.getZdbID());
            if(antibodyExternalNote.getSinglePubAttribution()!=null){
                antibodyExternalNoteDTO.setPublicationZdbID(antibodyExternalNote.getSinglePubAttribution().getPublication().getZdbID());
            }
            antibodyExternalNoteDTO.setNoteData(antibodyExternalNote.getNote());
            externalNotes.add(antibodyExternalNoteDTO);
        }
        antibodyDTO.setExternalNotes(externalNotes);


        // get alias's
        Set<MarkerAlias> aliases = antibody.getAliases();
        List<RelatedEntityDTO> aliasRelatedEntities = new ArrayList<RelatedEntityDTO>();
        if (aliases != null) {
            for (MarkerAlias alias : aliases) {
                Set<PublicationAttribution> publicationAttributions = alias.getPublications();
                aliasRelatedEntities.addAll(DTOService.createRelatedEntitiesForPublications(zdbID, alias.getAlias(), publicationAttributions));
            }
        }
        antibodyDTO.setAliasAttributes(aliasRelatedEntities);

        // get antigen genes
        Set<MarkerRelationship> markerRelationships = antibody.getSecondMarkerRelationships();
        logger.debug("# of marker relationships: " + markerRelationships.size());
        List<MarkerDTO> relatedGenes = new ArrayList<MarkerDTO>();
        for (MarkerRelationship markerRelationship : markerRelationships) {
            if (
                    markerRelationship.getFirstMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG)
                            // todo: should use a different type
                            &&
                            markerRelationship.getType().equals(MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY)
                    ) {
                Marker gene = markerRelationship.getFirstMarker();
//                relatedGenes.addAll(DTOHelper.createAttributesForPublication(gene.getAbbreviation(),markerRelationship.getPublications())) ;
                relatedGenes.addAll(DTOService.createLinksForPublication(DTOService.createMarkerDTOFromMarker(gene), markerRelationship.getPublications()));
            }
        }
        logger.debug("# of related genes: " + relatedGenes.size());

        antibodyDTO.setRelatedGeneAttributes(relatedGenes);

        return antibodyDTO ;
    }

    @Override
    public AntibodyTypesDTO getAntibodyTypes() {
        AntibodyTypesDTO antibodyTypesDTO = new AntibodyTypesDTO();

        List<String> hostSpecies = new ArrayList<String>() ;
        for(Species species : antibodyRepository.getHostSpeciesList()){
            hostSpecies.add(species.getCommonName());
        }
        antibodyTypesDTO.setHostOrganisms(hostSpecies);

        List<String> immunogenSpecies = new ArrayList<String>() ;
        for(Species species : antibodyRepository.getImmunogenSpeciesList()){
            immunogenSpecies.add(species.getCommonName());
        }
        antibodyTypesDTO.setImmunogenOrganisms(immunogenSpecies);

        List<String> heavyChainList = new ArrayList<String>();
        for (Isotype.HeavyChain iso : Isotype.HeavyChain.values()){
            heavyChainList.add(iso.toString());
        }
        antibodyTypesDTO.setHeavyChains(heavyChainList);

        List<String> lightChainList = new ArrayList<String>();
        for (Isotype.LightChain iso : Isotype.LightChain.values()){
            lightChainList.add(iso.toString());
        }
        antibodyTypesDTO.setLightChains(lightChainList);


        List<String> types = new ArrayList<String>();
//        types.add(AntibodyType.UNSPECIFIED.getName());
        types.add(AntibodyType.MONOCLONAL.getName());
        types.add(AntibodyType.POLYCLONAL.getName());
        antibodyTypesDTO.setTypes(types);

        return antibodyTypesDTO;
    }

    @Override
    public AntibodyDTO updateAntibodyData(AntibodyDTO antibodyDTO) {
        Antibody antibody = antibodyRepository.getAntibodyByID(antibodyDTO.getZdbID());
        logger.info("got antibody: " + antibody.getZdbID());

        HibernateUtil.createTransaction();

        DTOService.handleUpdatesTable(antibody, "host species", antibody.getHostSpecies(), antibodyDTO.getHostOrganism());
        antibody.setHostSpecies(antibodyDTO.getHostOrganism());

        DTOService.handleUpdatesTable(antibody, "immunogen species", antibody.getImmunogenSpecies(), antibodyDTO.getImmunogenOrganism());
        antibody.setImmunogenSpecies(antibodyDTO.getImmunogenOrganism());

        DTOService.handleUpdatesTable(antibody, "heavy chain", antibody.getHeavyChainIsotype(), antibodyDTO.getHeavyChain());
        antibody.setHeavyChainIsotype(antibodyDTO.getHeavyChain());

        DTOService.handleUpdatesTable(antibody, "light chain", antibody.getLightChainIsotype(), antibodyDTO.getLightChain());
        antibody.setLightChainIsotype(antibodyDTO.getLightChain());

        DTOService.handleUpdatesTable(antibody, "clonal type", antibody.getType().toString(), antibodyDTO.getType());
        antibody.setClonalType(antibodyDTO.getType());

        HibernateUtil.currentSession().update(antibody);
        HibernateUtil.flushAndCommitCurrentSession();
        logger.info("updated clone: " + antibody);

        updateAntibodyWiki(antibody) ;

        return getAntibodyForZdbID(antibody.getZdbID());
    }

    private void updateAntibodyWiki(Antibody antibody) {
        try {
            AntibodyWikiWebService.getInstance().updatePageForAntibody(antibody,antibody.getName());
        } catch (Exception e) {
            logger.error("Unable to update antibody wiki: "+antibody,e);
        }
    }

    @Override
    public void updateAntibodyHeaders(AntibodyDTO dto) {

        Antibody antibody = (Antibody) HibernateUtil.currentSession().get(Antibody.class, dto.getZdbID());

        HibernateUtil.createTransaction();
        if (!antibody.getName().equals(dto.getName())) {
            String oldName = antibody.getName();

            antibody.setAbbreviation(dto.getName());
            antibody.setName(dto.getName());

            InfrastructureService.insertUpdate(antibody, "Antibody Name", oldName, antibody.getName());
            //run regen script
            markerRepository.runMarkerNameFastSearchUpdate(antibody);
        }

        HibernateUtil.currentSession().update(antibody);
        HibernateUtil.flushAndCommitCurrentSession();

        updateAntibodyWiki(antibody);
    }
}