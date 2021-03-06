package org.zfin.gwt.marker.server;

import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.ImageStage;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.ImageRPCService;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

public class ImageRPCServiceImpl extends ZfinRemoteServiceServlet implements ImageRPCService {

    private transient Logger logger = LogManager.getLogger(ImageRPCServiceImpl.class);
    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
    private transient OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    public ImageDTO getImageForZdbID(String zdbID) {

        logger.debug("attempting to get image: " + zdbID);
        Image image = publicationRepository.getImageById(zdbID);
        ImageDTO dto = new ImageDTO();
        dto.setZdbID(image.getZdbID());
//        dto.setPublicationZdbID(image.getFigure().getPublication().getZdbID());

        if (image.getTerms() != null)
            logger.debug(image.getZdbID() +  " has " + image.getTerms().size() + " terms");


        ArrayList<TermDTO> termDTOs = new ArrayList<TermDTO>();
        for(GenericTerm term : image.getTerms()) {
            termDTOs.add(DTOConversionService.convertToTermDTO(term));
        }
        dto.setAnatomyTerms(termDTOs);

        StageDTO start = new StageDTO();
        StageDTO end = new StageDTO();

        DevelopmentStage unk = anatomyRepository.getStageByName(DevelopmentStage.UNKNOWN);

        if (image.getStart() != null) {
            start.setZdbID(image.getStart().getZdbID());
            start.setNameLong(image.getStart().getName());
            dto.setStart(start);
        } else {
            //treat null as unknown in the interface
            start.setZdbID(unk.getZdbID());
            start.setNameLong(unk.getName());
            dto.setStart(start);
        }


        if (image.getEnd() != null) {
            end.setZdbID(image.getEnd().getZdbID());
            end.setNameLong(image.getEnd().getName());
            dto.setEnd(end);
        } else {
            //treat null as unknown in the interface
            end.setZdbID(unk.getZdbID());
            end.setNameLong(unk.getName());
            dto.setEnd(end);
        }

        ArrayList<MarkerDTO> constructDTOs = new ArrayList<MarkerDTO>();
        if (image.getFigure()!=null) {
            for (Marker construct : image.getFigure().getConstructs()) {
                constructDTOs.add(DTOConversionService.convertToMarkerDTO(construct));
            }
            dto.setConstructs(constructDTOs);
        }
        return dto;
    }

    public TermDTO addTerm(String name, String imageZdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        GenericTerm term = ontologyRepository.getTermByName(name, Ontology.ANATOMY);
        
        Image image = publicationRepository.getImageById(imageZdbID);
        TermDTO termDTO = new TermDTO();
        termDTO.setName(term.getTermName());


        image.getTerms().add(term);

        session.save(image);

        transaction.commit();

        logger.debug("adding " + name + " to " + imageZdbID);
        return termDTO;
    }

    public void removeTerm(String name, String imageZdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        GenericTerm term = ontologyRepository.getTermByName(name, Ontology.ANATOMY);

        Image image = publicationRepository.getImageById(imageZdbID);

        image.getTerms().remove(term);

        session.save(image);

        transaction.commit();
        logger.debug("removing " + name + " from " + imageZdbID);
    }

    /**
     * This method is an exact duplicate of the getStages method in CurationExperimentRPCImpl,
     * if ever we can, we should refactor so that they can be shared rather than copied and
     * pasted
     * @return list of stages
     */
    public List<StageDTO> getStages() {
        List<DevelopmentStage> stages = RepositoryFactory.getAnatomyRepository().getAllStages();
        List<StageDTO> dtos = new ArrayList<StageDTO>(stages.size());
        for (DevelopmentStage stage : stages) {
            dtos.add(DTOConversionService.convertToStageDTO(stage));
        }
        return dtos;

    }

    public void setStages(String startStageZdbId, String endStageZdbId, String imageZdbId) {
        logger.debug(startStageZdbId + " " + endStageZdbId + " " + imageZdbId);

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        Image image = publicationRepository.getImageById(imageZdbId);
        DevelopmentStage start = anatomyRepository.getStageByID(startStageZdbId);
        DevelopmentStage end = anatomyRepository.getStageByID(endStageZdbId);

        if (image.getImageStage() == null) {
            ImageStage imageStage = new ImageStage();
            imageStage.setStart(start);
            imageStage.setEnd(end);
            imageStage.setZdbID(image.getZdbID());
            image.setImageStage(imageStage);
        } else {
            image.getImageStage().setStart(start);
            image.getImageStage().setEnd(end);
        }
        session.save(image);
        transaction.commit();
    }

    @Override
    public MarkerDTO addConstruct(String name, String imageZdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Marker construct = markerRepository.getMarkerByName(WordUtils.capitalize(name));

        Image image = publicationRepository.getImageById(imageZdbID);
        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setName(construct.getName());

        Figure figure = image.getFigure();
        figure.getConstructs().add(construct);

        session.save(figure);

        transaction.commit();

        logger.debug("adding construct " + name + " to " + imageZdbID);
        return markerDTO;
    }

    @Override
    public void removeConstruct(String name, String imageZdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Marker construct = markerRepository.getMarkerByName(name);

        Image image = publicationRepository.getImageById(imageZdbID);
        Figure figure = image.getFigure();
        figure.getConstructs().remove(construct);

        session.save(figure);

        transaction.commit();
        logger.debug("removing construct" + name + " from " + imageZdbID);
    }
}
