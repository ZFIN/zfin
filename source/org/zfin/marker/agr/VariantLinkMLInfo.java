package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.feature.FeatureNote;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.mapping.FeatureLocation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class VariantLinkMLInfo extends LinkMLInfo {

    public VariantLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.parseInt(args[0]);
        }
        VariantLinkMLInfo variantLinkMLInfo = new VariantLinkMLInfo(number);
        variantLinkMLInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<org.alliancegenome.curation_api.model.ingest.dto.VariantDTO> allVariantDTO = getAllVariantInfo();
        ingestDTO.setLinkMLVersion("v1.11.0");
        ingestDTO.setVariantIngestSet(allVariantDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_Variant_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<org.alliancegenome.curation_api.model.ingest.dto.VariantDTO> getAllVariantInfo() {
        List<FeatureGenomicMutationDetail> allVariants = getFeatureRepository().getAllFeatureGenomicMutationDetails();

        System.out.printf("%,d%n", allVariants.size());

        List<org.alliancegenome.curation_api.model.ingest.dto.VariantDTO> allVariantDTOList = allVariants.stream()
            .map(variant -> {
                org.alliancegenome.curation_api.model.ingest.dto.VariantDTO dto = new org.alliancegenome.curation_api.model.ingest.dto.VariantDTO();
                Feature feature = variant.getFeature();
                dto.setCurie("ZFIN:" + feature.getZdbID());
                dto.setTaxonCurie(ZfinDTO.taxonId);
                org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
                dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                dto.setDataProviderDto(dataProvider);
                dto.setCreatedByCurie("ZFIN:CURATOR");

                FeatureLocation ftrLoc = getFeatureRepository().getAllFeatureLocationsOnGRCz11(feature);
                if (ftrLoc != null
                    && ftrLoc.getStartLocation() != null && ftrLoc.getEndLocation() != null
                    && StringUtils.isNotEmpty(ftrLoc.getAssembly()) && StringUtils.isNotEmpty(ftrLoc.getReferenceSequenceAccessionNumber())
                ) {
                    switch (variant.getFeature().getType()) {
                        case POINT_MUTATION, DELETION, INSERTION, INDEL -> {
                            switch (variant.getFeature().getType()) {
                                case POINT_MUTATION -> {
                                    dto.setVariantTypeCurie("SO:1000008");
/*
                                dto.setGenomicReferenceSequence(variant.getFgmdSeqRef());
                                dto.setGenomicVariantSequence(variant.getFgmdSeqVar());
*/
                                    if (variant.getFgmdSeqRef() == null || variant.getFgmdSeqRef() == "" || variant.getFgmdSeqVar().length() > 1
                                        || variant.getFgmdSeqRef().length() > 1) {
                                        System.out.println(feature.getZdbID());
                                    }
                                    if (variant.getFgmdSeqVar() == null || variant.getFgmdSeqVar() == "") {
                                        System.out.println(feature.getZdbID());
                                    }
                                }
                                case DELETION -> {
                                    dto.setVariantTypeCurie("SO:0000159");
//                                dto.setGenomicVariantSequence("N/A");
                                    //                              dto.setGenomicReferenceSequence(variant.getFgmdSeqRef());
                                    if (variant.getFgmdSeqRef() == null || variant.getFgmdSeqRef() == "") {
                                        System.out.println(feature.getZdbID());
                                    }
                                }
                                case INSERTION -> {
                                    dto.setVariantTypeCurie("SO:0000667");
/*
                                dto.setGenomicReferenceSequence("N/A");
                                dto.setGenomicVariantSequence(variant.getFgmdSeqVar());
*/
                                    if (variant.getFgmdSeqVar() == null || variant.getFgmdSeqVar() == "") {
                                        System.out.println(feature.getZdbID());
                                    }
                                }
                                case INDEL -> {
                                    dto.setVariantTypeCurie("SO:1000032");
                                    //dto.setGenomicVariantSequence(variant.getFgmdSeqVar());
                                }
                                default -> System.out.println("invalid feature type");

                            }
                            ///                            dto.setSequenceOfReferenceAccessionNumber("RefSeq:" + ftrLoc.getReferenceSequenceAccessionNumber());
///                            dto.setAssembly(ftrLoc.getAssembly());
///                            dto.setStart(ftrLoc.getStartLocation());
///                            dto.setEnd(ftrLoc.getEndLocation());
///                            dto.setChromosome(ftrLoc.getChromosome());
/*
                            if (CollectionUtils.isNotEmpty(getPublicationRepository().getAllPublicationsForFeature(feature))) {
                                ArrayList<PublicationAgrDTO> datasetPubs = new ArrayList<>();
                                for (Publication pub : getPublicationRepository().getAllPublicationsForFeature(feature)) {

                                    PublicationAgrDTO fixedPub = new PublicationAgrDTO();
                                    List<String> pubPages = new ArrayList<>();
                                    pubPages.add("reference");

                                    CrossReferenceDTO pubXref = new CrossReferenceDTO("ZFIN", pub.getZdbID(), pubPages);
                                    if (pub.getAccessionNumber() != null) {
                                        fixedPub.setPublicationId("PMID:" + pub.getAccessionNumber());
                                        fixedPub.setCrossReference(pubXref);
                                    } else {
                                        fixedPub.setPublicationId("ZFIN:" + pub.getZdbID());
                                    }
                                    datasetPubs.add(fixedPub);
                                }
///                                dto.setReferences(datasetPubs);

                            }
*/
                            if (CollectionUtils.isNotEmpty(feature.getExternalNotes())) {
                                ArrayList<NoteDTO> noteDTOS = new ArrayList<>();
                                for (FeatureNote varNote : feature.getExternalNotes()) {
                                    if (varNote.isVariantNote()) {
                                        if (StringUtils.isNotEmpty(varNote.getNote())) {
                                            if (!varNote.getNote().contains("href") || !varNote.getNote().contains("\\n")) {
                                                ArrayList<PublicationAgrDTO> datasetPubs = new ArrayList<>();
                                                PublicationAgrDTO fixedPub = new PublicationAgrDTO();
                                                List<String> pubPages = new ArrayList<>();
                                                pubPages.add("reference");

                                                CrossReferenceDTO pubXref = new CrossReferenceDTO("ZFIN", varNote.getPublication().getZdbID(), pubPages);
                                                if (varNote.getPublication().getAccessionNumber() != null) {
                                                    fixedPub.setPublicationId("PMID:" + varNote.getPublication().getAccessionNumber());
                                                    fixedPub.setCrossReference(pubXref);
                                                } else {
                                                    fixedPub.setPublicationId("ZFIN:" + varNote.getPublication().getZdbID());
                                                }
                                                datasetPubs.add(fixedPub);
                                                NoteDTO noteDto = new NoteDTO(datasetPubs);
                                                noteDto.setNote(varNote.getNote());
                                                noteDTOS.add(noteDto);
                                            }
                                        }
                                    }
                                }
                                if (CollectionUtils.isNotEmpty(noteDTOS)) {
                                    dto.setNoteDtos(noteDTOS.stream().map(noteDTO -> {
                                        org.alliancegenome.curation_api.model.ingest.dto.NoteDTO noteDote = new org.alliancegenome.curation_api.model.ingest.dto.NoteDTO();
                                        noteDote.setFreeText(noteDTO.getNote());
                                        noteDote.setNoteTypeName("comment");
                                        return noteDote;
                                    }).toList());
                                }
                            }
                            List<String> pages = new ArrayList<>();
                            pages.add("variation");
                            List<CrossReferenceDTO> xRefs = new ArrayList<>();
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", feature.getZdbID(), pages);
                            xRefs.add(xref);
///                            dto.setCrossReferences(xRefs);
                        }
                        default -> {
                        }
                    }
                }

                return dto;
            }).toList();

/*
        List<VariantDTO> allVariantDTOListNoNulls = new ArrayList<>();
        for (VariantDTO vDto : allVariantDTOList) {
            if (!(vDto == null)) {
                if (!(vDto.getAlleleId() == null)) {
                    allVariantDTOListNoNulls.add(vDto);
                }
            }
        }
        System.out.printf("%,d%n", allVariantDTOListNoNulls.size());
        AllVariantDTO allVariantDTO = new AllVariantDTO();
        allVariantDTO.setVariants(allVariantDTOListNoNulls);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allVariantDTO.setMetaData(meta);
*/
        return allVariantDTOList;

    }

}
