package org.zfin.fish.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.marker.Marker;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@RestController
@RequestMapping("/api/efg")
public class EfgAPIController {

    @JsonView(View.API.class)
    public record Fpbase(String fpId, String abbreviation) {
    }

    ;

    @JsonView(View.API.class)
    @RequestMapping(value = "/{efgID}/fpbase", method = RequestMethod.GET)
    public List<Fpbase> getFpBase(@PathVariable String efgID) {

        HibernateUtil.createTransaction();
        Marker efg = getMarkerRepository().getMarkerByID(efgID);

        List<Fpbase> list = efg.getFluorescentProteinEfgs().stream()
            .map(fluorescentProtein -> new Fpbase(fluorescentProtein.getID(), fluorescentProtein.getName()))
            .toList();
        HibernateUtil.flushAndCommitCurrentSession();
        return list;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{efgID}/fpbase", method = RequestMethod.POST)
    public Fpbase createFPBaseAssociation(@PathVariable String efgID,
                                          @RequestBody Fpbase fpbase) {

        HibernateUtil.createTransaction();
        Marker efg = getMarkerRepository().getMarkerByID(efgID);

        List<FluorescentProtein> efgs = getMarkerRepository().getFluorescentProteins(fpbase.fpId);

        FluorescentProtein protein = efgs.get(0);
        FluorescentMarker flMarker = new FluorescentMarker();
        flMarker.setEfg(efg);
        flMarker.setProtein(protein);
        flMarker.setEmissionLength(protein.getEmissionLength());
        flMarker.setExcitationLength(protein.getExcitationLength());
        HibernateUtil.currentSession().save(flMarker);
        efg.getFluorescentProteinEfgs().add(protein);
        HibernateUtil.flushAndCommitCurrentSession();
        return new Fpbase(protein.getID(), protein.getName());
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/fpbase/autocomplete", method = RequestMethod.GET)
    public List<LookupEntry> getFpBaseAutocomplete(@RequestParam(value = "query") String query) {

        HibernateUtil.createTransaction();
        List<FluorescentProtein> efgs = getMarkerRepository().getFluorescentProteins(query);

        List<LookupEntry> list = efgs.stream()
            .map(fluorescentProtein -> {
                LookupEntry entry = new LookupEntry();
                entry.setId(String.valueOf(fluorescentProtein.getIdentifier()));
                entry.setValue(fluorescentProtein.getName());
                entry.setLabel(fluorescentProtein.getName());
                return entry;
            })
            .toList();
        return list;
    }


}

