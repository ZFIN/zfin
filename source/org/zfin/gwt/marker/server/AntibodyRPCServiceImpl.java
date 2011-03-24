package org.zfin.gwt.marker.server;

import org.apache.log4j.Logger;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.Isotype;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.AntibodyRPCService;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.AntibodyTypesDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.service.AntibodyWikiWebService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class AntibodyRPCServiceImpl extends ZfinRemoteServiceServlet implements AntibodyRPCService {

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


        antibodyDTO.setSuppliers(MarkerService.getSuppliers(antibody));

        // get direct attributions
        antibodyDTO.setRecordAttributions(MarkerService.getDirectAttributions(antibody));

        // get notes
        antibodyDTO.setCuratorNotes(DTOMarkerService.getCuratorNoteDTOs(antibody));
        antibodyDTO.setPublicNote(DTOMarkerService.getPublicNoteDTO(antibody));

        antibodyDTO.setExternalNotes(DTOMarkerService.getExternalNoteDTOs(antibody));

        // get alias's
        antibodyDTO.setAliasAttributes(DTOMarkerService.getMarkerAliasDTOs(antibody));

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
                relatedGenes.addAll(DTOConversionService.createLinks(DTOConversionService.convertToMarkerDTO(gene), markerRelationship.getPublications()));
            }
        }
        logger.debug("# of related genes: " + relatedGenes.size());

        antibodyDTO.setRelatedGeneAttributes(relatedGenes);

        return antibodyDTO;
    }

    @Override
    public AntibodyTypesDTO getAntibodyTypes() {
        AntibodyTypesDTO antibodyTypesDTO = new AntibodyTypesDTO();

        List<String> hostSpecies = new ArrayList<String>();
        for (Species species : antibodyRepository.getHostSpeciesList()) {
            hostSpecies.add(species.getCommonName());
        }
        antibodyTypesDTO.setHostOrganisms(hostSpecies);

        List<String> immunogenSpecies = new ArrayList<String>();
        for (Species species : antibodyRepository.getImmunogenSpeciesList()) {
            immunogenSpecies.add(species.getCommonName());
        }
        antibodyTypesDTO.setImmunogenOrganisms(immunogenSpecies);

        List<String> heavyChainList = new ArrayList<String>();
        for (Isotype.HeavyChain iso : Isotype.HeavyChain.values()) {
            heavyChainList.add(iso.toString());
        }
        antibodyTypesDTO.setHeavyChains(heavyChainList);

        List<String> lightChainList = new ArrayList<String>();
        for (Isotype.LightChain iso : Isotype.LightChain.values()) {
            lightChainList.add(iso.toString());
        }
        antibodyTypesDTO.setLightChains(lightChainList);


        List<String> types = new ArrayList<String>();
//        types.add(AntibodyType.UNSPECIFIED.getName());
        types.add(AntibodyType.MONOCLONAL.getValue());
        types.add(AntibodyType.POLYCLONAL.getValue());
        antibodyTypesDTO.setTypes(types);

        return antibodyTypesDTO;
    }

    @Override
    public AntibodyDTO updateAntibodyData(AntibodyDTO antibodyDTO) {
        Antibody antibody = antibodyRepository.getAntibodyByID(antibodyDTO.getZdbID());
        logger.info("got antibody: " + antibody.getZdbID());

        HibernateUtil.createTransaction();

        DTOMarkerService.insertMarkerUpdate(antibody, "host species", antibody.getHostSpecies(), antibodyDTO.getHostOrganism());
        antibody.setHostSpecies(antibodyDTO.getHostOrganism());

        DTOMarkerService.insertMarkerUpdate(antibody, "immunogen species", antibody.getImmunogenSpecies(), antibodyDTO.getImmunogenOrganism());
        antibody.setImmunogenSpecies(antibodyDTO.getImmunogenOrganism());

        DTOMarkerService.insertMarkerUpdate(antibody, "heavy chain", antibody.getHeavyChainIsotype(), antibodyDTO.getHeavyChain());
        antibody.setHeavyChainIsotype(antibodyDTO.getHeavyChain());

        DTOMarkerService.insertMarkerUpdate(antibody, "light chain", antibody.getLightChainIsotype(), antibodyDTO.getLightChain());
        antibody.setLightChainIsotype(antibodyDTO.getLightChain());

        DTOMarkerService.insertMarkerUpdate(antibody, "clonal type", antibody.getType().toString(), antibodyDTO.getType());
        antibody.setClonalType(antibodyDTO.getType());

        HibernateUtil.currentSession().update(antibody);
        HibernateUtil.flushAndCommitCurrentSession();
        logger.info("updated clone: " + antibody);

        updateAntibodyWiki(antibody);

        return getAntibodyForZdbID(antibody.getZdbID());
    }

    private void updateAntibodyWiki(Antibody antibody) {
        try {
            AntibodyWikiWebService.getInstance().updatePageForAntibody(antibody, antibody.getName());
        } catch (Exception e) {
            logger.error("Unable to update antibody wiki: " + antibody, e);
        }
    }

    @Override
    public void updateAntibodyHeaders(AntibodyDTO dto) {

        Antibody antibody = (Antibody) HibernateUtil.currentSession().get(Antibody.class, dto.getZdbID());

        HibernateUtil.createTransaction();
        if (!antibody.getName().equals(dto.getName())) {
            String oldName = antibody.getName();

            antibody.setAbbreviation(dto.getName().toLowerCase());
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