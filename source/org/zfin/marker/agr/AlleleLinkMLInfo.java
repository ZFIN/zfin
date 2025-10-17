package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.AlleleDTO;
import org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.SecondaryIdSlotAnnotationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.AlleleMutationTypeSlotAnnotationDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.ReplacedData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class AlleleLinkMLInfo extends LinkMLInfo {

    public AlleleLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        AlleleLinkMLInfo diseaseInfo = new AlleleLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<AlleleDTO> allDiseaseDTO = getAllAlleles(numfOfRecords);
        ingestDTO.setAlleleIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Allele_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AlleleDTO> getAllAlleles(int numberOrRecords) {
        List<Feature> features = getFeatureRepository().getAllFeatureList(numberOrRecords);
        return features.stream()
            .map(feature -> {
                AlleleDTO dto = new AlleleDTO();
                String primaryExternalId = "ZFIN:" + feature.getZdbID();
                dto.setAlleleSymbolDto(GeneLinkMLInfo.getNameSlotAnnotationDTOAbbrev(feature.getAbbreviation()));
                dto.setAlleleFullNameDto(GeneLinkMLInfo.getNameSlotAnnotationDTOName(feature.getName()));
                dto.setAlleleSynonymDtos(GeneLinkMLInfo.getSynonymSlotAnnotationDTOs(feature.getAliases().stream().map(DataAlias::getAlias).toList()));
/*
                List<String> curies = new ArrayList<>();
                curies.addAll(feature.getFeatureDnaMutationDetailSet().stream().map(mutationDetail -> mutationDetail.getDnaMutationTerm().getTerm().getOboID()).toList());
                curies.addAll(feature.getFeatureProteinMutationDetailSet().stream().map(mutationDetail -> mutationDetail.getProteinConsequence().getTerm().getOboID()).toList());
*/
                String curie = feature.getType().getCurie();
                if (StringUtils.isNotEmpty(curie)) {
                    dto.setAlleleMutationTypeDtos(getMutationTypeAnnotationDTOs(List.of(curie)));
                }
                List<String> secondaries = new ArrayList<>(feature.getSecondaryFeatureSet().stream().map(ReplacedData::getOldID).toList());
                if (CollectionUtils.isNotEmpty(secondaries)) {
                    dto.setAlleleSecondaryIdDtos(
                        secondaries.stream().map(id -> {
                            SecondaryIdSlotAnnotationDTO alleleDto = new SecondaryIdSlotAnnotationDTO();
                            alleleDto.setSecondaryId(id);
                            return alleleDto;
                        }).toList());
                }
                org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
                dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO crossReferenceDTO = new CrossReferenceDTO();
                crossReferenceDTO.setDisplayName(feature.getZdbID());
                crossReferenceDTO.setReferencedCurie(primaryExternalId);
                crossReferenceDTO.setPageArea("allele/references");
                crossReferenceDTO.setPrefix("ZFIN");
                dataProvider.setCrossReferenceDto(crossReferenceDTO);

                dto.setDataProviderDto(dataProvider);
                dto.setInternal(false);
                dto.setCreatedByCurie("ZFIN:CURATOR");
                dto.setTaxonCurie(ZfinDTO.taxonId);
                dto.setPrimaryExternalId(primaryExternalId);
                if (feature.getFtrEntryDate() != null) {
                    dto.setDateCreated(format(feature.getFtrEntryDate()));
                } else {
                    GregorianCalendar date = ActiveData.getDateFromId(feature.getZdbID());
                    dto.setDateCreated(format(date));
                }
                dto.setReferenceCuries(getReferencesById(feature.getZdbID()));
                if (feature.getSingleRelatedGenotype() != null) {
                    dto.setIsExtinct(feature.getSingleRelatedGenotype().isExtinct());
                } else {
                    dto.setIsExtinct(false);
                }
                return dto;
            })
            .collect(toList());
    }

    private List<AlleleMutationTypeSlotAnnotationDTO> getMutationTypeAnnotationDTOs(List<String> curies) {
        return curies.stream().map(curie -> {
            AlleleMutationTypeSlotAnnotationDTO dto = new AlleleMutationTypeSlotAnnotationDTO();
            dto.setMutationTypeCuries(List.of(curie));
            return dto;
        }).toList();
    }

    private List<String> getReferencesById(String zdbID) {
        return getPublicationRepository().getPubsForDisplay(zdbID).stream()
            .map(this::getSingleReference)
            .collect(toList());
    }

}
