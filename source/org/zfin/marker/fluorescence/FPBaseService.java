package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.zfin.framework.HibernateUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;


public class FPBaseService {
    public static final String API_ID_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?uuid__iexact=%s&format=json";
    public static final String API_NAME_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?name__iexact=%s&format=json";
    public static final String API_NAME_CONTAINS_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?name__icontains=%s&format=json";
    public static final String API_ALL_PROTEINS_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?format=json";


    public List<FPBaseApiResultItem> lookupFPBaseProteinByID(String id) {
        try {
            String json = IOUtils.toString(new URL(String.format(API_ID_ENDPOINT_TEMPLATE, id)), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FPBaseApiResultItem> lookupFPBaseProteinByName(String name) {
        try {
            String json = IOUtils.toString(new URL(String.format(API_NAME_ENDPOINT_TEMPLATE, name)), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FPBaseApiResultItem> lookupFPBaseProteinByNameContains(String namePart) {
        try {
            String json = IOUtils.toString(new URL(String.format(API_NAME_CONTAINS_ENDPOINT_TEMPLATE, namePart)), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FPBaseApiResultItem> fetchAllProteinData() {
        try {
            String json = IOUtils.toString(new URL(API_ALL_PROTEINS_ENDPOINT_TEMPLATE), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Link EFG markers to their FPBase {@code fluorescent_protein} rows by exact
     * base name — the protein name with any trailing {@code " (State)"} suffix
     * removed (FPBase models multi-state/photoconvertible proteins as separate
     * rows: {@code Kaede (Green)}, {@code Kaede (Red)}), matched case-insensitively
     * against the EFG's abbreviation or name. Every matching state row is linked,
     * so a photoconvertible EFG gets all of its colors. Idempotent — links that
     * already exist are skipped (there is no unique constraint on the join table).
     *
     * <p>Without this, {@link #importMissingProteins()} keeps the
     * {@code fluorescent_protein} table current but nothing connects a new protein
     * to its EFG marker, so the reporter/emission colors that read through
     * {@code fpProtein_efg} never appear (this was ZFIN-10352 for {@code mClover3}).
     *
     * <p>Exact base-name matching is deliberately conservative so it is safe to run
     * unattended: it self-links future multi-state proteins without the false
     * positives that prefix/fuzzy matching produces (e.g. the {@code Cre}
     * recombinase vs the {@code CreiLOV} protein, or {@code AM} vs {@code amCyan}).
     * Version-drift cases ({@code KikGR} marker vs {@code KikGR1} protein) and
     * generic reporters ({@code GFP}, {@code RFP}) do not match exactly and are
     * left to curation.
     *
     * @return the number of new links created
     */
    public int linkEfgsToProteinsByName() {
        String sql = """
            insert into fpProtein_efg (fe_mrkr_zdb_id, fe_fl_protein_id)
            select m.mrkr_zdb_id, p.fp_pk_id
            from marker m
            join fluorescent_protein p
              on lower(regexp_replace(p.fp_name, ' \\(.*\\)$', '')) in (lower(m.mrkr_abbrev), lower(m.mrkr_name))
            where m.mrkr_type = 'EFG'
            on conflict on constraint fpprotein_efg_uq do nothing
            """;
        return HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
    }

    public List<FluorescentProtein> importMissingProteins() {
        List<FluorescentProtein> allExistingProteinsLocally = getMarkerRepository().getAllFluorescentProteins();
        Set<String> existingProteinUUIDs = allExistingProteinsLocally.stream().map(FluorescentProtein::getUuid).collect(Collectors.toSet());
        List<FluorescentProtein> imported = new ArrayList<>();

        List<FPBaseApiResultItem> upstreamProteins = fetchAllProteinData();
        for (FPBaseApiResultItem protein : upstreamProteins) {
            if (!existingProteinUUIDs.contains(protein.uuid())) {
                imported.addAll(importProtein(protein));
            }
        }
        return imported;
    }

    private List<FluorescentProtein> importProtein(FPBaseApiResultItem protein) {
        List<FluorescentProtein> newProteins = new ArrayList<>();
        int numStates = protein.states().size();
        for(FPBaseApiResultState state : protein.states()) {
            FluorescentProtein newProtein = new FluorescentProtein();
            newProtein.setUuid(protein.uuid());
            newProtein.setName(protein.name());
            if (numStates > 1) {
                newProtein.setName(protein.name() + " (" + state.name() + ")");
            }
            newProtein.setEmissionLength(state.em_max());
            newProtein.setExcitationLength(state.ex_max());
            getMarkerRepository().addFluorescentProtein(newProtein);
            newProteins.add(newProtein);
        }
        return newProteins;
    }

    public List<FPBaseApiResultItem> deserializeApiResponse(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<FPBaseApiResultItem> dataList = objectMapper.readValue(json, new TypeReference<>() {});
        return dataList;
    }

}
