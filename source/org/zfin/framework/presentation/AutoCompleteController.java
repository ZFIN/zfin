package org.zfin.framework.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerFamilyName;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/autocomplete")
public class AutoCompleteController {

    private static Logger LOG = LogManager.getLogger(AutoCompleteController.class);
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @RequestMapping("/gene-family")
    protected @ResponseBody
    List<LookupEntry> geneFamilyHandler(@RequestParam("query") String query)  {

        List<LookupEntry> markerFamilyNames = new ArrayList<>();

        for (MarkerFamilyName markerFamilyName : markerRepository.getMarkerFamilyNamesBySubstring(query)) {
            markerFamilyNames.add(new LookupEntry(markerFamilyName.getMarkerFamilyName(), markerFamilyName.getMarkerFamilyName()));
        }

        return markerFamilyNames;
    }

    @ResponseBody
    @RequestMapping("/marker")
    public List<LookupEntry> lookupMarker(@RequestParam String query,
                                          @RequestParam(required = false) String typeGroup) {
        // if the UI didn't provide a typeGroup don't send any suggestions. the other option would
        // be to send suggestions for all marker types but that seems like it could be more error-prone
        Marker.TypeGroup group;
        try {
            group = Marker.TypeGroup.getType(typeGroup);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return markerRepository.getMarkerSuggestionList(query, group);
    }

}
