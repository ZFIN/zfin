package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.root.dto.*;

import java.util.List;
import java.util.Set;

/**
 * GWT class to facilitate curation of FX
 */
public interface CurationDiseaseRPC extends RemoteService {

    List<GenotypeDTO> getGenotypeList(String publicationID);

    List<FeatureDTO> getFeatureList(String publicationID);

    List<GenotypeDTO> searchGenotypes(String publicationID, String featureID, String genotypeID);

    GenotypeDTO addGenotypeToPublication(String publicationID, String zdbID) throws TermNotFoundException;

    List<GenotypeDTO> savePublicNote(String publicationID, ExternalNoteDTO externalNoteDTO) throws TermNotFoundException;

    List<GenotypeDTO> createPublicNote(String publicationID, GenotypeDTO genotypeDTO, String text) throws TermNotFoundException;

    List<GenotypeDTO> deletePublicNote(String publicationID, ExternalNoteDTO note) throws TermNotFoundException;

    List<GenotypeDTO> saveCuratorNote(String publicationID, CuratorNoteDTO externalNoteDTO) throws TermNotFoundException;

    List<GenotypeDTO> deleteCuratorNote(String publicationID, CuratorNoteDTO note) throws TermNotFoundException;

    List<GenotypeDTO> createCuratorNote(String publicationID, GenotypeDTO genotypeDTO, String text) throws TermNotFoundException;

    List<ZygosityDTO> getZygosityLists();

    GenotypeCreationReportDTO createGenotypeFish(String publicationID, List<GenotypeFeatureDTO> genotypeFeatureDTOList, List<GenotypeDTO> genotypeBackgroundList, Set<RelatedEntityDTO> strSet)
            throws TermNotFoundException;

    FishDTO retrieveFish(String zdbID);

    public static class App {
        private static final CurationDiseaseRPCAsync INSTANCE;

        static {
            INSTANCE = GWT.create(CurationDiseaseRPC.class);
            ((ServiceDefTarget) INSTANCE).setServiceEntryPoint("/ajax/curation-disease");
        }

        public static CurationDiseaseRPCAsync getInstance() {
            return INSTANCE;
        }
    }

    List<TermDTO> getHumanDiseaseList(String publicationID);

    List<DiseaseAnnotationDTO> getHumanDiseaseModelList(String publicationID) throws TermNotFoundException;

    List<DiseaseAnnotationDTO> addHumanDiseaseAnnotation(DiseaseAnnotationDTO diseaseAnnotationDTO) throws TermNotFoundException;

    List<RelatedEntityDTO> getStrList(String publicationID);

    List<FishDTO> getFishList(String publicationID);

    List<DiseaseAnnotationDTO> deleteDiseaseModel(DiseaseAnnotationDTO diseaseAnnotationDTO) throws TermNotFoundException;

    List<DiseaseAnnotationDTO> deleteDiseaseAnnotationModel(DiseaseAnnotationModelDTO diseaseAnnotationModelDTO) throws TermNotFoundException;

}
