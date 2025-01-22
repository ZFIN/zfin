package org.zfin.fish.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.marker.Marker;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@RestController
@RequestMapping("/api/efg")
public class EfgAPIController {

    //Create nested structure for errors such that js can call: responseJSON.fieldErrors[0].message (for example)
    @JsonView(View.API.class)
    private record FpbaseDTO(String fpId, String abbreviation, FpbaseResponseFieldError[] fieldErrors) {
        public FpbaseDTO(String fpId, String abbreviation, Optional<String> op) {
            this(fpId, abbreviation, op.map(message -> new FpbaseResponseFieldError[]{new FpbaseResponseFieldError(message)}).orElse(null));
        }
    };

    @JsonView(View.API.class)
    private record FpbaseResponseFieldError(String field, String message) {
        public FpbaseResponseFieldError(String message) {
            this("fpId", message);
        }
    };

    @JsonView(View.API.class)
    @RequestMapping(value = "/{efgID}/fpbase", method = RequestMethod.GET)
    public List<FpbaseDTO> getFpBase(@PathVariable String efgID) {

        HibernateUtil.createTransaction();
        Marker efg = getMarkerRepository().getMarkerByID(efgID);

        List<FpbaseDTO> list = efg.getFluorescentProteinEfgs().stream()
            .map(fluorescentProtein -> new FpbaseDTO(fluorescentProtein.getID(), fluorescentProtein.getName(), Optional.empty()))
            .toList();
        HibernateUtil.flushAndCommitCurrentSession();
        return list;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{efgID}/fpbase", method = RequestMethod.POST)
    public FpbaseDTO createFPBaseAssociation(@PathVariable String efgID,
                                          @RequestBody FpbaseDTO fpbase,
                                             HttpServletResponse response) {

        HibernateUtil.createTransaction();
        Marker efg = getMarkerRepository().getMarkerByID(efgID);

        FluorescentProtein protein = getMarkerRepository().getFluorescentProteinByName(fpbase.fpId);

        //If no EFG is found in our FluorescentProtein table, send back a 404 with error message
        if (protein == null) {
            HibernateUtil.rollbackTransaction();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            FpbaseDTO fpbaseDTO = new FpbaseDTO(null, null, Optional.of("Fluorescent protein named " + fpbase.fpId + " not found in ZFIN."));
            return fpbaseDTO;
        }

        FluorescentMarker flMarker = new FluorescentMarker();
        flMarker.setEfg(efg);
        flMarker.setProtein(protein);
        flMarker.setEmissionLength(protein.getEmissionLength());
        flMarker.setExcitationLength(protein.getExcitationLength());
        HibernateUtil.currentSession().save(flMarker);
        efg.getFluorescentProteinEfgs().add(protein);
        HibernateUtil.flushAndCommitCurrentSession();
        return new FpbaseDTO(protein.getID(), protein.getName(), Optional.empty());
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/fpbase/autocomplete", method = RequestMethod.GET)
    public List<LookupEntry> getFpBaseAutocomplete(@RequestParam(value = "query") String query) {
        String trimmedQuery = query.trim();
        HibernateUtil.createTransaction();
        List<FluorescentProtein> efgs = getMarkerRepository().getFluorescentProteins(trimmedQuery);

        List<LookupEntry> list = efgs.stream()
            .map(fluorescentProtein -> {
                LookupEntry entry = new LookupEntry();
                entry.setId(String.valueOf(fluorescentProtein.getIdentifier()));
                entry.setValue(fluorescentProtein.getName());
                if (fluorescentProtein.getUuid().equalsIgnoreCase(trimmedQuery)) {
                    entry.setLabel(fluorescentProtein.getName() + " [ID=" + fluorescentProtein.getUuid() + "]");
                } else {
                    entry.setLabel(fluorescentProtein.getName());
                }
                return entry;
            })
            .toList();
        return list;
    }
}
