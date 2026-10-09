package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.AntibodyDTO;
import org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.SecondaryMarker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * ZFIN-10409. Builds the Alliance antibody submission file from
 * {@code AntibodyDTO}, which arrived in agr_curation_api v0.53.0 -- the jar
 * upgrade in the preceding commit is what makes this compile at all.
 *
 * <p>The mapping is unusually direct, because ZFIN's antibody table already
 * holds the fields the Alliance models:
 *
 * <pre>
 *   atb_type            clonalType          -> clonalityName
 *   atb_host_organism   hostSpecies         -> hostTaxonTermName
 *   atb_immun_organism  immunogenSpecies    -> antigenTaxonTermName
 *   atb_hviso_name      heavyChainIsotype   -> heavyChainIsotypeName
 *   atb_ltiso_name      lightChainIsotype   -> lightChainIsotypeName
 * </pre>
 *
 * <p><b>Those five are validated against Alliance vocabularies</b> --
 * antibody_clonality, antibody_host_taxon, antibody_antigen_taxon,
 * antibody_heavy_chain_isotype and antibody_light_chain_isotype, per
 * AntibodyDTOValidator, which matches on the exact term name. Clonality and the
 * isotypes use the same names we store. The taxon terms are named by NCBITaxon
 * curie ("NCBITaxon:10090"; "mouse (Mus musculus)" is only the definition), so
 * our organism common names are translated through organism_taxid.
 */
public class AntibodyLinkMLInfo extends LinkMLInfo {

    // ZFIN organism common name (what atb_host_organism / atb_immun_organism
    // hold) -> NCBITaxon curie, which is the Alliance taxon term name.
    private final Map<String, String> taxonCurieByCommonName = new HashMap<>();

    public AntibodyLinkMLInfo(int number) {
        super(number);
    }

    public static void main(String[] args) throws IOException {
        mainParent(args);
        AntibodyLinkMLInfo antibodyInfo = new AntibodyLinkMLInfo(0);
        antibodyInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<AntibodyDTO> allAntibodyDTO = getAllAntibodyInfo();
        ingestDTO.setAntibodyIngestSet(allAntibodyDTO);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_Antibody_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AntibodyDTO> getAllAntibodyInfo() {
        List<Species> species = new ArrayList<>(getAntibodyRepository().getHostSpeciesList());
        species.addAll(getAntibodyRepository().getImmunogenSpeciesList());
        species.forEach(s -> taxonCurieByCommonName.put(s.getCommonName(), "NCBITaxon:" + s.getTaxonomyID()));

        List<Antibody> allAntibodies = getAntibodyRepository().getAllAntibodies();
        System.out.println("Antibodies exported: " + allAntibodies.size());

        return allAntibodies.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private AntibodyDTO toDTO(Antibody antibody) {
        String primaryExternalId = "ZFIN:" + antibody.getZdbID();

        AntibodyDTO dto = new AntibodyDTO();
        dto.setPrimaryExternalId(primaryExternalId);
        // name is the one required field the validator enforces; for an antibody
        // the abbreviation IS the name curators use (e.g. "Ab1-notch1a"), which is
        // why this is getAbbreviation() rather than a display name.
        dto.setName(antibody.getAbbreviation());
        dto.setCreatedByCurie("ZFIN:CURATOR");
        GregorianCalendar date = ActiveData.getDateFromId(antibody.getZdbID());
        dto.setDateCreated(format(date));

        // clonality is required by the Alliance; 287 antibodies have no atb_type,
        // and antibody_clonality carries "not_specified" for exactly that case.
        dto.setClonalityName(StringUtils.isNotEmpty(antibody.getClonalType())
            ? antibody.getClonalType() : "not_specified");
        dto.setHostTaxonTermName(taxonCurie(antibody.getHostSpecies()));
        dto.setAntigenTaxonTermName(taxonCurie(antibody.getImmunogenSpecies()));
        dto.setHeavyChainIsotypeName(antibody.getHeavyChainIsotype());
        dto.setLightChainIsotypeName(antibody.getLightChainIsotype());

        DataProviderDTO dataProvider = new DataProviderDTO();
        dataProvider.setSourceOrganizationAbbreviation("ZFIN");
        CrossReferenceDTO crossReference = new CrossReferenceDTO();
        crossReference.setDisplayName(antibody.getZdbID());
        crossReference.setReferencedCurie(primaryExternalId);
        crossReference.setPageArea("antibody");
        crossReference.setPrefix("ZFIN");
        dataProvider.setCrossReferenceDto(crossReference);
        dto.setDataProviderDto(dataProvider);

        List<PublicationAttribution> attributions =
            getInfrastructureRepository().getPublicationAttributions(antibody.getZdbID());
        if (CollectionUtils.isNotEmpty(attributions)) {
            List<String> referenceCuries = attributions.stream()
                .map(attribution -> getSingleReference(attribution.getPublication()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
            dto.setReferenceCuries(referenceCuries);
        }

        List<String> antigenGenes = antigenGeneIdentifiers(antibody);
        if (CollectionUtils.isNotEmpty(antigenGenes)) {
            dto.setAntibodyTargetGeneIdentifiers(antigenGenes);
        }

        // Merged-away ids, so the Alliance can follow a record it saw under an
        // older accession. Inherited from ReagentDTO rather than AntibodyDTO.
        if (CollectionUtils.isNotEmpty(antibody.getSecondaryMarkerSet())) {
            List<String> secondaryIdentifiers = antibody.getSecondaryMarkerSet().stream()
                .map(SecondaryMarker::getOldID)
                .map(oldId -> "ZFIN:" + oldId)
                .toList();
            dto.setSecondaryIdentifiers(secondaryIdentifiers);
        }

        return dto;
    }

    private String taxonCurie(String commonName) {
        if (StringUtils.isEmpty(commonName)) {
            return null;
        }
        String curie = taxonCurieByCommonName.get(commonName);
        // Both columns are FKs to organism, so a miss means the host/immunogen
        // flags on organism drifted; fail rather than emit an invalid term.
        if (curie == null) {
            throw new IllegalStateException("No taxon id for organism: " + commonName);
        }
        return curie;
    }

    /**
     * The genes whose product this antibody recognises, as ZFIN curies.
     *
     * <p>Deliberately not {@code antibody.getAntigenGenes()}: that field is
     * {@code @Transient} and is filled in by the detail-page service, so it is
     * empty for antibodies loaded in bulk here and would silently emit no
     * targets at all.
     */
    private List<String> antigenGeneIdentifiers(Antibody antibody) {
        List<String> identifiers = new ArrayList<>();
        if (antibody.getSecondMarkerRelationships() == null) {
            return identifiers;
        }
        for (MarkerRelationship relationship : antibody.getSecondMarkerRelationships()) {
            // The enum constant rather than its display string: ZFIN stores this
            // relationship with the gene as marker one and the antibody as marker
            // two, so the antigen is on the FIRST side of a SECOND-side traversal.
            if (MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY == relationship.getType()
                && relationship.getFirstMarker() != null) {
                String curie = "ZFIN:" + relationship.getFirstMarker().getZdbID();
                if (!identifiers.contains(curie)) {
                    identifiers.add(curie);
                }
            }
        }
        return identifiers;
    }
}
