package org.zfin.gwt.curation.server;

//import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.CurationFilterRPC;
import org.zfin.gwt.curation.ui.PublicationNotFoundException;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.people.CuratorSession;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT class to facilitate ajax calls related to the curation filter bar.
 */
public class CurationFilterRPCImpl extends ZfinRemoteServiceServlet implements CurationFilterRPC {

    private static ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static MutantRepository mutantRep = RepositoryFactory.getMutantRepository();
    private static FeatureRepository featureRep = RepositoryFactory.getFeatureRepository();

    private static final String FX_GENE_FILTER = "fx-gene-filter: ";
    private static final String FX_FISH_FILTER = "fx-fish-filter: ";

    /**
     * Retrieve the values to be used for the fx filter bar.
     *
     * @param publicationID publication
     * @return FilterValuesDTO
     */
    public FilterValuesDTO getFilterValues(String publicationID) {
        FilterValuesDTO values = new FilterValuesDTO();
        String uniqueKey = createSessionVariableName(publicationID, FX_FISH_FILTER);
        String fishID = (String) getServletContext().getAttribute(uniqueKey);
        if (fishID != null) {
            FishDTO dto = new FishDTO();
            dto.setZdbID(fishID);
            values.setFish(dto);
        }

        uniqueKey = createSessionVariableName(publicationID, FX_GENE_FILTER);
        String geneID = (String) getServletContext().getAttribute(uniqueKey);
        if (geneID != null) {
            MarkerDTO marker = new MarkerDTO();
            marker.setZdbID(geneID);
            values.setMarker(marker);
        }

        CuratorSession attribute = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID);
        if (attribute != null) {
            if (StringUtils.isNotEmpty(attribute.getValue())) {
                Figure figure = pubRepository.getFigureByID(attribute.getValue());
                if (figure==null){
                 profileRep.deleteCuratorSession(attribute);
            }
                else{
                FigureDTO figureDTO = new FigureDTO();
                figureDTO.setLabel(figure.getLabel());
                figureDTO.setZdbID(figure.getZdbID());
                values.setFigure(figureDTO);
                }
            }
        }
        return values;
    }

    public FilterValuesDTO getPossibleFilterValues(String publicationID) throws PublicationNotFoundException {
        // read all fish : check if genotype has record attribution with given pub.
        FilterValuesDTO values = new FilterValuesDTO();

        List<FishDTO> fishDTOs = createFishList(publicationID);
        values.setFishes(fishDTOs);

        List<FigureDTO> figureDTOs = createFigureList(publicationID);
        if (figureDTOs == null)
            return null;
        values.setFigures(figureDTOs);

        List<MarkerDTO> genes = createGeneList(publicationID);
        values.setMarkers(genes);

        List<FeatureDTO> features = createFeatureList(publicationID);
        values.setFeatures(features);

        return values;
    }

    private List<FeatureDTO> createFeatureList(String publicationID) {
        List<Feature> features = featureRep.getFeaturesByPublication(publicationID);
        List<FeatureDTO> dtos = new ArrayList<FeatureDTO>(10);
        for (Feature feature : features) {
            dtos.add(DTOConversionService.convertToFeatureDTO(feature));
        }
        return dtos;
    }

    private List<MarkerDTO> createGeneList(String publicationID) throws PublicationNotFoundException {
        // read all genes
        Publication publication = pubRepository.getPublication(publicationID);
        if (publication == null) {
            throw new PublicationNotFoundException(publicationID);
        }

        List<Marker> markers = pubRepository.getGenesByPublication(publicationID);
        List<MarkerDTO> genes = new ArrayList<MarkerDTO>(10);

        for (Marker marker : markers) {
            MarkerDTO gene = new MarkerDTO();
            gene.setName(marker.getAbbreviation());
            gene.setZdbID(marker.getZdbID());
            genes.add(gene);
        }
        return genes;
    }

    private List<FigureDTO> createFigureList(String publicationID) {
        List<Figure> figures = pubRepository.getFiguresByPublication(publicationID);
        if (figures == null)
            return null;

        List<FigureDTO> figureDTOs = new ArrayList<FigureDTO>();
        for (Figure figure : figures) {
            FigureDTO dto = new FigureDTO();
            dto.setLabel(figure.getLabel());
            dto.setZdbID(figure.getZdbID());
            figureDTOs.add(dto);
        }
        return figureDTOs;
    }

    private List<FishDTO> createFishList(String publicationID) {
        List<Genotype> genotypes = pubRepository.getGenotypesInPublication(publicationID);
        List<FishDTO> fishDTOs = new ArrayList<FishDTO>();

        Genotype geno = pubRepository.getGenotypeByHandle(Genotype.WT);
        fishDTOs.add(DTOConversionService.convertToFishDTOFromGenotype(geno));
        FishDTO separator = new FishDTO();
        separator.setName("--------------");
        fishDTOs.add(separator);

        for (Genotype genotype : genotypes) {
            fishDTOs.add(DTOConversionService.convertToFishDTOFromGenotype(genotype));
        }

        fishDTOs.add(separator);
        List<Genotype> wildtypes = mutantRep.getAllWildtypeGenotypes();
        for (Genotype genotype : wildtypes) {
            fishDTOs.add(DTOConversionService.convertToFishDTOFromGenotype(genotype));
        }
        return fishDTOs;
    }

    /**
     * Save the filter element zdb ID.
     * If the zdb ID is null unset the value.
     * In this case the typeString is used to identify the type of element that should be unset.
     * If typeString is null do nothing.
     *
     * @param publicationID publication
     * @param zdbID         zdbID
     */
    public void setFilterType(String publicationID, String zdbID, String typeString) {

        ActiveData.Type type;
        if (zdbID != null) {
            ActiveData activeData = new ActiveData();
            activeData.setZdbID(zdbID);
            type = activeData.validateID();
        } else {
            if (typeString == null)
                return;
            type = ActiveData.Type.getType(typeString);
        }
        // save figure info into database
        if (type == ActiveData.Type.FIG) {
            Transaction tx = HibernateUtil.currentSession().beginTransaction();
            try {
                if (zdbID != null)
                    profileRep.setCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID, zdbID);
                else {
                    CuratorSession curSession = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID);
                    if (curSession != null)
                        profileRep.deleteCuratorSession(curSession);
                }
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                throw e;
            }
        }
        // save gene info in session
        if (type == ActiveData.Type.GENE) {
            String uniqueKey = createSessionVariableName(publicationID, FX_GENE_FILTER);
            // if zdbID is null then this will unset the attribute
            getServletContext().setAttribute(uniqueKey, zdbID);
        }
        // save fish info in session
        if (type == ActiveData.Type.GENO) {
            String uniqueKey = createSessionVariableName(publicationID, FX_FISH_FILTER);
            getServletContext().setAttribute(uniqueKey, zdbID);
        }
    }

    private String createSessionVariableName(String publicationID, String elementName) {
        return elementName + publicationID;
    }

}
