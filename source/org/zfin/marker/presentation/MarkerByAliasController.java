package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerNotFoundException;
import org.zfin.marker.service.MarkerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@RestController
@RequestMapping("/api")
@Log4j2
public class MarkerByAliasController {

    @Autowired
    private MarkerService markerService;

    /**
     * Given an alias for a gene (which may be the official abbreviation), this returns the gene abbreviation as a string
     * For example, https://zfin.org/action/api/marker/from-alias/yap will return the string "yap1"
     * No match will return an empty string
     * @param alias
     * @return the gene abbreviation as a string
     */
    @JsonView(View.API.class)
    @RequestMapping("/marker/from-alias/{alias}")
    public String getMarkerAbbreviationByAlias(@PathVariable String alias) {
        try {
            String result = markerService.getActiveMarkerID(alias);
            Marker marker = getMarkerRepository().getMarker(result);
            return marker.getAbbreviation();
        } catch (MarkerNotFoundException e) {
            return "";
        }
    }

    /**
     * Given a list of aliases for genes (which may be the official abbreviations), this returns a map of the aliases to the gene abbreviations as strings.
     * No match will return an empty string.
     *
     * For example, https://zfin.org/action/api/marker/from-aliases with a POST request with the body ["yap", "yap1"] will return the map {"yap": "yap1", "yap1": "yap1"}
     * Example 2: curl -s -k -X POST -H "Content-Type: application/json" -d '["noi", "yap", "yap1", "pig"]' 'https://zfin.org/action/api/marker/from-aliases' |jq .
     * {
     *   "yap1": "yap1",
     *   "noi": "pax2a",
     *   "yap": "yap1",
     *   "pig": ""
     * }
     *
     * @param aliases
     * @return a map of the aliases to the gene abbreviations as strings
     */
    @JsonView(View.API.class)
    @RequestMapping(value = "/marker/from-aliases", method = RequestMethod.POST)
    public Map<String, String> getMarkerByAliases(@RequestBody List<String> aliases) {
        Map<String, String> resultsMap = new HashMap<>();
        for (String alias : aliases) {
            resultsMap.put(alias, getMarkerAbbreviationByAlias(alias));
        }
        return resultsMap;
    }


}
